package com.example.adagiom.bepim.services;

public class NotificationSingleton {

     private static boolean notification = false;
     private static String mensaje;
     private static NotificationSingleton singleton;

     public static NotificationSingleton getInstance(){
         if(singleton == null){
             singleton = new NotificationSingleton();
         }
         return singleton;
     }

    public synchronized void setNotificationTrue() {
        notification = true;
    }

    public synchronized void setNotificationFalse() {
        notification = false;
    }
    public static boolean isNotification() {
        return notification;
    }

    public static void setMensaje(String mensaje) {
        NotificationSingleton.mensaje = mensaje;
    }

    public static String getMensaje() {
        return mensaje;
    }
}
