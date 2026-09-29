package com.example.pokeappicesba;

import java.util.List;

public class Pokemon {

    private int id;
    private String name;
    private Sprites sprites;
    private List<PokemonStat> stats;

    public int getId() { return id; }
    public String getName() { return name; }
    public Sprites getSprites() { return sprites; }
    public List<PokemonStat> getStats() { return stats; }
}