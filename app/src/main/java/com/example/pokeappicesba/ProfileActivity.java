package com.example.pokeappicesba;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView imgCurrentAvatar;
    private TextView tvPlayerEmail;
    private EditText etTrainerName;
    private RecyclerView rvAvatarSelector;
    private Button btnSaveProfile;
    private ImageButton btnBackProfile;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    // Lista de identificadores de drawables disponibles
    private final List<String> avatarList = new ArrayList<>();
    private String selectedAvatarId = "avatar_red";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        imgCurrentAvatar = findViewById(R.id.imgCurrentAvatar);
        tvPlayerEmail = findViewById(R.id.tvPlayerEmail);
        etTrainerName = findViewById(R.id.etTrainerName);
        rvAvatarSelector = findViewById(R.id.rvAvatarSelector);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnBackProfile = findViewById(R.id.btnBackProfile);

        if (currentUser == null) {
            Toast.makeText(this, "Sesión no válida", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvPlayerEmail.setText(currentUser.getEmail());

        // Llenar lista de avatares disponibles
        cargarListaAvatares();

        // Configurar RecyclerView
        rvAvatarSelector.setLayoutManager(new GridLayoutManager(this, 4));
        AvatarAdapter adapter = new AvatarAdapter();
        rvAvatarSelector.setAdapter(adapter);

        // Cargar datos actuales desde Firestore
        cargarDatosUsuario();

        btnBackProfile.setOnClickListener(v -> finish());
        btnSaveProfile.setOnClickListener(v -> guardarDatosUsuario());
    }

    private void cargarListaAvatares() {
        avatarList.clear();
        avatarList.add("avatar_red");
        avatarList.add("avatar_leaf");
        avatarList.add("avatar_ethan");
        avatarList.add("avatar_lyra");
        avatarList.add("avatar_lucas");
        avatarList.add("avatar_dawn");
        avatarList.add("avatar_cynthia");
        avatarList.add("avatar_steven");
    }

    private int getDrawableResId(String name) {
        int resId = getResources().getIdentifier(name, "drawable", getPackageName());
        return resId != 0 ? resId : android.R.drawable.sym_def_app_icon;
    }

    private void cargarDatosUsuario() {
        DocumentReference docRef = db.collection("users").document(currentUser.getUid());
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String name = documentSnapshot.getString("playerName");
                String avatar = documentSnapshot.getString("avatarId");

                if (name != null) etTrainerName.setText(name);
                if (avatar != null) selectedAvatarId = avatar;
            } else {
                // Si no existe, inicializar con el nombre antes del arroba
                String defaultName = currentUser.getEmail() != null ? currentUser.getEmail().split("@")[0] : "Entrenador";
                etTrainerName.setText(defaultName);
            }
            imgCurrentAvatar.setImageResource(getDrawableResId(selectedAvatarId));
        }).addOnFailureListener(e -> {
            Toast.makeText(ProfileActivity.this, "Error al cargar perfil", Toast.LENGTH_SHORT).show();
        });
    }

    private void guardarDatosUsuario() {
        String newName = etTrainerName.getText().toString().trim();
        if (newName.isEmpty()) {
            etTrainerName.setError("Ingresa un nombre");
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("uid", currentUser.getUid());
        data.put("email", currentUser.getEmail());
        data.put("playerName", newName);
        data.put("avatarId", selectedAvatarId);

        btnSaveProfile.setEnabled(false);
        db.collection("users").document(currentUser.getUid())
                .set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    btnSaveProfile.setEnabled(true);
                    Toast.makeText(ProfileActivity.this, "Perfil guardado con éxito", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSaveProfile.setEnabled(true);
                    Toast.makeText(ProfileActivity.this, "Error al guardar perfil", Toast.LENGTH_SHORT).show();
                });
    }

    // Adaptador interno para seleccionar avatar
    private class AvatarAdapter extends RecyclerView.Adapter<AvatarAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_avatar_selector, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String avatarKey = avatarList.get(position);
            holder.img.setImageResource(getDrawableResId(avatarKey));

            // Resaltar seleccionado con borde o color
            if (avatarKey.equals(selectedAvatarId)) {
                holder.card.setCardBackgroundColor(Color.parseColor("#0284C7"));
            } else {
                holder.card.setCardBackgroundColor(Color.parseColor("#1E293B"));
            }

            holder.itemView.setOnClickListener(v -> {
                selectedAvatarId = avatarKey;
                imgCurrentAvatar.setImageResource(getDrawableResId(selectedAvatarId));
                notifyDataSetChanged();
            });
        }

        @Override
        public int getItemCount() {
            return avatarList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            CardView card;
            ImageView img;
            ViewHolder(View itemView) {
                super(itemView);
                card = itemView.findViewById(R.id.cardAvatarItem);
                img = itemView.findViewById(R.id.imgAvatarItem);
            }
        }
    }
}