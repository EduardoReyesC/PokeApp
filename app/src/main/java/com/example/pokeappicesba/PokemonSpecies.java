package com.example.pokeappicesba;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PokemonSpecies {
    @SerializedName("flavor_text_entries")
    private List<FlavorText> flavorTextEntries;
    private List<VarietyItem> varieties;

    public List<FlavorText> getFlavorTextEntries() { return flavorTextEntries; }
    public List<VarietyItem> getVarieties() { return varieties; }

    public static class FlavorText {
        @SerializedName("flavor_text")
        private String flavorText;
        private Language language;

        public String getFlavorText() { return flavorText; }
        public Language getLanguage() { return language; }
    }

    public static class Language {
        private String name;
        public String getName() { return name; }
    }

    public static class VarietyItem {
        private NamedResource pokemon;
        public NamedResource getPokemon() { return pokemon; }
    }

    public static class NamedResource {
        private String name;
        public String getName() { return name; }
    }
}