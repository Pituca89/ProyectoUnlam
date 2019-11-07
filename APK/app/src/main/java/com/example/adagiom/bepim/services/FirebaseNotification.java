package com.example.adagiom.bepim.services;

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
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.adagiom.bepim.R;
import com.example.adagiom.bepim.activity.DrawerPrincipal;
import com.example.adagiom.bepim.model.Plataforma;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

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
        notificacion.put("action", remoteMessage.getNotification().getClickAction());

        plataforma.setIp(remoteMessage.getData().get("ip"));
        plataforma.setDisponible(Integer.parseInt(remoteMessage.getData().get("disponible")));
        plataforma.setNombre(remoteMessage.getData().get("nombre"));
        plataforma.setIdsector(Integer.parseInt(remoteMessage.getData().get("idsector")));
        plataforma.setSectoract(remoteMessage.getData().get("sectoract"));
        plataforma.setChipid(remoteMessage.getData().get("chipid"));

        Log.i("Action",remoteMessage.getNotification().getClickAction());
        Log.i("Data",remoteMessage.getData().get("chipid").toString());
        Log.i("Notificacion","LLEGO");
        if(remoteMessage.getNotification().getBody().contains("LLEGADA")){
            NotificationSingleton singleton = new NotificationSingleton().getInstance();
            singleton.setNotificationTrue();
        }

        createNotification(notificacion, plataforma);
    }

    @SuppressLint("ResourceAsColor")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    public void createNotification(Map<String, String> notificacion, Plataforma s) {
        Intent notificacionIntent = null;
        notificacionIntent = new Intent(getApplicationContext(), DrawerPrincipal.class);
        notificacionIntent.putExtra("plataforma", s)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent notificacionPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                notificacionIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

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
                .addLine(notificacion.get("mensaje")).build();

        //el atributo flags de la notificación nos permite ciertas opciones
        notification.flags |= Notification.FLAG_AUTO_CANCEL;//oculta la notificación una vez pulsada
        notification.defaults |= Notification.DEFAULT_SOUND; //sonido
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notification.defaults |= Notification.DEFAULT_LIGHTS;

        notificationManager.notify(001, notificationBuilder.build());
    }
}
