package sium.nlu.stat;

import java.util.ArrayList;
import java.util.TreeSet;

import org.apache.log4j.Logger;

public class Distribution<E> {
	static Logger log = Logger.getLogger(Distribution.class.getName());
	
	private TreeSet<DistRow<E>> distribution;
	private boolean isNormalized;
	public int numToPrint = 1;
	
	public Distribution() {
		distribution = new TreeSet<DistRow<E>>();
		setNormalized(false);
	}
	
	
	public void clear() {
		distribution.clear();
	}
	
	/*
	 * Copy constructor
	 */
	public Distribution(Distribution<E> copy) {
		distribution = new TreeSet<DistRow<E>>();
		setNormalized(false);
		for (DistRow<E> row : copy.getDistribution()) {
			this.addProbability(row.getEntity(), row.getProbability());
		}
	}
	
	public boolean containsEntity(E entity) {
		for (DistRow<E> row : getDistribution()){
			if (row.getEntity().equals(entity))
				return true;
		}
		return false;
	}
	
	public int findRank(E object) {
		int i=1;
		for (DistRow<E> row : getDistribution()){
			if (row.getEntity().equals(object))
				return i;
			i++;
		}
		return -1;
	}

	public TreeSet<DistRow<E>> getDistribution() {
		return distribution;
	}
	
	public TreeSet<DistRow<E>> getTopN(int n) {
		TreeSet<DistRow<E>> newDist = new TreeSet<DistRow<E>>(getDistribution());
		
		while (newDist.size() > n) newDist.pollLast();
		return newDist;
	}
	
	
	
	public ArrayList<Double> getMargins() {
		ArrayList<Double> margins = new ArrayList<Double>();
		
		double prevProb = -1;
		
		for (DistRow<E> row : getDistribution()) {
			if (prevProb != -1) {
				margins.add(prevProb - row.getProbability());
			}
			prevProb = row.getProbability();
		}
		
		return margins;
	}
	
	public void addProbability(E entity, double prob) {
		 getDistribution().add(new DistRow<E>(entity, prob));
	}
	
	public void combineProbability(E entity, double prob) {
		combineProbability(entity, prob, 1.0);
	}
	
	public void combineProbability(E entity, double prob, double weight) {
		TreeSet<DistRow<E>> newDist = new TreeSet<DistRow<E>>();
		boolean found = false;
		while (!distribution.isEmpty()) {
			DistRow<E> row = distribution.pollFirst();
			Double newProb = row.getProbability(); 
			if (row.getEntity().equals(entity)) {
				newProb = newProb * weight + (1-weight) * prob;
				found = true;
			}
			newDist.add(new DistRow<E>(row.getEntity(), newProb));
		}
		if (!found) 
			newDist.add(new DistRow<E>(entity, prob));
		distribution = newDist;
		
	}
	
	public void setProbabilityForItem(E entity, double prob) {
		TreeSet<DistRow<E>> newDist = new TreeSet<DistRow<E>>();
		boolean found = false;
		while (!distribution.isEmpty()) {
			DistRow<E> row = distribution.pollFirst();
			Double newProb = row.getProbability(); 
			if (row.getEntity().equals(entity)) {
				newProb = prob;
				found = true;
			}
			newDist.add(new DistRow<E>(row.getEntity(), newProb));
		}
		if (!found) 
			newDist.add(new DistRow<E>(entity, prob));
		distribution = newDist;
	}
	
	
	public DistRow<E> getArgMax() {
		return  getDistribution().first();
	}
	
	public DistRow<E> getArgMin() {
		return  getDistribution().last();
	}
	
	public DistRow<E> pop() {
		return getDistribution().pollFirst();
	}
	
	public boolean isEmpty() {
		return getDistribution().isEmpty();
	}
	
	public DistRow<E> getItem(int i) {
		int j = 0;
		for (DistRow<E> distRow : getDistribution()) {
			if (j == i) {
				return distRow;
			}
			j++;
		}
		return null;
	}

	public int size() {
		return  getDistribution().size();
	}
	
	public void normalize() {
		double denom = 0.0;
		for (DistRow<E> d : getDistribution()) {
			denom += d.getProbability();
		}
		
		TreeSet<DistRow<E>> newDist = new TreeSet<DistRow<E>>();
		while (size() > 0) {
			DistRow<E> top = getDistribution().pollFirst();
			newDist.add(new DistRow<E>(top.getEntity(), top.getProbability() / denom));
		}
		distribution = newDist;
	}
	
	public boolean checkSumsToOne() {
		double sum = 0.0;
		for (DistRow<E> d : getDistribution()) {
			sum += d.getProbability();
		}
		if (sum == 1.0)
			return true;
		return false;
	}

	public boolean isNormalized() {
		return isNormalized;
	}

	public void setNormalized(boolean isNormalized) {
		this.isNormalized = isNormalized;
	}
	
	public void print() {
		System.out.println(this.distribution);
	}
	
	public double getProbabilityForItem(String item) {
		for (DistRow<E> row : getDistribution()) {
			
			if (item.equals(row.getEntity().toString())) {
				return row.getProbability();
			}
		}
		return 0.0;
	}
	
	public String toString() {
		String out = "";
		for (DistRow<E> row : getDistribution()) {
			out += row.getEntity().toString()+ ":" + row.getProbability() + ",";
			//break; // <-- when you want the whole distribution, comment out this break and the comment in the above row
		}
		out = out.substring(0,out.length()-1);
		return out.trim();
	}
	
	public double getEntropy() {
		double entropy = 0.0;
		
		for (DistRow<E> val : this.getDistribution()) {
			entropy += val.getProbability() * Math.log(val.getProbability()); 
		}
		if (Double.isNaN(entropy))
			return Double.POSITIVE_INFINITY;
		return -entropy;
	}

	public void combineDistribution(Distribution<E> dist) {
		combineDistribution(dist, 0.5);
	}
	
	public void combineDistribution(Distribution<E> dist, double weight) {
		if (dist.isEmpty()) return;
		while (!dist.isEmpty()) {
			DistRow<E> row = dist.pop();
			this.combineProbability(row.getEntity(), row.getProbability(), weight);
		}
	}


	public double getConfidence() {
		return getArgMax().getProbability();
	}
}
