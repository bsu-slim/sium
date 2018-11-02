package sium.nlu.language;

import org.junit.Test;

import static org.junit.Assert.*;


public class LingEvidenceTest {
	
	@Test
	public void testLingEvidence() {
		
		
		LingEvidence ev = new LingEvidence();
		
		ev.addEvidence("word", "red");
		ev.addEvidence("bigram", "the_red");
		
		testEV(ev);

//		test a copy
		LingEvidence ev2 = new LingEvidence(ev);
		testEV(ev2);
		
	}
	
	private void testEV(LingEvidence ev) {
		assertTrue(ev.getEvidenceString().contains("word=red"));
		assertTrue(ev.getEvidenceString().contains("bigram=the_red"));
	}

}
