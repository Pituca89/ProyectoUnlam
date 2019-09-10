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

    public void setNotification(boolean notification) {
        this.notification = notification;
    }

    public boolean isNotification() {
        return notification;
    }
}
