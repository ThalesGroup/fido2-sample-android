package com.thalesgroup.gemalto.fido2.sample.ui.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.thalesgroup.gemalto.fido2.RegisteredAuthenticators;
import com.thalesgroup.gemalto.fido2.sample.R;
import com.thalesgroup.gemalto.fido2.sample.ui.adapter.AuthenticatorRecyclerViewAdapter;

/**
 * A fragment representing a list of Items.
 */
public class AuthenticatorListFragment extends Fragment {

    private AuthenticatorRecyclerViewAdapter recyclerViewAdapter;
    private MenuItem editAuth;
    private MenuItem cancel;

    public AuthenticatorListFragment() {
    }

    public static AuthenticatorListFragment newInstance() {
        return new AuthenticatorListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_edit_authenticators, menu);
        editAuth = menu.findItem(R.id.edit_auth);
        cancel = menu.findItem(R.id.cancel);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_auth:
                editAuth.setVisible(false);
                cancel.setVisible(true);
                recyclerViewAdapter.setEditModeOnOff(false);
                break;
            case R.id.cancel:
                editAuth.setVisible(true);
                cancel.setVisible(false);
                recyclerViewAdapter.setEditModeOnOff(true);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_authenticator_item_list, container, false);
        // Set the adapter
        if (view instanceof RecyclerView) {
            Context context = view.getContext();
            RecyclerView recyclerView = (RecyclerView) view;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            RegisteredAuthenticators registeredAuthenticators = new RegisteredAuthenticators(getActivity());
            recyclerViewAdapter = new AuthenticatorRecyclerViewAdapter(getActivity(),registeredAuthenticators.execute());
            recyclerView.setAdapter(recyclerViewAdapter);
        }
        return view;
    }
}