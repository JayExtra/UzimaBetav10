package com.example.uzimabetav10.Messaging;

public class NotificationsConstructor {

    String from , message;

    public NotificationsConstructor(){


    }

    public NotificationsConstructor(String from, String message) {
        this.from = from;
        this.message = message;
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
