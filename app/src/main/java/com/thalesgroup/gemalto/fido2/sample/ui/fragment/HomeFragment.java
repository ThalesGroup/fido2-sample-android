package com.thalesgroup.gemalto.fido2.sample.ui.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.thalesgroup.gemalto.fido2.Authenticate;
import com.thalesgroup.gemalto.fido2.Fido2ErrorCode;
import com.thalesgroup.gemalto.fido2.Fido2Exception;
import com.thalesgroup.gemalto.fido2.Register;
import com.thalesgroup.gemalto.fido2.client.Fido2Client;
import com.thalesgroup.gemalto.fido2.client.Fido2ClientFactory;
import com.thalesgroup.gemalto.fido2.client.Fido2Response;
import com.thalesgroup.gemalto.fido2.sample.R;
import com.thalesgroup.gemalto.fido2.sample.domain.logger.Logger;
import com.thalesgroup.gemalto.fido2.sample.domain.logger.LoggerImpl;
import com.thalesgroup.gemalto.fido2.sample.ui.adapter.LogRecyclerViewAdapter;
import com.thalesgroup.gemalto.fido2.sample.util.JsonUtil;
import com.thalesgroup.gemalto.fido2.ui.SamplePasscodeLockoutUi;

import java.util.Objects;


public class HomeFragment extends Fragment {
    private final static String PREF_LOG_STATUS_KEY = "key_log_status";
    private String userName;
    private Handler mainThreadHandler;
    private CardView cardView;
    private RecyclerView recyclerViewLog;
    private LogRecyclerViewAdapter logRecyclerViewAdapter;
    private Logger logger;
    private MenuItem showLogs;
    private MenuItem hideLogs;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HomeFragment.
     */
    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mainThreadHandler = new Handler(Looper.getMainLooper());

        logRecyclerViewAdapter = new LogRecyclerViewAdapter();

        logger = new LoggerImpl() {
            @Override
            public void log(String text) {
                super.log(text);
                refreshUIConsole();
            }
        };
        logRecyclerViewAdapter.setItems(logger.getLogs());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        view.findViewById(R.id.btn_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logger.clean();
                register();
            }
        });

        view.findViewById(R.id.btn_authenticate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logger.clean();
                authenticate();
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cardView = view.findViewById(R.id.log_container);
        recyclerViewLog = view.findViewById(R.id.recyclerView_log);
        recyclerViewLog.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerViewLog.setAdapter(logRecyclerViewAdapter);
        logRecyclerViewAdapter.setOnItemClickListener(new LogRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onClickCopy(String item) {
                copyString(item);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_log, menu);
        showLogs = menu.findItem(R.id.show_logs);
        hideLogs = menu.findItem(R.id.hide_logs);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.show_logs:
                //Store the status as 'false' into shared preference
                storeLogStatusInPreference(false);
                break;
            case R.id.hide_logs:
                //Store the status as 'true' into shared preference
                storeLogStatusInPreference(true);
                break;
        }
        logMenuShowOrHide();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        logMenuShowOrHide();
    }

    private void logMenuShowOrHide() {
        // Get the status from the shared preference
        boolean status = getLogStatusFromPreference();
        // Change the menu options
        showLogs.setVisible(status);
        hideLogs.setVisible(!status);
        // Hide the View
        cardView.setVisibility(status? View.GONE : View.VISIBLE);

    }

    public void register() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(HomeFragment.this.getContext())
                        .setTitle(R.string.register_input_alert_title);

                AlertDialog alertDialog = alertDialogBuilder.create();
                View dialogView = getLayoutInflater().inflate(R.layout.custom_alert_dialog, null);
                final EditText userNameTxt = dialogView.findViewById(R.id.user_name);

                dialogView.findViewById(R.id.cancel_button).setOnClickListener(view -> alertDialog.dismiss());

                dialogView.findViewById(R.id.ok_button).setOnClickListener(view -> {
                    userName = userNameTxt.getText().toString();
                    if (userName.isEmpty()) {
                        Toast.makeText(getContext(), R.string.fido2_sample_alert_dialog_edit_text, Toast.LENGTH_SHORT).show();
                    } else {
                        alertDialog.dismiss();
                        // Call Register
                        executeRegister(new OnExecuteFinishListener() {
                            @Override
                            public void onSuccess(Fido2Response response) {
                                showAlertDialog(getActivity().getString(R.string.register_alert_title), getActivity().getString(R.string.register_alert_message));
                                logger.log("Registration Response:\n" + JsonUtil.prettyPrintJSON(response.raw()));
                            }

                            @Override
                            public void onError(Fido2Exception exception) {
                                // Recursively get the all exception message
                                String errorMessage = exception.getMessage();
                                Throwable ex = exception.getCause();
                                while (ex != null) {
                                    errorMessage+= "\n" + ex.getMessage();
                                    ex = ex.getCause();
                                }

                                showAlertDialog(getActivity().getString(R.string.error_alert_title), "Fido2 Error: " + errorMessage);
                                logger.log("Fido2 Error:\n" + errorMessage);
                            }
                        });
                    }
                });

                alertDialog.setView(dialogView);
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();
            }
        });
    }

    private void executeRegister(final OnExecuteFinishListener regOnFinishListener) {
        Register register = new Register(getActivity(), logger, userName);
        register.execute(regOnFinishListener);
    }

    public void authenticate() {
        // Create a Fido2 Client
        Fido2Client client = Fido2ClientFactory.createFido2Client(getActivity());
        // Get the registered authenticators and check the list is empty
        if (client.authenticatorRegistrations().isEmpty()) {
            showAlertDialog(getString(R.string.error_alert_title), getString(R.string.authenticate_alert_message_no_registration));
            return;
        }

        OnExecuteFinishListener authOnFinishListener = new OnExecuteFinishListener() {
            @Override
            public void onSuccess(Fido2Response response) {
                showAlertDialog(getActivity().getString(R.string.authenticate_alert_title), getActivity().getString(R.string.authenticate_alert_message));
                logger.log("Authentication Response:\n" + JsonUtil.prettyPrintJSON(response.raw()));
            }

            @Override
            public void onError(Fido2Exception exception) {
                // Recursively get the all exception message
                String errorMessage = exception.getMessage();
                Throwable ex = exception.getCause();
                while (ex != null) {
                    errorMessage+= "\n" + ex.getMessage();
                    ex = ex.getCause();
                }

                if (exception.getError() == Fido2ErrorCode.ERROR_USER_LOCKOUT) {
                    new SamplePasscodeLockoutUi(getActivity()).showLockoutUi();
                } else {
                    showAlertDialog("Error", "Fido2 Error: " + errorMessage);
                }

                logger.log("Fido2 Error:\n" + errorMessage);
            }
        };

        Authenticate authenticate = new Authenticate(getActivity(), logger);
        authenticate.execute(authOnFinishListener);
    }


    private void showAlertDialog(String title, String message) {
        getActivity().runOnUiThread(() -> new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    dialog.dismiss();
                })
                .show());
    }

    private void refreshUIConsole() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            logRecyclerViewAdapter.notifyDataSetChanged();
            recyclerViewLog.smoothScrollToPosition(
                    Objects.requireNonNull(recyclerViewLog.getAdapter()).getItemCount()
            );

        } else {
            mainThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    refreshUIConsole();
                }
            });
        }
    }

    private void storeLogStatusInPreference(boolean status) {
        SharedPreferences.Editor editor = getActivity().getPreferences(Context.MODE_PRIVATE).edit();
        editor.putBoolean(PREF_LOG_STATUS_KEY, status);
        editor.apply();
    }

    private boolean getLogStatusFromPreference() {
        return getActivity().getPreferences(Context.MODE_PRIVATE).getBoolean(PREF_LOG_STATUS_KEY, true);
    }


    private void copyString(String text) {
        ClipboardManager clipboardManager = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);

        if(clipboardManager != null) {
            ClipData clipData = ClipData.newPlainText(getString(R.string.copied_text), text);
            clipboardManager.setPrimaryClip(clipData);
            Toast.makeText(getContext(), getString(R.string.copied_text), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), getString(R.string.copied_text_error), Toast.LENGTH_SHORT).show();
        }
    }

    public interface OnExecuteFinishListener {
        void onSuccess(Fido2Response response);
        void onError(Fido2Exception exception);
    }

}