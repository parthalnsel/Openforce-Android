package com.openforce.interfaces;

import com.openforce.model.User;

public interface OnLoginCallback {

    void onErrorLogin(Exception error);

    void onSuccessfulLogin(User user);
}
