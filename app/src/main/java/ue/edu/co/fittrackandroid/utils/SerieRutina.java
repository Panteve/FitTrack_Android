package ue.edu.co.fittrackandroid.utils;

public class SerieRutina {
    private int id;
    private int numeroSerie;
    private int repeticionesObjetivo;
    private int pesoObjetivo;

    public SerieRutina() {
    }

    public SerieRutina(int numeroSerie, int id, int repeticionesObjetivo, int pesoObjetivo) {
        this.numeroSerie = numeroSerie;
        this.id = id;
        this.repeticionesObjetivo = repeticionesObjetivo;
        this.pesoObjetivo = pesoObjetivo;
    }

    public SerieRutina(int repeticionesObjetivo, int numeroSerie, int pesoObjetivo) {
        this.repeticionesObjetivo = repeticionesObjetivo;
        this.numeroSerie = numeroSerie;
        this.pesoObjetivo = pesoObjetivo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRepeticionesObjetivo() {
        return repeticionesObjetivo;
    }

    public void setRepeticionesObjetivo(int repeticionesObjetivo) {
        this.repeticionesObjetivo = repeticionesObjetivo;
    }

    public int getNumeroSerie() {
        return numeroSerie;
    }

    public void setNumeroSerie(int numeroSerie) {
        this.numeroSerie = numeroSerie;
    }

    public int getPesoObjetivo() {
        return pesoObjetivo;
    }

    public void setPesoObjetivo(int pesoObjetivo) {
        this.pesoObjetivo = pesoObjetivo;
    }
}
