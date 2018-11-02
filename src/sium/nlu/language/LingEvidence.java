package sium.nlu.language;

import java.util.HashMap;
import java.util.Set;

import org.apache.log4j.Logger;

import sium.system.SimpleUpdateModelException;

public class LingEvidence {

	
	static Logger log = Logger.getLogger(LingEvidence.class.getName());
	/*
	 * This class is to contain whatever kind of linguistic evidence that will be grounded with the context.
	 * 
	 * Only Strings are allowed at the moment. We want to force designers to know just what evidence they are using
	 * 
	 * Extend this and override the getEvidenceString() if you want it to produce something different
	 */
	
	private HashMap<String,String> evidence;
	
	public LingEvidence(LingEvidence copy) {
		evidence = new HashMap<String,String>();
		for (String key : copy.getKeys()) {
			evidence.put(key, copy.getValue(key));
		}
	}
	
	public boolean hasKey(String key) {
		return evidence.containsKey(key);
	}
	
	public Set<String> getKeys() {
		return evidence.keySet();
	}
	
	public String getValue(String key) {
		if (!evidence.containsKey(key)) {
			String msg = "LingEvidence does not contain key " + key; 
			log.warn(msg);
			throw new SimpleUpdateModelException(msg);
		}
		return evidence.get(key);
	}
	
	public LingEvidence() {
		evidence = new HashMap<String,String>();
	}
	
	public void addEvidence(String key, String value) {
		evidence.put(key, value);
	}
	
	public void removeEvidence(String key) {
		evidence.remove(key);
	}
	
	public String getEvidenceString() {
		StringBuilder str = new StringBuilder();
		for (String key : evidence.keySet()) {
			str.append(String.format("%s=%s", key, evidence.get(key)));
			str.append(" ");
		}
		return str.toString().trim();
	}
	
	public String toString() {
		return getEvidenceString();
	}

}
