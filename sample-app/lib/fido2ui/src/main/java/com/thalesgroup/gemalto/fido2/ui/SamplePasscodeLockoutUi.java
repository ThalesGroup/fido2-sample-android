package com.thalesgroup.gemalto.fido2.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.thalesgroup.gemalto.fido2.Fido2Exception;
import com.thalesgroup.gemalto.fido2.authenticator.passcode.Controller;
import com.thalesgroup.gemalto.fido2.authenticator.passcode.Keyboard;
import com.thalesgroup.gemalto.fido2.authenticator.passcode.PasscodeAuthenticator;
import com.thalesgroup.gemalto.fido2.authenticator.passcode.PasscodeAuthenticatorCallback;
import com.thalesgroup.gemalto.fido2.client.Fido2OperationInfo;

public class SamplePasscodeLockoutUi {

    private FragmentActivity activity;
    private PasscodeAuthenticator passcodeAuthenticator;

    public SamplePasscodeLockoutUi(FragmentActivity activity) {
        this.activity = activity;
        this.passcodeAuthenticator = PasscodeAuthenticator.of(activity, new PasscodeAuthenticatorCallback() {
            @Override
            public void onStart(Keyboard keyboard, Fido2OperationInfo fido2OperationInfo, Controller controller) { }

            @Override
            public void onSuccess() { }

            @Override
            public void onError(Fido2Exception exception) { }
        });
    }

    public void showLockoutUi() {
        long lockoutTimestamp = passcodeAuthenticator.getLockoutExpiryTimestamp();

        if (System.currentTimeMillis() < lockoutTimestamp) {
            DialogFragment fragment = new SamplePasscodeLockoutBottomSheetDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putLong(SamplePasscodeLockoutBottomSheetDialogFragment.KEY_LOCKOUT_TIMESTAMP, lockoutTimestamp);
            fragment.setArguments(bundle);
            fragment.show(activity.getSupportFragmentManager(), "");
        }
    }

    public static class SamplePasscodeLockoutBottomSheetDialogFragment extends BottomSheetDialogFragment {

        public static final String KEY_LOCKOUT_TIMESTAMP = "KEY_LOCKOUT_TIMESTAMP";

        private TextView textViewLockout;
        private Button buttonCancel;
        private long lockoutTimeStamp;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            lockoutTimeStamp = getArguments().getLong(KEY_LOCKOUT_TIMESTAMP);
            return inflater.inflate(R.layout.view_lockout, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            textViewLockout = view.findViewById(R.id.textViewLockout);
            buttonCancel = view.findViewById(R.id.buttonCancel);

            buttonCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismissAllowingStateLoss();
                }
            });

            getDialog().setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    FrameLayout dialogLayout = ((BottomSheetDialog) dialogInterface).findViewById(R.id.design_bottom_sheet);
                    ViewGroup.LayoutParams layoutParams = dialogLayout.getLayoutParams();
                    layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    dialogLayout.setLayoutParams(layoutParams);
                    BottomSheetBehavior behavior = BottomSheetBehavior.from(dialogLayout);
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    behavior.setPeekHeight(ViewGroup.LayoutParams.MATCH_PARENT);
                }
            });
        }

        Handler handler = new Handler(Looper.getMainLooper());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                if (now > lockoutTimeStamp) {
                    dismissAllowingStateLoss();
                }
                int secondInterval = (int) (lockoutTimeStamp - now) / 1000 + 1;
                String lockoutMessage = getResources().getQuantityString(R.plurals.lockout, secondInterval, secondInterval);
                textViewLockout.setText(lockoutMessage);
                handler.postDelayed(runnable, 1000);
            }
        };

        @Override
        public void onStart() {
            super.onStart();

            final Handler handler = new Handler();
            handler.removeCallbacks(runnable);
            handler.post(runnable);
        }

        @Override
        public void onStop() {
            super.onStop();
            handler.removeCallbacks(runnable);
        }
    }
}
