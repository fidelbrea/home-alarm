package estructuras.nodos;

/**
 * Nodo de una lista enlazada. Contiene un elemento y un enlace al siguiente
 * nodo *
 */
public class Nodo {

    /**
     * Elemento que contiene el nodo *
     */
    private Object elemento;

    /**
     * Enlace al siguiente nodo *
     */
    private Nodo enlace;

    /**
     * Constructor de la clase *
     */
    public Nodo(Object elemento) {
        this.elemento = elemento;
    }

    /**
     * Devuelve el elemento del nodo *
     */
    public Object getElemento() {
        return elemento;
    }

    /**
     * Establece el elemento pasado por parámetro *
     */
    public void setElemento(Object elemento) {
        this.elemento = elemento;
    }

    /**
     * Devuelve el enlace del nodo *
     */
    public Nodo getEnlace() {
        return enlace;
    }

    /**
     * Establece el enlace pasado por parámetro *
     */
    public void setEnlace(Nodo enlace) {
        this.enlace = enlace;
    }
}
