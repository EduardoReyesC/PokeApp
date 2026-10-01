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
    private List<PokemonMoveSlot> moves;
    private SpeciesRef species;

    public int getId() { return id; }
    public String getName() { return name; }
    public int getHeight() { return height; }
    public int getWeight() { return weight; }
    public Sprites getSprites() { return sprites; }
    public List<PokemonStat> getStats() { return stats; }
    public List<TypeSlot> getTypes() { return types; }
    public List<AbilitySlot> getAbilities() { return abilities; }
    public Cries getCries() { return cries; }
    public List<PokemonMoveSlot> getMoves() { return moves; }
    public SpeciesRef getSpecies() { return species; }

    public static class SpeciesRef {
        private String name;
        private String url;

        public String getName() { return name; }
        public String getUrl() { return url; }

        public int extractId() {
            if (url == null) return 1;
            String clean = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
            String[] parts = clean.split("/");
            try {
                return Integer.parseInt(parts[parts.length - 1]);
            } catch (Exception e) {
                return 1;
            }
        }
    }

    public static class PokemonMoveSlot {
        private MoveRef move;
        public MoveRef getMove() { return move; }
    }

    public static class MoveRef {
        private String name;
        public String getName() { return name; }
    }
}