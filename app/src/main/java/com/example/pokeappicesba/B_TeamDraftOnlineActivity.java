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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class B_TeamDraftOnlineActivity extends AppCompatActivity {

    private ArrayList<String> miEquipo = new ArrayList<>();
    private String currentSearchedPokemon = "";
    private final Random randomGenerator = new Random();

    private FirebaseFirestore db;
    private ListenerRegistration roomListener;
    private String roomCode;
    private String miRol;
    private boolean isHost;

    private TextView tvDraftTurn, tvBlindMessage;
    private EditText etSearchPokemon;
    private ImageView ivPreview, slot1, slot2, slot3;
    private Button btnSearch, btnRandom, btnAddTeam, btnRemoveLast, btnConfirmTeam, btnBlindReady;
    private FrameLayout overlayBlindScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Usamos el XML que ya existe
        setContentView(R.layout.activity_team_draft);

        db = FirebaseFirestore.getInstance();

        if (getIntent() != null) {
            roomCode = getIntent().getStringExtra("ROOM_CODE");
            miRol = getIntent().getStringExtra("ROLE");
            isHost = "host".equals(miRol);
        }

        initViews();
        prepararSalaEnFirebase();
        escucharActualizacionesDeSala();
    }

    private void initViews() {
        tvDraftTurn = findViewById(R.id.tvDraftTurn);
        etSearchPokemon = findViewById(R.id.etSearchPokemon);
        ivPreview = findViewById(R.id.ivPreview);
        slot1 = findViewById(R.id.slot1);
        slot2 = findViewById(R.id.slot2);
        slot3 = findViewById(R.id.slot3);

        btnSearch = findViewById(R.id.btnSearch);
        btnRandom = findViewById(R.id.btnRandom);
        btnAddTeam = findViewById(R.id.btnAddTeam);
        btnRemoveLast = findViewById(R.id.btnRemoveLast);
        btnConfirmTeam = findViewById(R.id.btnConfirmTeam);

        overlayBlindScreen = findViewById(R.id.overlayBlindScreen);
        tvBlindMessage = findViewById(R.id.tvBlindMessage);
        btnBlindReady = findViewById(R.id.btnBlindReady);
        ImageView btnBackDraft = findViewById(R.id.btnBackDraft);

        btnBackDraft.setVisibility(View.GONE);
        btnBlindReady.setVisibility(View.GONE);

        tvDraftTurn.setText(isHost ? "Arma tu equipo (Tú eres el Host)" : "Arma tu equipo (Tú eres el Invitado)");

        btnSearch.setOnClickListener(v -> {
            String query = etSearchPokemon.getText().toString().trim().toLowerCase();
            if (!query.isEmpty()) fetchPokemonData(query);
            else Toast.makeText(this, "Ingresa un ID o Nombre", Toast.LENGTH_SHORT).show();
        });

        btnRandom.setOnClickListener(v -> {
            int randomId = randomGenerator.nextInt(1025) + 1;
            etSearchPokemon.setText(String.valueOf(randomId));
            fetchPokemonData(String.valueOf(randomId));
        });

        btnAddTeam.setOnClickListener(v -> {
            if (currentSearchedPokemon.isEmpty()) {
                Toast.makeText(this, "Busca un Pokémon primero", Toast.LENGTH_SHORT).show();
                return;
            }
            if (miEquipo.size() < 3) {
                miEquipo.add(currentSearchedPokemon);
                updateSlotsUI();
                if (miEquipo.size() == 3) {
                    btnConfirmTeam.setEnabled(true);
                    btnConfirmTeam.setBackgroundColor(Color.parseColor("#22C55E"));
                }
            } else {
                Toast.makeText(this, "El equipo ya está completo", Toast.LENGTH_SHORT).show();
            }
        });

        btnRemoveLast.setOnClickListener(v -> {
            if (!miEquipo.isEmpty()) {
                miEquipo.remove(miEquipo.size() - 1);
                updateSlotsUI();
                btnConfirmTeam.setEnabled(false);
                btnConfirmTeam.setBackgroundColor(Color.parseColor("#B91C1C"));
            }
        });

        btnConfirmTeam.setOnClickListener(v -> confirmarEquipoEnFirebase());
    }

    private void fetchPokemonData(String query) {
        currentSearchedPokemon = query;
        String url = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + query + ".png";
        Glide.with(this).load(url).into(ivPreview);
    }

    private void updateSlotsUI() {
        slot1.setImageDrawable(null);
        slot2.setImageDrawable(null);
        slot3.setImageDrawable(null);
        if (miEquipo.size() >= 1) Glide.with(this).load("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + miEquipo.get(0) + ".png").into(slot1);
        if (miEquipo.size() >= 2) Glide.with(this).load("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + miEquipo.get(1) + ".png").into(slot2);
        if (miEquipo.size() >= 3) Glide.with(this).load("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + miEquipo.get(2) + ".png").into(slot3);
    }

    private void prepararSalaEnFirebase() {
        if (isHost) {
            Map<String, Object> updates = new HashMap<>();
            updates.put("equipoJ1", new ArrayList<String>());
            updates.put("equipoJ2", new ArrayList<String>());
            updates.put("j1Listo", false);
            updates.put("j2Listo", false);
            db.collection("rooms").document(roomCode).update(updates);
        }
    }

    private void confirmarEquipoEnFirebase() {
        tvBlindMessage.setText("Esperando al rival...\n(No cierres la aplicación)");
        overlayBlindScreen.setVisibility(View.VISIBLE);

        DocumentReference roomRef = db.collection("rooms").document(roomCode);
        String campoEquipo = isHost ? "equipoJ1" : "equipoJ2";
        String campoListo = isHost ? "j1Listo" : "j2Listo";

        roomRef.update(campoEquipo, miEquipo, campoListo, true)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error de conexión", Toast.LENGTH_SHORT).show();
                    overlayBlindScreen.setVisibility(View.GONE);
                });
    }

    private void escucharActualizacionesDeSala() {
        DocumentReference roomRef = db.collection("rooms").document(roomCode);
        roomListener = roomRef.addSnapshotListener((snapshot, e) -> {
            if (e != null || snapshot == null) return;

            Boolean j1Listo = snapshot.getBoolean("j1Listo");
            Boolean j2Listo = snapshot.getBoolean("j2Listo");

            if (Boolean.TRUE.equals(j1Listo) && Boolean.TRUE.equals(j2Listo)) {
                if (isHost) roomRef.update("status", "battling");

                List<String> equipoHost = (List<String>) snapshot.get("equipoJ1");
                List<String> equipoGuest = (List<String>) snapshot.get("equipoJ2");

                lanzarCombate(equipoHost, equipoGuest);
            }
        });
    }

    private void lanzarCombate(List<String> p1Team, List<String> p2Team) {
        if (roomListener != null) roomListener.remove();

        Toast.makeText(this, "¡Equipos listos! Entrando a la arena...", Toast.LENGTH_SHORT).show();

        // ESTO LO DESCOMENTAREMOS CUANDO CREEMOS B_VersusOnlineActivity
        /*
        Intent intent = new Intent(this, B_VersusOnlineActivity.class);
        intent.putExtra("ROOM_CODE", roomCode);
        intent.putExtra("ROLE", miRol);
        intent.putStringArrayListExtra("P1_TEAM", new ArrayList<>(p1Team));
        intent.putStringArrayListExtra("P2_TEAM", new ArrayList<>(p2Team));
        startActivity(intent);
        finish();
        */
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (roomListener != null) roomListener.remove();
    }
}