package com.example.pokeappicesba;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PokedexFragment extends Fragment {

    private EditText etPokemon;
    private Button btnConsultar, btnPlayCry, btnVerFavoritos;
    private ImageButton btnFavorite;
    private ImageView imgPokemon;

    private TextView txtNombrePokemon, txtIdPokemon;
    private TextView txtTipos, txtMedidas, txtHabilidades;
    private TextView txtHp, txtAtaque, txtDefensa, txtAtaqueEspecial, txtDefensaEspecial, txtVelocidad;

    private PokeApiService service;
    private SharedPreferences favPrefs;
    private Pokemon currentPokemon;
    private MediaPlayer mediaPlayer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pokedex, container, false);

        favPrefs = requireActivity().getSharedPreferences("PokeFavorites", Context.MODE_PRIVATE);
        service = ApiClient.getClient().create(PokeApiService.class);

        etPokemon = view.findViewById(R.id.etPokemon);
        btnConsultar = view.findViewById(R.id.btnConsultar);
        btnVerFavoritos = view.findViewById(R.id.btnVerFavoritos);
        btnPlayCry = view.findViewById(R.id.btnPlayCry);
        btnFavorite = view.findViewById(R.id.btnFavorite);
        imgPokemon = view.findViewById(R.id.imgPokemon);

        txtNombrePokemon = view.findViewById(R.id.txtNombrePokemon);
        txtIdPokemon = view.findViewById(R.id.txtIdPokemon);
        txtTipos = view.findViewById(R.id.txtTipos);
        txtMedidas = view.findViewById(R.id.txtMedidas);
        txtHabilidades = view.findViewById(R.id.txtHabilidades);

        txtHp = view.findViewById(R.id.txtHp);
        txtAtaque = view.findViewById(R.id.txtAtaque);
        txtDefensa = view.findViewById(R.id.txtDefensa);
        txtAtaqueEspecial = view.findViewById(R.id.txtAtaqueEspecial);
        txtDefensaEspecial = view.findViewById(R.id.txtDefensaEspecial);
        txtVelocidad = view.findViewById(R.id.txtVelocidad);

        btnConsultar.setOnClickListener(v -> {
            String query = etPokemon.getText().toString().trim().toLowerCase(Locale.ROOT);
            if (!query.isEmpty()) {
                consultarPokemon(query);
            }
        });

        btnPlayCry.setOnClickListener(v -> reproducirSonido());
        btnFavorite.setOnClickListener(v -> alternarFavorito());
        btnVerFavoritos.setOnClickListener(v -> mostrarDialogoFavoritos());

        return view;
    }

    public void consultarPokemon(String busqueda) {
        service.getPokemon(busqueda).enqueue(new Callback<Pokemon>() {
            @Override
            public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentPokemon = response.body();
                    mostrarPokemon(currentPokemon);
                } else {
                    limpiarResultado();
                    Toast.makeText(getContext(), "Pokémon no encontrado", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Pokemon> call, Throwable t) {
                limpiarResultado();
                Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarPokemon(Pokemon pokemon) {
        txtNombrePokemon.setText(pokemon.getName().toUpperCase(Locale.ROOT));
        txtIdPokemon.setText("#" + pokemon.getId());

        double alturaM = pokemon.getHeight() / 10.0;
        double pesoKg = pokemon.getWeight() / 10.0;
        txtMedidas.setText("Altura: " + alturaM + " m | Peso: " + pesoKg + " kg");

        StringBuilder tiposStr = new StringBuilder("Tipo: ");
        if (pokemon.getTypes() != null) {
            for (TypeSlot slot : pokemon.getTypes()) {
                tiposStr.append(slot.getType().getName().toUpperCase(Locale.ROOT)).append(" ");
            }
        }
        txtTipos.setText(tiposStr.toString().trim());

        StringBuilder habStr = new StringBuilder("Habilidades: ");
        if (pokemon.getAbilities() != null) {
            for (AbilitySlot slot : pokemon.getAbilities()) {
                habStr.append(slot.getAbility().getName()).append(", ");
            }
        }
        txtHabilidades.setText(habStr.length() > 14 ? habStr.substring(0, habStr.length() - 2) : habStr.toString());

        if (pokemon.getSprites() != null && getContext() != null) {
            Glide.with(getContext()).load(pokemon.getSprites().getFrontDefault()).into(imgPokemon);
        }

        reiniciarStats();
        if (pokemon.getStats() != null) {
            for (PokemonStat item : pokemon.getStats()) {
                if (item.getStat() == null) continue;
                int val = item.getBaseStat();
                switch (item.getStat().getName()) {
                    case "hp": txtHp.setText("HP: " + val); break;
                    case "attack": txtAtaque.setText("Ataque: " + val); break;
                    case "defense": txtDefensa.setText("Defensa: " + val); break;
                    case "special-attack": txtAtaqueEspecial.setText("Atq. Esp: " + val); break;
                    case "special-defense": txtDefensaEspecial.setText("Def. Esp: " + val); break;
                    case "speed": txtVelocidad.setText("Velocidad: " + val); break;
                }
            }
        }

        actualizarIconoFavorito(pokemon.getId());
    }

    private void alternarFavorito() {
        if (currentPokemon == null) return;
        String key = "fav_" + currentPokemon.getId();
        boolean exists = favPrefs.contains(key);

        if (exists) {
            favPrefs.edit().remove(key).apply();
            Toast.makeText(getContext(), "Eliminado de favoritos", Toast.LENGTH_SHORT).show();
        } else {
            // Guardamos el nombre formateado
            favPrefs.edit().putString(key, currentPokemon.getName().toUpperCase(Locale.ROOT)).apply();
            Toast.makeText(getContext(), "¡Añadido a favoritos!", Toast.LENGTH_SHORT).show();
        }
        actualizarIconoFavorito(currentPokemon.getId());
    }

    private void actualizarIconoFavorito(int id) {
        boolean isFav = favPrefs.contains("fav_" + id);
        btnFavorite.setImageResource(isFav ? android.R.drawable.star_big_on : android.R.drawable.star_big_off);
    }

    private void mostrarDialogoFavoritos() {
        Map<String, ?> todos = favPrefs.getAll();
        List<String> nombres = new ArrayList<>();

        for (Map.Entry<String, ?> entry : todos.entrySet()) {
            if (entry.getKey().startsWith("fav_") && entry.getValue() instanceof String) {
                nombres.add((String) entry.getValue());
            }
        }

        if (nombres.isEmpty()) {
            Toast.makeText(getContext(), "No tienes Pokémon marcados como favoritos", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] listaArray = nombres.toArray(new String[0]);

        new AlertDialog.Builder(requireContext())
                .setTitle("Tus Pokémon Favoritos")
                .setItems(listaArray, (dialog, which) -> {
                    String seleccionado = listaArray[which].toLowerCase(Locale.ROOT);
                    etPokemon.setText(seleccionado);
                    consultarPokemon(seleccionado); // Busca directamente en la Dex al presionar
                })
                .setNegativeButton("Cerrar", null)
                .show();
    }

    private void reproducirSonido() {
        if (currentPokemon == null || currentPokemon.getCries() == null || currentPokemon.getCries().getLatest() == null) {
            Toast.makeText(getContext(), "Cry no disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            if (mediaPlayer != null) mediaPlayer.release();
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build());
            mediaPlayer.setDataSource(currentPokemon.getCries().getLatest());
            mediaPlayer.setOnPreparedListener(MediaPlayer::start);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error de reproducción", Toast.LENGTH_SHORT).show();
        }
    }

    private void reiniciarStats() {
        txtHp.setText("HP: --");
        txtAtaque.setText("Ataque: --");
        txtDefensa.setText("Defensa: --");
        txtAtaqueEspecial.setText("Atq. Esp: --");
        txtDefensaEspecial.setText("Def. Esp: --");
        txtVelocidad.setText("Velocidad: --");
    }

    private void limpiarResultado() {
        currentPokemon = null;
        txtNombrePokemon.setText("POKÉMON");
        txtIdPokemon.setText("#---");
        txtTipos.setText("Tipo: --");
        txtMedidas.setText("Altura: -- | Peso: --");
        txtHabilidades.setText("Habilidades: --");
        imgPokemon.setImageDrawable(null);
        btnFavorite.setImageResource(android.R.drawable.star_big_off);
        reiniciarStats();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}