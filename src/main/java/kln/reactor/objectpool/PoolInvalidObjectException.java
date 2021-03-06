package kln.reactor.objectpool;

/**
 * This exception is to be thrown by ObjectPool.borrowObject(), if the object retrieved from the pool is invalid
 * (cannot be validated by <code>factory.validate(T obj)</code>)
 */
public class PoolInvalidObjectException extends RuntimeException {
	
	private static final long serialVersionUID = 3126643205027671879L;

	public PoolInvalidObjectException() {
       super("Cannot find an object that can be validated by ObjectFactory.validate()");
    }
}
