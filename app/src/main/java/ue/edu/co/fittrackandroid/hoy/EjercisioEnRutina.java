package ue.edu.co.fittrackandroid.hoy;

import java.util.List;

import ue.edu.co.fittrackandroid.utils.SerieRutina;

public class EjercisioEnRutina {
    private int id;
    private int ejercicioId;
    private String nombre;
    private int orden;
    private List<SerieRutina> series;

    public int getEjercicioId() {
        return ejercicioId;
    }
    public void setEjercicioId(int ejercicioId) {
        this.ejercicioId = ejercicioId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public List<SerieRutina> getSeries() {
        return series;
    }

    public void setSeries(List<SerieRutina> series) {
        this.series = series;
    }
}
