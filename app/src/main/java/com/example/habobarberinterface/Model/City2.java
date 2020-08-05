package com.example.habobarberinterface.Model;

import com.google.firebase.firestore.PropertyName;

public class City2 {

    @PropertyName("name ")
    private String name;

    public City2() {

    }

    @PropertyName("name ")
    public String getName() {
        return name;
    }

    @PropertyName("name ")
    public void setName(String name) {
        this.name = name;
    }
}
