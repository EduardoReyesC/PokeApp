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

    @SerializedName("evolution_chain")
    private EvolutionChainUrl evolutionChain;

    public EvolutionChainUrl getEvolutionChain() { return evolutionChain; }

    public static class EvolutionChainUrl {
        private String url;
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

}