package com.example.pokeappicesba;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private EditText etPokemon;
    private Button btnConsultar;
    private ImageView imgPokemon;

    private TextView txtNombrePokemon, txtIdPokemon;
    private TextView txtHp, txtAtaque, txtDefensa;
    private TextView txtAtaqueEspecial, txtDefensaEspecial, txtVelocidad;

    private PokeApiService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        enlazarVistas();

        service = ApiClient.getClient()
                .create(PokeApiService.class);

        btnConsultar.setOnClickListener(v -> {

            String busqueda = etPokemon
                    .getText()
                    .toString()
                    .trim()
                    .toLowerCase(Locale.ROOT);

            if (busqueda.isEmpty()) {
                Toast.makeText(
                        this,
                        "Escribe un nombre o ID",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            consultarPokemon(busqueda);
        });
    }

    private void enlazarVistas() {
        etPokemon = findViewById(R.id.etPokemon);
        btnConsultar = findViewById(R.id.btnConsultar);
        imgPokemon = findViewById(R.id.imgPokemon);

        txtNombrePokemon = findViewById(R.id.txtNombrePokemon);
        txtIdPokemon = findViewById(R.id.txtIdPokemon);

        txtHp = findViewById(R.id.txtHp);
        txtAtaque = findViewById(R.id.txtAtaque);
        txtDefensa = findViewById(R.id.txtDefensa);
        txtAtaqueEspecial = findViewById(R.id.txtAtaqueEspecial);
        txtDefensaEspecial = findViewById(R.id.txtDefensaEspecial);
        txtVelocidad = findViewById(R.id.txtVelocidad);
    }

    private void consultarPokemon(String busqueda) {

        Call<Pokemon> call = service.getPokemon(busqueda);

        call.enqueue(new Callback<Pokemon>() {

            @Override
            public void onResponse(
                    Call<Pokemon> call,
                    Response<Pokemon> response) {

                if (response.isSuccessful()
                        && response.body() != null) {

                    mostrarPokemon(response.body());

                } else {
                    limpiarResultado();
                    Toast.makeText(
                            MainActivity.this,
                            "Pokémon no encontrado",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    Call<Pokemon> call,
                    Throwable t) {

                limpiarResultado();

                Toast.makeText(
                        MainActivity.this,
                        "Error de conexión",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void mostrarPokemon(Pokemon pokemon) {

        txtNombrePokemon.setText(
                pokemon.getName().toUpperCase(Locale.ROOT)
        );

        txtIdPokemon.setText("#" + pokemon.getId());

        if (pokemon.getSprites() != null) {
            Glide.with(this)
                    .load(pokemon.getSprites().getFrontDefault())
                    .into(imgPokemon);
        }

        reiniciarStats();

        if (pokemon.getStats() == null) return;

        for (PokemonStat item : pokemon.getStats()) {

            if (item.getStat() == null) continue;

            String nombre = item.getStat().getName();
            int valor = item.getBaseStat();

            switch (nombre) {

                case "hp":
                    txtHp.setText("HP: " + valor);
                    break;

                case "attack":
                    txtAtaque.setText("Ataque: " + valor);
                    break;

                case "defense":
                    txtDefensa.setText("Defensa: " + valor);
                    break;

                case "special-attack":
                    txtAtaqueEspecial.setText(
                            "Ataque especial: " + valor
                    );
                    break;

                case "special-defense":
                    txtDefensaEspecial.setText(
                            "Defensa especial: " + valor
                    );
                    break;

                case "speed":
                    txtVelocidad.setText(
                            "Velocidad: " + valor
                    );
                    break;
            }
        }
    }

    private void reiniciarStats() {
        txtHp.setText("HP: --");
        txtAtaque.setText("Ataque: --");
        txtDefensa.setText("Defensa: --");
        txtAtaqueEspecial.setText("Ataque especial: --");
        txtDefensaEspecial.setText("Defensa especial: --");
        txtVelocidad.setText("Velocidad: --");
    }

    private void limpiarResultado() {
        txtNombrePokemon.setText("POKÉMON");
        txtIdPokemon.setText("#---");
        imgPokemon.setImageDrawable(null);
        reiniciarStats();
    }
}