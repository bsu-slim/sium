package sium.nlu.multi;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

import sium.nlu.stat.DistRow;
import sium.nlu.stat.Distribution;

/**
 * @author casey
 *
 * This class determines the distribution of points from a reference point, where the closer a point is, the higher the 
 * probability. 
 *
 */
public class DistanceDurationClassifier {

	private List<ObjectPoint> referencePoints;
	private Distribution<String> incrementalDist;
	private double incrementalTotal;
	
	public DistanceDurationClassifier(List<ObjectPoint> referencePoints) {
		setIncrementalDist(new Distribution<String>());
		setIncrementalTotal(0.0);
		setReferencePoints(referencePoints);
	}
	
	/**
	 * 
	 * Given a set of points and a reference point, this returns a normalised distribution for the set of points, where
	 * closer points receive higher probabilities. 
	 * 
	 * The following formula is used to determine the probability of a point (being a distance away from another point):
	 * 
	 * Pdist(Oi, Oj) = exp( -(xi - xj) ^ 2 / (2 stdev ^ 2 ) * exp( -(yi - yj) ^ 2 / (2 stdev ^ 2 )
	 * 
	 * Where a lower stdev would result in a steeper curve, thus only points very close to the reference point would
	 * recieve high probabilities. 
	 * 
	 * @param target
	 * @return Distribution
	 */
	public Distribution<String> classifyPoints(ObjectPoint target) {
		
		Distribution<String> dist = new Distribution<String>();
		
		Double std = findStandardDeviation(target);
		
		for (ObjectPoint p : getReferencePoints()) {
			double prob = pointCalc(target.getPoint().getX(), p.getPoint().getX(), std) * pointCalc(target.getPoint().getY(), p.getPoint().getY(), std);
			dist.addProbability(p.getID(), prob);
		}
		dist.normalize();
		return dist;
	}
	
	private Distribution<String> getUpdatedDistribution(List<ObjectPoint> targets) {
		Distribution<String> newDist = new Distribution<String>();
		for (ObjectPoint target : targets) {
			
			Distribution<String> pointDist = classifyPoints(target); // get distribution with current point as target
			
			for (ObjectPoint p : getReferencePoints()) {
				newDist.setProbabilityForItem(p.getID(), newDist.getProbabilityForItem(p.getID()) + pointDist.getProbabilityForItem(p.getID()) * target.getDuration());
			}
		}
		return newDist;
	}
	
	/**
	 * This returns a distribution over points, given a set of points and a reference point, based on the duration (e.g., of gaze
	 * or deixis intervals). The amount of mass awarded to each point is determined by its probability as defined by classifyPoints. 
	 * 
	 * @param targets
	 * @return Distribution
	 */
	public Distribution<String> classifyPointsWithDuration(List<ObjectPoint> targets) {
		
		Distribution<String> newDist = getUpdatedDistribution(targets);
		
		double totalDuration = getTotalDuration(targets);
		
		return computeFinalDist(newDist, totalDuration);
	}
	
	private Distribution<String> computeFinalDist(Distribution<String> newDist, double totalDuration) {
		Distribution<String> finalDist = new Distribution<String>();
		for (DistRow<String> obj : newDist.getDistribution()) {
			finalDist.addProbability(obj.getEntity(), obj.getProbability() / totalDuration);
		}
		return finalDist;
	}

	public Distribution<String> incrementalClassifyWithDuration(List<ObjectPoint> targets) {
		
		Distribution<String> newIncrement = getUpdatedDistribution(targets);
		
		Distribution<String> combinedDist = new Distribution<String>();
		if (!getIncrementalDist().isEmpty()) {
//			when there has been a previous increment and this is adding to it
			for (ObjectPoint p : getReferencePoints()) {
				combinedDist.addProbability(p.getID(), newIncrement.getProbabilityForItem(p.getID()) + getIncrementalDist().getProbabilityForItem(p.getID()));
			}
		}
		else {
//			when it's the first increment
			combinedDist = newIncrement;
		}
		
		setIncrementalDist(combinedDist);
		setIncrementalTotal(getIncrementalTotal() + getTotalDuration(targets));
		
		return computeFinalDist(combinedDist, getIncrementalTotal());
	}
	
	

	/**
	 * Sum the total duration of a list of points
	 * 
	 * @param points
	 * @return
	 */
	private double getTotalDuration(List<ObjectPoint> points) {
		double duration = 0.0;
		for (ObjectPoint p : points) {
			duration += p.getDuration();
		}
		return duration;
	}

	private double pointCalc(double a, double b, Double std) {
		return Math.exp( - diffsq(a, b) / (2.0 * Math.pow(std, 2.0)));
	}

	public Double findStandardDeviation(ObjectPoint target) {
		LinkedList<Double> distances = new LinkedList<Double>();
		for (ObjectPoint p : getReferencePoints()) {
			double dist = euclideanDistance(target, p.getPoint());
			distances.add(dist);
		}
		double mean = calculateMean(distances);
		double std = 0.0;
		for (double dist : distances) {
			std += diffsq(dist, mean);
		}
		return Math.sqrt(std / (double) getReferencePoints().size());
	}

	private double euclideanDistance(ObjectPoint target, Point p) {
		return Math.sqrt(diffsq(target.getPoint().getX(), p.getX()) + diffsq(target.getPoint().getX(), p.getY()));
	}

	private double calculateMean(LinkedList<Double> nums) {
		double mean = 0.0;
		for (Double num : nums) {
			mean += num;
		}
		return mean / (double) nums.size();
	}
	
	private double diffsq(double a, double b) {
		return Math.pow(a - b, 2.0);
	}

	public List<ObjectPoint> getReferencePoints() {
		return referencePoints;
	}

	public void setReferencePoints(List<ObjectPoint> referencePoints) {
		this.referencePoints = referencePoints;
	}

	public Distribution<String> getIncrementalDist() {
		return incrementalDist;
	}

	public void setIncrementalDist(Distribution<String> incrementalDist) {
		this.incrementalDist = incrementalDist;
	}

	public double getIncrementalTotal() {
		return incrementalTotal;
	}

	public void setIncrementalTotal(double incrementalTotal) {
		this.incrementalTotal = incrementalTotal;
	}
	
}
