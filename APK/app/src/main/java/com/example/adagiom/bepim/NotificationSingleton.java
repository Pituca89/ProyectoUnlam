package com.example.adagiom.bepim;

public class NotificationSingleton {

     private static boolean notification = false;
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
}
