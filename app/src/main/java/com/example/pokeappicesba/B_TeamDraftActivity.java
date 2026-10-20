package com.example.pokeappicesba;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Random;

public class B_TeamDraftActivity extends AppCompatActivity {

    private boolean isPlayer1Turn = true;
    private ArrayList<String> player1Team = new ArrayList<>();
    private ArrayList<String> player2Team = new ArrayList<>();

    private String currentSearchedPokemon = "";
    private String currentSpriteUrl = "";
    private final Random randomGenerator = new Random();

    private TextView tvDraftTurn, tvBlindMessage;
    private EditText etSearchPokemon;
    private ImageView ivPreview, slot1, slot2, slot3;
    private Button btnSearch, btnRandom, btnAddTeam, btnRemoveLast, btnConfirmTeam, btnBlindReady;
    private FrameLayout overlayBlindScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_draft);

        // Vincular Vistas
        tvDraftTurn = findViewById(R.id.tvDraftTurn);
        etSearchPokemon = findViewById(R.id.etSearchPokemon);
        ivPreview = findViewById(R.id.ivPreview);
        slot1 = findViewById(R.id.slot1);
        slot2 = findViewById(R.id.slot2);
        slot3 = findViewById(R.id.slot3);

        btnSearch = findViewById(R.id.btnSearch);
        btnRandom = findViewById(R.id.btnRandom); // NUEVO
        btnAddTeam = findViewById(R.id.btnAddTeam);
        btnRemoveLast = findViewById(R.id.btnRemoveLast); // NUEVO
        btnConfirmTeam = findViewById(R.id.btnConfirmTeam);

        overlayBlindScreen = findViewById(R.id.overlayBlindScreen);
        tvBlindMessage = findViewById(R.id.tvBlindMessage);
        btnBlindReady = findViewById(R.id.btnBlindReady);
        ImageView btnBackDraft = findViewById(R.id.btnBackDraft);

        btnBackDraft.setOnClickListener(v -> finish());

        // Botón Buscar
        btnSearch.setOnClickListener(v -> {
            String query = etSearchPokemon.getText().toString().trim().toLowerCase();
            if (!query.isEmpty()) {
                fetchPokemonData(query);
            } else {
                Toast.makeText(this, "Ingresa un ID o Nombre", Toast.LENGTH_SHORT).show();
            }
        });

        // NUEVO: Botón Azar (Saca un ID entre 1 y 1025 y busca automáticamente)
        btnRandom.setOnClickListener(v -> {
            int randomId = randomGenerator.nextInt(1025) + 1;
            etSearchPokemon.setText(String.valueOf(randomId));
            fetchPokemonData(String.valueOf(randomId));
        });

        // Botón Añadir al Equipo
        btnAddTeam.setOnClickListener(v -> {
            if (currentSearchedPokemon.isEmpty()) {
                Toast.makeText(this, "Busca un Pokémon primero", Toast.LENGTH_SHORT).show();
                return;
            }

            ArrayList<String> currentTeam = isPlayer1Turn ? player1Team : player2Team;

            if (currentTeam.size() < 3) {
                currentTeam.add(currentSearchedPokemon);
                updateSlotsUI(currentTeam);

                // Si el equipo está lleno, habilitar confirmación
                if (currentTeam.size() == 3) {
                    btnConfirmTeam.setEnabled(true);
                    btnConfirmTeam.setBackgroundColor(Color.parseColor("#22C55E")); // Verde
                }
            } else {
                Toast.makeText(this, "El equipo ya está completo", Toast.LENGTH_SHORT).show();
            }
        });

        // NUEVO: Botón Quitar Último Pokémon
        btnRemoveLast.setOnClickListener(v -> {
            ArrayList<String> currentTeam = isPlayer1Turn ? player1Team : player2Team;
            if (!currentTeam.isEmpty()) {
                currentTeam.remove(currentTeam.size() - 1);
                updateSlotsUI(currentTeam);

                // Si quitamos uno, el equipo ya no está lleno, así que bloqueamos "Confirmar"
                btnConfirmTeam.setEnabled(false);
                btnConfirmTeam.setBackgroundColor(Color.parseColor("#B91C1C")); // Rojo apagado
            } else {
                Toast.makeText(this, "El equipo ya está vacío", Toast.LENGTH_SHORT).show();
            }
        });

        // Botón Confirmar Equipo
        btnConfirmTeam.setOnClickListener(v -> {
            if (isPlayer1Turn) {
                isPlayer1Turn = false;
                showBlindScreen("Pasa el dispositivo al Jugador 2\n(Para elegir su equipo)");
            } else {
                startGame();
            }
        });

        // Botón "Estoy Listo" (Quitar Pantalla Ciega)
        btnBlindReady.setOnClickListener(v -> {
            overlayBlindScreen.setVisibility(View.GONE);
            if (!isPlayer1Turn && player2Team.isEmpty()) {
                tvDraftTurn.setText("Turno: Jugador 2");
                resetDraftUI();
            }
        });
    }

    private void fetchPokemonData(String query) {
        // Asignación de datos temporal para la UI (Retrofit lo procesará en la arena)
        currentSearchedPokemon = query;
        currentSpriteUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + query + ".png";

        Glide.with(this).load(currentSpriteUrl).into(ivPreview);
    }

    private void updateSlotsUI(ArrayList<String> team) {
        slot1.setImageDrawable(null);
        slot2.setImageDrawable(null);
        slot3.setImageDrawable(null);

        if (team.size() >= 1) Glide.with(this).load("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + team.get(0) + ".png").into(slot1);
        if (team.size() >= 2) Glide.with(this).load("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + team.get(1) + ".png").into(slot2);
        if (team.size() >= 3) Glide.with(this).load("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + team.get(2) + ".png").into(slot3);
    }

    private void resetDraftUI() {
        currentSearchedPokemon = "";
        etSearchPokemon.setText("");
        ivPreview.setImageDrawable(null);
        slot1.setImageDrawable(null);
        slot2.setImageDrawable(null);
        slot3.setImageDrawable(null);
        btnConfirmTeam.setEnabled(false);
        btnConfirmTeam.setBackgroundColor(Color.parseColor("#B91C1C"));
    }

    private void showBlindScreen(String message) {
        tvBlindMessage.setText(message);
        overlayBlindScreen.setVisibility(View.VISIBLE);
    }

    private void startGame() {
        Intent intent = new Intent(this, B_VersusLocalActivity.class); // Apunta a tu nueva Activity de Combate
        intent.putStringArrayListExtra("P1_TEAM", player1Team);
        intent.putStringArrayListExtra("P2_TEAM", player2Team);
        startActivity(intent);
        finish();
    }
}