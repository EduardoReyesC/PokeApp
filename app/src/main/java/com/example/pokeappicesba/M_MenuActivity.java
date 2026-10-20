package com.example.pokeappicesba;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class M_MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        RecyclerView rvMenuGrid = findViewById(R.id.rvMenuGrid);
        CardView cardConfig = findViewById(R.id.cardConfig);
        ImageView btnBack = findViewById(R.id.btnBack);
        ImageView btnBottomLogout = findViewById(R.id.btnBottomLogout);

        // Grid de 3 columnas
        rvMenuGrid.setLayoutManager(new GridLayoutManager(this, 3));

        List<M_MenuItemModel> items = new ArrayList<>();
        // Ahora solo pasamos el ID, el Nombre y la Imagen
        items.add(new M_MenuItemModel("A", "Pokédex", R.drawable.icon_pkedex));
        items.add(new M_MenuItemModel("B", "Battle\nEmulator", R.drawable.icon_simulador));
        items.add(new M_MenuItemModel("C", "Battle\nVersus", R.drawable.icon_vs));

        items.add(new M_MenuItemModel("D", "Torre\nPokémon", R.drawable.icon_torre));
        items.add(new M_MenuItemModel("E", "¿Quién es\nese Pokémon?", R.drawable.icon_trivia));
        items.add(new M_MenuItemModel("F", "Safari\nPokémon", R.drawable.icon_safari));

        items.add(new M_MenuItemModel("G", "Maestro\nde Tipos", R.drawable.icon_tipos));
        items.add(new M_MenuItemModel("H", "PokéMemory", R.drawable.icon_menorama));
        items.add(new M_MenuItemModel("I", "Favoritos", R.drawable.icon_favoritos));

        items.add(new M_MenuItemModel("J", "Historial", R.drawable.icon_historial));
        items.add(new M_MenuItemModel("K", "Medallas", R.drawable.icon_medallas));
        items.add(new M_MenuItemModel("L", "Mi Perfil", R.drawable.icon_perfil));

        M_MenuAdapter adapter = new M_MenuAdapter(items, item -> {
            if ("A".equals(item.getId())) {
                startActivity(new Intent(M_MenuActivity.this, MainActivity.class));
            } else if ("B".equals(item.getId())) {
                startActivity(new Intent(M_MenuActivity.this, B_BattleSetupActivity.class));
            } else if ("C".equals(item.getId())) {
                startActivity(new Intent(M_MenuActivity.this, B_VersusModeActivity.class));
            } else if ("D".equals(item.getId())) {
                startActivity(new Intent(M_MenuActivity.this, B_TorreDraftActivity.class));
            } else if ("J".equals(item.getId())) {
                startActivity(new Intent(M_MenuActivity.this, HistoryActivity.class));
            } else if ("L".equals(item.getId())) {
                startActivity(new Intent(M_MenuActivity.this, ProfileActivity.class));
            } else {
                Toast.makeText(M_MenuActivity.this, "Módulo " + item.getId() + " en desarrollo", Toast.LENGTH_SHORT).show();
            }
        });

        rvMenuGrid.setAdapter(adapter);

        // Módulo M: Configuración
        if (cardConfig != null) {
            cardConfig.setOnClickListener(v ->
                    startActivity(new Intent(M_MenuActivity.this, ProfileActivity.class))
            );
        }

        // Controles
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnBottomLogout != null) {
            btnBottomLogout.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(M_MenuActivity.this, LoginActivity.class));
                finish();
            });
        }
    }
}