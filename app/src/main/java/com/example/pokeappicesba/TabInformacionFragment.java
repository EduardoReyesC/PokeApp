package com.example.pokeappicesba;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;
import java.util.Locale;

public class TabInformacionFragment extends Fragment {

    private TextView tvDescription, tvDetails;
    private LinearLayout containerTypes, containerDebilidades, containerResistencias;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tab_info, container, false);
        tvDescription = v.findViewById(R.id.tvDescription);
        tvDetails = v.findViewById(R.id.tvDetails);
        containerTypes = v.findViewById(R.id.containerTypes);
        containerDebilidades = v.findViewById(R.id.containerDebilidades);
        containerResistencias = v.findViewById(R.id.containerResistencias);
        return v;
    }

    public void actualizarDatos(String desc, String details, List<String> tipos, List<String> debilidades, List<String> resistencias) {
        if (tvDescription != null && desc != null) tvDescription.setText(desc);
        if (tvDetails != null && details != null) tvDetails.setText(details);

        if (getContext() == null) return;

        // 1. Llenar Tipos
        if (containerTypes != null && tipos != null) {
            containerTypes.removeAllViews();
            for (String t : tipos) {
                containerTypes.addView(crearChipTipo(t.toUpperCase(Locale.ROOT), t));
            }
        }

        // 2. Llenar Debilidades
        if (containerDebilidades != null && debilidades != null) {
            containerDebilidades.removeAllViews();
            if (debilidades.isEmpty()) {
                containerDebilidades.addView(crearTextoVacio("Ninguna debilidad notable"));
            } else {
                for (String d : debilidades) {
                    containerDebilidades.addView(crearChipTipo(d, d));
                }
            }
        }

        // 3. Llenar Resistencias
        if (containerResistencias != null && resistencias != null) {
            containerResistencias.removeAllViews();
            if (resistencias.isEmpty()) {
                containerResistencias.addView(crearTextoVacio("Ninguna resistencia notable"));
            } else {
                for (String r : resistencias) {
                    containerResistencias.addView(crearChipTipo(r, r));
                }
            }
        }
    }

    private View crearChipTipo(String textoVisible, String tipoParaColor) {
        TextView tv = new TextView(getContext());
        tv.setText(textoVisible);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(12f);
        tv.setTypeface(null, Typeface.BOLD);
        tv.setPadding(24, 10, 24, 10);

        // Fondo redondeado con el color correspondiente
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(16f);
        shape.setColor(TypeColorUtil.getColorForType(tipoParaColor));
        tv.setBackground(shape);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 16, 0); // Margen derecho entre chips
        tv.setLayoutParams(params);

        return tv;
    }

    private View crearTextoVacio(String texto) {
        TextView tv = new TextView(getContext());
        tv.setText(texto);
        tv.setTextColor(Color.parseColor("#94A3B8"));
        tv.setTextSize(12f);
        return tv;
    }
}