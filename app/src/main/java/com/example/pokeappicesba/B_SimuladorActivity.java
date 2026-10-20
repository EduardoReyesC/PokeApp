package com.example.pokeappicesba;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class B_SimuladorActivity extends AppCompatActivity {

    private TextView tvPlayerName, tvPlayerHpExact, tvEnemyName, tvEnemyHpPercent, tvBattleLog, tvLiveHistoryLog;
    private TextView tvPlayerDamagePopup, tvEnemyDamagePopup;
    private TextView tvWinnerTitle, tvWinnerSubtitle;
    private CardView cardWinnerBanner;
    private ProgressBar pbPlayerHp, pbEnemyHp;
    private ImageView imgPlayerSprite, imgEnemySprite;
    private ImageButton btnBackBattle;
    private Button btnReplayBattle;
    private ScrollView scrollHistory;

    private B_BattlePokemon activePlayerPokemon;
    private B_BattlePokemon enemyPokemon;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();

    private PokeApiService service;
    private String playerQuery = "1";
    private String rivalQuery = "25";

    private int currentTurn = 1;
    private final List<String> battleLogList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        service = ApiClient.getClient().create(PokeApiService.class);

        if (getIntent() != null) {
            if (getIntent().hasExtra("PLAYER_QUERY")) playerQuery = getIntent().getStringExtra("PLAYER_QUERY");
            if (getIntent().hasExtra("RIVAL_QUERY")) rivalQuery = getIntent().getStringExtra("RIVAL_QUERY");
        }

        initViews();
        iniciarFaseDeCarga();
    }

    private void initViews() {
        btnBackBattle = findViewById(R.id.btnBackBattle);
        btnReplayBattle = findViewById(R.id.btnReplayBattle);
        tvPlayerName = findViewById(R.id.tvPlayerName);
        tvPlayerHpExact = findViewById(R.id.tvPlayerHpExact);
        tvEnemyName = findViewById(R.id.tvEnemyName);
        tvEnemyHpPercent = findViewById(R.id.tvEnemyHpPercent);
        tvBattleLog = findViewById(R.id.tvBattleLog);
        tvLiveHistoryLog = findViewById(R.id.tvLiveHistoryLog);
        tvPlayerDamagePopup = findViewById(R.id.tvPlayerDamagePopup);
        tvEnemyDamagePopup = findViewById(R.id.tvEnemyDamagePopup);
        tvWinnerTitle = findViewById(R.id.tvWinnerTitle);
        tvWinnerSubtitle = findViewById(R.id.tvWinnerSubtitle);
        cardWinnerBanner = findViewById(R.id.cardWinnerBanner);
        scrollHistory = findViewById(R.id.scrollHistory);
        pbPlayerHp = findViewById(R.id.pbPlayerHp);
        pbEnemyHp = findViewById(R.id.pbEnemyHp);
        imgPlayerSprite = findViewById(R.id.imgPlayerSprite);
        imgEnemySprite = findViewById(R.id.imgEnemySprite);

        btnBackBattle.setOnClickListener(v -> finish());
        if (btnReplayBattle != null) btnReplayBattle.setOnClickListener(v -> recreate());
    }

    // =========================================================================================
    // FASE 1: DESCARGA SECUENCIAL Y PREPARACIÓN DE DATOS (CERO LÓGICA DE COMBATE AQUÍ)
    // =========================================================================================
    private void iniciarFaseDeCarga() {
        escribirLog("Buscando Pokémon del Jugador...");

        service.getPokemon(playerQuery).enqueue(new Callback<Pokemon>() {
            @Override
            public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                if (response.isSuccessful() && response.body() != null) {
                    activePlayerPokemon = mapearABattlePokemon(response.body(), true);
                    descargarPokemonRival();
                } else errorDeCarga("Error al cargar tu Pokémon");
            }
            @Override
            public void onFailure(Call<Pokemon> call, Throwable t) { errorDeCarga("Fallo de conexión"); }
        });
    }

    private void descargarPokemonRival() {
        escribirLog("Buscando Pokémon del Rival...");

        service.getPokemon(rivalQuery).enqueue(new Callback<Pokemon>() {
            @Override
            public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                if (response.isSuccessful() && response.body() != null) {
                    enemyPokemon = mapearABattlePokemon(response.body(), false);
                    precargarAtaques();
                } else errorDeCarga("Error al cargar el rival");
            }
            @Override
            public void onFailure(Call<Pokemon> call, Throwable t) { errorDeCarga("Fallo de conexión"); }
        });
    }

    private void precargarAtaques() {
        escribirLog("Preparando movimientos...");

        // Cargar los ataques de ambos al mismo tiempo. Cuando terminen ambos, inicia la batalla.
        descargarDetallesDeAtaques(activePlayerPokemon, () -> {
            descargarDetallesDeAtaques(enemyPokemon, this::setupBattle);
        });
    }

    private void descargarDetallesDeAtaques(B_BattlePokemon pokemon, Runnable onComplete) {
        if (pokemon.moves.length == 0) {
            pokemon.loadedMoves.add(new B_TurnAction("Struggle", 50, "normal", true));
            onComplete.run();
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
                        // Failsafe si la API falla en un ataque
                        pokemon.loadedMoves.add(new B_TurnAction(moveName, 40, "normal", true));
                    }
                    if (--pendientes[0] == 0) onComplete.run();
                }

                @Override
                public void onFailure(Call<MoveDetail> call, Throwable t) {
                    pokemon.loadedMoves.add(new B_TurnAction(moveName, 40, "normal", true));
                    if (--pendientes[0] == 0) onComplete.run();
                }
            });
        }
    }

    private B_BattlePokemon mapearABattlePokemon(Pokemon p, boolean isPlayer) {
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
        String icon = p.getSprites() != null ? p.getSprites().getFrontDefault() : "";
        String nombre = p.getName().substring(0, 1).toUpperCase() + p.getName().substring(1);

        return new B_BattlePokemon(nombre, hp, atk, def, spA, spD, spe, spriteAnimado, icon, selectedMoves, pkmnTypes);
    }

    private void errorDeCarga(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
        finish();
    }

    // =========================================================================================
    // FASE 2: EL BUCLE DEL SIMULADOR (INTERACTÚA CON EL MOTOR)
    // =========================================================================================
    private void setupBattle() {
        Glide.with(this).load(activePlayerPokemon.backSpriteUrl).into(imgPlayerSprite);
        Glide.with(this).load(enemyPokemon.backSpriteUrl).into(imgEnemySprite);
        actualizarNombresYBarrasBase();

        escribirLog("¡Combate iniciado: " + activePlayerPokemon.name + " VS " + enemyPokemon.name + "!");
        handler.postDelayed(this::ejecutarSiguienteTurnoAutomatico, 1500);
    }

    private void ejecutarSiguienteTurnoAutomatico() {
        if (activePlayerPokemon.isFainted() || enemyPokemon.isFainted()) return;

        registrarEnHistorial("--- Turno " + currentTurn + " ---");

        // 1. Elegir ataques aleatorios de la lista ya cargada
        B_TurnAction p1Action = activePlayerPokemon.loadedMoves.get(random.nextInt(activePlayerPokemon.loadedMoves.size()));
        B_TurnAction p2Action = enemyPokemon.loadedMoves.get(random.nextInt(enemyPokemon.loadedMoves.size()));

        // 2. EL MOTOR CALCULA TODO AL INSTANTE
        B_TurnResult result = B_BattleEngine.resolverTurno(activePlayerPokemon, p1Action, enemyPokemon, p2Action);

        // 3. ANIMAR LOS RESULTADOS POCO A POCO
        mostrarMensajeSecuencial(result.logMessages, 0, () -> {

            // Refrescar barras de vida y estados
            actualizarNombresYBarrasBase();

            if (result.p1Fainted || result.p2Fainted) {
                finalizarCombate();
            } else {
                currentTurn++;
                handler.postDelayed(this::ejecutarSiguienteTurnoAutomatico, 1200);
            }
        });
    }

    // =========================================================================================
    // FASE 3: INTERFAZ Y ANIMACIONES
    // =========================================================================================

    // Función recursiva para imprimir el log mensaje por mensaje con retraso (efecto máquina de escribir)
    private void mostrarMensajeSecuencial(List<String> mensajes, int index, Runnable onComplete) {
        if (index >= mensajes.size()) {
            onComplete.run();
            return;
        }

        String msg = mensajes.get(index);
        tvBattleLog.setText(msg);
        registrarEnHistorial(msg);

        // Lanzar popup de daño si el mensaje implica que hubo daño (Lógica visual simple)
        if (msg.contains("daño") || msg.contains("eficaz") || msg.contains("HP")) {
            animarBarrasVidaConTransicion(activePlayerPokemon.currentHp, enemyPokemon.currentHp);
        }

        handler.postDelayed(() -> mostrarMensajeSecuencial(mensajes, index + 1, onComplete), 900);
    }

    private void animarBarrasVidaConTransicion(int nuevoHpP1, int nuevoHpP2) {
        // Animación suave de la barra del Jugador
        ValueAnimator animP1 = ValueAnimator.ofInt(pbPlayerHp.getProgress(), nuevoHpP1);
        animP1.setDuration(500);
        animP1.addUpdateListener(animation -> {
            int val = (int) animation.getAnimatedValue();
            pbPlayerHp.setProgress(val);
            tvPlayerHpExact.setText(val + "/" + activePlayerPokemon.maxHp);
        });
        animP1.start();

        // Animación suave de la barra del Rival
        int pctNuevoRival = (int) (((double) nuevoHpP2 / enemyPokemon.maxHp) * 100);
        ValueAnimator animP2 = ValueAnimator.ofInt(pbEnemyHp.getProgress(), pctNuevoRival);
        animP2.setDuration(500);
        animP2.addUpdateListener(animation -> {
            int val = (int) animation.getAnimatedValue();
            pbEnemyHp.setProgress(val);
            tvEnemyHpPercent.setText(val + "%");
        });
        animP2.start();
    }

    private void actualizarNombresYBarrasBase() {
        String pStatus = activePlayerPokemon.getStatusString().isEmpty() ? "" : " [" + activePlayerPokemon.getStatusString() + "]";
        tvPlayerName.setText(activePlayerPokemon.name + pStatus + " Lvl 50");
        pbPlayerHp.setMax(activePlayerPokemon.maxHp);
        pbPlayerHp.setProgress(activePlayerPokemon.currentHp);
        tvPlayerHpExact.setText(activePlayerPokemon.currentHp + "/" + activePlayerPokemon.maxHp);

        String eStatus = enemyPokemon.getStatusString().isEmpty() ? "" : " [" + enemyPokemon.getStatusString() + "]";
        tvEnemyName.setText(enemyPokemon.name + eStatus + " Lvl 50");
        pbEnemyHp.setMax(100);
        pbEnemyHp.setProgress((int) (((double) enemyPokemon.currentHp / enemyPokemon.maxHp) * 100));
        tvEnemyHpPercent.setText((int) (((double) enemyPokemon.currentHp / enemyPokemon.maxHp) * 100) + "%");
    }

    private void finalizarCombate() {
        String ganador;
        if (!activePlayerPokemon.isFainted()) {
            ganador = activePlayerPokemon.name;
            escribirLog("¡" + enemyPokemon.name + " se ha debilitado! ¡Has ganado!");
            if (tvWinnerTitle != null) {
                tvWinnerTitle.setText("¡VICTORIA!");
                tvWinnerTitle.setTextColor(Color.parseColor("#FBBF24")); // Dorado
            }
        } else {
            ganador = enemyPokemon.name;
            escribirLog("¡" + activePlayerPokemon.name + " se ha debilitado! Ganó " + enemyPokemon.name);
            if (tvWinnerTitle != null) {
                tvWinnerTitle.setText("¡DERROTA!");
                tvWinnerTitle.setTextColor(Color.parseColor("#EF4444")); // Rojo
            }
        }

        if (cardWinnerBanner != null && tvWinnerSubtitle != null) {
            tvWinnerSubtitle.setText("Ganador: " + ganador);
            cardWinnerBanner.setVisibility(View.VISIBLE);
            cardWinnerBanner.setAlpha(0f);
            cardWinnerBanner.setScaleX(0.7f);
            cardWinnerBanner.setScaleY(0.7f);
            cardWinnerBanner.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(500).start();
        }

        if (btnReplayBattle != null) btnReplayBattle.setVisibility(View.VISIBLE);
        guardarRegistroEnFirestore(ganador);
    }

    private void registrarEnHistorial(String linea) {
        battleLogList.add(linea);
        if (tvLiveHistoryLog != null) {
            tvLiveHistoryLog.append(linea + "\n");
            if (scrollHistory != null) scrollHistory.post(() -> scrollHistory.fullScroll(View.FOCUS_DOWN));
        }
    }

    private void guardarRegistroEnFirestore(String ganador) {
        if (currentUser == null) return;
        B_BattleRecord record = new B_BattleRecord(activePlayerPokemon.name, enemyPokemon.name, ganador, System.currentTimeMillis(), battleLogList);
        db.collection("users").document(currentUser.getUid()).collection("battle_history")
                .add(record)
                .addOnSuccessListener(documentReference -> Toast.makeText(B_SimuladorActivity.this, "Combate guardado en el historial", Toast.LENGTH_SHORT).show());
    }

    private void escribirLog(String mensaje) { tvBattleLog.setText(mensaje); }
}