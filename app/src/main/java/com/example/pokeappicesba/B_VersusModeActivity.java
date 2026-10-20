package com.example.pokeappicesba;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class B_VersusModeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_versus_mode);

        ImageView btnBack = findViewById(R.id.btnBack);
        CardView cardLocal = findViewById(R.id.cardLocal);
        CardView cardOnline = findViewById(R.id.cardOnline);

        // Regresar al menú principal
        btnBack.setOnClickListener(v -> finish());

        // Modo Local (Hot-Seat)
        cardLocal.setOnClickListener(v -> {
            Intent intent = new Intent(B_VersusModeActivity.this, B_TeamDraftActivity.class);
            startActivity(intent);
        });

        // NUEVO: Modo Online (Abre el Lobby de Firebase)
        cardOnline.setOnClickListener(v -> {
            Intent intent = new Intent(B_VersusModeActivity.this, B_VersusLobbyActivity.class);
            startActivity(intent);
        });
    }
}