package sium.nlu.language;

import static org.junit.Assert.*;

import org.junit.Test;

import sium.nlu.context.Context;
import sium.nlu.grounding.Grounder;
import sium.nlu.language.LingEvidence;
import sium.nlu.language.mapping.MaxEntMapping;
import sium.nlu.stat.Distribution;
import sium.system.util.FileUtils;

public class MaxEntMappingEvalTest {
	
	@Test
	public void testMaxEntMapping() {
		
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
		MaxEntMapping mapping = new MaxEntMapping("temp.txt");
		
		
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
		
//		just make sure it's the clear winner, don't worry about the actual prob in case MaxEnt settings are changed
//		assertTrue(grounder.getPosterior().getArgMax().getProbability() > 0.5);
		
//		clean up temp test files
		FileUtils.deleteFile("temp.txt");
		FileUtils.deleteFile("tempModel.txt");
	}

}
