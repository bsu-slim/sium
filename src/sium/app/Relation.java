package sium.app;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.varia.NullAppender;

import sium.nlu.context.Context;
import sium.nlu.context.Entity;
import sium.nlu.context.Property;
import sium.nlu.grounding.Grounder;
import sium.nlu.language.LingEvidence;
import sium.nlu.language.mapping.NaiveBayesMapping;
import sium.nlu.stat.DistRow;
import sium.nlu.stat.Distribution;
import sium.system.util.PentoSqlUtils;

public class Relation {

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
				System.out.println(episode);
				
				Context<String,String> context = pento.getContext(episode);
				System.out.println(context);

				Grounder<String, String> grounder = new Grounder<String, String>();
				
				LingEvidence ling = new LingEvidence();
						
				ling = new LingEvidence();
				ling.addEvidence("w1", "grüne");
				Distribution<String> currentDist = mapping.applyEvidenceToContext(ling);
				grounder.groundIncrement(context, currentDist);
				System.out.println(grounder.getPosterior());		
				
				
				Grounder<String, String> grounder2 = new Grounder<String, String>();			

				ling = new LingEvidence();
				ling.addEvidence("w1", "gelbe");
				Distribution<String> currentDist2 = mapping.applyEvidenceToContext(ling);
				grounder2.groundIncrement(context, currentDist2);
				System.out.println(grounder2.getPosterior());	
				
				Distribution<String> nextToDist = new Distribution<String>();
//				tile-1 and tile-2 are next to each other and highest-ranking, so combining those would not help. 
//				We can negate it, by considering that all tiles are next-to each other except for tile-1 and tile-2. 
//				That means tile-3 should be the winner since it is orange and not next to something. 
				for (DistRow<String> e1 : grounder2.getPosterior().getDistribution()) {
					double prob = e1.getProbability();
					for (DistRow<String> e2 : grounder.getPosterior().getDistribution()) {
						if (e1.getEntity().equals("tile-1") && e2.getEntity().equals("tile-2") ||
							e1.getEntity().equals("tile-2") && e2.getEntity().equals("tile-1")) {
							prob *= 0.001;
						}
						prob *= e2.getProbability();
					}
					nextToDist.addProbability(e1.getEntity(), prob);
				}
				nextToDist.normalize();
				System.out.println(nextToDist);
				
				break;
			}
			
			pento.closeConnection();
			
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		
		
	}

//	private static void findNextTo(Context<String, String> context) {
//		
//		Context<String,String> nextTo = new Context<String,String>();
//
//		for (Entity<String> e1 : context.getEntities()) {
//			for (Entity<String> e2: context.getEntities()) {
//				if (e1.getEntity().equals(e2.getEntity())) continue; //ignore if they are the same
//				
//				for (Property<String> p1 : context.getPropertiesForEntity(e1.getEntity())) {
//					if (p1.getProperty().contains("R"))
//					for (Property<String> p2 : context.getPropertiesForEntity(e2.getEntity())) {
//						if (p2.getProperty().contains("R")) {
//							int i1 = Integer.parseInt(p1.getProperty().substring(0,1));
//							int i2 = Integer.parseInt(p2.getProperty().substring(0,1));
//							if (i1 == i2) { //if they are in the same row, see if their cols are 1 apart
//								for (Property<String> p1sub : context.getPropertiesForEntity(e1.getEntity())) {
//									if (p1sub.getProperty().contains("C"))
//									for (Property<String> p2sub : context.getPropertiesForEntity(e2.getEntity())) {
//										if (p2sub.getProperty().contains("C")) {
//											int c1 = Integer.parseInt(p1sub.getProperty().substring(0,1));
//											int c2 = Integer.parseInt(p2sub.getProperty().substring(0,1));
//											if (Math.abs(c1 - c2) == 1) {
//												nextTo.addPropertyToEntity(e1.getEntity(), e2.getEntity());
//											}
//										}
//									}
//								}
//							}
//						}
//					}
//				}
//			}
//		}
//	}

}
