package com.example.pokeappicesba;

import java.util.List;

public class BattlePokemon {
    public String name;
    public int maxHp;
    public int currentHp;
    public int attack, defense, spAttack, spDefense, speed;
    public String backSpriteUrl;
    public String iconUrl;
    public String[] moves;
    public List<String> types; // Para debilidades y STAB

    public enum Status { NONE, BURN, PARALYSIS, POISON, SLEEP, FREEZE }
    public Status currentStatus = Status.NONE;
    public int sleepTurns = 0; // Agregamos el contador oficial

    // Recibe las 6 estadísticas base
    public BattlePokemon(String name, int baseHp, int baseAtk, int baseDef, int baseSpAtk, int baseSpDef, int baseSpeed,
                         String backSpriteUrl, String iconUrl, String[] moves, List<String> types) {
        this.name = name;

        // CORRECCIÓN: Quitamos el "50" porque el BattleEngine ya sabe el nivel.
        // Solo mandamos el stat base y "true" si es Vida, o "false" si son los demás stats.
        this.maxHp = BattleEngine.calculateStat(baseHp, true);
        this.currentHp = this.maxHp;

        this.attack = BattleEngine.calculateStat(baseAtk, false);
        this.defense = BattleEngine.calculateStat(baseDef, false);
        this.spAttack = BattleEngine.calculateStat(baseSpAtk, false);
        this.spDefense = BattleEngine.calculateStat(baseSpDef, false);
        this.speed = BattleEngine.calculateStat(baseSpeed, false);

        this.backSpriteUrl = backSpriteUrl;
        this.iconUrl = iconUrl;
        this.moves = moves;
        this.types = types;
    }

    public boolean isFainted() { return currentHp <= 0; }

    public String getStatusString() {
        switch (currentStatus) {
            case BURN: return "BRN";
            case PARALYSIS: return "PAR";
            case POISON: return "PSN";
            case SLEEP: return "SLP";
            case FREEZE: return "FRZ";
            default: return "";
        }
    }
}