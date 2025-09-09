/*
 * Copyright © 2021 THALES. All rights reserved.
 */
package com.thalesgroup.gemalto.fido2.sample.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thalesgroup.gemalto.fido2.sample.R
import com.thalesgroup.gemalto.fido2.sample.RegisteredAuthenticators
import com.thalesgroup.gemalto.fido2.sample.ui.adapter.AuthenticatorRecyclerViewAdapter

/**
 * A fragment representing a list of Items.
 */
class AuthenticatorListFragment : Fragment() {
    private var recyclerViewAdapter: AuthenticatorRecyclerViewAdapter? = null
    private var editAuth: MenuItem? = null
    private var cancel: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_edit_authenticators, menu)
        editAuth = menu.findItem(R.id.edit_auth)
        cancel = menu.findItem(R.id.cancel)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.edit_auth -> {
                editAuth?.setVisible(false)
                cancel?.setVisible(true)
                recyclerViewAdapter?.setEditModeOnOff(false)
            }

            R.id.cancel -> {
                editAuth?.setVisible(true)
                cancel?.setVisible(false)
                recyclerViewAdapter?.setEditModeOnOff(true)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_authenticator_item_list, container, false)
        // Set the adapter
        if (view is RecyclerView) {
            val context = view.getContext()
            val recyclerView = view
            recyclerView.setLayoutManager(LinearLayoutManager(context))
            val registeredAuthenticators = RegisteredAuthenticators(requireActivity())
            recyclerViewAdapter = AuthenticatorRecyclerViewAdapter(
                requireActivity(),
                registeredAuthenticators.execute().filterNotNull().toMutableList()
            )
            recyclerView.setAdapter(recyclerViewAdapter)
        }
        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(): AuthenticatorListFragment {
            return AuthenticatorListFragment()
        }
    }
}
