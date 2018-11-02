/**
 * 
 */
package sium.system;

/**
 * @author casey
 *
 * This extends RuntimeException, so it *should* only be used for 
 * Runtime type Exceptions, and it also means we don't need to
 * decorate methods with throws declarations.
 *
 */
public class SimpleUpdateModelException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SimpleUpdateModelException (String msg) {
		super(msg);
	}
	
	
}
