package com.example.pokeappicesba;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class EvolutionChainResponse {
    private ChainLink chain;

    public ChainLink getChain() { return chain; }

    public static class ChainLink {
        private SpeciesSummary species;

        @SerializedName("evolves_to")
        private List<ChainLink> evolvesTo;

        public SpeciesSummary getSpecies() { return species; }
        public List<ChainLink> getEvolvesTo() { return evolvesTo; }
    }

    public static class SpeciesSummary {
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
}