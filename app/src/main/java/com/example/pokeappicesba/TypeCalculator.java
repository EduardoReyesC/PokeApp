package com.example.pokeappicesba;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TypeCalculator {

    private static final String[] ALL_TYPES = new String[]{
            "normal", "fire", "water", "grass", "electric", "ice",
            "fighting", "poison", "ground", "flying", "psychic", "bug",
            "rock", "ghost", "dragon", "steel", "dark", "fairy"
    };

    public static class TypeMatchups {
        public List<String> weaknesses = new ArrayList<>();
        public List<String> resistances = new ArrayList<>();
    }

    public static TypeMatchups calculate(List<String> pokemonTypes) {
        Map<String, Double> effectiveness = new HashMap<>();
        for (String t : ALL_TYPES) {
            effectiveness.put(t, 1.0);
        }

        for (String pType : pokemonTypes) {
            applyDefensiveType(pType.toLowerCase(Locale.ROOT), effectiveness);
        }

        TypeMatchups result = new TypeMatchups();
        for (Map.Entry<String, Double> entry : effectiveness.entrySet()) {
            double eff = entry.getValue();
            String typeName = entry.getKey().toUpperCase(Locale.ROOT);

            if (eff > 1.0) {
                String mult = (eff == 4.0) ? " (x4)" : " (x2)";
                result.weaknesses.add(typeName + mult);
            } else if (eff < 1.0) {
                String mult = (eff == 0.0) ? " (x0)" : (eff == 0.25 ? " (x0.25)" : " (x0.5)");
                result.resistances.add(typeName + mult);
            }
        }
        return result;
    }

    private static void applyDefensiveType(String type, Map<String, Double> eff) {
        switch (type) {
            case "normal":
                mult(eff, "fighting", 2.0); mult(eff, "ghost", 0.0);
                break;
            case "fire":
                mult(eff, "water", 2.0); mult(eff, "ground", 2.0); mult(eff, "rock", 2.0);
                mult(eff, "fire", 0.5); mult(eff, "grass", 0.5); mult(eff, "ice", 0.5);
                mult(eff, "bug", 0.5); mult(eff, "steel", 0.5); mult(eff, "fairy", 0.5);
                break;
            case "water":
                mult(eff, "electric", 2.0); mult(eff, "grass", 2.0);
                mult(eff, "fire", 0.5); mult(eff, "water", 0.5); mult(eff, "ice", 0.5); mult(eff, "steel", 0.5);
                break;
            case "grass":
                mult(eff, "fire", 2.0); mult(eff, "ice", 2.0); mult(eff, "poison", 2.0);
                mult(eff, "flying", 2.0); mult(eff, "bug", 2.0);
                mult(eff, "water", 0.5); mult(eff, "electric", 0.5); mult(eff, "grass", 0.5); mult(eff, "ground", 0.5);
                break;
            case "electric":
                mult(eff, "ground", 2.0);
                mult(eff, "electric", 0.5); mult(eff, "flying", 0.5); mult(eff, "steel", 0.5);
                break;
            case "ice":
                mult(eff, "fire", 2.0); mult(eff, "fighting", 2.0); mult(eff, "rock", 2.0); mult(eff, "steel", 2.0);
                mult(eff, "ice", 0.5);
                break;
            case "fighting":
                mult(eff, "flying", 2.0); mult(eff, "psychic", 2.0); mult(eff, "fairy", 2.0);
                mult(eff, "bug", 0.5); mult(eff, "rock", 0.5); mult(eff, "dark", 0.5);
                break;
            case "poison":
                mult(eff, "ground", 2.0); mult(eff, "psychic", 2.0);
                mult(eff, "grass", 0.5); mult(eff, "fighting", 0.5); mult(eff, "poison", 0.5);
                mult(eff, "bug", 0.5); mult(eff, "fairy", 0.5);
                break;
            case "ground":
                mult(eff, "water", 2.0); mult(eff, "grass", 2.0); mult(eff, "ice", 2.0);
                mult(eff, "poison", 0.5); mult(eff, "rock", 0.5); mult(eff, "electric", 0.0);
                break;
            case "flying":
                mult(eff, "electric", 2.0); mult(eff, "ice", 2.0); mult(eff, "rock", 2.0);
                mult(eff, "grass", 0.5); mult(eff, "fighting", 0.5); mult(eff, "bug", 0.5); mult(eff, "ground", 0.0);
                break;
            case "psychic":
                mult(eff, "bug", 2.0); mult(eff, "ghost", 2.0); mult(eff, "dark", 2.0);
                mult(eff, "fighting", 0.5); mult(eff, "psychic", 0.5);
                break;
            case "bug":
                mult(eff, "fire", 2.0); mult(eff, "flying", 2.0); mult(eff, "rock", 2.0);
                mult(eff, "grass", 0.5); mult(eff, "fighting", 0.5); mult(eff, "ground", 0.5);
                break;
            case "rock":
                mult(eff, "water", 2.0); mult(eff, "grass", 2.0); mult(eff, "fighting", 2.0);
                mult(eff, "ground", 2.0); mult(eff, "steel", 2.0);
                mult(eff, "normal", 0.5); mult(eff, "fire", 0.5); mult(eff, "poison", 0.5); mult(eff, "flying", 0.5);
                break;
            case "ghost":
                mult(eff, "ghost", 2.0); mult(eff, "dark", 2.0);
                mult(eff, "poison", 0.5); mult(eff, "bug", 0.5);
                mult(eff, "normal", 0.0); mult(eff, "fighting", 0.0);
                break;
            case "dragon":
                mult(eff, "ice", 2.0); mult(eff, "dragon", 2.0); mult(eff, "fairy", 2.0);
                mult(eff, "fire", 0.5); mult(eff, "water", 0.5); mult(eff, "electric", 0.5); mult(eff, "grass", 0.5);
                break;
            case "steel":
                mult(eff, "fire", 2.0); mult(eff, "fighting", 2.0); mult(eff, "ground", 2.0);
                mult(eff, "normal", 0.5); mult(eff, "grass", 0.5); mult(eff, "ice", 0.5);
                mult(eff, "flying", 0.5); mult(eff, "psychic", 0.5); mult(eff, "bug", 0.5);
                mult(eff, "rock", 0.5); mult(eff, "dragon", 0.5); mult(eff, "steel", 0.5); mult(eff, "fairy", 0.5);
                mult(eff, "poison", 0.0);
                break;
            case "dark":
                mult(eff, "fighting", 2.0); mult(eff, "bug", 2.0); mult(eff, "fairy", 2.0);
                mult(eff, "ghost", 0.5); mult(eff, "dark", 0.5); mult(eff, "psychic", 0.0);
                break;
            case "fairy":
                mult(eff, "poison", 2.0); mult(eff, "steel", 2.0);
                mult(eff, "fighting", 0.5); mult(eff, "bug", 0.5); mult(eff, "dark", 0.5); mult(eff, "dragon", 0.0);
                break;
        }
    }

    private static void mult(Map<String, Double> map, String key, double value) {
        if (map.containsKey(key)) {
            map.put(key, map.get(key) * value);
        }
    }

    // Añade esto dentro de TypeCalculator.java
    public static double getAttackMultiplier(String moveType, List<String> defenderTypes) {
        if (moveType == null || defenderTypes == null || defenderTypes.isEmpty()) return 1.0;

        double totalMultiplier = 1.0;

        for (String defType : defenderTypes) {
            // Reutilizamos tu lógica defensiva creando un mapa temporal para el tipo defensivo
            Map<String, Double> eff = new HashMap<>();
            for (String t : ALL_TYPES) { eff.put(t, 1.0); }

            applyDefensiveType(defType.toLowerCase(Locale.ROOT), eff);

            // El daño que hace el ataque es la debilidad del defensor a ese ataque
            if (eff.containsKey(moveType.toLowerCase(Locale.ROOT))) {
                totalMultiplier *= eff.get(moveType.toLowerCase(Locale.ROOT));
            }
        }
        return totalMultiplier;
    }
}