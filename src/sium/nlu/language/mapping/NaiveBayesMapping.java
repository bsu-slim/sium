package sium.nlu.language.mapping;

import java.util.ArrayList;

import de.daslaboratorium.machinelearning.classifier.BayesClassifier;
import de.daslaboratorium.machinelearning.classifier.Classification;
import sium.nlu.context.Properties;
import sium.nlu.context.Property;
import sium.nlu.language.LingEvidence;
import sium.nlu.stat.Distribution;

public class NaiveBayesMapping implements Mapping<String> {

	BayesClassifier<String, String> bayes;
	
	public NaiveBayesMapping() {
		bayes = new BayesClassifier<String, String>();
		bayes.setMemoryCapacity(1000000); //TODO: do this dynamically?
	}
	
	public void clear() {
		bayes.reset();
	}
	
	@Override
	public void train() {
//		It trains as it is fed training instances, so this does nothing
	}

	@Override
	public Distribution<String> applyEvidenceToContext(LingEvidence evidence) {
		
		Distribution<String> dist = new Distribution<String>();
		
		ArrayList<String> evidenceValues = getEvidenceValues(evidence);
		
		for (Classification<String, String> result : bayes.classifyDetailed(evidenceValues)) {
			dist.addProbability(result.getCategory(), result.getProbability());
		}
		return dist;
	}

	@Override
	public void addEvidenceToTrain(LingEvidence evidence, Properties<Property<String>> properties) {
		ArrayList<String> evidenceValues = getEvidenceValues(evidence);
		for (Property<String> property : properties) {
			addBayesEvidence(property, evidenceValues);
		}
	}


	private ArrayList<String> getEvidenceValues(LingEvidence evidence) {
		ArrayList<String> evidenceValues = new ArrayList<String>(); 
		for (String key : evidence.getKeys()) {
			evidenceValues.add(evidence.getValue(key));
		}
		return evidenceValues;
	}


	private void addBayesEvidence(Property<String> property, ArrayList<String> evidenceValues) {
		bayes.learn(property.getProperty(), evidenceValues);
	}


}
