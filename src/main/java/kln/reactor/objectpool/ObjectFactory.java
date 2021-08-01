package kln.reactor.objectpool;

/**
 * @author Daniel
 */
public interface ObjectFactory<T> {

    /**
     * @return the object to be created
     */
    T create();

    void destroy(T t);

    boolean validate(T t);

}
