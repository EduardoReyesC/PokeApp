package com.example.pokeappicesba;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private TextView tvEmptyHistory;
    private ImageView btnBackHistory;

    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private final List<BattleRecord> battleList = new ArrayList<>();
    private HistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        rvHistory = findViewById(R.id.rvHistory);
        tvEmptyHistory = findViewById(R.id.tvEmptyHistory);
        btnBackHistory = findViewById(R.id.btnBackHistory);

        btnBackHistory.setOnClickListener(v -> finish());

        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new HistoryAdapter(battleList, this::mostrarDetalleBatalla);
        rvHistory.setAdapter(adapter);

        cargarHistorialDesdeFirestore();
    }

    private void cargarHistorialDesdeFirestore() {
        if (currentUser == null) {
            tvEmptyHistory.setVisibility(View.VISIBLE);
            tvEmptyHistory.setText("Inicia sesión para ver tu historial");
            return;
        }

        db.collection("users")
                .document(currentUser.getUid())
                .collection("battle_history")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    battleList.clear();
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                            BattleRecord record = doc.toObject(BattleRecord.class);
                            if (record != null) {
                                battleList.add(record);
                            }
                        }
                        tvEmptyHistory.setVisibility(View.GONE);
                    } else {
                        tvEmptyHistory.setVisibility(View.VISIBLE);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(HistoryActivity.this, "Error al cargar historial", Toast.LENGTH_SHORT).show();
                    tvEmptyHistory.setVisibility(View.VISIBLE);
                });
    }

    private void mostrarDetalleBatalla(BattleRecord record) {
        StringBuilder sb = new StringBuilder();
        if (record.getTurnLogs() != null && !record.getTurnLogs().isEmpty()) {
            for (String log : record.getTurnLogs()) {
                sb.append(log).append("\n");
            }
        } else {
            sb.append("No hay desglose disponible para esta batalla.");
        }

        new AlertDialog.Builder(this)
                .setTitle(record.getPlayerPokemon() + " VS " + record.getEnemyPokemon())
                .setMessage(sb.toString())
                .setPositiveButton("Cerrar", null)
                .show();
    }

    // Adaptador Interno del Historial
    public static class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

        public interface OnItemClickListener {
            void onItemClick(BattleRecord item);
        }

        private final List<BattleRecord> list;
        private final OnItemClickListener listener;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        public HistoryAdapter(List<BattleRecord> list, OnItemClickListener listener) {
            this.list = list;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_card, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            BattleRecord item = list.get(position);

            holder.tvMatchup.setText(item.getPlayerPokemon() + " VS " + item.getEnemyPokemon());
            holder.tvWinnerBadge.setText("Ganó: " + item.getWinner());
            holder.tvBattleDate.setText(dateFormat.format(new Date(item.getTimestamp())));

            holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvMatchup, tvWinnerBadge, tvBattleDate;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvMatchup = itemView.findViewById(R.id.tvMatchup);
                tvWinnerBadge = itemView.findViewById(R.id.tvWinnerBadge);
                tvBattleDate = itemView.findViewById(R.id.tvBattleDate);
            }
        }
    }
}