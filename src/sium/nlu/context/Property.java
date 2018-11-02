/**
 * 
 */
package sium.nlu.context;

import org.apache.log4j.Logger;


/**
 * @author casey
 *
 */
public class Property<T extends Comparable<T>> implements Comparable<T> {

	static Logger log = Logger.getLogger(Property.class.getName());
	
	private T property;
	private Double confidence;
	
	public Property() {}
	
	public Property(T property, Double confidence) {
		this.setProperty(property);
		this.setConfidence(confidence);
	}
	
	public Property(T property) {
		this.setProperty(property);
		this.setConfidence(1.0);
	}

	public void setProperty(T property) {
		this.property = property;
	}
	
	public T getProperty() {
		return property;
	}
	
	public void setConfidence(Double confidence) {
		this.confidence = confidence;
	}
	
	public Double getConfidence() {
		return confidence;
	}

	@Override
	public int compareTo(T o) {
		return o.compareTo(getProperty());
	}
	
	public boolean equals(Property<T> other) {
		return other.getProperty().equals(getProperty());
	}
	
}
