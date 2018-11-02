package sium.nlu.grounding;

import org.junit.Test;

import sium.nlu.context.Context;
import sium.nlu.language.LingEvidence;
import sium.nlu.language.mapping.ExampleMapping;
import sium.nlu.language.mapping.Mapping;
import sium.nlu.stat.Distribution;
import static org.junit.Assert.*;


public class GroundingTest {

	@Test
	public void groundingTest() {
		
//		set up the context P(R|I) with two objects, a red cross and a red corner
		Context<String,String> context = new Context<String,String>();

		String entity = "tile-1";
		context.addPropertyToEntity(entity, "red");
		context.addPropertyToEntity(entity, "X");
		
		entity = "tile-2";
		context.addPropertyToEntity(entity, "red");
		context.addPropertyToEntity(entity, "L");
		
		
		
//		utterance: "the red cross"
		
//		set up grounder 
		Grounder<String, String> grounder = new Grounder<String, String>();
		
//		set up the mapping P(U|R)
		ExampleMapping mapping = new ExampleMapping();		
		
//		word 0: "the"
		LingEvidence ev0 = new LingEvidence();
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
		
//		the following line will fail if any of the numbers in ExampleMapping are changed!
//		assertTrue(grounder.getPosterior().getArgMax().getProbability() == 0.8050241181427542);
		
//		revert to "the red"
		grounder.undoStep();
		
//		revert to "the"
		grounder.undoStep(); 
		
//		word 1: "red"
		ev1 = new LingEvidence();
		ev1.addEvidence("word", "red");
		currentDist = mapping.applyEvidenceToContext(ev1);
		grounder.groundIncrement(context, currentDist);
		
//		word 2: "cross"
		ev2 = new LingEvidence();
		ev2.addEvidence("word", "cross");
		currentDist = mapping.applyEvidenceToContext(ev2);
		grounder.groundIncrement(context, currentDist);
		
		
//		the results should be the same as before
		assertTrue(grounder.getPosterior().getArgMax().getEntity().equals("tile-1"));
		
//		assertTrue(grounder.getPosterior().getArgMax().getProbability() == 0.8050241181427542);
	}
	
	
}

