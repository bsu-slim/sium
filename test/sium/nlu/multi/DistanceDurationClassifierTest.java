package sium.nlu.multi;

import static org.junit.Assert.*;

import java.awt.Point;
import java.util.LinkedList;

import junit.framework.*;

import org.junit.Test;

import sium.nlu.stat.Distribution;

public class DistanceDurationClassifierTest {

	@Test
	public void test() {
		
		LinkedList<ObjectPoint> points = new LinkedList<ObjectPoint>();
		points.add(new ObjectPoint("obj1", 4,4));
		points.add(new ObjectPoint("obj2", 5,3));
		points.add(new ObjectPoint("obj3", 6,4));
		points.add(new ObjectPoint("obj4", 3,7));
		points.add(new ObjectPoint("obj5", 1,1));

		ObjectPoint target = new ObjectPoint("", 4, 4, 0.5);
		
		DistanceDurationClassifier ddc = new DistanceDurationClassifier(points);
		
		double std =  ddc.findStandardDeviation(target);
		assertTrue(std == 1.4552853290177885);
		
		Distribution<String> dist = ddc.classifyPoints(target);
		
		assertTrue(dist.getItem(1).getProbability() == 0.29400757419802609);
		
		LinkedList<ObjectPoint> pointsList = new LinkedList<ObjectPoint>();
		pointsList.add(target);
		pointsList.add(new ObjectPoint("", 2,3,0.7));
		dist = ddc.classifyPointsWithDuration(pointsList);
		
		assertTrue(dist.getItem(1).getProbability() == 0.2502087838414964);
		
		Distribution<String> toCombine = new Distribution<String>();
		toCombine.addProbability("obj1", 0.3);
		toCombine.addProbability("obj2", 0.15);
		toCombine.addProbability("obj3", 0.05);
		toCombine.addProbability("obj4", 0.05);
		toCombine.addProbability("obj5", 0.05);
		toCombine.normalize();

		toCombine.combineDistribution(dist, 0.5);
		assertTrue(toCombine.getItem(1).getProbability() == 0.22366687005076186);
		assertTrue(toCombine.getItem(1).getEntity().equals("obj2"));
		
	}
	
	@Test 
	public void incrementalTest() {
		LinkedList<ObjectPoint> points = new LinkedList<ObjectPoint>();
		points.add(new ObjectPoint("obj1", 4,4));
		points.add(new ObjectPoint("obj2", 5,3));
		points.add(new ObjectPoint("obj3", 6,4));
		points.add(new ObjectPoint("obj4", 3,7));
		points.add(new ObjectPoint("obj5", 1,1));

		ObjectPoint target = new ObjectPoint("", 4, 4, 0.5);
		
		DistanceDurationClassifier ddc = new DistanceDurationClassifier(points);
		
		
		LinkedList<ObjectPoint> pointsList = new LinkedList<ObjectPoint>();
		pointsList.add(target);
		Distribution<String> dist = ddc.incrementalClassifyWithDuration(pointsList);
		
		assertTrue(dist.getItem(1).getProbability() == 0.2940075741980261);
		
		Distribution<String> toCombine = new Distribution<String>();
		toCombine.addProbability("obj1", 0.3);
		toCombine.addProbability("obj2", 0.15);
		toCombine.addProbability("obj3", 0.05);
		toCombine.addProbability("obj4", 0.05);
		toCombine.addProbability("obj5", 0.05);
		toCombine.normalize();
		
		pointsList = new LinkedList<ObjectPoint>();
		pointsList.add(new ObjectPoint("", 2,3,0.7));

		dist = ddc.incrementalClassifyWithDuration(pointsList);
		
		assertTrue(ddc.getIncrementalTotal() == 1.2);
		
		toCombine.combineDistribution(dist, 0.5);
		
		assertTrue(toCombine.getItem(1).getProbability() == 0.22366687005076186);
		assertTrue(toCombine.getItem(1).getEntity().equals("obj2"));
	}
	
	
}
