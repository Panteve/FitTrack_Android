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
     * Convierte el peso escrito en un número, para poder copiarlo al resumen del
     * entrenamiento terminado.
     *
     * @return el peso como número, o cero si el usuario no escribió un valor válido.
     */
    public double obtenerPesoNumerico() {
        if (!esPesoValido()) {
            return 0;
        }
        return Double.parseDouble(peso.trim().replace(',', '.'));
    }

    /**
     * Convierte las repeticiones escritas en un número, para poder copiarlas al resumen
     * del entrenamiento terminado.
     *
     * @return las repeticiones como número, o cero si no son válidas.
     */
    public int obtenerRepeticionesNumericas() {
        if (!sonRepeticionesValidas()) {
            return 0;
        }
        return Integer.parseInt(repeticiones.trim());
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

        return obtenerPesoNumerico() * obtenerRepeticionesNumericas();
    }
}
