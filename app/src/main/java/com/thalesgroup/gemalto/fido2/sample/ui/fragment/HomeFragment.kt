/*
 * Copyright © 2021 THALES. All rights reserved.
 */
package com.thalesgroup.gemalto.fido2.sample.ui.fragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thalesgroup.gemalto.fido2.Fido2ErrorCode
import com.thalesgroup.gemalto.fido2.Fido2Exception
import com.thalesgroup.gemalto.fido2.client.Fido2Client
import com.thalesgroup.gemalto.fido2.client.Fido2ClientFactory
import com.thalesgroup.gemalto.fido2.client.Fido2Response
import com.thalesgroup.gemalto.fido2.sample.Authenticate
import com.thalesgroup.gemalto.fido2.sample.R
import com.thalesgroup.gemalto.fido2.sample.Register
import com.thalesgroup.gemalto.fido2.sample.domain.logger.Logger
import com.thalesgroup.gemalto.fido2.sample.domain.logger.LoggerImpl
import com.thalesgroup.gemalto.fido2.sample.ui.adapter.LogRecyclerViewAdapter
import com.thalesgroup.gemalto.fido2.sample.util.JsonUtil
import com.thalesgroup.gemalto.fido2.ui.SamplePasscodeLockoutUi
import java.util.Objects

class HomeFragment : Fragment() {
    private var userName: String? = null
    private var mainThreadHandler: Handler? = null
    private var cardView: CardView? = null
    private var recyclerViewLog: RecyclerView? = null
    private var logRecyclerViewAdapter: LogRecyclerViewAdapter? = null
    private var logger: Logger? = null
    private var showLogs: MenuItem? = null
    private var hideLogs: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        mainThreadHandler = Handler(Looper.getMainLooper())

        logRecyclerViewAdapter = LogRecyclerViewAdapter()

        logger = object : LoggerImpl() {
            public override fun log(text: String?) {
                super.log(text)
                refreshUIConsole()
            }
        }
        logRecyclerViewAdapter?.setItems(logger?.logs)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        view.findViewById<View?>(R.id.btn_register)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View?) {
                    logger?.clean()
                    register()
                }
            })

        view.findViewById<View?>(R.id.btn_authenticate)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View?) {
                    logger?.clean()
                    authenticate()
                }
            })

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cardView = view.findViewById<CardView>(R.id.log_container)
        recyclerViewLog = view.findViewById<RecyclerView>(R.id.recyclerView_log)
        recyclerViewLog?.setLayoutManager(LinearLayoutManager(this.getContext()))
        recyclerViewLog?.setAdapter(logRecyclerViewAdapter)
        logRecyclerViewAdapter?.setOnItemClickListener(object :
            LogRecyclerViewAdapter.OnItemClickListener {
            override fun onClickCopy(item: String?) {
                copyString(item)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_log, menu)
        showLogs = menu.findItem(R.id.show_logs)
        hideLogs = menu.findItem(R.id.hide_logs)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.show_logs ->                 //Store the status as 'false' into shared preference
                storeLogStatusInPreference(false)

            R.id.hide_logs ->                 //Store the status as 'true' into shared preference
                storeLogStatusInPreference(true)
        }
        logMenuShowOrHide()
        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        logMenuShowOrHide()
    }

    private fun logMenuShowOrHide() {
        // Get the status from the shared preference
        val status = this.logStatusFromPreference
        // Change the menu options
        showLogs?.setVisible(status)
        hideLogs?.setVisible(!status)
        // Hide the View
        cardView?.setVisibility(if (status) View.GONE else View.VISIBLE)
    }

    fun register() {
        requireActivity().runOnUiThread(object : Runnable {
            override fun run() {
                val alertDialogBuilder = AlertDialog.Builder(this@HomeFragment.requireContext())
                    .setTitle(R.string.register_input_alert_title)

                val alertDialog = alertDialogBuilder.create()
                val dialogView = getLayoutInflater().inflate(R.layout.custom_alert_dialog, null)
                val userNameTxt = dialogView.findViewById<EditText>(R.id.user_name)

                dialogView.findViewById<View?>(R.id.cancel_button)
                    .setOnClickListener(View.OnClickListener { view: View? -> alertDialog.dismiss() })

                dialogView.findViewById<View?>(R.id.ok_button)
                    .setOnClickListener(View.OnClickListener { view: View? ->
                        userName = userNameTxt.getText().toString()
                        if (userName?.isEmpty() == true) {
                            Toast.makeText(
                                getContext(),
                                R.string.fido2_sample_alert_dialog_edit_text,
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            alertDialog.dismiss()
                            // Call Register
                            executeRegister(object : OnExecuteFinishListener {
                                override fun onSuccess(response: Fido2Response?) {
                                    showAlertDialog(
                                        requireActivity().getString(R.string.register_alert_title),
                                        requireActivity().getString(R.string.register_alert_message)
                                    )
                                    logger?.log(
                                        "Registration Response:\n" + JsonUtil.prettyPrintJSON(
                                            response?.raw()
                                        )
                                    )
                                }

                                override fun onError(exception: Fido2Exception?) {
                                    // Recursively get the all exception message
                                    var errorMessage = exception?.message
                                    var ex = exception?.cause
                                    while (ex != null) {
                                        errorMessage += "\n" + ex.message
                                        ex = ex.cause
                                    }

                                    showAlertDialog(
                                        requireActivity().getString(R.string.error_alert_title),
                                        "Fido2 Error: " + errorMessage
                                    )
                                    logger?.log("Fido2 Error:\n" + errorMessage)
                                }
                            })
                        }
                    })

                alertDialog.setView(dialogView)
                alertDialog.setCanceledOnTouchOutside(false)
                alertDialog.show()
            }
        })
    }

    private fun executeRegister(regOnFinishListener: OnExecuteFinishListener?) {
        val register = Register(requireActivity(), logger, userName)
        register.execute(regOnFinishListener)
    }

    fun authenticate() {
        // Create a Fido2 Client
        var client: Fido2Client? = null
        try {
            client = Fido2ClientFactory.createFido2Client(requireContext())
            client.setActivity(getActivity())
        } catch (e: Fido2Exception) {
        }
        // Get the registered authenticators and check the list is empty
        if (client?.authenticatorRegistrations()?.isEmpty() != false) {
            showAlertDialog(
                getString(R.string.error_alert_title),
                getString(R.string.authenticate_alert_message_no_registration)
            )
            return
        }

        val authOnFinishListener: OnExecuteFinishListener = object : OnExecuteFinishListener {
            override fun onSuccess(response: Fido2Response?) {
                showAlertDialog(
                    requireActivity().getString(R.string.authenticate_alert_title),
                    requireActivity().getString(R.string.authenticate_alert_message)
                )
                logger?.log("Authentication Response:\n" + JsonUtil.prettyPrintJSON(response?.raw()))
            }

            override fun onError(exception: Fido2Exception?) {
                // Recursively get the all exception message
                var errorMessage = exception?.message
                var ex = exception?.cause
                while (ex != null) {
                    errorMessage += "\n" + ex.message
                    ex = ex.cause
                }

                if (exception?.getError() == Fido2ErrorCode.ERROR_USER_LOCKOUT) {
                    SamplePasscodeLockoutUi(getActivity()).showLockoutUi()
                } else {
                    showAlertDialog("Error", "Fido2 Error: " + errorMessage)
                }

                logger?.log("Fido2 Error:\n" + errorMessage)
            }
        }

        val authenticate = Authenticate(requireActivity(), logger)
        authenticate.execute(authOnFinishListener)
    }


    private fun showAlertDialog(title: String?, message: String?) {
        requireActivity().runOnUiThread(Runnable {
            AlertDialog.Builder(requireActivity())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(
                    android.R.string.ok,
                    DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                        dialog?.dismiss()
                    })
                .show()
        })
    }

    private fun refreshUIConsole() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            logRecyclerViewAdapter?.notifyDataSetChanged()
            recyclerViewLog?.smoothScrollToPosition(
                Objects.requireNonNull<RecyclerView.Adapter<*>?>(recyclerViewLog?.getAdapter())
                    .getItemCount()
            )
        } else {
            mainThreadHandler?.post(object : Runnable {
                override fun run() {
                    refreshUIConsole()
                }
            })
        }
    }

    private fun storeLogStatusInPreference(status: Boolean) {
        val editor = requireActivity().getPreferences(Context.MODE_PRIVATE).edit()
        editor.putBoolean(PREF_LOG_STATUS_KEY, status)
        editor.apply()
    }

    private val logStatusFromPreference: Boolean
        get() = requireActivity().getPreferences(Context.MODE_PRIVATE)
            .getBoolean(PREF_LOG_STATUS_KEY, true)


    private fun copyString(text: String?) {
        val clipboardManager =
            requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?

        if (clipboardManager != null) {
            val clipData = ClipData.newPlainText(getString(R.string.copied_text), text)
            clipboardManager.setPrimaryClip(clipData)
            Toast.makeText(getContext(), getString(R.string.copied_text), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(getContext(), getString(R.string.copied_text_error), Toast.LENGTH_SHORT)
                .show()
        }
    }

    interface OnExecuteFinishListener {
        fun onSuccess(response: Fido2Response?)
        fun onError(exception: Fido2Exception?)
    }

    companion object {
        private const val PREF_LOG_STATUS_KEY = "key_log_status"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment HomeFragment.
         */
        @JvmStatic
        fun newInstance(): HomeFragment {
            val fragment = HomeFragment()
            val args = Bundle()
            fragment.setArguments(args)
            return fragment
        }
    }
}
