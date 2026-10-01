package com.example.pokeappicesba;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TabVariantesFragment extends Fragment {

    public static class VariantOption {
        public String label;
        public String queryTarget;
        public boolean isShiny;

        public VariantOption(String label, String queryTarget, boolean isShiny) {
            this.label = label;
            this.queryTarget = queryTarget;
            this.isShiny = isShiny;
        }
    }

    private final List<VariantOption> list = new ArrayList<>();
    private RecyclerView rv;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab_variantes, container, false);
        rv = view.findViewById(R.id.rvVariantes);
        rv.setLayoutManager(new GridLayoutManager(getContext(), 2));
        actualizarAdapter();
        return view;
    }

    public void setVariantes(String baseName, List<String> apiVariantes) {
        list.clear();

        // 1. Opciones principales: Normal y Shiny
        list.add(new VariantOption("Forma Base", baseName, false));
        list.add(new VariantOption("✨ Shiny (Base)", baseName, true));

        // 2. Variantes de PokeAPI (evitando duplicar la forma base ya agregada)
        if (apiVariantes != null) {
            for (String varName : apiVariantes) {
                if (!varName.equalsIgnoreCase(baseName)) {
                    String cleanLabel = varName.replace(baseName + "-", "").replace("-", " ").toUpperCase();
                    list.add(new VariantOption(cleanLabel, varName, false));
                    list.add(new VariantOption("✨ " + cleanLabel, varName, true));
                }
            }
        }

        actualizarAdapter();
    }

    private void actualizarAdapter() {
        if (rv == null) return;
        rv.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                Button btn = new Button(parent.getContext());
                ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(6, 6, 6, 6);
                btn.setLayoutParams(params);
                btn.setBackgroundColor(0xFF1E293B);
                btn.setTextColor(0xFFFFFFFF);
                btn.setTextSize(12f);
                return new RecyclerView.ViewHolder(btn) {};
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                Button b = (Button) holder.itemView;
                VariantOption opt = list.get(position);
                b.setText(opt.label);

                if (opt.isShiny) {
                    b.setTextColor(0xFFFBBF24); // Tono dorado para Shiny
                } else {
                    b.setTextColor(0xFFFFFFFF);
                }

                b.setOnClickListener(v -> {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).aplicarVariante(opt.queryTarget, opt.isShiny);
                    }
                });
            }

            @Override
            public int getItemCount() {
                return list.size();
            }
        });
    }
}