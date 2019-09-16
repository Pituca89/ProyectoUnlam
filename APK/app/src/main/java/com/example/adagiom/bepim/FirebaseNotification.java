package com.example.adagiom.bepim;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FirebaseNotification extends FirebaseMessagingService{

    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Map<String, String> notificacion = new HashMap<String, String>();
        Plataforma plataforma = new Plataforma();

        notificacion.put("titulo", remoteMessage.getNotification().getTitle());
        notificacion.put("mensaje", remoteMessage.getNotification().getBody());
        notificacion.put("action",remoteMessage.getNotification().getClickAction());

        if(remoteMessage.getNotification().getBody().contains("LLEGADA")){
                NotificationSingleton singleton = new NotificationSingleton().getInstance();
                singleton.setNotification(true);
        }

        //createNotification(notificacion, plataforma);
    }

    @SuppressLint("ResourceAsColor")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    public void createNotification(Map<String, String> notificacion, Plataforma s) {
        Intent notificacionIntent = null;

        if(notificacion.get("action").equals("TABS")) {
            notificacionIntent = new Intent(getApplicationContext(), ListPlataforma.class);
            notificacionIntent.putExtra("plataforma", s);
            notificacionIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        PendingIntent notificacionPendingIntent = PendingIntent.getActivity(this, 0, notificacionIntent,
                PendingIntent.FLAG_ONE_SHOT);

        @SuppressLint("ResourceAsColor")
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
        .setSmallIcon(R.mipmap.ic_bepim)
        .setContentTitle(notificacion.get("titulo"))
        .setContentText(notificacion.get("mensaje"))
        .setTicker(notificacion.get("titulo"))
        .setSmallIcon(R.mipmap.ic_bepim)
        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_bepim))
        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE))
        .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
        .setAutoCancel(true)
        .setContentIntent(notificacionPendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setColor(Color.WHITE);
        }

        NotificationManager notificationManager =(NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = new NotificationCompat.InboxStyle(notificationBuilder)
                .addLine(notificacion.get("mensaje"))
                .addLine("Plataforma: "+ s.getNombre()).build();

        //el atributo flags de la notificación nos permite ciertas opciones
        notification.flags |= Notification.FLAG_AUTO_CANCEL;//oculta la notificación una vez pulsada
        notification.defaults |= Notification.DEFAULT_SOUND; //sonido
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notification.defaults |= Notification.DEFAULT_LIGHTS;

        notificationManager.notify(001, notificationBuilder.build());
    }
}
