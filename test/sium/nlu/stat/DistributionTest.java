package sium.nlu.stat;

import static org.junit.Assert.*;
import org.junit.Test;

public class DistributionTest {
	
	@Test
	public void disttest() {
		Distribution<String> d = new Distribution<String>();
		d.addProbability("a",0.5);
		d.addProbability("b",0.4);
		d.addProbability("c",0.7);

		assertTrue(d.size() == 3);
		assertTrue(d.getArgMax().getEntity().equals("c"));
		assertTrue(d.getArgMax().getProbability() == 0.7);
		d.normalize();
		assertTrue(d.checkSumsToOne());
	}

}
