package com.example.pokeappicesba;

import android.content.Intent;
import android.graphics.Color;
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

public class MenuActivity extends AppCompatActivity {

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

        List<MenuItemModel> items = new ArrayList<>();
        // Mapeo exacto de módulos A a L con colores del diseño
        items.add(new MenuItemModel("A", "Pokédex", android.R.drawable.ic_menu_agenda, Color.parseColor("#EF4444")));
        items.add(new MenuItemModel("B", "Battle\nEmulator", android.R.drawable.ic_menu_crop, Color.parseColor("#F97316")));
        items.add(new MenuItemModel("C", "Battle\nVersus", android.R.drawable.ic_menu_share, Color.parseColor("#3B82F6")));

        items.add(new MenuItemModel("D", "Torre\nPokémon", android.R.drawable.ic_menu_compass, Color.parseColor("#8B5CF6")));
        items.add(new MenuItemModel("E", "¿Quién es ese Pokémon?", android.R.drawable.ic_menu_help, Color.parseColor("#EAB308")));
        items.add(new MenuItemModel("F", "Safari\nPokémon", android.R.drawable.ic_menu_view, Color.parseColor("#22C55E")));

        items.add(new MenuItemModel("G", "Maestro\nde Tipos", android.R.drawable.ic_menu_rotate, Color.parseColor("#EC4899")));
        items.add(new MenuItemModel("H", "PokéMemory", android.R.drawable.ic_menu_gallery, Color.parseColor("#06B6D4")));
        items.add(new MenuItemModel("I", "Favoritos", android.R.drawable.btn_star_big_on, Color.parseColor("#F43F5E")));

        items.add(new MenuItemModel("J", "Historial", android.R.drawable.ic_menu_recent_history, Color.parseColor("#2563EB")));
        items.add(new MenuItemModel("K", "Medallas", android.R.drawable.ic_dialog_info, Color.parseColor("#475569")));
        items.add(new MenuItemModel("L", "Mi Perfil", android.R.drawable.ic_menu_myplaces, Color.parseColor("#A855F7")));

        MenuAdapter adapter = new MenuAdapter(items, item -> {
            if ("A".equals(item.getId())) {
                // Abre el módulo Pokédex
                startActivity(new Intent(MenuActivity.this, MainActivity.class));
            } else {
                Toast.makeText(MenuActivity.this, "Módulo " + item.getId() + " (" + item.getTitle().replace("\n", " ") + ") en desarrollo", Toast.LENGTH_SHORT).show();
            }
        });

        rvMenuGrid.setAdapter(adapter);

        // Módulo M
        cardConfig.setOnClickListener(v ->
                Toast.makeText(this, "Módulo M: Configuración", Toast.LENGTH_SHORT).show()
        );

        // Controles de navegación y salida
        btnBack.setOnClickListener(v -> finish());
        btnBottomLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}