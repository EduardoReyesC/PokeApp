package com.example.pokeappicesba;

import java.util.ArrayList;
import java.util.List;

public class UserProfile {
    private String uid;
    private String email;
    private String playerName;
    private String avatarId;
    private List<Integer> favorites;

    // Constructor vacío requerido por Firestore
    public UserProfile() {
        this.favorites = new ArrayList<>();
    }

    public UserProfile(String uid, String email, String playerName, String avatarId) {
        this.uid = uid;
        this.email = email;
        this.playerName = playerName;
        this.avatarId = avatarId;
        this.favorites = new ArrayList<>();
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }

    public String getAvatarId() { return avatarId; }
    public void setAvatarId(String avatarId) { this.avatarId = avatarId; }

    public List<Integer> getFavorites() { return favorites; }
    public void setFavorites(List<Integer> favorites) { this.favorites = favorites; }
}