/**
 * 
 */
package sium.nlu.context;

import java.util.TreeSet;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

/**
 * @author casey
 *
 */
@XmlRootElement
public class Properties<T> extends TreeSet<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	static Logger log = Logger.getLogger(Context.class.getName());
	

	
}
