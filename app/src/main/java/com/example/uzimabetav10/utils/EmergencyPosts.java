package com.example.uzimabetav10.utils;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import java.util.Date;

public class EmergencyPosts extends  PostId {

    public String title, user_id, details, name,house_image_uri;
    public GeoPoint location;
    public Date timestamp;



    public EmergencyPosts (){}



    public EmergencyPosts(String title, String user_id, String details, String name, String house_image_uri, GeoPoint location, Timestamp timestamp) {
        this.title = title;
        this.user_id = user_id;
        this.details = details;
        this.name = name;
        this.house_image_uri = house_image_uri;
        this.location = location;

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHouse_image_uri() {
        return house_image_uri;
    }

    public void setHouse_image_uri(String house_image_uri) {
        this.house_image_uri = house_image_uri;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }
    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

}
