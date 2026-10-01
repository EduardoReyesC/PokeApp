package com.example.pokeappicesba;

import com.google.gson.annotations.SerializedName;

public class Sprites {

    @SerializedName("front_default")
    private String frontDefault;

    @SerializedName("front_shiny")
    private String frontShiny;

    public String getFrontDefault() {
        return frontDefault;
    }

    public String getFrontShiny() {
        return frontShiny;
    }
}