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
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

public class FirebaseNotification extends FirebaseMessagingService {

    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Map<String,String> notificacion=new HashMap<String,String>();
        Log.i("Notificacion",remoteMessage.getData().get("msj"));
        notificacion.put("titulo",remoteMessage.getData().get("titulo"));
        notificacion.put("mensaje",remoteMessage.getData().get("msj"));
        Plataforma plataforma = new Plataforma();
        plataforma.setChipid(remoteMessage.getData().get("chipid"));
        plataforma.setNombre(remoteMessage.getData().get("nombre"));
        plataforma.setDisponible(Integer.parseInt(remoteMessage.getData().get("disponible")));
        plataforma.setIp(remoteMessage.getData().get("ip"));
        plataforma.setSectoract(Integer.parseInt(remoteMessage.getData().get("sectoractual")));
        createNotification(notificacion,plataforma);

    }

    @SuppressLint("ResourceAsColor")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    public void createNotification(Map<String, String> notificacion, Plataforma s) {
        NotificationCompat.Builder notificationBuilder;
        NotificationManager notificationManager =(NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);

        Intent notificacionIntent = new Intent(getApplicationContext(), TabsActivity.class);
        notificacionIntent.putExtra("plataforma",s);

        PendingIntent notificacionPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificacionIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder = new NotificationCompat.Builder(getBaseContext())
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
