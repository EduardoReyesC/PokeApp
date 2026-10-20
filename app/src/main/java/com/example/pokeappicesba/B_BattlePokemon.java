package com.example.pokeappicesba;

import java.util.ArrayList;
import java.util.List;

public class B_BattlePokemon {
    public String name;
    public int maxHp;
    public int currentHp;
    public int attack, defense, spAttack, spDefense, speed;
    public String backSpriteUrl;
    public String iconUrl;
    public String[] moves;
    public List<String> types;

    // NUEVO: Lista de ataques pre-cargados y listos para el Motor
    public List<B_TurnAction> loadedMoves = new ArrayList<>();

    public enum Status { NONE, BURN, PARALYSIS, POISON, SLEEP, FREEZE }
    public Status currentStatus = Status.NONE;
    public int sleepTurns = 0;

    public B_BattlePokemon(String name, int baseHp, int baseAtk, int baseDef, int baseSpAtk, int baseSpDef, int baseSpeed,
                           String backSpriteUrl, String iconUrl, String[] moves, List<String> types) {
        this.name = name;
        this.maxHp = B_BattleEngine.calculateStat(baseHp, true);
        this.currentHp = this.maxHp;
        this.attack = B_BattleEngine.calculateStat(baseAtk, false);
        this.defense = B_BattleEngine.calculateStat(baseDef, false);
        this.spAttack = B_BattleEngine.calculateStat(baseSpAtk, false);
        this.spDefense = B_BattleEngine.calculateStat(baseSpDef, false);
        this.speed = B_BattleEngine.calculateStat(baseSpeed, false);
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