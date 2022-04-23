package estructuras.listas;

/**
 * Esta interface no está especificada en el manual de la asigntaura. Se ha
 * extraido de la página 57 del Tema 3.
 *
 * @author franciscojavier.caceres
 *
 */
public interface Lista {

    /**
     * Inserta el elemento pasado por parámetro después de la posición actual
     *
     * @param elemento
     */
    public void insertar(Object elemento);

    /**
     * Elimina el elemento pasado por parámetro si existe.
     *
     * @param elemento
     */
    public void eliminar(Object elemento);

    /**
     * Determina si existe el elemento pasado por parámetro en la lista y
     * coloca la posición actual en la situación de dicho elemento en caso de
     * existir.
     *
     * @param elemento
     * @return
     */
    public boolean buscar(Object elemento);

    /**
     * Coloca la posición actual antes del primer elemento de la lista.
     */
    public void cero();

    /**
     * Sitúa la posición actual en el primer elemento de la lista.
     */
    public void primero();

    /**
     * Avanza la posición actual.
     */
    public void avanzar();

    /**
     * Determina si la posición actual es una posición válida. De esta
     * manera, se puede comprobar si se ha llegado al final de la lista.
     *
     * @return
     */
    public boolean estaDentro();

    /**
     * Devuelve el elemento de la posición actual.
     *
     * @return
     */
    public Object recuperar();

    public void imprimir();
}
