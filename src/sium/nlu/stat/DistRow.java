package sium.nlu.stat;



public class DistRow<E> implements Comparable<Object> {
		
		private double probability;
		private E entity;
		
		public DistRow(E o, double d) {
			this.entity = o;
			this.probability = d;
		}
		
		public void setProbability(double prob) {
			this.probability = prob;
		}

		@Override
		public int compareTo(Object o) {
			DistRow<E> dr = (DistRow<E>)o;
			if (dr.getProbability() > this.getProbability())
				return 1;
			else
				return -1;
			//return Double.compare(dr.getProbability(), this.getProbability());
		}
		
		public double getProbability() {
			return this.probability;
		}
		
		public E getEntity() {
			return this.entity;
		}
		
		public String toString() {
			return getEntity().toString() + " "+ getProbability();
		}
	}