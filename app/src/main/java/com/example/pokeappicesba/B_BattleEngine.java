package com.example.pokeappicesba;

import java.util.Random;

public class B_BattleEngine {

    private static final Random random = new Random();
    private static final int BATTLE_LEVEL = 50;

    // ------------------------------------------------------------------------
    // 1. CÁLCULO DE ESTADÍSTICAS BASE (Nivel 50)
    // ------------------------------------------------------------------------
    public static int calculateStat(int baseStat, boolean isHp) {
        int iv = 31;
        int ev = 0;
        if (isHp) {
            return (((2 * baseStat + iv + (ev / 4)) * BATTLE_LEVEL) / 100) + BATTLE_LEVEL + 10;
        } else {
            return (((2 * baseStat + iv + (ev / 4)) * BATTLE_LEVEL) / 100) + 5;
        }
    }

    // ------------------------------------------------------------------------
    // 2. EL MOTOR MAESTRO DE TURNOS
    // ------------------------------------------------------------------------
    public static B_TurnResult resolverTurno(B_BattlePokemon p1, B_TurnAction a1, B_BattlePokemon p2, B_TurnAction a2) {
        B_TurnResult result = new B_TurnResult();
        result.addLog("--- Resolviendo turno ---");

        // FASE A: Cambios de Pokémon (Tienen prioridad máxima)
        if (a1.type == B_TurnAction.ActionType.SWITCH) {
            result.addLog(p1.name + " volvió a su Pokéball.");
            result.p1Switched = true;
        }
        if (a2.type == B_TurnAction.ActionType.SWITCH) {
            result.addLog(p2.name + " volvió a su Pokéball.");
            result.p2Switched = true;
        }

        // Determinar orden de ataque (si ambos atacan)
        boolean p1GoesFirst = doesAGoFirst(p1, p2, 0, 0);

        // FASE B: Ejecutar Primer Ataque
        if (p1GoesFirst) {
            ejecutarAccionDeAtaque(p1, p2, a1, result, true);
            if (!p2.isFainted()) ejecutarAccionDeAtaque(p2, p1, a2, result, false);
        } else {
            ejecutarAccionDeAtaque(p2, p1, a2, result, false);
            if (!p1.isFainted()) ejecutarAccionDeAtaque(p1, p2, a1, result, true);
        }

        // FASE C: Efectos de final de turno (Veneno / Quemadura)
        aplicarEfectosDeEstadoFinales(p1, result, true);
        aplicarEfectosDeEstadoFinales(p2, result, false);

        // Validar Faints (Debilitamientos finales)
        result.p1Fainted = p1.isFainted();
        result.p2Fainted = p2.isFainted();

        return result;
    }

    // ------------------------------------------------------------------------
    // MÉTODOS INTERNOS DE RESOLUCIÓN
    // ------------------------------------------------------------------------
    private static void ejecutarAccionDeAtaque(B_BattlePokemon atacante, B_BattlePokemon defensor, B_TurnAction accion, B_TurnResult result, boolean isAtacanteP1) {
        if (accion.type != B_TurnAction.ActionType.ATTACK || atacante.isFainted()) return;

        // Comprobar si está dormido, congelado o paralizado
        if (!canMoveThisTurn(atacante, result)) return;

        result.addLog("¡" + atacante.name + " usó " + accion.moveName + "!");

        // Validar si el ataque hace daño (poder > 0)
        if (accion.power > 0) {
            boolean isStab = atacante.types != null && atacante.types.contains(accion.moveType);
            double typeEffectiveness = TypeCalculator.getAttackMultiplier(accion.moveType, defensor.types);

            int damage = calculateDamage(accion.power, atacante, defensor, typeEffectiveness, isStab, accion.isPhysical);

            // Aplicar daño al objeto
            defensor.currentHp = Math.max(0, defensor.currentHp - damage);

            // Registrar daño para las animaciones de la UI
            if (isAtacanteP1) result.p2DamageTaken += damage;
            else result.p1DamageTaken += damage;

            // Mensajes de efectividad
            if (typeEffectiveness > 1.0) result.addLog("¡Es súper eficaz!");
            else if (typeEffectiveness > 0 && typeEffectiveness < 1.0) result.addLog("No es muy eficaz...");
            else if (typeEffectiveness == 0) result.addLog("No afecta a " + defensor.name + "...");

            // Posibilidad de aplicar estado (10% por ataque genérico, para darle sabor al combate)
            aplicarEstadoAleatorio(accion.moveName, defensor, result);

        } else {
            result.addLog("El ataque no hace daño directo.");
        }

        // Mensaje si el defensor cae
        if (defensor.isFainted()) {
            result.addLog("¡" + defensor.name + " se ha debilitado!");
        }
    }

    private static int calculateDamage(int power, B_BattlePokemon attacker, B_BattlePokemon defender, double typeEffectiveness, boolean isStab, boolean isPhysicalMove) {
        if (power == 0 || typeEffectiveness == 0) return 0;

        double levelFactor = ((2.0 * BATTLE_LEVEL) / 5.0) + 2.0;

        double a = isPhysicalMove ? attacker.attack : attacker.spAttack;
        double d = isPhysicalMove ? defender.defense : defender.spDefense;

        double baseDamage = ((levelFactor * power * (a / d)) / 50.0) + 2.0;
        double stabModifier = isStab ? 1.5 : 1.0;
        double burnModifier = (isPhysicalMove && attacker.currentStatus == B_BattlePokemon.Status.BURN) ? 0.5 : 1.0;
        double randomModifier = (85 + random.nextInt(16)) / 100.0;

        double totalDamage = baseDamage * stabModifier * typeEffectiveness * burnModifier * randomModifier;
        int finalDamage = (int) totalDamage;

        return Math.max(1, finalDamage); // Mínimo 1 de daño si la efectividad no es 0
    }

    private static boolean doesAGoFirst(B_BattlePokemon pkmA, B_BattlePokemon pkmB, int priorityA, int priorityB) {
        if (priorityA != priorityB) return priorityA > priorityB;

        int actualSpeedA = (pkmA.currentStatus == B_BattlePokemon.Status.PARALYSIS) ? pkmA.speed / 2 : pkmA.speed;
        int actualSpeedB = (pkmB.currentStatus == B_BattlePokemon.Status.PARALYSIS) ? pkmB.speed / 2 : pkmB.speed;

        if (actualSpeedA != actualSpeedB) return actualSpeedA > actualSpeedB;
        return random.nextBoolean(); // Empate de velocidad (Speed Tie)
    }

    private static boolean canMoveThisTurn(B_BattlePokemon p, B_TurnResult result) {
        if (p.currentStatus == B_BattlePokemon.Status.PARALYSIS) {
            if (random.nextInt(100) < 25) {
                result.addLog(p.name + " está paralizado y no puede moverse.");
                return false;
            }
        } else if (p.currentStatus == B_BattlePokemon.Status.SLEEP) {
            p.sleepTurns--;
            if (p.sleepTurns <= 0) {
                p.currentStatus = B_BattlePokemon.Status.NONE;
                result.addLog(p.name + " se despertó.");
                return true;
            }
            result.addLog(p.name + " está profundamente dormido.");
            return false;
        } else if (p.currentStatus == B_BattlePokemon.Status.FREEZE) {
            if (random.nextInt(100) < 20) {
                p.currentStatus = B_BattlePokemon.Status.NONE;
                result.addLog(p.name + " se descongeló.");
                return true;
            }
            result.addLog(p.name + " está congelado.");
            return false;
        }
        return true;
    }

    private static void aplicarEstadoAleatorio(String moveName, B_BattlePokemon defensor, B_TurnResult result) {
        if (defensor.isFainted() || defensor.currentStatus != B_BattlePokemon.Status.NONE) return;

        // 10% de probabilidad de quemar o paralizar al azar (puedes pulir esto luego según el tipo de ataque)
        if (random.nextInt(100) < 10) {
            if (random.nextBoolean()) {
                defensor.currentStatus = B_BattlePokemon.Status.BURN;
                result.addLog("¡" + defensor.name + " fue quemado!");
            } else {
                defensor.currentStatus = B_BattlePokemon.Status.PARALYSIS;
                result.addLog("¡" + defensor.name + " fue paralizado!");
            }
        }
    }

    private static void aplicarEfectosDeEstadoFinales(B_BattlePokemon p, B_TurnResult result, boolean isPlayer1) {
        if (p.isFainted()) return;

        if (p.currentStatus == B_BattlePokemon.Status.BURN || p.currentStatus == B_BattlePokemon.Status.POISON) {
            int damage = Math.max(1, p.maxHp / 8);
            p.currentHp = Math.max(0, p.currentHp - damage);

            if (isPlayer1) result.p1DamageTaken += damage;
            else result.p2DamageTaken += damage;

            String condition = p.currentStatus == B_BattlePokemon.Status.BURN ? "quemadura" : "veneno";
            result.addLog(p.name + " sufre por la " + condition + ".");

            if (p.isFainted()) result.addLog("¡" + p.name + " se ha debilitado por la " + condition + "!");
        }
    }
}