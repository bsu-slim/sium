package sium.app;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.varia.NullAppender;

import sium.nlu.context.Context;
import sium.nlu.context.Entity;
import sium.nlu.grounding.Grounder;
import sium.nlu.language.LingEvidence;
import sium.nlu.language.mapping.NaiveBayesMapping;
import sium.nlu.stat.Distribution;
import sium.system.util.PentoSqlUtils;

public class Negation {

	public static void main(String[] args) {
		
		BasicConfigurator.configure(new NullAppender()); // turn logging off for the demo
		
		try {
			
			PentoSqlUtils pento = new PentoSqlUtils();
			pento.createConnection();
			ArrayList<String> episodes = pento.getAllEpisodes();
			
			NaiveBayesMapping mapping = new NaiveBayesMapping();
			
			for (String episode : episodes) {
				Context<String,String> context = pento.getContext(episode);
				ArrayList<LingEvidence> ling = pento.getLingEvidence(episode);
				String gold = pento.getGoldPiece(episode);
				for (LingEvidence evidence : ling) {
					mapping.addEvidenceToTrain(evidence, context.getPropertiesForEntity(gold));
				}
			}
			mapping.train();
			
			
			
			for (String episode : episodes) {
				
				Context<String,String> context = pento.getContext(episode);
				System.out.println(context);

				Grounder<String, String> grounder = new Grounder<String, String>();
				
				
				LingEvidence ling = new LingEvidence();
				ling.addEvidence("w1", "nimm");
				Distribution<String> currentDist = mapping.applyEvidenceToContext(ling);
				grounder.groundIncrement(context, currentDist);
				
				ling = new LingEvidence();
				ling.addEvidence("w1", "das");
				currentDist = mapping.applyEvidenceToContext(ling);
				grounder.groundIncrement(context, currentDist);				

				ling = new LingEvidence();
				ling.addEvidence("w1", "grüne");
				currentDist = mapping.applyEvidenceToContext(ling);
				grounder.groundIncrement(negateContext(context), currentDist);
//				grounder.groundIncrement(context, currentDist);
				
				ling = new LingEvidence();
				ling.addEvidence("w1", "balken");
				currentDist = mapping.applyEvidenceToContext(ling);
				grounder.groundIncrement(negateContext(context), currentDist);
//				grounder.groundIncrement(context, currentDist);
				
				System.out.println(grounder.getPosterior());				
				
				
				
				break;
			}
			
			pento.closeConnection();
			
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		
		
	}

	private static Context<String, String> negateContext(
			Context<String, String> context) {
		Context<String,String> negated = new Context<String,String> ();
		
		Set<String> properties = context.getPropertiesSet();
		for (Entity<String> entity : context.getEntities()) {
			for (String prop : properties) {
				if (! context.entityContainsProperty(entity.getEntity(), prop)) {
					negated.addPropertyToEntity(entity.getEntity(), prop);
				}
			}
		}
		System.out.println();
		System.out.println(negated);
		
		return negated;
	}

}
