package com.example.pokeappicesba;

public class M_MenuItemModel {
    private String id;
    private String title;
    private int imageResId;

    public M_MenuItemModel(String id, String title, int imageResId) {
        this.id = id;
        this.title = title;
        this.imageResId = imageResId;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public int getImageResId() { return imageResId; }
}