package com.example.pokeappicesba;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class GamesFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        String[] modos = {"Battle Emulator (1v1)", "Battle Versus (3v3)", "Torre Pokémon CESBA"};
        for (String modo : modos) {
            Button btn = new Button(getContext());
            btn.setText(modo);
            btn.setOnClickListener(v -> Toast.makeText(getContext(), modo + " disponible en su día correspondiente", Toast.LENGTH_SHORT).show());
            layout.addView(btn);
        }
        return layout;
    }
}