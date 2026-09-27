package ue.edu.co.fittrackandroid.entrenamiento;

/**
 * Modelo de una serie dentro de un entrenamiento en curso.
 * El peso y las repeticiones se guardan como texto porque el usuario los digita
 * en la pantalla y todavía no se han validado.
 * A diferencia de SerieRutina (usada al crear una rutina), aquí la serie también
 * recuerda si ya fue completada, porque de eso depende el volumen del entrenamiento.
 */
public class SerieEntrenamiento {

    private String peso = "";
    private String repeticiones = "";
    private boolean completada = false;

    public String getPeso() {
        return peso;
    }

    public void setPeso(String peso) {
        this.peso = peso;
    }

    public String getRepeticiones() {
        return repeticiones;
    }

    public void setRepeticiones(String repeticiones) {
        this.repeticiones = repeticiones;
    }

    public boolean isCompletada() {
        return completada;
    }

    public void setCompletada(boolean completada) {
        this.completada = completada;
    }

    /**
     * Revisa que el peso sea un número igual o mayor que cero y que las repeticiones
     * sean un entero mayor que cero. El cero se acepta en el peso porque sirve
     * para los ejercicios de peso corporal.
     *
     * @return true si la serie tiene los dos valores usables.
     */
    public boolean tieneDatosValidos() {
        return esPesoValido() && sonRepeticionesValidas();
    }

    /** @return true si el peso escrito es un número igual o mayor que cero. */
    public boolean esPesoValido() {
        String numeroPeso = peso.trim();
        if (numeroPeso.isEmpty()) {
            return false;
        }

        try {
            // En algunos teclados el separador decimal es la coma.
            return Double.parseDouble(numeroPeso.replace(',', '.')) >= 0;
        } catch (NumberFormatException error) {
            return false;
        }
    }

    /** @return true si las repeticiones escritas son un entero mayor que cero. */
    public boolean sonRepeticionesValidas() {
        String numeroRepeticiones = repeticiones.trim();
        if (numeroRepeticiones.isEmpty()) {
            return false;
        }

        try {
            return Integer.parseInt(numeroRepeticiones) > 0;
        } catch (NumberFormatException error) {
            return false;
        }
    }

    /**
     * Calcula el volumen de la serie: peso multiplicado por repeticiones.
     * Una serie que todavía no está completada, o que tiene valores inválidos,
     * no aporta nada al volumen del entrenamiento.
     *
     * @return el volumen de la serie, o cero si todavía no cuenta.
     */
    public double calcularVolumen() {
        if (!completada || !tieneDatosValidos()) {
            return 0;
        }

        double pesoRegistrado = Double.parseDouble(peso.trim().replace(',', '.'));
        int repeticionesRegistradas = Integer.parseInt(repeticiones.trim());
        return pesoRegistrado * repeticionesRegistradas;
    }
}
