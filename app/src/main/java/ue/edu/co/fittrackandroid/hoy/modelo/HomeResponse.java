package ue.edu.co.fittrackandroid.hoy.modelo;

import java.util.List;

public class HomeResponse {
    private ProximaRutina proximaRutina;

    private List<UltimoEntrenamiento> ultimosEntrenamientos;

    public List<UltimoEntrenamiento> getUltimosEntrenamientos() {
        return ultimosEntrenamientos;
    }

    public void setUltimosEntrenamientos(List<UltimoEntrenamiento> ultimosEntrenamientos) {
        this.ultimosEntrenamientos = ultimosEntrenamientos;
    }

    public ProximaRutina getProximaRutina() {
        return proximaRutina;
    }

    public void setProximaRutina(ProximaRutina proximaRutina) {
        this.proximaRutina = proximaRutina;
    }
}
