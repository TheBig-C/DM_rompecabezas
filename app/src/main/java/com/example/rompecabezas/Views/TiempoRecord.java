package com.example.rompecabezas.Views;

public class TiempoRecord {
    private String nombre;
    private long tiempo;

    public TiempoRecord(String nombre, long tiempo) {
        this.nombre = nombre;
        this.tiempo = tiempo;
    }

    public String getNombre() {
        return nombre;
    }

    public long getTiempo() {
        return tiempo;
    }
}
