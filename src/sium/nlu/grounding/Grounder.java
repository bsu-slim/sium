package sium.nlu.grounding;


import java.util.LinkedList;

import org.apache.log4j.Logger;

import sium.nlu.context.Context;
import sium.nlu.context.Entity;
import sium.nlu.context.Property;
import sium.nlu.stat.Distribution;

public class Grounder<E,P extends Comparable<P>> {
	
	static Logger log = Logger.getLogger(Grounder.class.getName());
	
	protected Distribution<E> prior;
	protected Distribution<E> posterior;
	protected Distribution<E> combinedProperties;
	protected LinkedList<Distribution<E>> history;
	
	
	public Grounder(Distribution<E> prior) {
		init();
		setPrior(prior);
	}
	
	public Grounder() {
		init();
	}

	protected void init() {
		setPrior(new Distribution<E>());
		setPosterior(new Distribution<E>());
		combinedProperties = new Distribution<E>();
		setHistory(new LinkedList<Distribution<E>>());
	}


	public void groundIncrement(Context<E, P> context, Distribution<E> mappedDist) {
		
//		sum (r in R): P(U|R) * P(R|I)
//		I = context objects
//		R = context object properties
//		U = mappedDist results
		
		if (prior.isEmpty()) {
			log.debug("Grounding increment.");
		}
		
		Distribution<E> newDist = new Distribution<E>();
		
		for (Entity<E> entity : context.getEntities()) {
			double sum = 0.000001; //"smoothing" for those cases where there might not be any probability mass
			double numProperties = context.entityHasNumProperties(entity.getEntity());
			for (Property<P> property : context.getPropertiesForEntity(entity.getEntity())) {
				sum = sum + ((mappedDist.getProbabilityForItem(property.getProperty().toString()) * property.getConfidence()) / numProperties); 
			}
			
//			treat an empty prior as a uniform distribution
			if (prior.isEmpty()) {
				newDist.addProbability(entity.getEntity(), sum);
			}
			else {
				newDist.addProbability(entity.getEntity(), sum * prior.getProbabilityForItem(entity.getEntity().toString()));
			}
		}
		
//		newDist.normalize();
		combineProperties(mappedDist);
		addToHistory(newDist);
		setPosterior(newDist);
		setPrior(newDist);
	}

	public void addToHistory(Distribution<E> newDist) {
		this.history.add(new Distribution<E>(newDist));
	}
	
	public void undoStep() {
		if (history.isEmpty()) return;
		setPosterior(history.pollLast());
		if (history.isEmpty())
			setPrior(new Distribution<E>());
		else
			setPrior(history.peekLast());
	}

	public Distribution<E> getPrior() {
		return prior;
	}

	public void setPrior(Distribution<E> prior) {
		this.prior = prior;
	}

	public Distribution<E> getPosterior() {
		return posterior;
	}

	public void setPosterior(Distribution<E> posterior) {
		this.posterior = posterior;
	}
	
	private void setHistory(LinkedList<Distribution<E>> history) {
		this.history = history; 
	}
	
	public Distribution<E> getCombinedProperties() {
		return this.combinedProperties;
	}
	
	private void combineProperties(Distribution<E> p) {
		this.combinedProperties.combineDistribution(p);
	}

	public void clear() {
		getPrior().clear();
		getPosterior().clear();
		this.combinedProperties.clear();
		history.clear();
	}


}
