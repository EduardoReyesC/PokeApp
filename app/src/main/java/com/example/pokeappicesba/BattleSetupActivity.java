package com.example.pokeappicesba;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class BattleSetupActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battle_setup);

        EditText etSetupPlayer = findViewById(R.id.etSetupPlayer);
        EditText etSetupRival = findViewById(R.id.etSetupRival);
        ImageButton btnRandomPlayer = findViewById(R.id.btnRandomPlayer);
        ImageButton btnRandomRival = findViewById(R.id.btnRandomRival);
        Button btnStartBattle = findViewById(R.id.btnStartBattle);
        Button btnCancelSetup = findViewById(R.id.btnCancelSetup);

        // Si vienes desde la Pokédex, bloquea tu Pokémon. Si vienes del menú, déjalo libre.
        if (getIntent() != null && getIntent().hasExtra("PLAYER_POKEMON_ID")) {
            int playerId = getIntent().getIntExtra("PLAYER_POKEMON_ID", 1);
            etSetupPlayer.setText(String.valueOf(playerId));
            etSetupPlayer.setEnabled(false);
            btnRandomPlayer.setEnabled(false);
        }

        btnRandomPlayer.setOnClickListener(v -> etSetupPlayer.setText(String.valueOf(new Random().nextInt(1025) + 1)));
        btnRandomRival.setOnClickListener(v -> etSetupRival.setText(String.valueOf(new Random().nextInt(1025) + 1)));

        btnStartBattle.setOnClickListener(v -> {
            String pQuery = etSetupPlayer.getText().toString().trim();
            String rQuery = etSetupRival.getText().toString().trim();

            if (pQuery.isEmpty() || rQuery.isEmpty()) {
                Toast.makeText(this, "Llena ambos campos", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent battleIntent = new Intent(BattleSetupActivity.this, BattleActivity.class);
            battleIntent.putExtra("PLAYER_QUERY", pQuery.toLowerCase());
            battleIntent.putExtra("RIVAL_QUERY", rQuery.toLowerCase());
            startActivity(battleIntent);
            finish();
        });

        btnCancelSetup.setOnClickListener(v -> finish());
    }
}