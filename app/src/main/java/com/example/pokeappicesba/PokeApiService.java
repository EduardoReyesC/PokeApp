package com.example.pokeappicesba;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface PokeApiService {

    @GET("pokemon/{pokemon}")
    Call<Pokemon> getPokemon(
            @Path("pokemon") String pokemon
    );
}