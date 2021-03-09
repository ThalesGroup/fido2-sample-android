package com.thalesgroup.gemalto.fido2.sample.ui.adapter;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.thalesgroup.gemalto.fido2.client.Fido2AuthenticatorRegistrationInfo;
import com.thalesgroup.gemalto.fido2.client.Fido2Client;
import com.thalesgroup.gemalto.fido2.client.Fido2ClientFactory;
import com.thalesgroup.gemalto.fido2.sample.R;
import com.thalesgroup.gemalto.fido2.sample.util.Base64;

import java.util.List;


public class AuthenticatorRecyclerViewAdapter extends RecyclerView.Adapter<AuthenticatorRecyclerViewAdapter.ViewHolder> {

    private FragmentActivity activity;
    private List<Fido2AuthenticatorRegistrationInfo> registrationInfoList;
    private boolean isEditModeEnable = true;

    public AuthenticatorRecyclerViewAdapter(FragmentActivity activity, List<Fido2AuthenticatorRegistrationInfo> registrationInfoList) {
        this.activity = activity;
        this.registrationInfoList = registrationInfoList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_authenticator_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Fido2AuthenticatorRegistrationInfo info = registrationInfoList.get(position);
        String rpIdHash = Base64.encodeToString(info.getRpIdHash(), Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
        String credentialId = Base64.encodeToString(info.getCredentialId(), Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
        holder.authenticatorCredId.setText(credentialId);
        holder.authenticatorRpIdHash.setText(rpIdHash);
        switch (info.getVerifyMethod()) {
            case PASSCODE:
                holder.imgAuthenticator.setImageResource(R.drawable.ic_dialpad);
                break;
            case PROPRIETARY_BIOMETRIC:
            case EMBEDDED_BIOMETRIC:
                holder.imgAuthenticator.setImageResource(R.drawable.ic_fingerprint);
                break;
            default:
                break;
        }

        holder.imgRemove.setVisibility(isEditModeEnable? View.GONE : View.VISIBLE);

        //Delete Authenticator
        holder.imgRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create a Fido2 Client
                Fido2Client client = Fido2ClientFactory.createFido2Client(activity);
                // Delete the selected authenticator
                client.deleteAuthenticatorRegistration(info);
                updateRegistrationInfo();
                showAlertDialog(activity.getString(R.string.removeauthenticator_alert_title), activity.getString(R.string.removeauthenticator_alert_message), false);
            }
        });
    }

    @Override
    public int getItemCount() {
        return registrationInfoList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView imgAuthenticator;
        public final TextView authenticatorCredId;
        public final TextView authenticatorRpIdHash;
        public final ImageView imgRemove;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            imgAuthenticator = (ImageView) view.findViewById(R.id.img_authenticator_icon);
            authenticatorCredId = (TextView) view.findViewById(R.id.txt_authenticator_credId);
            authenticatorRpIdHash = (TextView) view.findViewById(R.id.txt_authenticator_rpId);
            imgRemove = (ImageView) view.findViewById(R.id.img_delete_icon);
        }
    }

    public void updateRegistrationInfo() {
        // Create a Fido2 Client
        Fido2Client client = Fido2ClientFactory.createFido2Client(activity);
        // Get the registered authenticators
        registrationInfoList = client.authenticatorRegistrations();
        notifyDataSetChanged();
    }


    protected void showAlertDialog(final String title, final String message, final boolean popBack) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity)
                        .setTitle(title)
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                if(popBack) {
                                    // Back to the Settings Fragment
                                    activity.getSupportFragmentManager().popBackStack();
                                }
                            }
                        });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();
            }
        });
    }

    public void setEditModeOnOff(boolean mode) {
        isEditModeEnable = mode;
        notifyDataSetChanged();
    }
}