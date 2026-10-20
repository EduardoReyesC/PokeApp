package com.example.pokeappicesba;

public class B_TurnAction {
    public enum ActionType { ATTACK, SWITCH }

    public ActionType type;

    // --- Atributos si es ATAQUE ---
    public String moveName;
    public int power;
    public String moveType;
    public boolean isPhysical;

    // --- Atributos si es CAMBIO ---
    public int switchTargetIndex; // El índice del Pokémon al que vamos a cambiar (0, 1 o 2)

    // Constructor para ATAQUE
    public B_TurnAction(String moveName, int power, String moveType, boolean isPhysical) {
        this.type = ActionType.ATTACK;
        this.moveName = moveName;
        this.power = power;
        this.moveType = moveType;
        this.isPhysical = isPhysical;
    }

    // Constructor para CAMBIO
    public B_TurnAction(int switchTargetIndex) {
        this.type = ActionType.SWITCH;
        this.switchTargetIndex = switchTargetIndex;
    }
}