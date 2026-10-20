package com.example.pokeappicesba;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class B_VersusLocalActivity extends AppCompatActivity {

    // Equipos y Estado
    private B_BattlePokemon[] p1Team = new B_BattlePokemon[3];
    private B_BattlePokemon[] p2Team = new B_BattlePokemon[3];
    private int p1ActiveIndex = 0;
    private int p2ActiveIndex = 0;

    private ArrayList<String> p1QueryList, p2QueryList;
    private int downloadedPokemon = 0;
    private int downloadedMoves = 0;

    // Control de Turnos
    private boolean isPlayer1Turn = true;
    private B_TurnAction p1Action = null;
    private B_TurnAction p2Action = null;
    private int totalTurns = 1;

    // Vistas de la Arena
    private TextView tvPlayerName, tvPlayerHpExact, tvEnemyName, tvEnemyHpPercent, tvBattleLog;
    private ProgressBar pbPlayerHp, pbEnemyHp;
    private ImageView imgPlayerSprite, imgEnemySprite;
    private ImageView[] p1Slots = new ImageView[3];
    private ImageView[] p2Slots = new ImageView[3];

    // Controles y Pantalla Ciega
    private Button btnAtk1, btnAtk2, btnAtk3, btnAtk4, btnChangePkmn;
    private FrameLayout overlayBattleBlind;
    private TextView tvBattleBlindMessage;
    private Button btnBattleReady;

    private PokeApiService service;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_versus_battle);

        service = ApiClient.getClient().create(PokeApiService.class);

        p1QueryList = getIntent().getStringArrayListExtra("P1_TEAM");
        p2QueryList = getIntent().getStringArrayListExtra("P2_TEAM");

        initViews();
        descargarEquipos();
    }

    private void initViews() {
        tvPlayerName = findViewById(R.id.tvPlayerName);
        pbPlayerHp = findViewById(R.id.pbPlayerHp);
        imgPlayerSprite = findViewById(R.id.ivPlayerSprite);

        tvEnemyName = findViewById(R.id.tvOpponentName);
        pbEnemyHp = findViewById(R.id.pbOpponentHp);
        imgEnemySprite = findViewById(R.id.ivOpponentSprite);

        tvBattleLog = findViewById(R.id.tvBattleLog);

        // Botones (Asegúrate de que btnAtk4 exista en tu XML, o quítalo si usas 3)
        btnAtk1 = findViewById(R.id.btnAtk1);
        btnAtk2 = findViewById(R.id.btnAtk2);
        btnAtk3 = findViewById(R.id.btnAtk3);
        btnAtk4 = findViewById(R.id.btnAtk4); // <-- NUEVO
        btnChangePkmn = findViewById(R.id.btnChangePkmn);

        // Pantalla Ciega
        overlayBattleBlind = findViewById(R.id.overlayBattleBlind);
        tvBattleBlindMessage = findViewById(R.id.tvBattleBlindMessage);
        btnBattleReady = findViewById(R.id.btnBattleReady);

        // Indicadores de equipo (las 3 pokeballs por jugador)
        p1Slots[0] = findViewById(R.id.p1Slot1);
        p1Slots[1] = findViewById(R.id.p1Slot2);
        p1Slots[2] = findViewById(R.id.p1Slot3);
        p2Slots[0] = findViewById(R.id.oppSlot1);
        p2Slots[1] = findViewById(R.id.oppSlot2);
        p2Slots[2] = findViewById(R.id.oppSlot3);

        // Configurar Botón "Estoy Listo"
        btnBattleReady.setOnClickListener(v -> {
            overlayBattleBlind.setVisibility(View.GONE);
            configurarPanelDeControl();
        });

        // Configurar Botón de Cambio
        btnChangePkmn.setOnClickListener(v -> abrirDialogoDeCambio(false));
    }

    // =========================================================================================
    // FASE 1: DESCARGA DE LOS 6 POKÉMON Y SUS ATAQUES
    // =========================================================================================
    private void descargarEquipos() {
        tvBattleLog.setText("Descargando equipos...");
        for (int i = 0; i < 3; i++) {
            final int index = i;
            descargarUnPokemon(p1QueryList.get(i), true, index);
            descargarUnPokemon(p2QueryList.get(i), false, index);
        }
    }

    private void descargarUnPokemon(String query, boolean isP1, int index) {
        service.getPokemon(query).enqueue(new Callback<Pokemon>() {
            @Override
            public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                if (response.isSuccessful() && response.body() != null) {
                    B_BattlePokemon bp = mapearABattlePokemon(response.body(), isP1);
                    if (isP1) p1Team[index] = bp;
                    else p2Team[index] = bp;

                    downloadedPokemon++;
                    if (downloadedPokemon == 6) precargarTodosLosAtaques();
                }
            }
            @Override
            public void onFailure(Call<Pokemon> call, Throwable t) {
                Toast.makeText(B_VersusLocalActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void precargarTodosLosAtaques() {
        tvBattleLog.setText("Cargando movimientos...");
        for (int i = 0; i < 3; i++) {
            descargarDetallesDeAtaques(p1Team[i]);
            descargarDetallesDeAtaques(p2Team[i]);
        }
    }

    private void descargarDetallesDeAtaques(B_BattlePokemon pokemon) {
        if (pokemon.moves.length == 0) {
            pokemon.loadedMoves.add(new B_TurnAction("Struggle", 50, "normal", true));
            checkMovesComplete();
            return;
        }

        int[] pendientes = {pokemon.moves.length};
        for (String moveName : pokemon.moves) {
            String query = moveName.toLowerCase().replace(" ", "-");
            service.getMoveDetail(query).enqueue(new Callback<MoveDetail>() {
                @Override
                public void onResponse(Call<MoveDetail> call, Response<MoveDetail> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        MoveDetail body = response.body();
                        int power = body.getPower() != null ? body.getPower() : 0;
                        String type = body.getType() != null ? body.getType().getName() : "normal";
                        boolean isPhysical = "physical".equals(body.getDamageClass());
                        pokemon.loadedMoves.add(new B_TurnAction(moveName, power, type, isPhysical));
                    } else {
                        pokemon.loadedMoves.add(new B_TurnAction(moveName, 40, "normal", true));
                    }
                    if (--pendientes[0] == 0) checkMovesComplete();
                }
                @Override
                public void onFailure(Call<MoveDetail> call, Throwable t) {
                    pokemon.loadedMoves.add(new B_TurnAction(moveName, 40, "normal", true));
                    if (--pendientes[0] == 0) checkMovesComplete();
                }
            });
        }
    }

    private void checkMovesComplete() {
        downloadedMoves++;
        if (downloadedMoves == 6) {
            iniciarCombateLocal();
        }
    }

    private B_BattlePokemon mapearABattlePokemon(Pokemon p, boolean isPlayer) {
        // Misma lógica de mapeo que en el Simulador (Stats base a Nivel 50)
        int hp = 50, atk = 50, def = 50, spA = 50, spD = 50, spe = 50;
        if (p.getStats() != null) {
            for (PokemonStat stat : p.getStats()) {
                if (stat.getStat() == null) continue;
                switch (stat.getStat().getName()) {
                    case "hp": hp = stat.getBaseStat(); break;
                    case "attack": atk = stat.getBaseStat(); break;
                    case "defense": def = stat.getBaseStat(); break;
                    case "special-attack": spA = stat.getBaseStat(); break;
                    case "special-defense": spD = stat.getBaseStat(); break;
                    case "speed": spe = stat.getBaseStat(); break;
                }
            }
        }
        List<String> allMoves = p.getLevelUpMoves();
        Collections.shuffle(allMoves);
        int moveCount = Math.min(4, allMoves.size());
        String[] selectedMoves = new String[moveCount];
        for (int i = 0; i < moveCount; i++) selectedMoves[i] = allMoves.get(i);

        List<String> pkmnTypes = new ArrayList<>();
        if (p.getTypes() != null) {
            for (TypeSlot ts : p.getTypes()) if (ts.getType() != null) pkmnTypes.add(ts.getType().getName());
        }

        String spriteAnimado = isPlayer
                ? "https://play.pokemonshowdown.com/sprites/gen5ani-back/" + p.getName() + ".gif"
                : "https://play.pokemonshowdown.com/sprites/gen5ani/" + p.getName() + ".gif";
        return new B_BattlePokemon(p.getName().toUpperCase(), hp, atk, def, spA, spD, spe, spriteAnimado, "", selectedMoves, pkmnTypes);
    }

    // =========================================================================================
    // FASE 2: FLUJO DE TURNOS Y PANTALLA CIEGA
    // =========================================================================================
    private void iniciarCombateLocal() {
        actualizarArenaVisual();
        tvBattleLog.setText("¡El combate comienza!");
        mostrarPantallaCiega("Turno del Jugador 1\nPrepárate");
    }

    private void mostrarPantallaCiega(String mensaje) {
        tvBattleBlindMessage.setText(mensaje);
        overlayBattleBlind.setVisibility(View.VISIBLE);
    }

    private void configurarPanelDeControl() {
        B_BattlePokemon activo = isPlayer1Turn ? p1Team[p1ActiveIndex] : p2Team[p2ActiveIndex];
        tvBattleLog.setText("¿Qué hará " + activo.name + "?");

        // Ahora incluimos los 4 botones de ataque
        Button[] atkButtons = {btnAtk1, btnAtk2, btnAtk3, btnAtk4};

        for (int i = 0; i < atkButtons.length; i++) {
            if (i < activo.loadedMoves.size()) {
                B_TurnAction move = activo.loadedMoves.get(i);
                atkButtons[i].setText(move.moveName);
                atkButtons[i].setVisibility(View.VISIBLE);

                // Aplicar el color según el tipo de ataque
                int colorFondo = obtenerColorPorTipo(move.moveType);
                atkButtons[i].setBackgroundTintList(android.content.res.ColorStateList.valueOf(colorFondo));

                atkButtons[i].setOnClickListener(v -> registrarAccion(move));
            } else {
                atkButtons[i].setVisibility(View.INVISIBLE);
            }
        }
    }

    // Metodo auxiliar para obtener los colores oficiales de Pokémon
    // (Si prefieres usar tu clase TypeColorUtil, simplemente reemplaza el interior de este metodo)
    private int obtenerColorPorTipo(String tipo) {
        if (tipo == null) return android.graphics.Color.parseColor("#A8A878");
        switch (tipo.toLowerCase()) {
            case "fire": return android.graphics.Color.parseColor("#F08030");
            case "water": return android.graphics.Color.parseColor("#6890F0");
            case "grass": return android.graphics.Color.parseColor("#78C850");
            case "electric": return android.graphics.Color.parseColor("#F8D030");
            case "ice": return android.graphics.Color.parseColor("#98D8D8");
            case "fighting": return android.graphics.Color.parseColor("#C03028");
            case "poison": return android.graphics.Color.parseColor("#A040A0");
            case "ground": return android.graphics.Color.parseColor("#E0C068");
            case "flying": return android.graphics.Color.parseColor("#A890F0");
            case "psychic": return android.graphics.Color.parseColor("#F85888");
            case "bug": return android.graphics.Color.parseColor("#A8B820");
            case "rock": return android.graphics.Color.parseColor("#B8A038");
            case "ghost": return android.graphics.Color.parseColor("#705898");
            case "dragon": return android.graphics.Color.parseColor("#7038F8");
            case "dark": return android.graphics.Color.parseColor("#705848");
            case "steel": return android.graphics.Color.parseColor("#B8B8D0");
            case "fairy": return android.graphics.Color.parseColor("#EE99AC");
            default: return android.graphics.Color.parseColor("#A8A878"); // Normal
        }
    }

    private void registrarAccion(B_TurnAction action) {
        if (isPlayer1Turn) {
            p1Action = action;
            isPlayer1Turn = false;
            mostrarPantallaCiega("Turno del Jugador 2\nPasa el dispositivo");
        } else {
            p2Action = action;
            isPlayer1Turn = true;
            // Ambos eligieron, ocultamos la pantalla ciega y resolvemos
            overlayBattleBlind.setVisibility(View.GONE);
            resolverTurnoEnMotor();
        }
    }

    private void abrirDialogoDeCambio(boolean esForzadoPorMuerte) {
        B_BattlePokemon[] miEquipo = isPlayer1Turn ? p1Team : p2Team;
        int miIndiceActual = isPlayer1Turn ? p1ActiveIndex : p2ActiveIndex;

        List<String> opciones = new ArrayList<>();
        List<Integer> indicesReales = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            if (i != miIndiceActual && !miEquipo[i].isFainted()) {
                opciones.add(miEquipo[i].name + " (HP: " + miEquipo[i].currentHp + ")");
                indicesReales.add(i);
            }
        }

        if (opciones.isEmpty()) return; // No hay a quién cambiar

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cambiar Pokémon");
        builder.setItems(opciones.toArray(new String[0]), (dialog, which) -> {
            int nuevoIndice = indicesReales.get(which);

            if (esForzadoPorMuerte) {
                // Relevo gratuito tras morir
                if (isPlayer1Turn) p1ActiveIndex = nuevoIndice;
                else p2ActiveIndex = nuevoIndice;

                actualizarArenaVisual();
                verificarSiContinuaCombateORelevo();
            } else {
                // Cambio en turno regular
                registrarAccion(new B_TurnAction(nuevoIndice));
            }
        });

        if (!esForzadoPorMuerte) builder.setNegativeButton("Cancelar", null);
        builder.setCancelable(!esForzadoPorMuerte);
        builder.show();
    }

    // =========================================================================================
    // FASE 3: RESOLUCIÓN Y ANIMACIÓN
    // =========================================================================================
    private void resolverTurnoEnMotor() {
        tvBattleLog.setText("Resolviendo el turno " + totalTurns + "...");

        // Aquí la magia del esqueleto: ¡El Engine hace todo!
        B_TurnResult result = B_BattleEngine.resolverTurno(
                p1Team[p1ActiveIndex], p1Action,
                p2Team[p2ActiveIndex], p2Action
        );

        // Revisar si alguien cambió para actualizar su sprite ANTES de los ataques
        if (result.p1Switched) p1ActiveIndex = p1Action.switchTargetIndex;
        if (result.p2Switched) p2ActiveIndex = p2Action.switchTargetIndex;
        actualizarArenaVisual();

        // Animar los textos secuencialmente
        mostrarMensajeSecuencial(result, 0);
    }

    private void mostrarMensajeSecuencial(B_TurnResult result, int index) {
        if (index >= result.logMessages.size()) {
            actualizarArenaVisual();
            verificarSiContinuaCombateORelevo();
            return;
        }

        tvBattleLog.setText(result.logMessages.get(index));

        // Animar barras visualmente si hubo daño
        animarBarrasVidaConTransicion();

        handler.postDelayed(() -> mostrarMensajeSecuencial(result, index + 1), 1200);
    }

    private void animarBarrasVidaConTransicion() {
        B_BattlePokemon p1 = p1Team[p1ActiveIndex];
        B_BattlePokemon p2 = p2Team[p2ActiveIndex];

        pbPlayerHp.setProgress(p1.currentHp);
        pbEnemyHp.setProgress(p2.currentHp);
    }

    private void actualizarArenaVisual() {
        B_BattlePokemon p1 = p1Team[p1ActiveIndex];
        B_BattlePokemon p2 = p2Team[p2ActiveIndex];

        tvPlayerName.setText(p1.name + " Lvl 50");
        pbPlayerHp.setMax(p1.maxHp);
        pbPlayerHp.setProgress(p1.currentHp);
        Glide.with(this).load(p1.backSpriteUrl).into(imgPlayerSprite);

        tvEnemyName.setText(p2.name + " Lvl 50");
        pbEnemyHp.setMax(p2.maxHp);
        pbEnemyHp.setProgress(p2.currentHp);
        Glide.with(this).load(p2.backSpriteUrl).into(imgEnemySprite); // Usa frontSpriteUrl si tienes, para que te mire

        // Actualizar iconos de Pokeballs (gris si está debilitado)
        for(int i=0; i<3; i++) {
            p1Slots[i].setColorFilter(p1Team[i].isFainted() ? Color.DKGRAY : Color.TRANSPARENT);
            p2Slots[i].setColorFilter(p2Team[i].isFainted() ? Color.DKGRAY : Color.TRANSPARENT);
        }
    }

    private void verificarSiContinuaCombateORelevo() {
        boolean p1Pierde = true, p2Pierde = true;
        for (B_BattlePokemon p : p1Team) if (!p.isFainted()) p1Pierde = false;
        for (B_BattlePokemon p : p2Team) if (!p.isFainted()) p2Pierde = false;

        if (p1Pierde || p2Pierde) {
            finalizarCombate(p1Pierde ? "Jugador 2" : "Jugador 1");
            return;
        }

        // Si alguien murió pero aún tiene Pokémon, forzar cambio
        if (p1Team[p1ActiveIndex].isFainted()) {
            isPlayer1Turn = true;
            mostrarPantallaCiega("Tu Pokémon se debilitó.\nPasa el dispositivo al Jugador 1");
            btnBattleReady.setOnClickListener(v -> {
                overlayBattleBlind.setVisibility(View.GONE);
                abrirDialogoDeCambio(true);
            });
            return;
        }

        if (p2Team[p2ActiveIndex].isFainted()) {
            isPlayer1Turn = false;
            mostrarPantallaCiega("Tu Pokémon se debilitó.\nPasa el dispositivo al Jugador 2");
            btnBattleReady.setOnClickListener(v -> {
                overlayBattleBlind.setVisibility(View.GONE);
                abrirDialogoDeCambio(true);
            });
            return;
        }

        // Si nadie murió, pasar al siguiente turno normal
        totalTurns++;
        isPlayer1Turn = true;

        // Restaurar el botón normal de "Estoy Listo"
        btnBattleReady.setOnClickListener(v -> {
            overlayBattleBlind.setVisibility(View.GONE);
            configurarPanelDeControl();
        });

        mostrarPantallaCiega("Turno del Jugador 1\nPasa el dispositivo");
    }

    private void finalizarCombate(String ganador) {
        Intent resultIntent = new Intent(this, B_VersusResultActivity.class); // Usa el ResultActivity que ya creamos
        resultIntent.putExtra("WINNER", ganador);
        resultIntent.putExtra("TOTAL_TURNS", totalTurns);

        int vivos = 0;
        B_BattlePokemon[] equipoGanador = ganador.equals("Jugador 1") ? p1Team : p2Team;
        for (B_BattlePokemon p : equipoGanador) if (!p.isFainted()) vivos++;

        resultIntent.putExtra("REMAINING", vivos);
        startActivity(resultIntent);
        finish();
    }
}