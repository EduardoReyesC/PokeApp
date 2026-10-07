package com.example.pokeappicesba;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BattleActivity extends AppCompatActivity {

    private TextView tvPlayerName, tvPlayerHpExact, tvEnemyName, tvEnemyHpPercent, tvBattleLog;
    private ProgressBar pbPlayerHp, pbEnemyHp;
    private ImageView imgPlayerSprite, imgEnemySprite;
    private LinearLayout boxTeamSwitch;
    private CardView btnAttack1, btnAttack2, btnAttack3, btnAttack4;
    private ImageButton btnBackBattle;
    private Button btnReplayBattle;

    private BattlePokemon enemyPokemon;
    private BattlePokemon activePlayerPokemon;
    private List<BattlePokemon> playerTeam = new ArrayList<>();

    private boolean isTurnExecuting = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();

    private PokeApiService service;
    private String playerQuery = "1";
    private String rivalQuery = "25";
    private boolean playerLoaded = false;
    private boolean rivalLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle);

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
        pbPlayerHp = findViewById(R.id.pbPlayerHp);
        pbEnemyHp = findViewById(R.id.pbEnemyHp);
        imgPlayerSprite = findViewById(R.id.imgPlayerSprite);
        imgEnemySprite = findViewById(R.id.imgEnemySprite);
        boxTeamSwitch = findViewById(R.id.boxTeamSwitch);
        btnAttack1 = findViewById(R.id.btnAttack1);
        btnAttack2 = findViewById(R.id.btnAttack2);
        btnAttack3 = findViewById(R.id.btnAttack3);
        btnAttack4 = findViewById(R.id.btnAttack4);

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
                    playerTeam.add(activePlayerPokemon);
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

        // --- FILTRO DE ATAQUES (DRY usando modelo centralizado) ---
        List<String> poolFinal = new ArrayList<>();
        if (p.getLevelUpMoves() != null) {
            poolFinal.addAll(p.getLevelUpMoves());
        }
        Collections.shuffle(poolFinal);

        String[] randomMoves = new String[]{"STRUGGLE", "STRUGGLE", "STRUGGLE", "STRUGGLE"};
        for (int i = 0; i < Math.min(4, poolFinal.size()); i++) {
            randomMoves[i] = poolFinal.get(i);
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

        actualizarBotonesAtaque();
        setupListeners();
        renderizarEquipo();
        actualizarUI();
        escribirLog("¡Un " + enemyPokemon.name + " salvaje apareció!");
    }

    private void actualizarBotonesAtaque() {
        TextView[] nameViews = {
                btnAttack1.findViewById(R.id.tvAtk1Name), btnAttack2.findViewById(R.id.tvAtk2Name),
                btnAttack3.findViewById(R.id.tvAtk3Name), btnAttack4.findViewById(R.id.tvAtk4Name)
        };
        TextView[] infoViews = {
                btnAttack1.findViewById(R.id.tvAtk1Info), btnAttack2.findViewById(R.id.tvAtk2Info),
                btnAttack3.findViewById(R.id.tvAtk3Info), btnAttack4.findViewById(R.id.tvAtk4Info)
        };
        CardView[] btnCards = { btnAttack1, btnAttack2, btnAttack3, btnAttack4 };

        for (int i = 0; i < 4; i++) {
            String moveName = activePlayerPokemon.moves[i];
            nameViews[i].setText(moveName);
            infoViews[i].setText("...");
            btnCards[i].setCardBackgroundColor(Color.parseColor("#64748B"));

            String queryMove = moveName.toLowerCase().replace(" ", "-");
            final int index = i;

            service.getMoveDetail(queryMove).enqueue(new Callback<MoveDetail>() {
                @Override
                public void onResponse(Call<MoveDetail> call, Response<MoveDetail> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().getType() != null) {
                        String typeName = response.body().getType().getName();
                        infoViews[index].setText(typeName.toUpperCase());
                        btnCards[index].setCardBackgroundColor(TypeColorUtil.getColorForType(typeName));
                    }
                }
                @Override
                public void onFailure(Call<MoveDetail> call, Throwable t) {
                    infoViews[index].setText("???");
                }
            });
        }
    }

    private void setupListeners() {
        btnAttack1.setOnClickListener(v -> ejecutarTurno(activePlayerPokemon.moves[0]));
        btnAttack2.setOnClickListener(v -> ejecutarTurno(activePlayerPokemon.moves[1]));
        btnAttack3.setOnClickListener(v -> ejecutarTurno(activePlayerPokemon.moves[2]));
        btnAttack4.setOnClickListener(v -> ejecutarTurno(activePlayerPokemon.moves[3]));
    }

    private void renderizarEquipo() {
        boxTeamSwitch.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (BattlePokemon pkm : playerTeam) {
            View card = inflater.inflate(R.layout.item_battle_team, boxTeamSwitch, false);
            ImageView imgIcon = card.findViewById(R.id.imgTeamIcon);
            ProgressBar pbHp = card.findViewById(R.id.pbTeamHp);
            CardView rootCard = (CardView) card;

            Glide.with(this).load(pkm.iconUrl).into(imgIcon);
            pbHp.setProgress((int) (((double) pkm.currentHp / pkm.maxHp) * 100));

            if (pkm.isFainted()) {
                rootCard.setCardBackgroundColor(Color.parseColor("#475569"));
                imgIcon.setAlpha(0.4f);
            } else if (pkm == activePlayerPokemon) {
                rootCard.setCardBackgroundColor(Color.parseColor("#0284C7"));
            }

            card.setOnClickListener(v -> ejecutarCambio(pkm));
            boxTeamSwitch.addView(card);
        }
    }

    private void ejecutarCambio(BattlePokemon nuevoPokemon) {
        if (isTurnExecuting || nuevoPokemon.isFainted() || nuevoPokemon == activePlayerPokemon) return;
        isTurnExecuting = true;

        escribirLog("¡Vuelve, " + activePlayerPokemon.name + "!");

        handler.postDelayed(() -> {
            activePlayerPokemon = nuevoPokemon;

            actualizarBotonesAtaque();
            setupListeners();

            actualizarUI();
            renderizarEquipo();
            escribirLog("¡Adelante, " + activePlayerPokemon.name + "!");

            String eMove = enemyPokemon.moves[random.nextInt(4)];
            handler.postDelayed(() -> buscarDañoYEjecutarRival(eMove), 1200);
        }, 1200);
    }

    private void buscarDañoYEjecutarRival(String eMove) {
        String queryEnemyMove = eMove.toLowerCase().replace(" ", "-");
        service.getMoveDetail(queryEnemyMove).enqueue(new Callback<MoveDetail>() {
            @Override
            public void onResponse(Call<MoveDetail> call, Response<MoveDetail> response) {
                int tempEPower = 40;
                String tempEType = "normal";
                boolean tempEIsPhysical = false;

                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getPower() != null) tempEPower = response.body().getPower();
                    if (response.body().getType() != null) tempEType = response.body().getType().getName();
                    if (response.body().getDamageClass() != null && response.body().getDamageClass().equals("physical")) tempEIsPhysical = true;
                }

                final int ePower = tempEPower;
                final boolean eIsPhysical = tempEIsPhysical;
                final boolean eIsStab = enemyPokemon.types != null && enemyPokemon.types.contains(tempEType);
                final double eEff = TypeCalculator.getAttackMultiplier(tempEType, activePlayerPokemon.types);

                procesarAtaque(enemyPokemon, activePlayerPokemon, eMove, ePower, eEff, eIsStab, eIsPhysical, BattleActivity.this::aplicarEfectosFinalTurno);
            }
            @Override
            public void onFailure(Call<MoveDetail> call, Throwable t) {
                procesarAtaque(enemyPokemon, activePlayerPokemon, eMove, 40, 1.0, false, false, BattleActivity.this::aplicarEfectosFinalTurno);
            }
        });
    }

    private void ejecutarTurno(String pMove) {
        if (isTurnExecuting || activePlayerPokemon.isFainted() || enemyPokemon.isFainted()) return;
        isTurnExecuting = true;

        String eMove = enemyPokemon.moves[random.nextInt(4)];
        String queryPlayerMove = pMove.toLowerCase().replace(" ", "-");
        String queryEnemyMove = eMove.toLowerCase().replace(" ", "-");

        escribirLog("Atacando...");

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
                        isTurnExecuting = false;
                        escribirLog("Error al cargar ataque rival.");
                    }
                });
            }

            @Override
            public void onFailure(Call<MoveDetail> call, Throwable t) {
                isTurnExecuting = false;
                escribirLog("Error al cargar tu ataque.");
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
            if (atacante.currentStatus == BattlePokemon.Status.SLEEP) {
                escribirLog(atacante.name + " está profundamente dormido.");
            } else if (atacante.currentStatus == BattlePokemon.Status.PARALYSIS) {
                escribirLog(atacante.name + " está paralizado y no puede moverse.");
            } else if (atacante.currentStatus == BattlePokemon.Status.FREEZE) {
                escribirLog(atacante.name + " está congelado sólido.");
            }
            handler.postDelayed(onComplete, 1200);
            return;
        }

        escribirLog("¡" + atacante.name + " usó " + moveName + "!");

        handler.postDelayed(() -> {
            int damage = BattleEngine.calculateDamage(power, atacante, defensor, eff, isStab, isPhysical);
            defensor.currentHp = Math.max(0, defensor.currentHp - damage);
            actualizarUI();
            renderizarEquipo();

            if (eff > 1.0) escribirLog("¡Es súper eficaz!");
            else if (eff < 1.0) escribirLog("No es muy eficaz...");

            aplicarProbabilidadEstado(moveName, defensor);

            handler.postDelayed(onComplete, 1200);
        }, 1200);
    }

    private void aplicarProbabilidadEstado(String moveName, BattlePokemon defensor) {
        if (defensor.isFainted() || defensor.currentStatus != BattlePokemon.Status.NONE) return;

        if (random.nextInt(100) < 10) {
            if (random.nextBoolean()) {
                defensor.currentStatus = BattlePokemon.Status.BURN;
                escribirLog("¡" + defensor.name + " fue quemado!");
            } else {
                defensor.currentStatus = BattlePokemon.Status.PARALYSIS;
                escribirLog("¡" + defensor.name + " fue paralizado!");
            }
            actualizarUI();
        }
    }

    private void aplicarEfectosFinalTurno() {
        boolean dañoResidual = false;

        if (!activePlayerPokemon.isFainted() &&
                (activePlayerPokemon.currentStatus == BattlePokemon.Status.BURN || activePlayerPokemon.currentStatus == BattlePokemon.Status.POISON)) {
            int damage = Math.max(1, activePlayerPokemon.maxHp / 8);
            activePlayerPokemon.currentHp = Math.max(0, activePlayerPokemon.currentHp - damage);
            escribirLog(activePlayerPokemon.name + " sufre por su estado.");
            dañoResidual = true;
        }

        if (!enemyPokemon.isFainted() &&
                (enemyPokemon.currentStatus == BattlePokemon.Status.BURN || enemyPokemon.currentStatus == BattlePokemon.Status.POISON)) {
            int damage = Math.max(1, enemyPokemon.maxHp / 8);
            enemyPokemon.currentHp = Math.max(0, enemyPokemon.currentHp - damage);
            escribirLog(enemyPokemon.name + " sufre por su estado.");
            dañoResidual = true;
        }

        actualizarUI();
        renderizarEquipo();

        if (dañoResidual) {
            handler.postDelayed(this::finalizarTurno, 1200);
        } else {
            finalizarTurno();
        }
    }

    private void finalizarTurno() {
        if (activePlayerPokemon.isFainted()) {
            escribirLog("¡" + activePlayerPokemon.name + " se ha debilitado!");
            if (btnReplayBattle != null) btnReplayBattle.setVisibility(View.VISIBLE);
            isTurnExecuting = false;
        } else if (enemyPokemon.isFainted()) {
            escribirLog("¡" + enemyPokemon.name + " se ha debilitado! ¡Has ganado!");
            if (btnReplayBattle != null) btnReplayBattle.setVisibility(View.VISIBLE);
        } else {
            escribirLog("¿Qué debería hacer " + activePlayerPokemon.name + "?");
            isTurnExecuting = false;
        }
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
        pbEnemyHp.setMax(enemyPokemon.maxHp);
        pbEnemyHp.setProgress(enemyPokemon.currentHp);
        tvEnemyHpPercent.setText((int) (((double) enemyPokemon.currentHp / enemyPokemon.maxHp) * 100) + "%");
    }

    private void escribirLog(String mensaje) {
        tvBattleLog.setText(mensaje);
    }
}