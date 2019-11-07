package com.example.adagiom.bepim.services;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class Firebase_ID_Service extends FirebaseInstanceIdService {
    private static final String TAG = "Firebase_ID_Service";
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.i("Token",token);
    }
}
