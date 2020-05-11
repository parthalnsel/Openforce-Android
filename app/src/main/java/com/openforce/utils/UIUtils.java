package com.openforce.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import com.google.android.material.snackbar.Snackbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public final class UIUtils {

    public static ProgressDialog showProgress(Context context, String title, String message,
                                    boolean indeterminate, boolean cancelable,
                                    DialogInterface.OnCancelListener cancelListener) {
        return ProgressDialog.show(context, title, message, indeterminate, cancelable, cancelListener);
    }

    public static Snackbar showSnackMessage(View view, String message) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        snackbar.show();
        return snackbar;
    }

    public static void hideKeyboard(View root) {
        if (root != null) {
            InputMethodManager imm = (InputMethodManager) root.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(root.getWindowToken(), 0);
        }
    }

    public static void showKeyboard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, 0);
        }
    }
}
