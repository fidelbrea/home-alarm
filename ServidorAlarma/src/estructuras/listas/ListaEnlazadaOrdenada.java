package estructuras.listas;

import estructuras.nodos.*;

/**
 * Clase que implementa una lista enlazada *
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ListaEnlazadaOrdenada implements Lista {

    /**
     * Enlace al nodo cabecera *
     */
    protected Nodo cabecera;

    /**
     * Enlace al nodo de la posición actual *
     */
    protected Nodo actual;

    /**
     * Constructor de la clase. Construye una lista vacía, que empieza con el
     * nodo cabecera.
     *
     */
    public ListaEnlazadaOrdenada() {
        // Se crea el nodo cabecera
        cabecera = new Nodo(null);
        cabecera.setEnlace(null);
        // Se establece la posición actual en la cabecera
        actual = cabecera;
    }

    /**
     * Inserta el elemento pasado por parámetro después de la posición
     * actual. *
     */
    public void insertar(Object elemento) {
        // Se comprueba que el elemento es comparable
        if (elemento instanceof Comparable) {
            insertar((Comparable) elemento);
        } else {
            throw new UnsupportedOperationException("No se puede insertar un objeto no comparable.");
        }
    }

    /**
     * Inserta el elemento en la posición marcada por el orden *
     */
    public void insertar(Comparable elemento) {
        // Se empieza el recorrido por el principio de la lista
        Nodo iterador = cabecera;

        while (iterador.getEnlace() != null && elemento.compareTo(iterador.getEnlace().getElemento()) > 0) {
            iterador = iterador.getEnlace();
        }

        //Se crea un nodo y se enlaza con el siguiente nodo al iterador
        Nodo nuevo = new Nodo(elemento);
        nuevo.setEnlace(iterador.getEnlace());

        //Se actualiza el enlace del nodo actual
        iterador.setEnlace(nuevo);
    }

    /**
     * Elimina el elemento pasado por parámetro si existe. *
     */
    public void eliminar(Object elemento) {
        /*
		 * Se recorren todos los nodos hasta situarse en el nodo anterior al que se
		 * quiere eliminar si existe. Para esto, se empieza en el nodo cabecera y las
		 * condiciones se realizan sobre el nodo siguiente al nodo iterador.
         */
        Nodo nodoIterador = cabecera;
        while (nodoIterador.getEnlace() != null && !nodoIterador.getEnlace().getElemento().equals(elemento)) {
            nodoIterador = nodoIterador.getEnlace();
        }
        /*
		 * Si existe, se elimina el elemento. Para ello, el nodo anterior al nodo
		 * eliminado se enlaza con el nodo siguiente al nodo eliminado. De esta manera
		 * se excluye el nodo que se quiere eliminar
         */
        if (nodoIterador.getEnlace() != null) {
            actual = nodoIterador;
            nodoIterador.setEnlace(nodoIterador.getEnlace().getEnlace());
        }
    }

    /**
     * Determina si existe el elemento pasado por parámetro en la lista y
     * coloca la posición actual en la situación de dicho elemento en caso de
     * existir.
     *
     */
    public boolean buscar(Object elemento) {
        // Se recorren todos los nodos desde la cabecera para encontrar el elemento
        Nodo nodoIterador = cabecera.getEnlace();
        while (nodoIterador != null && !nodoIterador.getElemento().equals(elemento)) {
            nodoIterador = nodoIterador.getEnlace();
        }
        // Si se encuentra el nodo se altera la posición actual
        if (nodoIterador != null) {
            actual = nodoIterador;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Coloca la posición actual antes del primer elemento de la lista *
     */
    public void cero() {
        // La posición actual se sitúa en el nodo cabecera
        actual = cabecera;
    }

    /**
     * Sitúa la posición actual en el primer elemento de la lista *
     */
    public void primero() {
        /*
		 * La posición actual se sitúa en el nodo siguiente a la cabecera, el primero de
		 * la lista.
         */
        actual = cabecera.getEnlace();
    }

    /**
     * Avanza la posición actual *
     */
    public void avanzar() {
        // Se avanza la posición actual, comprobando que no se ha llegado al final
        if (estaDentro()) {
            actual = actual.getEnlace();
        }
    }

    /**
     * Determina si la posición actual es una posición válida. De esta
     * manera, se puede comprobar si se ha llegado al final de la lista.
     *
     */
    public boolean estaDentro() {
        return (actual != null);
    }

    /**
     * Devuelve el elemento de la posición actual. *
     */
    public Object recuperar() {
        /*
		 * Se devuelve el elemento de la posición actual, comprobando que no se ha
		 * llegado al final
         */
        if (estaDentro()) {
            return actual.getElemento();
        } else {
            return null;
        }
    }

    /**
     * Muestra por pantalla la lista de objetos *
     */
    public void imprimir() {
        // Se recorren todos los nodos y se muestran por pantalla.
        Nodo nodoIterador = cabecera.getEnlace();
        while (nodoIterador != null) {
            System.out.printf("\t" + nodoIterador.getElemento() + "\n");
            nodoIterador = nodoIterador.getEnlace();
        }
    }
}
