package com.example.pokeappicesba;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        TextView tv = new TextView(getContext());
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        tv.setText("Rincón Personal\nEntrenador: " + (user != null ? user.getEmail() : "Sin sesión"));
        tv.setTextColor(0xFFFFFFFF);
        tv.setPadding(40, 40, 40, 40);
        return tv;
    }
}