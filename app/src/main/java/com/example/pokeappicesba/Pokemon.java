package com.example.pokeappicesba;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Pokemon {
    private int id;
    private String name;
    private int height;
    private int weight;
    private Sprites sprites;
    private List<PokemonStat> stats;
    private List<TypeSlot> types;
    private List<AbilitySlot> abilities;
    private Cries cries;

    public int getId() { return id; }
    public String getName() { return name; }
    public int getHeight() { return height; }
    public int getWeight() { return weight; }
    public Sprites getSprites() { return sprites; }
    public List<PokemonStat> getStats() { return stats; }
    public List<TypeSlot> getTypes() { return types; }
    public List<AbilitySlot> getAbilities() { return abilities; }
    public Cries getCries() { return cries; }
}