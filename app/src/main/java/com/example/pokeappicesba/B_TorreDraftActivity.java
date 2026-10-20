package com.example.pokeappicesba;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class B_TorreDraftActivity extends AppCompatActivity {

    private TextView tvInstrucciones;
    private ProgressBar pbLoading;
    private GridLayout gridOptions;
    private ImageView slot1, slot2, slot3;
    private Button btnConfirmTeam;

    private PokeApiService service;
    private final Random random = new Random();

    // Lista de las 6 opciones generadas
    private final List<PokemonOption> opcionesGeneradas = new ArrayList<>();
    // Lista de IDs de los 3 seleccionados
    private final ArrayList<String> equipoSeleccionado = new ArrayList<>();

    private int downloadedCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_torre_draft);

        service = ApiClient.getClient().create(PokeApiService.class);

        initViews();
        generarSeisPokemonAlAzar();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBack);
        tvInstrucciones = findViewById(R.id.tvInstrucciones);
        pbLoading = findViewById(R.id.pbLoading);
        gridOptions = findViewById(R.id.gridOptions);

        slot1 = findViewById(R.id.slot1);
        slot2 = findViewById(R.id.slot2);
        slot3 = findViewById(R.id.slot3);

        btnConfirmTeam = findViewById(R.id.btnConfirmTeam);

        btnBack.setOnClickListener(v -> finish());

        btnConfirmTeam.setOnClickListener(v -> {
            // Guardar el equipo y pasar al Mapa (Elevador)
            Intent intent = new Intent(B_TorreDraftActivity.this, B_TorreMapaActivity.class);
            intent.putStringArrayListExtra("MI_EQUIPO", equipoSeleccionado);
            startActivity(intent);
            finish();
        });
    }

    private void generarSeisPokemonAlAzar() {
        for (int i = 0; i < 6; i++) {
            int randomId = random.nextInt(1025) + 1;
            descargarOpcion(String.valueOf(randomId));
        }
    }

    private void descargarOpcion(String pokemonId) {
        service.getPokemon(pokemonId).enqueue(new Callback<Pokemon>() {
            @Override
            public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Pokemon p = response.body();
                    String spriteUrl = p.getSprites() != null ? p.getSprites().getFrontDefault() : "";
                    opcionesGeneradas.add(new PokemonOption(String.valueOf(p.getId()), p.getName(), spriteUrl));
                }
                checkDescargas();
            }

            @Override
            public void onFailure(Call<Pokemon> call, Throwable t) {
                // Si falla uno, ponemos a Pikachu por defecto para no romper el juego
                opcionesGeneradas.add(new PokemonOption("25", "pikachu", "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png"));
                checkDescargas();
            }
        });
    }

    private void checkDescargas() {
        downloadedCount++;
        if (downloadedCount == 6) {
            pbLoading.setVisibility(View.GONE);
            tvInstrucciones.setText("Selecciona 3 Pokémon para tu equipo:");
            gridOptions.setVisibility(View.VISIBLE);
            dibujarCuadricula();
        }
    }

    private void dibujarCuadricula() {
        gridOptions.removeAllViews();

        for (PokemonOption opcion : opcionesGeneradas) {
            // Crear una vista (tarjeta) por código para cada Pokémon
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setGravity(android.view.Gravity.CENTER);
            card.setPadding(16, 16, 16, 16);
            card.setBackgroundColor(Color.parseColor("#E2E8F0"));

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(8, 8, 8, 8);
            card.setLayoutParams(params);

            ImageView img = new ImageView(this);
            img.setLayoutParams(new LinearLayout.LayoutParams(150, 150));
            Glide.with(this).load(opcion.spriteUrl).into(img);

            TextView tvName = new TextView(this);
            tvName.setText(opcion.name.toUpperCase());
            tvName.setTextSize(12f);
            tvName.setGravity(android.view.Gravity.CENTER);

            card.addView(img);
            card.addView(tvName);

            // Evento de clic para seleccionar / deseleccionar
            card.setOnClickListener(v -> manejarSeleccion(opcion, card));

            gridOptions.addView(card);
        }
    }

    private void manejarSeleccion(PokemonOption opcion, LinearLayout card) {
        if (equipoSeleccionado.contains(opcion.id)) {
            // Deseleccionar
            equipoSeleccionado.remove(opcion.id);
            card.setBackgroundColor(Color.parseColor("#E2E8F0")); // Gris normal
        } else {
            // Seleccionar
            if (equipoSeleccionado.size() < 3) {
                equipoSeleccionado.add(opcion.id);
                card.setBackgroundColor(Color.parseColor("#86EFAC")); // Verde brillante
            } else {
                Toast.makeText(this, "Ya tienes 3 Pokémon", Toast.LENGTH_SHORT).show();
            }
        }
        actualizarSlots();
    }

    private void actualizarSlots() {
        slot1.setImageDrawable(null);
        slot2.setImageDrawable(null);
        slot3.setImageDrawable(null);

        if (equipoSeleccionado.size() >= 1) Glide.with(this).load(obtenerUrl(equipoSeleccionado.get(0))).into(slot1);
        if (equipoSeleccionado.size() >= 2) Glide.with(this).load(obtenerUrl(equipoSeleccionado.get(1))).into(slot2);
        if (equipoSeleccionado.size() >= 3) Glide.with(this).load(obtenerUrl(equipoSeleccionado.get(2))).into(slot3);

        if (equipoSeleccionado.size() == 3) {
            btnConfirmTeam.setEnabled(true);
            btnConfirmTeam.setBackgroundColor(Color.parseColor("#22C55E")); // Verde
        } else {
            btnConfirmTeam.setEnabled(false);
            btnConfirmTeam.setBackgroundColor(Color.parseColor("#B91C1C")); // Rojo apagado
        }
    }

    private String obtenerUrl(String id) {
        for (PokemonOption p : opcionesGeneradas) {
            if (p.id.equals(id)) return p.spriteUrl;
        }
        return "";
    }

    // Clase auxiliar interna para guardar los datos de las opciones
    private static class PokemonOption {
        String id;
        String name;
        String spriteUrl;

        PokemonOption(String id, String name, String spriteUrl) {
            this.id = id;
            this.name = name;
            this.spriteUrl = spriteUrl;
        }
    }
}