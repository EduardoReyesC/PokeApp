package com.example.pokeappicesba;

public class MoveDetail {
    private String name;
    private Integer power;
    private Integer accuracy;
    private TypeRef type;
    private DamageClassRef damage_class;

    public String getName() { return name; }
    public Integer getPower() { return power; }
    public Integer getAccuracy() { return accuracy; }
    public TypeRef getType() { return type; }

    // Si la API falla, asumimos especial por seguridad
    public String getDamageClass() { return damage_class != null ? damage_class.getName() : "special"; }

    public static class TypeRef {
        private String name;
        public String getName() { return name; }
    }

    public static class DamageClassRef {
        private String name;
        public String getName() { return name; }
    }
}