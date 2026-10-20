package com.example.pokeappicesba;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class B_VersusResultActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_versus_result);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        TextView tvWinner = findViewById(R.id.tvWinner);
        TextView tvTotalTurns = findViewById(R.id.tvTotalTurns);
        TextView tvRemainingPkmn = findViewById(R.id.tvRemainingPkmn);
        Button btnRematch = findViewById(R.id.btnRematch);
        Button btnExitMenu = findViewById(R.id.btnExitMenu);

        // Recibir Extras
        Intent intent = getIntent();
        String winner = intent.getStringExtra("WINNER");
        int totalTurns = intent.getIntExtra("TOTAL_TURNS", 0);
        int remaining = intent.getIntExtra("REMAINING", 0);

        // Actualizar UI
        tvWinner.setText("¡" + winner + " Gana!");
        tvTotalTurns.setText("Turnos totales: " + totalTurns);
        tvRemainingPkmn.setText("Pokémon restantes: " + remaining + "/3");

        // Registrar en Firebase Perfil (Si es el Jugador 1 quien es el dueño del teléfono)
        updateProfileStats(winner);

        // Acciones
        btnRematch.setOnClickListener(v -> {
            // Regresa al Draft Local
            startActivity(new Intent(B_VersusResultActivity.this, B_TeamDraftActivity.class));
            finish();
        });

        btnExitMenu.setOnClickListener(v -> finish());
    }

    private void updateProfileStats(String winner) {
        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            String fieldToUpdate = winner.equals("Jugador 1") ? "victorias_locales" : "derrotas_locales";

            db.collection("users").document(uid)
                    .update(fieldToUpdate, FieldValue.increment(1))
                    .addOnSuccessListener(aVoid -> Log.d("VersusResult", "Estadística local actualizada: " + fieldToUpdate))
                    .addOnFailureListener(e -> Toast.makeText(this, "Error guardando estadísticas: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}