package com.example.pokeappicesba;

import android.graphics.Color;
import java.util.Locale;

public class TypeColorUtil {

    public static int getColorForType(String typeName) {
        if (typeName == null) return Color.parseColor("#64748B");
        String t = typeName.toLowerCase(Locale.ROOT).trim();

        // Extraer solo la palabra si viene con multiplicador (ej: "ELECTRIC (x2)" -> "electric")
        if (t.contains(" ")) {
            t = t.substring(0, t.indexOf(" "));
        }

        switch (t) {
            case "fire": return Color.parseColor("#F97316");       // Fuego: Naranja fuego
            case "water": return Color.parseColor("#3B82F6");      // Agua: Azul
            case "grass": return Color.parseColor("#22C55E");      // Planta: Verde
            case "electric": return Color.parseColor("#EAB308");   // Eléctrico: Amarillo brillante
            case "ice": return Color.parseColor("#38BDF8");        // Hielo: Celeste
            case "fighting": return Color.parseColor("#DC2626");   // Lucha: Rojo oscuro
            case "poison": return Color.parseColor("#A855F7");     // Veneno: Morado
            case "ground": return Color.parseColor("#D97706");     // Tierra: Ocre / café claro
            case "flying": return Color.parseColor("#818CF8");     // Volador: Azul cielo
            case "psychic": return Color.parseColor("#EC4899");    // Psíquico: Magenta/Rosa
            case "bug": return Color.parseColor("#84CC16");        // Bicho: Verde lima
            case "rock": return Color.parseColor("#78716C");       // Roca: Gris piedra
            case "ghost": return Color.parseColor("#6366F1");      // Fantasma: Índigo
            case "dragon": return Color.parseColor("#4F46E5");     // Dragón: Azul profundo
            case "steel": return Color.parseColor("#64748B");      // Acero: Gris metálico
            case "dark": return Color.parseColor("#334155");       // Siniestro: Gris oscuro/negro
            case "fairy": return Color.parseColor("#F472B6");      // Hada: Rosa claro
            case "normal":
            default:
                return Color.parseColor("#94A3B8");               // Normal: Gris neutro
        }
    }
}