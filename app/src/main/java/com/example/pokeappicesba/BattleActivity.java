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

public class BattleActivity extends AppCompatActivity {

    private TextView tvPlayerName, tvPlayerHpExact, tvEnemyName, tvEnemyHpPercent, tvBattleLog, tvLiveHistoryLog;
    private TextView tvPlayerDamagePopup, tvEnemyDamagePopup;
    private TextView tvWinnerTitle, tvWinnerSubtitle;
    private CardView cardWinnerBanner;
    private ProgressBar pbPlayerHp, pbEnemyHp;
    private ImageView imgPlayerSprite, imgEnemySprite;
    private ImageButton btnBackBattle;
    private Button btnReplayBattle;
    private ScrollView scrollHistory;

    private BattlePokemon enemyPokemon;
    private BattlePokemon activePlayerPokemon;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();

    private PokeApiService service;
    private String playerQuery = "1";
    private String rivalQuery = "25";
    private boolean playerLoaded = false;
    private boolean rivalLoaded = false;

    // Control de Turnos e Historial
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
            if (getIntent().hasExtra("PLAYER_QUERY")) {
                playerQuery = getIntent().getStringExtra("PLAYER_QUERY");
            }
            if (getIntent().hasExtra("RIVAL_QUERY")) {
                rivalQuery = getIntent().getStringExtra("RIVAL_QUERY");
            }
        }

        initViews();
        cargarDatosDeCombate();
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
        if (btnReplayBattle != null) {
            btnReplayBattle.setOnClickListener(v -> recreate());
        }
    }

    private void cargarDatosDeCombate() {
        escribirLog("Conectando con el estadio...");

        service.getPokemon(playerQuery).enqueue(new Callback<Pokemon>() {
            @Override
            public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                if (response.isSuccessful() && response.body() != null) {
                    activePlayerPokemon = mapearABattlePokemon(response.body(), true);
                    playerLoaded = true;
                    verificarCargaCompleta();
                } else {
                    errorDeCarga("Error al cargar tu Pokémon");
                }
            }
            @Override
            public void onFailure(Call<Pokemon> call, Throwable t) {
                errorDeCarga("Fallo de conexión (Jugador)");
            }
        });

        service.getPokemon(rivalQuery).enqueue(new Callback<Pokemon>() {
            @Override
            public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                if (response.isSuccessful() && response.body() != null) {
                    enemyPokemon = mapearABattlePokemon(response.body(), false);
                    rivalLoaded = true;
                    verificarCargaCompleta();
                } else {
                    errorDeCarga("Error al cargar el rival");
                }
            }
            @Override
            public void onFailure(Call<Pokemon> call, Throwable t) {
                errorDeCarga("Fallo de conexión (Rival)");
            }
        });
    }

    private BattlePokemon mapearABattlePokemon(Pokemon p, boolean isPlayer) {
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

        String[] randomMoves = new String[]{"Struggle", "Struggle", "Struggle", "Struggle"};
        if (p.getMoves() != null && !p.getMoves().isEmpty()) {
            List<Pokemon.PokemonMoveSlot> allMoves = new ArrayList<>(p.getMoves());
            Collections.shuffle(allMoves);
            for (int i = 0; i < Math.min(4, allMoves.size()); i++) {
                randomMoves[i] = allMoves.get(i).getMove().getName().replace("-", " ").toUpperCase();
            }
        }

        List<String> pkmnTypes = new ArrayList<>();
        if (p.getTypes() != null) {
            for (TypeSlot ts : p.getTypes()) {
                if (ts.getType() != null) pkmnTypes.add(ts.getType().getName());
            }
        }

        String spriteAnimado = isPlayer
                ? "https://play.pokemonshowdown.com/sprites/gen5ani-back/" + p.getName() + ".gif"
                : "https://play.pokemonshowdown.com/sprites/gen5ani/" + p.getName() + ".gif";

        String icon = p.getSprites() != null ? p.getSprites().getFrontDefault() : "";
        String nombre = p.getName().substring(0, 1).toUpperCase() + p.getName().substring(1);

        return new BattlePokemon(nombre, hp, atk, def, spA, spD, spe, spriteAnimado, icon, randomMoves, pkmnTypes);
    }

    private void verificarCargaCompleta() {
        if (playerLoaded && rivalLoaded) {
            setupBattle();
        }
    }

    private void errorDeCarga(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void setupBattle() {
        Glide.with(this).load(enemyPokemon.backSpriteUrl).into(imgEnemySprite);
        actualizarUI();
        escribirLog("¡Combate iniciado: " + activePlayerPokemon.name + " VS " + enemyPokemon.name + "!");

        handler.postDelayed(this::ejecutarSiguienteTurnoAutomatico, 1500);
    }

    private void ejecutarSiguienteTurnoAutomatico() {
        if (activePlayerPokemon.isFainted() || enemyPokemon.isFainted()) return;

        String pMove = activePlayerPokemon.moves[random.nextInt(4)];
        String eMove = enemyPokemon.moves[random.nextInt(4)];

        String queryPlayerMove = pMove.toLowerCase().replace(" ", "-");
        String queryEnemyMove = eMove.toLowerCase().replace(" ", "-");

        escribirLog("Turno " + currentTurn + " en curso...");

        service.getMoveDetail(queryPlayerMove).enqueue(new Callback<MoveDetail>() {
            @Override
            public void onResponse(Call<MoveDetail> call, Response<MoveDetail> pRes) {
                int tempPPower = 40;
                String tempPType = "normal";
                boolean tempPIsPhysical = false;

                if (pRes.isSuccessful() && pRes.body() != null) {
                    if (pRes.body().getPower() != null) tempPPower = pRes.body().getPower();
                    if (pRes.body().getType() != null) tempPType = pRes.body().getType().getName();
                    if (pRes.body().getDamageClass() != null && pRes.body().getDamageClass().equals("physical")) tempPIsPhysical = true;
                }

                final int pPower = tempPPower;
                final String pType = tempPType;
                final boolean pIsPhysical = tempPIsPhysical;
                final boolean pIsStab = activePlayerPokemon.types != null && activePlayerPokemon.types.contains(pType);
                final double pEff = TypeCalculator.getAttackMultiplier(pType, enemyPokemon.types);

                service.getMoveDetail(queryEnemyMove).enqueue(new Callback<MoveDetail>() {
                    @Override
                    public void onResponse(Call<MoveDetail> call2, Response<MoveDetail> eRes) {
                        int tempEPower = 40;
                        String tempEType = "normal";
                        boolean tempEIsPhysical = false;

                        if (eRes.isSuccessful() && eRes.body() != null) {
                            if (eRes.body().getPower() != null) tempEPower = eRes.body().getPower();
                            if (eRes.body().getType() != null) tempEType = eRes.body().getType().getName();
                            if (eRes.body().getDamageClass() != null && eRes.body().getDamageClass().equals("physical")) tempEIsPhysical = true;
                        }

                        final int ePower = tempEPower;
                        final boolean eIsPhysical = tempEIsPhysical;
                        final boolean eIsStab = enemyPokemon.types != null && enemyPokemon.types.contains(tempEType);
                        final double eEff = TypeCalculator.getAttackMultiplier(tempEType, activePlayerPokemon.types);

                        registrarEnHistorial("--- Turno " + currentTurn + " ---");
                        registrarEnHistorial(activePlayerPokemon.name + " usó " + pMove);
                        registrarEnHistorial(enemyPokemon.name + " usó " + eMove);

                        boolean playerGoesFirst = BattleEngine.doesAGoFirst(activePlayerPokemon, enemyPokemon, 0, 0);

                        if (playerGoesFirst) {
                            procesarAtaque(activePlayerPokemon, enemyPokemon, pMove, pPower, pEff, pIsStab, pIsPhysical, () -> {
                                procesarAtaque(enemyPokemon, activePlayerPokemon, eMove, ePower, eEff, eIsStab, eIsPhysical, BattleActivity.this::aplicarEfectosFinalTurno);
                            });
                        } else {
                            procesarAtaque(enemyPokemon, activePlayerPokemon, eMove, ePower, eEff, eIsStab, eIsPhysical, () -> {
                                procesarAtaque(activePlayerPokemon, enemyPokemon, pMove, pPower, pEff, pIsStab, pIsPhysical, BattleActivity.this::aplicarEfectosFinalTurno);
                            });
                        }
                    }

                    @Override
                    public void onFailure(Call<MoveDetail> call2, Throwable t) {
                        ejecutarSiguienteTurnoAutomatico();
                    }
                });
            }

            @Override
            public void onFailure(Call<MoveDetail> call, Throwable t) {
                ejecutarSiguienteTurnoAutomatico();
            }
        });
    }

    private void procesarAtaque(BattlePokemon atacante, BattlePokemon defensor, String moveName,
                                int power, double eff, boolean isStab, boolean isPhysical, Runnable onComplete) {

        if (atacante.isFainted() || defensor.isFainted()) {
            onComplete.run();
            return;
        }

        if (!BattleEngine.canMoveThisTurn(atacante)) {
            String statusMsg = atacante.name + " no puede moverse por su estado.";
            escribirLog(statusMsg);
            registrarEnHistorial(statusMsg);
            handler.postDelayed(onComplete, 1200);
            return;
        }

        String actionMsg = "¡" + atacante.name + " usó " + moveName + "!";
        escribirLog(actionMsg);

        handler.postDelayed(() -> {
            int damage = BattleEngine.calculateDamage(power, atacante, defensor, eff, isStab, isPhysical);
            aplicarDanioConAnimacion(defensor, damage);

            if (eff > 1.0) {
                escribirLog("¡Es súper eficaz!");
                registrarEnHistorial("¡Es súper eficaz! (-" + damage + " HP a " + defensor.name + ")");
            } else if (eff < 1.0) {
                escribirLog("No es muy eficaz...");
                registrarEnHistorial("No es muy eficaz... (-" + damage + " HP a " + defensor.name + ")");
            } else {
                registrarEnHistorial("-" + damage + " HP a " + defensor.name);
            }

            aplicarProbabilidadEstado(moveName, defensor);
            handler.postDelayed(onComplete, 1400);
        }, 1200);
    }

    private void aplicarDanioConAnimacion(BattlePokemon defensor, int damage) {
        int hpAnterior = defensor.currentHp;
        int nuevoHp = Math.max(0, defensor.currentHp - damage);
        defensor.currentHp = nuevoHp;

        boolean isPlayer = (defensor == activePlayerPokemon);
        ProgressBar pb = isPlayer ? pbPlayerHp : pbEnemyHp;
        TextView popup = isPlayer ? tvPlayerDamagePopup : tvEnemyDamagePopup;

        if (popup != null && damage > 0) {
            popup.setText("-" + damage + " HP");
            popup.setVisibility(View.VISIBLE);
            popup.setAlpha(1f);
            popup.setTranslationY(0f);
            popup.animate()
                    .translationY(-25f)
                    .alpha(0f)
                    .setDuration(1200)
                    .withEndAction(() -> popup.setVisibility(View.INVISIBLE))
                    .start();
        }

        if (isPlayer) {
            ValueAnimator animator = ValueAnimator.ofInt(hpAnterior, nuevoHp);
            animator.setDuration(600);
            animator.addUpdateListener(animation -> {
                int val = (int) animation.getAnimatedValue();
                pb.setProgress(val);
                tvPlayerHpExact.setText(val + "/" + activePlayerPokemon.maxHp);
            });
            animator.start();
        } else {
            int max = enemyPokemon.maxHp;
            int pctAnterior = (int) (((double) hpAnterior / max) * 100);
            int pctNuevo = (int) (((double) nuevoHp / max) * 100);

            ValueAnimator animator = ValueAnimator.ofInt(pctAnterior, pctNuevo);
            animator.setDuration(600);
            animator.addUpdateListener(animation -> {
                int val = (int) animation.getAnimatedValue();
                pb.setProgress(val);
                tvEnemyHpPercent.setText(val + "%");
            });
            animator.start();
        }
    }

    private void aplicarProbabilidadEstado(String moveName, BattlePokemon defensor) {
        if (defensor.isFainted() || defensor.currentStatus != BattlePokemon.Status.NONE) return;

        if (random.nextInt(100) < 10) {
            String msg;
            if (random.nextBoolean()) {
                defensor.currentStatus = BattlePokemon.Status.BURN;
                msg = "¡" + defensor.name + " fue quemado!";
            } else {
                defensor.currentStatus = BattlePokemon.Status.PARALYSIS;
                msg = "¡" + defensor.name + " fue paralizado!";
            }
            escribirLog(msg);
            registrarEnHistorial(msg);
            actualizarUI();
        }
    }

    private void aplicarEfectosFinalTurno() {
        boolean dañoResidual = false;

        if (!activePlayerPokemon.isFainted() &&
                (activePlayerPokemon.currentStatus == BattlePokemon.Status.BURN || activePlayerPokemon.currentStatus == BattlePokemon.Status.POISON)) {
            int damage = Math.max(1, activePlayerPokemon.maxHp / 8);
            aplicarDanioConAnimacion(activePlayerPokemon, damage);
            String msg = activePlayerPokemon.name + " sufre por su estado.";
            escribirLog(msg);
            registrarEnHistorial(msg + " (-" + damage + " HP)");
            dañoResidual = true;
        }

        if (!enemyPokemon.isFainted() &&
                (enemyPokemon.currentStatus == BattlePokemon.Status.BURN || enemyPokemon.currentStatus == BattlePokemon.Status.POISON)) {
            int damage = Math.max(1, enemyPokemon.maxHp / 8);
            aplicarDanioConAnimacion(enemyPokemon, damage);
            String msg = enemyPokemon.name + " sufre por su estado.";
            escribirLog(msg);
            registrarEnHistorial(msg + " (-" + damage + " HP)");
            dañoResidual = true;
        }

        actualizarUI();

        if (dañoResidual) {
            handler.postDelayed(this::finalizarTurno, 1300);
        } else {
            finalizarTurno();
        }
    }

    private void finalizarTurno() {
        if (activePlayerPokemon.isFainted() || enemyPokemon.isFainted()) {
            String ganador;
            boolean playerGano = !activePlayerPokemon.isFainted();

            if (playerGano) {
                ganador = activePlayerPokemon.name;
                escribirLog("¡" + enemyPokemon.name + " se ha debilitado! ¡Has ganado!");
                registrarEnHistorial("Ganador: " + activePlayerPokemon.name);

                if (tvWinnerTitle != null) {
                    tvWinnerTitle.setText("¡VICTORIA!");
                    tvWinnerTitle.setTextColor(Color.parseColor("#FBBF24")); // Dorado
                }
            } else {
                ganador = enemyPokemon.name;
                escribirLog("¡" + activePlayerPokemon.name + " se ha debilitado! Ganó " + enemyPokemon.name);
                registrarEnHistorial("Ganador: " + enemyPokemon.name);

                if (tvWinnerTitle != null) {
                    tvWinnerTitle.setText("¡DERROTA!");
                    tvWinnerTitle.setTextColor(Color.parseColor("#EF4444")); // Rojo
                }
            }

            // Mostrar el cartel central de ganador
            if (cardWinnerBanner != null && tvWinnerSubtitle != null) {
                tvWinnerSubtitle.setText("Ganador: " + ganador);
                cardWinnerBanner.setVisibility(View.VISIBLE);
                cardWinnerBanner.setAlpha(0f);
                cardWinnerBanner.setScaleX(0.7f);
                cardWinnerBanner.setScaleY(0.7f);
                cardWinnerBanner.animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(500)
                        .start();
            }

            if (btnReplayBattle != null) btnReplayBattle.setVisibility(View.VISIBLE);
            guardarRegistroEnFirestore(ganador);
        } else {
            currentTurn++;
            handler.postDelayed(this::ejecutarSiguienteTurnoAutomatico, 1200);
        }
    }

    private void registrarEnHistorial(String linea) {
        battleLogList.add(linea);
        if (tvLiveHistoryLog != null) {
            tvLiveHistoryLog.append(linea + "\n");
            if (scrollHistory != null) {
                scrollHistory.post(() -> scrollHistory.fullScroll(View.FOCUS_DOWN));
            }
        }
    }

    private void guardarRegistroEnFirestore(String ganador) {
        if (currentUser == null) return;

        BattleRecord record = new BattleRecord(
                activePlayerPokemon.name,
                enemyPokemon.name,
                ganador,
                System.currentTimeMillis(),
                battleLogList
        );

        db.collection("users")
                .document(currentUser.getUid())
                .collection("battle_history")
                .add(record)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(BattleActivity.this, "Combate guardado en el historial", Toast.LENGTH_SHORT).show();
                });
    }

    private void actualizarUI() {
        String pStatus = activePlayerPokemon.getStatusString().isEmpty() ? "" : " [" + activePlayerPokemon.getStatusString() + "]";
        tvPlayerName.setText(activePlayerPokemon.name + pStatus + " Lvl 50");
        pbPlayerHp.setMax(activePlayerPokemon.maxHp);
        pbPlayerHp.setProgress(activePlayerPokemon.currentHp);
        tvPlayerHpExact.setText(activePlayerPokemon.currentHp + "/" + activePlayerPokemon.maxHp);
        Glide.with(this).load(activePlayerPokemon.backSpriteUrl).into(imgPlayerSprite);

        String eStatus = enemyPokemon.getStatusString().isEmpty() ? "" : " [" + enemyPokemon.getStatusString() + "]";
        tvEnemyName.setText(enemyPokemon.name + eStatus + " Lvl 50");
        pbEnemyHp.setMax(100);
        pbEnemyHp.setProgress((int) (((double) enemyPokemon.currentHp / enemyPokemon.maxHp) * 100));
        tvEnemyHpPercent.setText((int) (((double) enemyPokemon.currentHp / enemyPokemon.maxHp) * 100) + "%");
    }

    private void escribirLog(String mensaje) {
        tvBattleLog.setText(mensaje);
    }
}