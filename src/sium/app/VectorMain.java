package sium.app;

import java.util.ArrayList;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;

import sium.nlu.context.Context;
import sium.nlu.grounding.Grounder;
import sium.nlu.language.LingEvidence;
import sium.nlu.language.mapping.Mapping;
import sium.nlu.language.mapping.NaiveBayesMapping;
import sium.nlu.stat.Distribution;

public class VectorMain {

	static Logger log = Logger.getLogger(VectorMain.class.getName());


	private void run() {
		
		
		// Mapping is what we give all the training information: "context" (i.e., properties) and the RE that goes along with it
		Mapping<String> mapping = new NaiveBayesMapping();
		
		
		
		// TRAINING
		
		String vector = "(20681200,0.5714,0.4286,0.1767,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0.0,0,0,0)"; // vector of the referred object
		String utterance = "walk through the same trees"; // utterance/RE for that referred object
		
		
		// First, we need a Context where everything is happening. 
		Context<String,String> context = new Context<String,String>();
		
		// put the vectors into that context
		addVectorToContext(context, vector);
		
		// Second, we need some linguistic evidence (in this case, just the words in the utterance/RE)
		ArrayList<LingEvidence> ling = getLingEvidenceForUtterance(utterance);
		
		// for training we only care about the context for the referred object
		for (LingEvidence ev: ling) {
			mapping.addEvidenceToTrain(ev, context.getPropertiesForEntity("20681200")); 
		}
		
		mapping.train();
		
		
		// EVALUATION
		
		
		// need all vectors of a scene to be put into the context
		String vector1 = "(162241900,0.8333,0.6667,0.0865,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0.0,0,0,1)"; // vector of an object
		String vector2 = "(61287128,0.5,0.6667,0.0129,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,1.0,0,1,0)"; // vector of another object
		// ...
		String vectorN = "(372703321,0.8333,1.0,0.7613,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0.0,0,0,0)"; // vector of another object
		
		context = new Context<String,String>();
		
		addVectorToContext(context, vector1);
		addVectorToContext(context, vector2);
		// ...
		addVectorToContext(context, vectorN);
		
		utterance = "i cross another cycle path"; // utterance/RE for that referred object
		
		ling = getLingEvidenceForUtterance(utterance);
		
		// now, for application/evaluation we need a Grounder
		Grounder<String, String> grounder = new Grounder<String, String>();
		
		// this this point an alternative initial prior could be set: grounder.setPrior(prior);
		
		// step through each word and ground the utterance/RE to the context--obtain a distribution over potentially referred entities
		for (LingEvidence ev : ling) {
			Distribution<String> currentDist = mapping.applyEvidenceToContext(ev);
			grounder.groundIncrement(context,currentDist);
		}
		
		System.out.println("distribution over entities: " +grounder.getPosterior());
		System.out.println("argmax entity: " + grounder.getPosterior().getArgMax().getEntity());
		
	}
	
	
	
	private ArrayList<LingEvidence> getLingEvidenceForUtterance(String utterance) {
		ArrayList<LingEvidence> ling = new ArrayList<LingEvidence>();
		
		String[] words = utterance.trim().split(" ");
		// we'll use trigrams
		String prevWord = "<s>";
		String prevPrevWord = prevWord;
		
		for (int i=0; i<words.length; i++) {
			String word = words[i];
			LingEvidence ev = new LingEvidence();
			ev.addEvidence("w1", word);
			ev.addEvidence("w2", prevWord);
			ev.addEvidence("w3", prevPrevWord);
			ling.add(ev);
			prevPrevWord = prevWord;
			prevWord = word;
		}
		
		return ling;
	}


	private void addVectorToContext(Context<String, String> context, String vector) {
		
		String[] vec = vector.substring(1, vector.length()-1).split(",");
		
		String entityID = vec[0];
		String featureName = "f";
		for (int i=1; i<vec.length; i++) { // start with 1 because 0is the entity's ID
			double prob = Double.parseDouble(vec[i]);
			if (prob == 0) continue; // ignore ones with zero probability
			context.addPropertyToEntity(entityID, featureName+i, prob);
		}
	}
	
	
	
	public static void main(String[] args) {
		BasicConfigurator.configure(new NullAppender());
		
		new VectorMain().run();
	}

}
