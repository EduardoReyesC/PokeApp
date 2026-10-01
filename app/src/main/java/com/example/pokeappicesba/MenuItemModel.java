package com.example.pokeappicesba;

public class MenuItemModel {
    private String id;        // "A", "B", etc.
    private String title;     // "Pokédex", "Battle Emulator", etc.
    private int iconResId;    // R.drawable.ic_...
    private int backgroundColor; // Color hexadecimal en int

    public MenuItemModel(String id, String title, int iconResId, int backgroundColor) {
        this.id = id;
        this.title = title;
        this.iconResId = iconResId;
        this.backgroundColor = backgroundColor;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public int getIconResId() { return iconResId; }
    public int getBackgroundColor() { return backgroundColor; }
}