package com.openforce.utils;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;

public class PinTextWatcher implements TextWatcher {

    private EditText pinInput1;
    private EditText pinInput2;
    private EditText pinInput3;
    private EditText pinInput4;
    private PinCallbacks pinCallbacks;

    public PinTextWatcher(EditText pinInput1, EditText pinInput2, EditText pinInput3,
                          EditText pinInput4, PinCallbacks pinCallbacks) {
        this.pinInput1 = pinInput1;
        this.pinInput2 = pinInput2;
        this.pinInput3 = pinInput3;
        this.pinInput4 = pinInput4;
        this.pinCallbacks = pinCallbacks;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (TextUtils.isEmpty(s)) {
            // user has deleted
            if (pinInput4.getText().toString().length() != 1) {
                if (pinInput3.getText().toString().length() != 1) {
                    if (pinInput2.getText().toString().length() != 1) {
                        pinInput1.requestFocus();
                    } else {
                        pinInput2.requestFocus();
                    }
                } else {
                    pinInput3.requestFocus();
                }
            } else if (pinInput3.getText().toString().length() != 1) {
                if (pinInput2.getText().toString().length() != 1) {
                    pinInput1.requestFocus();
                } else {
                    pinInput2.requestFocus();
                }
            } else if (pinInput2.getText().toString().length() != 1) {
                pinInput1.requestFocus();
            }
        } else {
            if (pinInput1.getText().toString().length() == 1
                    && pinInput2.getText().toString().length() == 1
                    && pinInput3.getText().toString().length() == 1
                    && pinInput4.getText().toString().length() == 1) {
                pinCallbacks.onPinComplete(pinInput1.getText().toString() + pinInput2.getText().toString()
                        + pinInput3.getText().toString()
                        + pinInput4.getText().toString());
            } else {
                if (pinInput1.getText().toString().length() != 1) {
                    pinInput1.requestFocus();
                } else if (pinInput2.getText().toString().length() != 1) {
                    pinInput2.requestFocus();
                } else if (pinInput3.getText().toString().length() != 1) {
                    pinInput3.requestFocus();
                } else if (pinInput4.getText().toString().length() != 1) {
                    pinInput4.requestFocus();
                }
            }
        }
    }

    public interface PinCallbacks {
        void onPinComplete(String pin);
    }
}
