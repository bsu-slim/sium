package sium.nlu.language.mapping;

import sium.nlu.context.Properties;
import sium.nlu.context.Property;
import sium.nlu.language.LingEvidence;
import sium.nlu.stat.Distribution;

public interface Mapping<E> {
	
	public void train();
	
	public Distribution<E> applyEvidenceToContext(LingEvidence evidence);

	void addEvidenceToTrain(LingEvidence evidence,	Properties<Property<String>> properties);

	public void clear();
	

}
