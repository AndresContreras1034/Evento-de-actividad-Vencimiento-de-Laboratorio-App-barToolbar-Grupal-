package com.edupilot.solicitudes;

public class Solicitud {
    public String titulo;
    public String descripcion;
    public boolean atendida;

    public Solicitud(String titulo, String descripcion, boolean atendida) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.atendida = atendida;
    }

    @Override
    public String toString() {
        String marca = atendida ? "✔ " : "• ";
        return descripcion.isEmpty() ? marca + titulo : marca + titulo + "\n" + descripcion;
    }
}