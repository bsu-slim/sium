package sium.nlu.language.mapping;

import java.util.HashMap;

import sium.nlu.context.Properties;
import sium.nlu.context.Property;
import sium.nlu.language.LingEvidence;
import sium.nlu.stat.Distribution;

public class ExampleMapping implements Mapping<String> {
	
	/*
	 * This is an example instantiation of Mapping, where some properties and words have been observed 
	 * to co-occur together. 
	 */
	
	// property -> word -> prob
	HashMap<String, Distribution<String>> cooccurances;
	
	
	public ExampleMapping() {
		cooccurances = new HashMap<String, Distribution<String>>();
		setup();
	}
	
	public void clear() {
	}

	private void setup() {
		
		String prop = "red";
		addMapping(prop);
		addWord(prop, "the", 1.0);
		addWord(prop, "red", 5.0);
		addWord(prop, "cross", 1.0);
		addWord(prop, "corner", 1.0);
		addWord(prop, "blue", 0.1);
		normalize(prop);
		
		prop = "blue";
		addMapping(prop);
		addWord(prop, "the", 1.0);
		addWord(prop, "red", 0.1);
		addWord(prop, "cross", 1.0);
		addWord(prop, "corner", 1.0);
		addWord(prop, "blue", 5.0);
		normalize(prop);
		
		prop = "L";
		addMapping(prop);
		addWord(prop, "the", 1.0);
		addWord(prop, "red", 2.0);
		addWord(prop, "cross", 0.5);
		addWord(prop, "corner", 6.0);
		addWord(prop, "blue", 1.0);
		normalize(prop);
		
		prop = "X";
		addMapping(prop);
		addWord(prop, "the", 1.0);
		addWord(prop, "red", 1.0);
		addWord(prop, "cross", 6.0);
		addWord(prop, "corner", 0.5);
		addWord(prop, "blue", 1.0);
		normalize(prop);
		
	}

	private void addWord(String prop, String word, double d) {
		cooccurances.get(prop).addProbability(word, d);
	}

	private void addMapping(String prop) {
		cooccurances.put(prop, new Distribution<String>());
	}

	private void normalize(String prop) {
		cooccurances.get(prop).normalize();
	}
	
	@Override
	public void train() {

	}

	@Override
	public Distribution<String> applyEvidenceToContext(LingEvidence evidence) {
		Distribution<String> dist = new Distribution<String>();
		
		String word = evidence.getValue("word");
		
		for (String prop : cooccurances.keySet()) {
			dist.addProbability(prop, cooccurances.get(prop).getProbabilityForItem(word));
		}
		
		dist.normalize();
	
		return dist;
	}

	@Override
	public void addEvidenceToTrain(LingEvidence evidence,
			Properties<Property<String>> properties) {
		// TODO Auto-generated method stub
		
	}

}
