package com.example.uzimabetav10.Messaging;

import com.google.firebase.firestore.DocumentId;

public class NotificationsConstructor {

    String from , message;

    @DocumentId
    String notificationId;



    public NotificationsConstructor(){


    }

    public NotificationsConstructor(String from, String message ,String notificationId) {
        this.from = from;
        this.message = message;
        this.notificationId = notificationId;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }
    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


}
