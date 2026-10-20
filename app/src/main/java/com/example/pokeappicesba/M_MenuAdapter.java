package com.example.pokeappicesba;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class M_MenuAdapter extends RecyclerView.Adapter<M_MenuAdapter.MenuViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(M_MenuItemModel item);
    }

    private final List<M_MenuItemModel> items;
    private final OnItemClickListener listener;

    public M_MenuAdapter(List<M_MenuItemModel> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu_card, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        M_MenuItemModel item = items.get(position);

        holder.tvTitle.setText(item.getId() + ". " + item.getTitle().replace("\n", " "));
        holder.ivIcon.setImageResource(item.getImageResId());

        // Ya no cambiamos el color, siempre será blanco.

        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class MenuViewHolder extends RecyclerView.ViewHolder {
        CardView cardItem;
        ImageView ivIcon;
        TextView tvTitle;

        public MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            cardItem = itemView.findViewById(R.id.cardItem);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvTitle = itemView.findViewById(R.id.tvTitle);
        }
    }
}