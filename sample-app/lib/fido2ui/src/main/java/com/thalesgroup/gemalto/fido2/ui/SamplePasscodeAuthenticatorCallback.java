package com.thalesgroup.gemalto.fido2.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.thalesgroup.gemalto.fido2.Fido2ErrorCode;
import com.thalesgroup.gemalto.fido2.Fido2Exception;
import com.thalesgroup.gemalto.fido2.authenticator.passcode.Controller;
import com.thalesgroup.gemalto.fido2.authenticator.passcode.Keyboard;
import com.thalesgroup.gemalto.fido2.authenticator.passcode.KeyboardCallback;
import com.thalesgroup.gemalto.fido2.authenticator.passcode.KeyboardKey;
import com.thalesgroup.gemalto.fido2.authenticator.passcode.KeyboardUsage;
import com.thalesgroup.gemalto.fido2.authenticator.passcode.PasscodeAuthenticatorCallback;
import com.thalesgroup.gemalto.fido2.client.Fido2OperationInfo;

public class SamplePasscodeAuthenticatorCallback implements PasscodeAuthenticatorCallback {
    private FragmentActivity activity;
    private SamplePasscodeAuthenticatorBottomSheetDialogFragment fragment;

    public SamplePasscodeAuthenticatorCallback(FragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public void onStart(Keyboard keyboard, Fido2OperationInfo fido2OperationInfo, Controller controller) {
        if (fragment == null) {
            fragment = new SamplePasscodeAuthenticatorBottomSheetDialogFragment();
            fragment.setKeyboard(keyboard)
                    .setOperationInfo(fido2OperationInfo)
                    .setController(controller)
                    .show(activity.getSupportFragmentManager(), "");
        } else {
            fragment.setOperationInfo(fido2OperationInfo)
                    .setController(controller)
                    .updateKeyboard(keyboard);
        }
    }

    @Override
    public void onSuccess() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dismissPasscodeAuthenticatorDialog();
                Toast.makeText(activity, "Success", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onError(final Fido2Exception exception) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dismissPasscodeAuthenticatorDialog();
                Toast.makeText(activity, "Error: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                if (exception.getError() == Fido2ErrorCode.ERROR_USER_LOCKOUT) {
                    new SamplePasscodeLockoutUi(activity).showLockoutUi();
                }
            }
        });
    }

    public void dismissPasscodeAuthenticatorDialog() {
        if (fragment != null) {
            fragment.dismissAllowingStateLoss();
            fragment = null;
        }
    }

    public static class SamplePasscodeAuthenticatorBottomSheetDialogFragment extends BottomSheetDialogFragment implements KeyboardCallback {
        private TextView textViewTitle;
        private TextView textViewSubtitle;
        private TextView textViewPasscode;
        private Button buttonCancel;

        private Keyboard keyboard;
        private Fido2OperationInfo operationInfo;
        private Controller controller;
        private KeyboardUsage keyboardUsage;

        private int passcodeCount = 0;

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.view_passcode_authenticator, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            setRetainInstance(true);
            super.onViewCreated(view, savedInstanceState);
            textViewTitle = view.findViewById(R.id.textViewTitle);
            textViewSubtitle = view.findViewById(R.id.textViewSubtitle);
            textViewPasscode = view.findViewById(R.id.textViewPasscode);
            buttonCancel = view.findViewById(R.id.buttonCancel);

            textViewTitle.setSelected(true);

            if (keyboardUsage == null) keyboardUsage = keyboard.getKeyboardUsage();
            textViewTitle.setText(getTitleMessage(keyboardUsage));

            textViewSubtitle.setSelected(true);
            updateSubtitle(null, false);

            TextViewCompat.setAutoSizeTextTypeWithDefaults(textViewPasscode, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);

            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentPasscodeContainer, keyboard.getFragment())
                    .commitAllowingStateLoss();

            buttonCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Context context = SamplePasscodeAuthenticatorBottomSheetDialogFragment.this.getContext();
                    if (context != null) {
                        new AlertDialog.Builder(context)
                                .setTitle(R.string.cancel_dialog_title)
                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        controller.cancel();
                                        SamplePasscodeAuthenticatorBottomSheetDialogFragment.this.dismissAllowingStateLoss();
                                    }
                                })
                                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                })
                                .show();
                    }
                }
            });

            getDialog().setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {
                    FrameLayout dialogLayout = ((BottomSheetDialog) dialogInterface).findViewById(R.id.design_bottom_sheet);
                    BottomSheetBehavior behavior = BottomSheetBehavior.from(dialogLayout);
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    behavior.setPeekHeight(dialogLayout.getHeight());
                }
            });

            keyboard.clear();
            updatePasscode(0);
        }

        @Override
        public void onConfigurationChanged(@NonNull Configuration newConfig) {
            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.beginTransaction().detach(this).commitAllowingStateLoss();
            super.onConfigurationChanged(newConfig);
            fragmentManager.beginTransaction().attach(this).commitAllowingStateLoss();
        }

        public SamplePasscodeAuthenticatorBottomSheetDialogFragment setKeyboard(Keyboard keyboard) {
            this.keyboard = keyboard;
            this.keyboard.setCallback(this);
            return this;
        }

        public SamplePasscodeAuthenticatorBottomSheetDialogFragment setOperationInfo(Fido2OperationInfo operationInfo) {
            this.operationInfo = operationInfo;
            return this;
        }

        public SamplePasscodeAuthenticatorBottomSheetDialogFragment setController(Controller controller) {
            this.controller = controller;
            return this;
        }

        public void updateKeyboard(Keyboard keyboard) {
            setKeyboard(keyboard);
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentPasscodeContainer, keyboard.getFragment())
                    .commitAllowingStateLoss();
        }

        @Override
        public void onKeyReceived(KeyboardKey key) {
            switch (key) {
                case INPUT:
                    updatePasscode(passcodeCount + 1);
                    break;
                case DELETE:
                    updatePasscode(passcodeCount - 1);
            }
        }

        private void updatePasscode(int newCount) {
            passcodeCount = Math.max(newCount, 0);
            String passcodeText = new String(new char[passcodeCount]).replace("\0", "●");
            textViewPasscode.setText(passcodeText);
        }

        private void updateSubtitle(String text, boolean isWarning) {
            if (text == null) {
                textViewSubtitle.setTextColor(Color.DKGRAY);
                if (operationInfo != null) {
                    textViewSubtitle.setText(operationInfo.getRpId());
                } else {
                    textViewSubtitle.setText("");
                }
            } else {
                textViewSubtitle.setText(text);
                if (isWarning) {
                    textViewSubtitle.setTextColor(Color.RED);
                } else {
                    textViewSubtitle.setTextColor(Color.DKGRAY);
                }
            }
        }

        @Override
        public void onAuthenticationFailed(int retryCountRemaining) {
            keyboard.clear();
            updatePasscode(0);
            String text = getResources().getQuantityString(R.plurals.retry_left, retryCountRemaining, retryCountRemaining);
            updateSubtitle(text, true);
        }

        @Override
        public void onUsageChanged(KeyboardUsage usage) {
            updatePasscode(0);
            updateSubtitle(null, false);
            textViewTitle.setText(getTitleMessage(usage));
            if (keyboardUsage == KeyboardUsage.ENROLL_CONFIRM && usage == KeyboardUsage.ENROLL_NEW
                    || keyboardUsage == KeyboardUsage.CHANGE_PASSCODE_CONFIRM && usage == KeyboardUsage.CHANGE_PASSCODE_NEW) {
                Toast.makeText(getContext(), R.string.message_invalid_passcode, Toast.LENGTH_LONG).show();
            }
            keyboardUsage = usage;
        }

        private int getTitleMessage(KeyboardUsage usage) {
            switch (usage) {
                case ENROLL_NEW:
                case CHANGE_PASSCODE_NEW:
                    return R.string.usage_new;
                case ENROLL_CONFIRM:
                case CHANGE_PASSCODE_CONFIRM:
                    return R.string.usage_confirm;
                case FIDO_OPERATION:
                case CHANGE_PASSCODE_OLD:
                case UNENROLL:
                    return R.string.usage_default;
            }
            throw new IllegalStateException("Unknown keyboard usage");
        }

        @Override
        public void onCancel(@NonNull DialogInterface dialog) {
            super.onCancel(dialog);
            controller.cancel();
        }
    }
}
