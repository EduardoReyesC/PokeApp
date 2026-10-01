package com.example.pokeappicesba;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class HomePagerAdapter extends FragmentStateAdapter {

    public HomePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new PokedexFragment();
            case 1:
                return new ProfileFragment(); // "Rincón personal"
            case 2:
                return new GamesFragment();   // Battle Emulator, Versus, Torre
            default:
                return new PokedexFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}