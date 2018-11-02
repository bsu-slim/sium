package sium.nlu.language;


import static org.junit.Assert.*;

import org.junit.Test;

import sium.nlu.context.Context;
import sium.nlu.grounding.Grounder;
import sium.nlu.language.mapping.NaiveBayesMapping;
import sium.nlu.stat.Distribution;

public class NaiveBayesMappingTest {
	
	@Test
	public void testNBModel() {
		
//		set up the mapping P(U|R)
		NaiveBayesMapping mapping = new NaiveBayesMapping();
		
//		training context
		Context<String,String> context = new Context<String,String>();
		
		String entity = "tile-1";
		context.addPropertyToEntity(entity, "red");
		context.addPropertyToEntity(entity, "X");
		
		entity = "tile-2";
		context.addPropertyToEntity(entity, "blue");
		context.addPropertyToEntity(entity, "L");
		
		LingEvidence ev0 = new LingEvidence();
		
		//add tile-1 reference
		ev0.addEvidence("word", "the");
		mapping.addEvidenceToTrain(ev0, context.getPropertiesForEntity("tile-1"));
		
		ev0.addEvidence("word", "red");
		mapping.addEvidenceToTrain(ev0, context.getPropertiesForEntity("tile-1"));
		
		ev0.addEvidence("word", "cross");
		mapping.addEvidenceToTrain(ev0, context.getPropertiesForEntity("tile-1"));
		
		
		//add tile-2 reference
		ev0.addEvidence("word", "the");
		mapping.addEvidenceToTrain(ev0, context.getPropertiesForEntity("tile-2"));
		
		ev0.addEvidence("word", "blue");
		mapping.addEvidenceToTrain(ev0, context.getPropertiesForEntity("tile-2"));
		
		ev0.addEvidence("word", "corner");
		mapping.addEvidenceToTrain(ev0, context.getPropertiesForEntity("tile-2"));
		
		mapping.train();
		
		
//		Evaluation
		
		context = new Context<String,String>();
		
		entity = "tile-1";
		context.addPropertyToEntity(entity, "red");
		context.addPropertyToEntity(entity, "X");
		
		entity = "tile-2";
		context.addPropertyToEntity(entity, "red");
		context.addPropertyToEntity(entity, "L");
		
//		utterance: "the red cross"
		
//		set up grounder 
		Grounder<String, String> grounder = new Grounder<String, String>();
	
//		word 0: "the"
		ev0 = new LingEvidence();
		ev0.addEvidence("word", "the");
		Distribution<String> currentDist = mapping.applyEvidenceToContext(ev0);
		grounder.groundIncrement(context, currentDist);
		
//		word 1: "red"
		LingEvidence ev1 = new LingEvidence();
		ev1.addEvidence("word", "red");
		currentDist = mapping.applyEvidenceToContext(ev1);
		grounder.groundIncrement(context, currentDist);
		
//		word 2: "cross"
		LingEvidence ev2 = new LingEvidence();
		ev2.addEvidence("word", "cross");
		currentDist = mapping.applyEvidenceToContext(ev2);
		grounder.groundIncrement(context, currentDist);
		
//		after the final word, make sure it referred to the correct object
		assertTrue(grounder.getPosterior().getArgMax().getEntity().equals("tile-1"));
		
//		just make sure it's the clear winner, don't worry about the actual prob in case MaxEnt settings are changed
//		assertTrue(grounder.getPosterior().getArgMax().getProbability() > 0.5);

	}
	

}
