package ue.edu.co.fittrackandroid.hoy.modelo;

import java.util.List;

import ue.edu.co.fittrackandroid.utils.SerieRutina;

public class EjercisioEnRutina {
    private Long id;
    private Long ejercicioId;
    private String nombre;
    private String grupoMuscular;
    private int orden;
    private List<SerieRutina> series;

    public Long getEjercicioId() {
        return ejercicioId;
    }

    public void setEjercicioId(Long ejercicioId) {
        this.ejercicioId = ejercicioId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getGrupoMuscular() {
        return grupoMuscular;
    }

    public void setGrupoMuscular(String grupoMuscular) {
        this.grupoMuscular = grupoMuscular;
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
