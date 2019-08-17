package com.example.adagiom.bepim;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

public class ServiceNotification extends Service implements InterfazAsyntask{
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mostrarToastMake("Inicio servicio de notificación");
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void mostrarToastMake(String msg) {

    }

    @Override
    public void VerificarMensaje(JSONObject msg) throws JSONException {

    }
}
