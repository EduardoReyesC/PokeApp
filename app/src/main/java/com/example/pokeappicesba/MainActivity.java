package com.example.pokeappicesba;

import android.content.Intent;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

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
    private TextView txtHeaderTitle, txtBaseNumber;
    private RadarStatsView headerRadarStats;
    private LinearLayout evolutionContainer;
    private CardView btnBottomHome;

    private TabLayout dexTabLayout;
    private ViewPager2 dexViewPager;

    private final TabVariantesFragment tabVariantes = new TabVariantesFragment();
    private final TabInformacionFragment tabInfo = new TabInformacionFragment();
    private final TabMovimientosFragment tabMoves = new TabMovimientosFragment();

    private PokeApiService service;
    private Pokemon currentPokemon;
    private MediaPlayer mediaPlayer;

    private int currentNationalDexNumber = 1;
    private int loadedSpeciesId = -1;
    private boolean isCurrentShiny = false;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private List<Long> userFavorites = new ArrayList<>();

    private Button btnSimulateBattle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        service = ApiClient.getClient().create(PokeApiService.class);

        etPokemon = findViewById(R.id.etPokemon);
        btnConsultar = findViewById(R.id.btnConsultar);
        btnPlayCry = findViewById(R.id.btnPlayCry);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnSimulateBattle = findViewById(R.id.btnSimulateBattle);
        btnPrev = findViewById(R.id.btnPrevPokemon);
        btnNext = findViewById(R.id.btnNextPokemon);
        imgPokemon = findViewById(R.id.imgPokemon);
        txtHeaderTitle = findViewById(R.id.txtHeaderTitle);
        txtBaseNumber = findViewById(R.id.txtBaseNumber);
        headerRadarStats = findViewById(R.id.headerRadarStats);
        evolutionContainer = findViewById(R.id.evolutionContainer);
        btnBottomHome = findViewById(R.id.btnBottomHome);
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
            public int getItemCount() {
                return 3;
            }
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

        btnPrev.setOnClickListener(v -> {
            if (currentNationalDexNumber > 1) {
                currentNationalDexNumber--;
                isCurrentShiny = false;
                consultarPokemon(String.valueOf(currentNationalDexNumber));
            }
        });

        btnNext.setOnClickListener(v -> {
            currentNationalDexNumber++;
            isCurrentShiny = false;
            consultarPokemon(String.valueOf(currentNationalDexNumber));
        });

        btnPlayCry.setOnClickListener(v -> reproducirSonido());
        btnFavorite.setOnClickListener(v -> alternarFavorito());

        btnBottomHome.setOnClickListener(v -> finish());

        consultarPokemon("1");
        cargarFavoritosDesdeNube();

        btnSimulateBattle.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BattleSetupActivity.class);
            intent.putExtra("PLAYER_POKEMON_ID", currentNationalDexNumber);
            startActivity(intent);
        });
    }

    public void consultarPokemon(String query) {
        service.getPokemon(query).enqueue(new Callback<Pokemon>() {
            @Override
            public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentPokemon = response.body();

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
                Toast.makeText(MainActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void aplicarVariante(String variantTarget, boolean shiny) {
        this.isCurrentShiny = shiny;
        if (currentPokemon != null && currentPokemon.getName().equalsIgnoreCase(variantTarget)) {
            actualizarSprite();
        } else {
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

        if (txtBaseNumber != null) {
            txtBaseNumber.setText("#" + num);
        }

        actualizarSprite();
        actualizarIconoFavorito(currentNationalDexNumber);

        // 1. Estadísticas en hexágono
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
        headerRadarStats.setStats(hp, atk, def, spd, sDef, sAtk);

        // 2. Cálculo de tipos, debilidades y resistencias
        List<String> tiposList = new ArrayList<>();
        if (p.getTypes() != null) {
            for (TypeSlot ts : p.getTypes()) {
                tiposList.add(ts.getType().getName());
            }
        }
        TypeCalculator.TypeMatchups matchups = TypeCalculator.calculate(tiposList);

        String details = "Altura: " + (p.getHeight() / 10.0) + " m | Peso: " + (p.getWeight() / 10.0) + " kg";

        // 3. Consulta de Species y Cadena Evolutiva
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

                        if (sp.getEvolutionChain() != null) {
                            cargarEvoluciones(sp.getEvolutionChain().extractId());
                        }
                    }

                    String baseName = p.getSpecies() != null ? p.getSpecies().getName() : p.getName();
                    tabInfo.actualizarDatos(desc, details, tiposList, matchups.weaknesses, matchups.resistances);
                    tabVariantes.setVariantes(baseName, variantes);
                }

                @Override
                public void onFailure(Call<PokemonSpecies> call, Throwable t) {
                    tabInfo.actualizarDatos("Error al cargar descripción.", details, tiposList, matchups.weaknesses, matchups.resistances);
                }
            });
        } else {
            tabInfo.actualizarDatos(null, details, tiposList, matchups.weaknesses, matchups.resistances);
        }

        // 4. Movimientos (AHORA USA EL MÉTODO DRY DE POKEMON.JAVA)
        List<TabMovimientosFragment.MoveRow> moveRows = new ArrayList<>();
        List<String> ataquesLimpios = p.getLevelUpMoves();

        if (ataquesLimpios != null && !ataquesLimpios.isEmpty()) {
            int limit = Math.min(8, ataquesLimpios.size());
            for (int i = 0; i < limit; i++) {
                String moveName = ataquesLimpios.get(i);
                String queryName = moveName.toLowerCase().replace(" ", "-");

                service.getMoveDetail(queryName).enqueue(new Callback<MoveDetail>() {
                    @Override
                    public void onResponse(Call<MoveDetail> call, Response<MoveDetail> resp) {
                        if (resp.isSuccessful() && resp.body() != null) {
                            String pow = resp.body().getPower() != null ? String.valueOf(resp.body().getPower()) : "--";
                            String acc = resp.body().getAccuracy() != null ? resp.body().getAccuracy() + "%" : "--";
                            moveRows.add(new TabMovimientosFragment.MoveRow(resp.body().getName(), acc, pow));
                            tabMoves.setMoves(moveRows);
                        }
                    }

                    @Override
                    public void onFailure(Call<MoveDetail> call, Throwable t) {}
                });
            }
        } else {
            tabMoves.setMoves(new ArrayList<>()); // Limpiar si no tiene ataques (raro)
        }
    }

    private void cargarEvoluciones(int chainId) {
        service.getEvolutionChain(chainId).enqueue(new Callback<EvolutionChainResponse>() {
            @Override
            public void onResponse(Call<EvolutionChainResponse> call, Response<EvolutionChainResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getChain() != null) {
                    evolutionContainer.removeAllViews();
                    pintarCadena(response.body().getChain());
                }
            }

            @Override
            public void onFailure(Call<EvolutionChainResponse> call, Throwable t) {}
        });
    }

    private void pintarCadena(EvolutionChainResponse.ChainLink link) {
        if (link == null || link.getSpecies() == null) return;

        agregarVistaEvolucion(link.getSpecies().getName(), link.getSpecies().extractId());

        if (link.getEvolvesTo() != null && !link.getEvolvesTo().isEmpty()) {
            for (EvolutionChainResponse.ChainLink next : link.getEvolvesTo()) {
                TextView arrow = new TextView(this);
                arrow.setText(" → ");
                arrow.setTextColor(0xFF0284C7);
                arrow.setTextSize(15f);
                arrow.setTypeface(null, Typeface.BOLD);
                arrow.setPadding(4, 0, 4, 0);
                evolutionContainer.addView(arrow);

                pintarCadena(next);
            }
        }
    }

    private void agregarVistaEvolucion(String name, int id) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(4, 2, 4, 2);

        ImageView img = new ImageView(this);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(95, 95);
        img.setLayoutParams(imgParams);

        String spriteUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/" + id + ".png";
        Glide.with(this).load(spriteUrl).into(img);

        TextView tv = new TextView(this);
        tv.setText(name.toUpperCase(Locale.ROOT));
        tv.setTextColor(0xFF0F172A);
        tv.setTextSize(9.5f);
        tv.setTypeface(null, Typeface.BOLD);

        box.addView(img);
        box.addView(tv);

        box.setOnClickListener(v -> {
            isCurrentShiny = false;
            consultarPokemon(String.valueOf(id));
        });

        evolutionContainer.addView(box);
    }

    private void actualizarSprite() {
        if (currentPokemon != null && currentPokemon.getSprites() != null) {
            String spriteUrl = isCurrentShiny && currentPokemon.getSprites().getFrontShiny() != null
                    ? currentPokemon.getSprites().getFrontShiny()
                    : currentPokemon.getSprites().getFrontDefault();

            Glide.with(this).load(spriteUrl).into(imgPokemon);
        }
    }

    private void cargarFavoritosDesdeNube() {
        if (currentUser == null) return;
        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists() && doc.get("favorites") != null) {
                        userFavorites = (List<Long>) doc.get("favorites");
                        actualizarIconoFavorito(currentNationalDexNumber);
                    }
                });
    }

    private void alternarFavorito() {
        if (currentUser == null) {
            Toast.makeText(this, "Inicia sesión para guardar favoritos", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference userRef = db.collection("users").document(currentUser.getUid());
        long currentIdLong = (long) currentNationalDexNumber;

        if (userFavorites.contains(currentIdLong)) {
            userRef.update("favorites", FieldValue.arrayRemove(currentIdLong))
                    .addOnSuccessListener(aVoid -> {
                        userFavorites.remove(currentIdLong);
                        actualizarIconoFavorito(currentNationalDexNumber);
                        Toast.makeText(MainActivity.this, "Eliminado de favoritos", Toast.LENGTH_SHORT).show();
                    });
        } else {
            userRef.update("favorites", FieldValue.arrayUnion(currentIdLong))
                    .addOnSuccessListener(aVoid -> {
                        userFavorites.add(currentIdLong);
                        actualizarIconoFavorito(currentNationalDexNumber);
                        Toast.makeText(MainActivity.this, "Guardado en favoritos", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void actualizarIconoFavorito(int id) {
        boolean esFavorito = userFavorites.contains((long) id);
        btnFavorite.setImageResource(esFavorito ? android.R.drawable.star_big_on : android.R.drawable.star_big_off);
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