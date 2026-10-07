package com.example.pokeappicesba;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
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

    // --- NUEVO MÉTODO CENTRALIZADO (DRY) ---
    // Devuelve solo los nombres de los ataques aprendidos por nivel
    public List<String> getLevelUpMoves() {
        List<String> ataquesPorNivel = new ArrayList<>();
        List<String> ataquesDeRespaldo = new ArrayList<>();

        if (moves != null) {
            for (PokemonMoveSlot slot : moves) {
                if (slot.getMove() == null || slot.getMove().getName() == null) continue;

                String moveName = slot.getMove().getName().replace("-", " ").toUpperCase();
                ataquesDeRespaldo.add(moveName);

                boolean isLevelUp = false;
                if (slot.getVersionGroupDetails() != null) {
                    for (VersionGroupDetail detail : slot.getVersionGroupDetails()) {
                        if (detail.getMoveLearnMethod() != null && "level-up".equals(detail.getMoveLearnMethod().getName())) {
                            isLevelUp = true;
                            break;
                        }
                    }
                }

                if (isLevelUp) {
                    ataquesPorNivel.add(moveName);
                }
            }
        }
        return ataquesPorNivel.isEmpty() ? ataquesDeRespaldo : ataquesPorNivel;
    }

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
        @SerializedName("version_group_details")
        private List<VersionGroupDetail> versionGroupDetails;

        public MoveRef getMove() { return move; }
        public List<VersionGroupDetail> getVersionGroupDetails() { return versionGroupDetails; }
    }

    public static class MoveRef {
        private String name;
        public String getName() { return name; }
    }

    public static class VersionGroupDetail {
        @SerializedName("move_learn_method")
        private MoveLearnMethod moveLearnMethod;

        public MoveLearnMethod getMoveLearnMethod() { return moveLearnMethod; }
    }

    public static class MoveLearnMethod {
        private String name;
        public String getName() { return name; }
    }
}