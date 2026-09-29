package ue.edu.co.fittrackandroid.utils;

public class SerieRutina {
    private Long id;
    private int numeroSerie;
    private int repeticionesObjetivo;
    private double pesoObjetivo;

    public SerieRutina() {
    }

    public SerieRutina(int numeroSerie, Long id, int repeticionesObjetivo,
                       double pesoObjetivo) {
        this.numeroSerie = numeroSerie;
        this.id = id;
        this.repeticionesObjetivo = repeticionesObjetivo;
        this.pesoObjetivo = pesoObjetivo;
    }

    public SerieRutina(int repeticionesObjetivo, int numeroSerie, double pesoObjetivo) {
        this.repeticionesObjetivo = repeticionesObjetivo;
        this.numeroSerie = numeroSerie;
        this.pesoObjetivo = pesoObjetivo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    public double getPesoObjetivo() {
        return pesoObjetivo;
    }

    public void setPesoObjetivo(double pesoObjetivo) {
        this.pesoObjetivo = pesoObjetivo;
    }
}
