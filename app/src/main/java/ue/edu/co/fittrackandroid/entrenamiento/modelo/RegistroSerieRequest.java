package ue.edu.co.fittrackandroid.entrenamiento.modelo;

/** Datos de una serie completada que se enviarán al backend. */
public class RegistroSerieRequest {

    private final Long rutinaEjercicioId;
    private final int numeroSerie;
    private final int repeticiones;
    private final double peso;

    /**
     * Crea el registro de una serie terminada, con el bloque de la rutina al que pertenece
     * y los valores que el usuario escribió.
     */
    public RegistroSerieRequest(Long rutinaEjercicioId, int numeroSerie,
                                int repeticiones, double peso) {
        this.rutinaEjercicioId = rutinaEjercicioId;
        this.numeroSerie = numeroSerie;
        this.repeticiones = repeticiones;
        this.peso = peso;
    }
}
