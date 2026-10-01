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

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(MenuItemModel item);
    }

    private final List<MenuItemModel> items;
    private final OnItemClickListener listener;

    public MenuAdapter(List<MenuItemModel> items, OnItemClickListener listener) {
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
        MenuItemModel item = items.get(position);
        holder.tvTitle.setText(item.getId() + ". " + item.getTitle());
        holder.ivIcon.setImageResource(item.getIconResId());
        holder.cardItem.setCardBackgroundColor(item.getBackgroundColor());

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