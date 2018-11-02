package sium.nlu.language.mapping;

import java.util.HashMap;

import sium.nlu.context.Properties;
import sium.nlu.context.Property;
import sium.nlu.language.LingEvidence;
import sium.nlu.stat.Distribution;

public class StringMatchMapping implements Mapping<String> {
	
	private HashMap<String,String> wordsAndProps;
	
	public StringMatchMapping() {
		wordsAndProps = new HashMap<String,String>();
		
		wordsAndProps.put("rot", "red");
		wordsAndProps.put("rote", "red");
		wordsAndProps.put("roten", "red");
		wordsAndProps.put("rotes", "red");
		
		wordsAndProps.put("blau", "blue");
		wordsAndProps.put("blaue", "blue");
		wordsAndProps.put("blaues", "blue");
		wordsAndProps.put("blauen", "blue");
		
		wordsAndProps.put("gelb", "yellow");
		wordsAndProps.put("gelbe", "orange");
		wordsAndProps.put("gelbes", "yellow");
		wordsAndProps.put("gelben", "yellow");
		
		wordsAndProps.put("grün", "green");
		wordsAndProps.put("grüne", "green");
		wordsAndProps.put("grünes", "green");
		wordsAndProps.put("grünen", "green");
		
		wordsAndProps.put("grau", "gray");
		wordsAndProps.put("graue", "gray");
		wordsAndProps.put("graues", "gray");
		wordsAndProps.put("grauen", "gray");
		
		wordsAndProps.put("violette", "magenta");
		wordsAndProps.put("lila", "magenta");
		wordsAndProps.put("lilane", "magenta");
		wordsAndProps.put("pink", "magenta");
		
		wordsAndProps.put("hellblau", "cyan");
		wordsAndProps.put("hellblaue", "cyan");
		wordsAndProps.put("hellblaues", "cyan");
		wordsAndProps.put("hellblauen", "cyan");
		wordsAndProps.put("türkise", "cyan");
		
		wordsAndProps.put("es", "selected");
		wordsAndProps.put("sie", "selected");
		
		wordsAndProps.put("rechts", "grid2:grid4");
		wordsAndProps.put("oben", "grid1:grid2");
		wordsAndProps.put("links", "grid1:grid3");
		wordsAndProps.put("unten", "grid2:grid3");
		
		wordsAndProps.put("$f", "f");
		wordsAndProps.put("$u", "u");
		wordsAndProps.put("c", "u");
		wordsAndProps.put("$w", "w");
		wordsAndProps.put("$t", "t");
		wordsAndProps.put("krücke", "y");
		wordsAndProps.put("kreuz", "x");
		wordsAndProps.put("plus", "x");
		wordsAndProps.put("balken", "i");
		wordsAndProps.put("eins", "i");
		wordsAndProps.put("schlange", "z");
		wordsAndProps.put("spalte", "n");
		wordsAndProps.put("gewehr", "n");
		wordsAndProps.put("winkel", "v");
		wordsAndProps.put("dreieck", "v");
		wordsAndProps.put("bajonett", "u");
		wordsAndProps.put("hand", "p");
		wordsAndProps.put("handy", "p");
		
		
		
	}

	@Override
	public void train() {
		
	}

	@Override
	public Distribution<String> applyEvidenceToContext(LingEvidence evidence) {
		Distribution<String> dist = new Distribution<String>();
		String word = evidence.getValue("w1");
		if (wordsAndProps.containsKey(word)) {
			String prop = wordsAndProps.get(word);
			if (prop.contains(":")) {
				String[] props = prop.split(":");
				for (int i=0; i<props.length; i++) {
					dist.addProbability(props[i], 1.0);
				}
			}
			else {
				dist.addProbability(prop, 1.0);	
			}
			
		}
		
		return dist;
	}

	@Override
	public void addEvidenceToTrain(LingEvidence evidence, Properties<Property<String>> properties) {
		
	}

	@Override
	public void clear() {
		
	}

}
