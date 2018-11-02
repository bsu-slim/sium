/**
 * 
 */
package sium.nlu.context;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.log4j.Logger;

/**
 * @author casey
 *
 */
@XmlRootElement
public class Entity<T> {
	
	static Logger log = Logger.getLogger(Entity.class.getName());

	public Entity() {}
	
	private T entity;
	
	public Entity(T entity) {
		this.setEntity(entity);
	}

	public T getEntity() {
		return entity;
	}
	
	public String toString() {
		return this.getEntity().toString();
	}

	@XmlElement
	public void setEntity(T entity) {
		this.entity = entity;
	}
	
	/*
	 * We want to compare the types that the entities contain, 
	 * not the Entities themselves
	 */
	public boolean equals(Entity<T> other) {
		return other.getEntity().equals(getEntity());
	}
	
}
