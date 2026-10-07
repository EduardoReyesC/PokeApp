package com.example.pokeappicesba;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class TabMovimientosFragment extends Fragment {

    public static class MoveRow {
        public String name;
        public String accuracy;
        public String power;
        public MoveRow(String n, String a, String p) { name = n; accuracy = a; power = p; }
    }

    private final List<MoveRow> moves = new ArrayList<>();
    private RecyclerView rv;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tab_movimientos, container, false);
        rv = v.findViewById(R.id.rvMovimientos);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        actualizarAdapter();
        return v;
    }

    public void setMoves(List<MoveRow> newMoves) {
        moves.clear();
        if (newMoves != null) moves.addAll(newMoves);
        actualizarAdapter();
    }

    private void actualizarAdapter() {
        if (rv == null) return;
        rv.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                LinearLayout row = new LinearLayout(parent.getContext());
                row.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setPadding(6, 12, 6, 12);

                // Nombre del Ataque (Oscuro sobre fondo blanco)
                TextView tvN = new TextView(parent.getContext());
                tvN.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.4f));
                tvN.setTextColor(0xFF0F172A);
                tvN.setTextSize(12f);
                tvN.setTypeface(null, Typeface.BOLD);

                // Precisión (Azul cielo legible)
                TextView tvA = new TextView(parent.getContext());
                tvA.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.8f));
                tvA.setTextColor(0xFF0284C7);
                tvA.setTextSize(12f);
                tvA.setGravity(android.view.Gravity.CENTER);

                // Daño (Rojo Pokédex contrastado)
                TextView tvP = new TextView(parent.getContext());
                tvP.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 0.8f));
                tvP.setTextColor(0xFFDC2626);
                tvP.setTextSize(12f);
                tvP.setTypeface(null, Typeface.BOLD);
                tvP.setGravity(android.view.Gravity.END);

                row.addView(tvN);
                row.addView(tvA);
                row.addView(tvP);
                return new RecyclerView.ViewHolder(row) {};
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                LinearLayout l = (LinearLayout) holder.itemView;
                MoveRow item = moves.get(position);
                ((TextView) l.getChildAt(0)).setText(item.name.replace("-", " "));
                ((TextView) l.getChildAt(1)).setText(item.accuracy);
                ((TextView) l.getChildAt(2)).setText(item.power);
            }

            @Override
            public int getItemCount() { return moves.size(); }
        });
    }
}