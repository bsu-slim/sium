/**
 * 
 */
package sium.nlu.context;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import sium.system.SimpleUpdateModelException;
import sium.system.util.XMLUtils;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

/**
 * @author casey
 *
 *	This is an object that represents the "context" of the dialogue. The context could be visual, 
 *  abstract, or anything else. In other words, the notion of "context" is very broad. 
 *  
 *  Generics are used here so developers can use other, complex types rather than just Strings.
 *  
 *  TODO: the "create" and "get" methods might be confusing, might re-think the naming conventions
 *	TODO: xml reading/writing should be done with marshallers to handle more complex data types 
 */
public class Context<E,P extends Comparable<P>> {
	
	static Logger log = Logger.getLogger(Context.class.getName());

	private HashMap<E, Entity<E>> entities;
	private HashMap<String, Property<P>> properties; // we need a String key to encode entity_property pairs since different entities will have different properties prob/conf scores
	private HashMap<Entity<E>, Properties<Property<P>>> entitiesAndTheirProperties;
	
	private String contextID;
	
	public Context() {
		entitiesAndTheirProperties = new HashMap<Entity<E>, Properties<Property<P>>>();
		entities = new HashMap<E, Entity<E>>();
		properties = new HashMap<String, Property<P>>();
	}
	
	public Context(String contextID) {
		this();
		setContextID(contextID);
	}
	
	private Entity<E> createEntity(E entity) {
		if (!entities.containsKey(entity)) {
			log.debug("Creating new Entity:" + entity);
			entities.put(entity, new Entity<E>(entity));
		}
		return entities.get(entity);
	}
	
	private String createPropertyString(E entity, P property) {
		return entity.toString() + "_" + property.toString();
	}
	
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (Entity<E> entity : getEntities()) {
			str.append(entity.getEntity().toString() + " (");
			for (Property<P> prop : getPropertiesForEntity(entity.getEntity())) {
				str.append(prop.getProperty());
				str.append(",");
			}
			str.append(")\n");
		}
		
		return str.toString();
	}
	
	
	/*
	 * properties should not be shared across all entities, e.g.:
	 * "redness" of obj1 has a different confidence than that of obj2.
	 */
	private Property<P> createProperty(E entity, P property) {
		String pString = createPropertyString(entity, property);
		if (!properties.containsKey(pString)) {
			properties.put(pString, new Property<P>(property));
			log.debug("Creating new Property: " + property + " for Entity: " + entity);
		}
		
		return properties.get(pString);
	}
	
	private void removeProperty(E entity, P property) {
		getEntity(entity).remove(createProperty(entity, property));
		String pString = createPropertyString(entity, property);
		properties.remove(pString);
	}
	
	private Property<P> createProperty(E entity, P property, Double confidence) {
		Property<P> newProp = createProperty(entity, property);
		newProp.setConfidence(confidence);
		return newProp;
	}
	
	public void addEntity(E entity) {
		if (!entitiesAndTheirProperties.containsKey(createEntity(entity)))
			entitiesAndTheirProperties.put(createEntity(entity), new Properties<Property<P>>());
	}
	
	public void setEntity(E entity, Properties<Property<P>> properties) {
		entitiesAndTheirProperties.put(createEntity(entity), properties);
	}

	public void addPropertyToEntity(E entity, P property) {
		addEntity(entity);
		getEntity(entity).add(createProperty(entity, property));
	}
	
	public void addPropertyToEntity(E entity, P property, Double confidence) {
		addEntity(entity);
		getEntity(entity).add(createProperty(entity, property, confidence));
	}

	private Properties<Property<P>> getEntity(E entity) {
		if (!entitiesAndTheirProperties.containsKey(createEntity(entity)))
				return null;
		return entitiesAndTheirProperties.get(createEntity(entity));
	}
	
	public Set<Entity<E>> getEntities() {
		synchronized(entitiesAndTheirProperties) {
			return entitiesAndTheirProperties.keySet();
		}
	}
	
	public Set<P> getPropertiesSet() {
		
		TreeSet<P> propertiesSet = new TreeSet<P>();
		
		for (Entity<E> entity : entitiesAndTheirProperties.keySet()) {
			for (Property<P> property : entitiesAndTheirProperties.get(entity))
				propertiesSet.add(property.getProperty());
		}
		return propertiesSet;
	}

	public boolean containsEntity(E entity) {
		return getEntity(entity) != null;
	}

	public Properties<Property<P>> getPropertiesForEntity(E entity) {
		return getEntity(entity);
	}
	
	public Property<P> getPropertyForEntity(E entity, P property) {
		if (!entityContainsProperty(entity, property))
				throw new SimpleUpdateModelException("Entity " + entity + " does not have the Property " + property);
		return createProperty(entity, property);
	}

	public boolean entityContainsProperty(E entity, P property) {
		return properties.containsKey(createPropertyString(entity, property));
	}
	
	public int entityHasNumProperties(E entity) {
		return getEntity(entity).size();
	}
	
	public void removeEntityAndProperties(E entity) {
		synchronized(entitiesAndTheirProperties) {
			if (entities.containsKey(entity))
				entities.remove(entity);
			if (properties.containsKey(entity)) 
				properties.remove(entity);
			Entity<E> toRemove = null;
			for (Entity<E> e : this.getEntities()) {
				if (e.getEntity().equals(entity))
					toRemove = e;
			}
			if (toRemove != null)
			entitiesAndTheirProperties.remove(toRemove);
		}
	}

	public void removePropertyForEntity(E entity, P property) {
		if (!entityContainsProperty(entity, property))
			throw new SimpleUpdateModelException("Entity " + entity + " does not have the Property " + property);
		removeProperty(entity, property);
	}
	
	public static Context<String, String> readFromXml(String path) {
		
		Context<String, String> context = new Context<String,String>();
		
		try {
			Document doc = XMLUtils.read(path);
			doc.getDocumentElement().normalize();
			NodeList nodeList = doc.getElementsByTagName("entity"); 
			
			for (int n=0; n<nodeList.getLength(); n++) {
				Node node = nodeList.item(n);
				if (node.getNodeType() == Node.ELEMENT_NODE) { 
					Element entity = (Element) node;
					String id = entity.getAttribute("id");
					NodeList propertiesList = entity.getElementsByTagName("property");
					for (int i=0; i<propertiesList.getLength(); i++) {
						Node pNode = propertiesList.item(i);
						if (pNode.getNodeType() == Node.ELEMENT_NODE) {
							Element pEntity = (Element) pNode;
							String prop = pEntity.getTextContent();
							String conf = pEntity.getAttribute("confidence");
							context.addPropertyToEntity(id, prop, Double.parseDouble(conf));
						}
					}
				}
			}
		} 
		catch (ParserConfigurationException e) {
			String msg = "Problem loading the xml file.";;
			log.debug(msg);
			e.printStackTrace();
			throw new SimpleUpdateModelException(msg);
		} 
		catch (SAXException e) {
			String msg = "Problem parsing the xml file.";;
			log.debug(msg);
			e.printStackTrace();
			throw new SimpleUpdateModelException(msg);
		} 
		catch (IOException e) {
			String msg = "XML file cannot be read...does it exist?";;
			log.debug(msg);
			e.printStackTrace();
			throw new SimpleUpdateModelException(msg);
		}
		
		return context;
	}

	public void writeToXml(String path)  {
		
		try {
		
			Document doc = XMLUtils.createDocument();
			
			Element rootElement = doc.createElement("context");
			doc.appendChild(rootElement);
			
			for (Entity<E> entity : entitiesAndTheirProperties.keySet()) {
				Element e = doc.createElement("entity");
				Attr eAttr = doc.createAttribute("id");
				eAttr.setValue(entity.getEntity().toString());
				e.setAttributeNode(eAttr);
				rootElement.appendChild(e);
				for (Property<P> property : entitiesAndTheirProperties.get(entity)) {
					Element p = doc.createElement("property");
					Text pTextNode = doc.createTextNode(property.getProperty().toString());	
					p.appendChild(pTextNode);
					Attr attr = doc.createAttribute("confidence");
					attr.setValue(property.getConfidence()+"");
					p.setAttributeNode(attr);
					e.appendChild(p);
				}
			}
			
			XMLUtils.write(doc, path);
		
		} 
		catch (ParserConfigurationException e1) {
			String msg = "Problem saving the xml file.";;
			log.debug(msg);
			e1.printStackTrace();
			throw new SimpleUpdateModelException(msg);
		} 
		catch (TransformerConfigurationException e) {
			String msg = "Problem saving the xml file.";
			log.debug(msg);
			e.printStackTrace();
			throw new SimpleUpdateModelException(msg);
		} 
		catch (TransformerException e) {
			String msg = "Problem saving the xml file.";
			log.debug(msg);
			e.printStackTrace();
			throw new SimpleUpdateModelException(msg);
		}
	}

	public String getContextID() {
		return contextID;
	}

	public void setContextID(String contextID) {
		this.contextID = contextID;
	}

	
}
	