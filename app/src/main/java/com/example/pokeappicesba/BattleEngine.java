package com.example.pokeappicesba;

import java.util.Random;

public class BattleEngine {

    private static final Random random = new Random();
    private static final int BATTLE_LEVEL = 50;

    public static int calculateStat(int baseStat, boolean isHp) {
        int iv = 31;
        int ev = 0;
        if (isHp) {
            return ((2 * baseStat + iv + (ev / 4)) * BATTLE_LEVEL / 100) + BATTLE_LEVEL + 10;
        } else {
            return ((2 * baseStat + iv + (ev / 4)) * BATTLE_LEVEL / 100) + 5;
        }
    }

    public static int calculateDamage(int power, BattlePokemon attacker, BattlePokemon defender,
                                      double typeEffectiveness, boolean isStab, boolean isPhysicalMove) {

        if (power == 0) return 0; // Movimientos de estado no hacen daño directo

        double levelFactor = (2.0 * BATTLE_LEVEL / 5.0) + 2.0;

        // CORRECCIÓN: Nombres exactos y uso dinámico de Físico o Especial
        double a = isPhysicalMove ? attacker.attack : attacker.spAttack;
        double d = isPhysicalMove ? defender.defense : defender.spDefense;

        double baseDamage = ((levelFactor * power * (a / d)) / 50.0) + 2.0;

        double stabModifier = isStab ? 1.5 : 1.0;

        // Regla oficial: La quemadura reduce a la mitad el daño físico
        double burnModifier = (isPhysicalMove && attacker.currentStatus == BattlePokemon.Status.BURN) ? 0.5 : 1.0;

        double randomModifier = (85 + random.nextInt(16)) / 100.0;

        double totalDamage = baseDamage * stabModifier * typeEffectiveness * burnModifier * randomModifier;

        int finalDamage = (int) totalDamage;
        if (finalDamage == 0 && typeEffectiveness > 0) finalDamage = 1;

        return finalDamage;
    }

    public static boolean doesAGoFirst(BattlePokemon pkmA, BattlePokemon pkmB, int priorityA, int priorityB) {
        if (priorityA > priorityB) return true;
        if (priorityB > priorityA) return false;

        // Regla oficial: La parálisis reduce la velocidad de combate un 50%
        int actualSpeedA = (pkmA.currentStatus == BattlePokemon.Status.PARALYSIS) ? pkmA.speed / 2 : pkmA.speed;
        int actualSpeedB = (pkmB.currentStatus == BattlePokemon.Status.PARALYSIS) ? pkmB.speed / 2 : pkmB.speed;

        if (actualSpeedA > actualSpeedB) return true;
        if (actualSpeedB > actualSpeedA) return false;

        return random.nextBoolean(); // Speed Tie
    }

    /**
     * Comprueba si el Pokémon puede ejecutar su ataque o si un estado se lo impide.
     */
    public static boolean canMoveThisTurn(BattlePokemon p) {
        if (p.currentStatus == BattlePokemon.Status.PARALYSIS) {
            // 25% de probabilidad de no poder moverse
            return random.nextInt(100) >= 25;
        }
        if (p.currentStatus == BattlePokemon.Status.SLEEP) {
            p.sleepTurns--; // Restamos un turno de sueño

            if (p.sleepTurns <= 0) {
                p.currentStatus = BattlePokemon.Status.NONE; // ¡Se despierta obligatoriamente!
                return true; // Y puede atacar en este mismo turno
            }
            return false; // Sigue dormido
        }
        if (p.currentStatus == BattlePokemon.Status.FREEZE) {
            // 20% de probabilidad de descongelarse
            if (random.nextInt(100) < 20) {
                p.currentStatus = BattlePokemon.Status.NONE;
                return true;
            }
            return false;
        }
        return true;
    }
}