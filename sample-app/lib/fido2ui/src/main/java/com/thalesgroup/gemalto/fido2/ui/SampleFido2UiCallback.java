package com.thalesgroup.gemalto.fido2.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;

import com.thalesgroup.gemalto.fido2.Fido2Exception;
import com.thalesgroup.gemalto.fido2.client.AuthenticatorDescriptionCallback;
import com.thalesgroup.gemalto.fido2.client.AuthenticatorDescriptor;
import com.thalesgroup.gemalto.fido2.client.AuthenticatorSelectionCallback;
import com.thalesgroup.gemalto.fido2.client.Fido2AuthenticatorInfo;
import com.thalesgroup.gemalto.fido2.client.Fido2UiCallback;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SampleFido2UiCallback extends Fido2UiCallback {
    private Context context;

    public SampleFido2UiCallback(Context context) {
        this.context = context;
    }

    @Override
    public void showAuthenticators(List<Fido2AuthenticatorInfo> authenticators, final AuthenticatorSelectionCallback callback) throws Fido2Exception {
        List<CharSequence> names = new ArrayList<>();
        for (Fido2AuthenticatorInfo info : authenticators) {
            names.add(info.getName());
        }
        new AlertDialog.Builder(context)
                .setTitle("Select an Authenticator")
                .setItems(names.toArray(new CharSequence[]{}), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback.onAuthenticatorSelected(which);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback.cancel();
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @Override
    public void retrieveAuthenticatorDescription(Fido2AuthenticatorInfo selectedAuthenticator, final AuthenticatorDescriptionCallback callback) {
        final EditText input = new EditText(context);
        input.setText(selectedAuthenticator.getName() + " " + DateFormat.getDateTimeInstance().format(new Date()));
        input.setPadding(40, 40, 40 ,40);
        new AlertDialog.Builder(context)
                .setTitle("Add Authenticator Name")
                .setView(input)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AuthenticatorDescriptor descriptor = new AuthenticatorDescriptor(input.getText().toString());
                        callback.onAuthenticatorDescriptionProvided(descriptor);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        callback.cancel();
                        dialog.dismiss();
                    }
                })
                .show();
    }
}
