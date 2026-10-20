package com.example.pokeappicesba;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class B_VersusLobbyActivity extends AppCompatActivity {

    private EditText etRoomCode;
    private TextView tvStatus;
    private Button btnCreateRoom, btnJoinRoom;

    private FirebaseFirestore db;
    private String currentUserId;
    private ListenerRegistration roomListener;
    private String currentRoomCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_versus_lobby);

        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etRoomCode = findViewById(R.id.etRoomCode);
        tvStatus = findViewById(R.id.tvStatus);
        btnCreateRoom = findViewById(R.id.btnCreateRoom);
        btnJoinRoom = findViewById(R.id.btnJoinRoom);
        ImageView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        btnCreateRoom.setOnClickListener(v -> createRoom());
        btnJoinRoom.setOnClickListener(v -> joinRoom());
    }

    private void createRoom() {
        currentRoomCode = String.valueOf(new Random().nextInt(9000) + 1000);
        long battleSeed = System.currentTimeMillis();

        Map<String, Object> roomData = new HashMap<>();
        roomData.put("hostId", currentUserId);
        roomData.put("guestId", "");
        roomData.put("status", "waiting");
        roomData.put("seed", battleSeed);

        tvStatus.setText("Creando sala...");
        deshabilitarBotones();

        db.collection("rooms").document(currentRoomCode).set(roomData)
                .addOnSuccessListener(aVoid -> {
                    tvStatus.setText("Sala creada: " + currentRoomCode + "\nEsperando al rival...");
                    etRoomCode.setText(currentRoomCode);
                    escucharSala("host");
                })
                .addOnFailureListener(e -> {
                    tvStatus.setText("Error al crear sala");
                    habilitarBotones();
                });
    }

    private void joinRoom() {
        currentRoomCode = etRoomCode.getText().toString().trim();
        if (currentRoomCode.length() != 4) {
            Toast.makeText(this, "Ingresa un código válido de 4 dígitos", Toast.LENGTH_SHORT).show();
            return;
        }

        tvStatus.setText("Buscando sala...");
        deshabilitarBotones();

        DocumentReference roomRef = db.collection("rooms").document(currentRoomCode);
        roomRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    String status = document.getString("status");
                    if ("waiting".equals(status)) {
                        roomRef.update("guestId", currentUserId, "status", "drafting")
                                .addOnSuccessListener(aVoid -> escucharSala("guest"))
                                .addOnFailureListener(e -> habilitarBotones());
                    } else {
                        tvStatus.setText("La sala ya está llena o en curso");
                        habilitarBotones();
                    }
                } else {
                    tvStatus.setText("La sala no existe");
                    habilitarBotones();
                }
            }
        });
    }

    private void escucharSala(String role) {
        DocumentReference roomRef = db.collection("rooms").document(currentRoomCode);

        roomListener = roomRef.addSnapshotListener((snapshot, e) -> {
            if (e != null || snapshot == null) return;

            String status = snapshot.getString("status");
            if ("drafting".equals(status)) {
                if (roomListener != null) roomListener.remove();

                Toast.makeText(this, "¡Rival encontrado! Yendo a selección...", Toast.LENGTH_SHORT).show();

                // Abre la pantalla de Draft Online
                Intent intent = new Intent(this, B_TeamDraftOnlineActivity.class);
                intent.putExtra("ROOM_CODE", currentRoomCode);
                intent.putExtra("ROLE", role); // "host" o "guest"
                startActivity(intent);
                finish();
            }
        });
    }

    private void deshabilitarBotones() {
        btnCreateRoom.setEnabled(false);
        btnJoinRoom.setEnabled(false);
        etRoomCode.setEnabled(false);
    }

    private void habilitarBotones() {
        btnCreateRoom.setEnabled(true);
        btnJoinRoom.setEnabled(true);
        etRoomCode.setEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (roomListener != null) roomListener.remove();
    }
}