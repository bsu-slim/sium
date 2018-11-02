package sium.nlu.language.mapping;

import java.util.HashMap;
import java.util.TreeSet;

import sium.nlu.context.Properties;
import sium.nlu.context.Property;
import sium.nlu.language.LingEvidence;
import sium.nlu.stat.Distribution;

public class CoocurrenceMapping implements  Mapping<String> {
	
	HashMap<String,HashMap<String,Double>> mapping = new HashMap<String,HashMap<String,Double>>();
	TreeSet<String> vocabulary = new TreeSet<String>();
	double alpha = 0.1;

	@Override
	public void train() {
//		normalise
		for (String prop : mapping.keySet()) {
			double total = 0.0;
			for (String word : mapping.get(prop).keySet()) total += mapping.get(prop).get(word);
			mapping.get(prop).put("<TOTAL>", total);
//			for (String word : mapping.get(prop).keySet()) mapping.get(prop).put(word, mapping.get(prop).get(word) / total);
		}
	}

	@Override
	public Distribution<String> applyEvidenceToContext(LingEvidence evidence) {
		Distribution<String> dist = new Distribution<String>();
		
		String word = evidence.getValue("w1");
		double d = (double) vocabulary.size();
		
		for (String prop : mapping.keySet()) {
			double n = mapping.get(prop).get("<TOTAL>");
			double prob =  alpha / (alpha * d + n);
			if (mapping.get(prop).containsKey(word)) 
				prob = (alpha + mapping.get(prop).get(word)) / (alpha * d + n);
			dist.addProbability(prop, prob);
		}
		
		
		return dist;
	}

	@Override
	public void addEvidenceToTrain(LingEvidence evidence,
			Properties<Property<String>> properties) {
		
		for (Property<String> prop : properties) {
			if (!mapping.containsKey(prop.getProperty())) 
				mapping.put(prop.getProperty(), new HashMap<String,Double>());

//			for (String word : evidence.getKeys()){
			String word = evidence.getValue("w1");
			vocabulary.add(word);
				if (!mapping.get(prop.getProperty()).containsKey(word))
					mapping.get(prop.getProperty()).put(word, 0.0);
				double temp = mapping.get(prop.getProperty()).get(word);
				mapping.get(prop.getProperty()).put(word, temp + 1.0);
				
//			}
		}
	}

	@Override
	public void clear() {
		mapping.clear();
	}

}
