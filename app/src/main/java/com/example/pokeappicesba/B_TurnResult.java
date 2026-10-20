package com.example.pokeappicesba;

import java.util.ArrayList;
import java.util.List;

public class B_TurnResult {
    // Registro de texto para la consola (tvBattleLog)
    public List<String> logMessages = new ArrayList<>();

    // Daño total recibido en este turno (Ataques + Veneno/Quemadura)
    public int p1DamageTaken = 0;
    public int p2DamageTaken = 0;

    // Banderas de estado crítico (Para saber si la UI debe pedir un relevo o declarar victoria)
    public boolean p1Fainted = false;
    public boolean p2Fainted = false;

    // Banderas de relevo (Para saber si la UI debe cambiar los sprites)
    public boolean p1Switched = false;
    public boolean p2Switched = false;

    // Método de ayuda para añadir texto al log
    public void addLog(String message) {
        logMessages.add(message);
    }
}