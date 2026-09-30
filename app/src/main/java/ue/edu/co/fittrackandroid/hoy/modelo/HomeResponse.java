package ue.edu.co.fittrackandroid.hoy.modelo;

import java.util.List;

public class HomeResponse {
    private ProximaRutina proximaRutina;

    private List<UltimoEntrenamiento> ultimosEntrenamientos;

    /** Obtiene la lista de entrenamientos recientes que se muestran en la pantalla de inicio. */
    public List<UltimoEntrenamiento> getUltimosEntrenamientos() {
        return ultimosEntrenamientos;
    }

    /** Cambia la lista de entrenamientos recientes de la pantalla de inicio. */
    public void setUltimosEntrenamientos(List<UltimoEntrenamiento> ultimosEntrenamientos) {
        this.ultimosEntrenamientos = ultimosEntrenamientos;
    }

    /** Obtiene la rutina que el servidor sugiere como próxima, o null si el usuario no tiene ninguna asignada. */
    public ProximaRutina getProximaRutina() {
        return proximaRutina;
    }

    /** Cambia la rutina que se muestra como próxima en la pantalla de inicio. */
    public void setProximaRutina(ProximaRutina proximaRutina) {
        this.proximaRutina = proximaRutina;
    }
}
