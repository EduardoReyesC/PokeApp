package com.example.pokeappicesba;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class TabInformacionFragment extends Fragment {

    private TextView tvDescription, tvDetails;
    private RadarStatsView radarStats;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tab_info, container, false);
        tvDescription = v.findViewById(R.id.tvDescription);
        tvDetails = v.findViewById(R.id.tvDetails);
        radarStats = v.findViewById(R.id.radarStats);
        return v;
    }

    public void actualizarDatos(String desc, String details, int hp, int atk, int def, int speed, int spDef, int spAtk) {
        if (tvDescription != null && desc != null) tvDescription.setText(desc);
        if (tvDetails != null && details != null) tvDetails.setText(details);
        if (radarStats != null) radarStats.setStats(hp, atk, def, speed, spDef, spAtk);
    }
}