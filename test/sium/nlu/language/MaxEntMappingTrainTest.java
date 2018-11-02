package sium.nlu.language;

import org.junit.Test;

import sium.nlu.context.Context;
import sium.nlu.language.LingEvidence;
import sium.nlu.language.mapping.ExampleMapping;
import sium.nlu.language.mapping.MaxEntMapping;
import sium.nlu.stat.Distribution;
import sium.system.util.FileUtils;
import static org.junit.Assert.*;


public class MaxEntMappingTrainTest {
	
	@Test
	public void testMaxEntTraining() {
		
		MaxEntMapping mapping = new MaxEntMapping("temp.txt");
		
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
		
		// no errors? Hooray! We won't conduct any asserts here.
	}


}
