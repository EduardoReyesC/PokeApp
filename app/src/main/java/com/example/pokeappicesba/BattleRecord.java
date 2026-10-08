package com.example.pokeappicesba;

import java.util.List;

public class BattleRecord {
    private String playerPokemon;
    private String enemyPokemon;
    private String winner;
    private long timestamp;
    private List<String> turnLogs;

    public BattleRecord() {} // Constructor vacío requerido por Firestore

    public BattleRecord(String playerPokemon, String enemyPokemon, String winner, long timestamp, List<String> turnLogs) {
        this.playerPokemon = playerPokemon;
        this.enemyPokemon = enemyPokemon;
        this.winner = winner;
        this.timestamp = timestamp;
        this.turnLogs = turnLogs;
    }

    public String getPlayerPokemon() { return playerPokemon; }
    public String getEnemyPokemon() { return enemyPokemon; }
    public String getWinner() { return winner; }
    public long getTimestamp() { return timestamp; }
    public List<String> getTurnLogs() { return turnLogs; }
}