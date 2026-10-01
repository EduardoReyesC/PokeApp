package com.example.pokeappicesba;

import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private EditText etPokemon;
    private Button btnConsultar;
    private ImageButton btnPlayCry, btnFavorite, btnPrev, btnNext;
    private ImageView imgPokemon;
    private TextView txtHeaderTitle, txtTiposTag;

    private TabLayout dexTabLayout;
    private ViewPager2 dexViewPager;

    private final TabVariantesFragment tabVariantes = new TabVariantesFragment();
    private final TabInformacionFragment tabInfo = new TabInformacionFragment();
    private final TabMovimientosFragment tabMoves = new TabMovimientosFragment();

    private PokeApiService service;
    private SharedPreferences favPrefs;
    private Pokemon currentPokemon;
    private MediaPlayer mediaPlayer;

    // Control de posición en la Pokédex Nacional
    private int currentNationalDexNumber = 1;
    private int loadedSpeciesId = -1;
    private boolean isCurrentShiny = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        favPrefs = getSharedPreferences("PokeFavorites", MODE_PRIVATE);
        service = ApiClient.getClient().create(PokeApiService.class);

        etPokemon = findViewById(R.id.etPokemon);
        btnConsultar = findViewById(R.id.btnConsultar);
        btnPlayCry = findViewById(R.id.btnPlayCry);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnPrev = findViewById(R.id.btnPrevPokemon);
        btnNext = findViewById(R.id.btnNextPokemon);
        imgPokemon = findViewById(R.id.imgPokemon);
        txtHeaderTitle = findViewById(R.id.txtHeaderTitle);
        txtTiposTag = findViewById(R.id.txtTiposTag);
        dexTabLayout = findViewById(R.id.dexTabLayout);
        dexViewPager = findViewById(R.id.dexViewPager);

        dexViewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                if (position == 0) return tabVariantes;
                if (position == 1) return tabInfo;
                return tabMoves;
            }

            @Override
            public int getItemCount() { return 3; }
        });

        new TabLayoutMediator(dexTabLayout, dexViewPager, (tab, position) -> {
            if (position == 0) tab.setText("Variantes");
            else if (position == 1) tab.setText("Información");
            else tab.setText("Movimientos");
        }).attach();

        btnConsultar.setOnClickListener(v -> {
            String q = etPokemon.getText().toString().trim().toLowerCase(Locale.ROOT);
            if (!q.isEmpty()) {
                isCurrentShiny = false;
                consultarPokemon(q);
            }
        });

        // Flecha Izquierda: Va al número anterior de la Pokédex Nacional
        btnPrev.setOnClickListener(v -> {
            if (currentNationalDexNumber > 1) {
                currentNationalDexNumber--;
                isCurrentShiny = false;
                consultarPokemon(String.valueOf(currentNationalDexNumber));
            }
        });

        // Flecha Derecha: Va al siguiente número de la Pokédex Nacional (ej: Venusaur -> Charmander)
        btnNext.setOnClickListener(v -> {
            currentNationalDexNumber++;
            isCurrentShiny = false;
            consultarPokemon(String.valueOf(currentNationalDexNumber));
        });

        btnPlayCry.setOnClickListener(v -> reproducirSonido());
        btnFavorite.setOnClickListener(v -> alternarFavorito());

        // Iniciar en Bulbasaur (#1)
        consultarPokemon("1");

        CardView btnBottomHome = findViewById(R.id.btnBottomHome);
        btnBottomHome.setOnClickListener(v -> {
            finish(); // Cierra la Pokédex y vuelve al Menú Principal
        });
    }

    public void consultarPokemon(String query) {
        service.getPokemon(query).enqueue(new Callback<Pokemon>() {
            @Override
            public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentPokemon = response.body();

                    // Si el Pokémon tiene especie asociada, fijamos su ID nacional
                    if (currentPokemon.getSpecies() != null) {
                        currentNationalDexNumber = currentPokemon.getSpecies().extractId();
                    } else if (currentPokemon.getId() <= 1025) {
                        currentNationalDexNumber = currentPokemon.getId();
                    }

                    mostrarPokemon(currentPokemon);
                } else {
                    Toast.makeText(MainActivity.this, "Pokémon no encontrado", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Pokemon> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void aplicarVariante(String variantTarget, boolean shiny) {
        this.isCurrentShiny = shiny;
        // Si la variante solicitada es la misma ya cargada, solo alternamos el sprite Shiny
        if (currentPokemon != null && currentPokemon.getName().equalsIgnoreCase(variantTarget)) {
            actualizarSprite();
        } else {
            // Consulta la variante sin resetear la especie base
            service.getPokemon(variantTarget).enqueue(new Callback<Pokemon>() {
                @Override
                public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        currentPokemon = response.body();
                        mostrarPokemon(currentPokemon);
                    }
                }

                @Override
                public void onFailure(Call<Pokemon> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "Error al cargar variante", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void mostrarPokemon(Pokemon p) {
        String num = String.format(Locale.ROOT, "%04d", currentNationalDexNumber);
        String shinySymbol = isCurrentShiny ? " ✨" : "";
        txtHeaderTitle.setText(num + " " + p.getName().replace("-", " ").toUpperCase(Locale.ROOT) + shinySymbol);

        // Tipos
        StringBuilder tipos = new StringBuilder();
        if (p.getTypes() != null) {
            for (TypeSlot t : p.getTypes()) tipos.append(t.getType().getName().toUpperCase(Locale.ROOT)).append(" / ");
        }
        txtTiposTag.setText(tipos.length() > 3 ? tipos.substring(0, tipos.length() - 3) : "NORMAL");

        actualizarSprite();
        actualizarIconoFavorito(p.getId());

        // Stats para Radar
        int hp = 50, atk = 50, def = 50, spd = 50, sDef = 50, sAtk = 50;
        if (p.getStats() != null) {
            for (PokemonStat stat : p.getStats()) {
                if (stat.getStat() == null) continue;
                int val = stat.getBaseStat();
                switch (stat.getStat().getName()) {
                    case "hp": hp = val; break;
                    case "attack": atk = val; break;
                    case "defense": def = val; break;
                    case "speed": spd = val; break;
                    case "special-defense": sDef = val; break;
                    case "special-attack": sAtk = val; break;
                }
            }
        }

        String details = "Altura: " + (p.getHeight() / 10.0) + " m | Peso: " + (p.getWeight() / 10.0) + " kg";
        final int fHp = hp, fAtk = atk, fDef = def, fSpd = spd, fSDef = sDef, fSAtk = sAtk;

        // Cargar Species con el currentNationalDexNumber (evita perder las variantes al estar en megas)
        int targetSpeciesId = currentNationalDexNumber;
        if (loadedSpeciesId != targetSpeciesId) {
            loadedSpeciesId = targetSpeciesId;
            service.getPokemonSpecies(targetSpeciesId).enqueue(new Callback<PokemonSpecies>() {
                @Override
                public void onResponse(Call<PokemonSpecies> call, Response<PokemonSpecies> response) {
                    String desc = "No hay descripción disponible.";
                    List<String> variantes = new ArrayList<>();

                    if (response.isSuccessful() && response.body() != null) {
                        PokemonSpecies sp = response.body();
                        if (sp.getFlavorTextEntries() != null) {
                            for (PokemonSpecies.FlavorText f : sp.getFlavorTextEntries()) {
                                if (f.getLanguage() != null && "es".equals(f.getLanguage().getName())) {
                                    desc = f.getFlavorText().replace("\n", " ").replace("\f", " ");
                                    break;
                                }
                            }
                        }
                        if (sp.getVarieties() != null) {
                            for (PokemonSpecies.VarietyItem var : sp.getVarieties()) {
                                variantes.add(var.getPokemon().getName());
                            }
                        }
                    }

                    String baseName = p.getSpecies() != null ? p.getSpecies().getName() : p.getName();
                    tabInfo.actualizarDatos(desc, details, fHp, fAtk, fDef, fSpd, fSDef, fSAtk);
                    tabVariantes.setVariantes(baseName, variantes);
                }

                @Override
                public void onFailure(Call<PokemonSpecies> call, Throwable t) {
                    tabInfo.actualizarDatos("Error al cargar descripción.", details, fHp, fAtk, fDef, fSpd, fSDef, fSAtk);
                }
            });
        } else {
            // Ya tenemos la especie cargada, actualizamos stats de la variante sin recargar texto
            tabInfo.actualizarDatos(null, details, fHp, fAtk, fDef, fSpd, fSDef, fSAtk);
        }

        // Movimientos
        List<TabMovimientosFragment.MoveRow> moveRows = new ArrayList<>();
        if (p.getMoves() != null) {
            int limit = Math.min(8, p.getMoves().size());
            for (int i = 0; i < limit; i++) {
                String moveName = p.getMoves().get(i).getMove().getName();
                service.getMoveDetail(moveName).enqueue(new Callback<MoveDetail>() {
                    @Override
                    public void onResponse(Call<MoveDetail> call, Response<MoveDetail> resp) {
                        if (resp.isSuccessful() && resp.body() != null) {
                            String pow = resp.body().getPower() != null ? String.valueOf(resp.body().getPower()) : "--";
                            String acc = resp.body().getAccuracy() != null ? resp.body().getAccuracy() + "%" : "--";
                            moveRows.add(new TabMovimientosFragment.MoveRow(resp.body().getName(), acc, pow));
                            tabMoves.setMoves(moveRows);
                        }
                    }
                    @Override public void onFailure(Call<MoveDetail> call, Throwable t) {}
                });
            }
        }
    }

    private void actualizarSprite() {
        if (currentPokemon != null && currentPokemon.getSprites() != null) {
            String spriteUrl = isCurrentShiny && currentPokemon.getSprites().getFrontShiny() != null
                    ? currentPokemon.getSprites().getFrontShiny()
                    : currentPokemon.getSprites().getFrontDefault();

            Glide.with(this).load(spriteUrl).into(imgPokemon);
        }
    }

    private void alternarFavorito() {
        if (currentPokemon == null) return;
        String key = "fav_" + currentNationalDexNumber;
        if (favPrefs.contains(key)) {
            favPrefs.edit().remove(key).apply();
            Toast.makeText(this, "Quitado de favoritos", Toast.LENGTH_SHORT).show();
        } else {
            favPrefs.edit().putString(key, currentPokemon.getName()).apply();
            Toast.makeText(this, "Guardado en favoritos", Toast.LENGTH_SHORT).show();
        }
        actualizarIconoFavorito(currentPokemon.getId());
    }

    private void actualizarIconoFavorito(int id) {
        btnFavorite.setImageResource(favPrefs.contains("fav_" + currentNationalDexNumber) ? android.R.drawable.star_big_on : android.R.drawable.star_big_off);
    }

    private void reproducirSonido() {
        if (currentPokemon == null || currentPokemon.getCries() == null || currentPokemon.getCries().getLatest() == null) {
            Toast.makeText(this, "Audio no disponible", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Error al reproducir audio", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}