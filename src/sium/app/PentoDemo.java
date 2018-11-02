package sium.app;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;

import sium.nlu.context.Context;
import sium.nlu.grounding.Grounder;
import sium.nlu.language.LingEvidence;
import sium.nlu.language.mapping.Mapping;
import sium.nlu.language.mapping.MaxEntMapping;
import sium.nlu.stat.Distribution;
import sium.system.util.PentoSqlUtils;


public class PentoDemo {
	
	static Logger log = Logger.getLogger(PentoDemo.class.getName());

	private int max = 1500;
	private int numFolds = 10;
	private int foldSize = 150;
	
	private Mapping<String> mapping;
	
	FileWriter writer;
	private double rank = 0.0;
	
	private PentoSqlUtils pento;

	public PentoDemo() throws IOException {
		writer = new FileWriter(new File("/home/casey/Desktop/pento_overtime"));
		BasicConfigurator.configure(new NullAppender()); // turn logging off for the demo
		pento = new PentoSqlUtils();
	}
	
	private void run() throws SQLException, IOException {
		pento.createConnection();
		
		ArrayList<String> episodes = pento.getAllEpisodes();
		

		
		
		
		for (int numTrainItems = 1; numTrainItems < 1350; numTrainItems++) {
	
		double total = 0.0;
		double correct = 0.0;
		rank = 0.0;
		
		for (int i=1; i<=numFolds; i++) {
			int j = 1;
			
			System.out.println("Processing fold " + i + " out of " + numFolds);
			
//			mapping = new NaiveBayesMapping();
			mapping = new MaxEntMapping("pento.txt");
//			mapping = new StringMatchMapping();
//			mapping = new CoocurrenceMapping();
//			mapping = new LanguageModelMapping();
			
//			setup the training data
			int curTrainItems = 0;
			for (String episode : episodes) {
				if (exceedsMax(j)) break;
			
				if (isTrainData(j, i)) {
					if (curTrainItems >= numTrainItems) break;
//					System.out.println(episode);
					Context<String,String> context = pento.getContext(episode);
					
					ArrayList<LingEvidence> ling = pento.getLingEvidence(episode);
					String gold = pento.getGoldPiece(episode);
					
					for (LingEvidence evidence : ling) {
						mapping.addEvidenceToTrain(evidence, context.getPropertiesForEntity(gold));
					}
					curTrainItems++;
				}
				j++;
			}
			
			mapping.train();
			
//			now, run evaluation on this fold
			j = 0;
			for (String episode : episodes) {
				if (exceedsMax(j)) break;
			
				if (!isTrainData(j, i)) {
					
					Context<String,String> context = pento.getContext(episode);
					ArrayList<LingEvidence> ling = pento.getLingEvidence(episode);
					String gold = pento.getGoldPiece(episode);
					 
//					step through the utterance one word at a time
					Grounder<String, String> grounder = new Grounder<String, String>();
					
					String line = "";
					for (LingEvidence evidence : ling) {
						Distribution<String> currentDist = mapping.applyEvidenceToContext(evidence);
						grounder.groundIncrement(context,currentDist);
						Distribution<String> temp = grounder.getPosterior();
						
						String t = "False";
						if (gold.equals(temp.getArgMax().getEntity())) {
							t = "True";
						}
						line += t + " ";
					}
//					System.out.println(line);

					
					Distribution<String> post = grounder.getPosterior();
					int r = post.findRank(gold);
					if (r == -1) System.out.println(gold);
					rank += 1.0/r;
//					writer.write(r + "\n");
					if (gold.equals(post.getArgMax().getEntity())) 
						correct++;
					total++;
				}
				j++;
			}
			
			mapping.clear();
			break;
		}



		System.out.println(numTrainItems + " " + (correct / total) + " " + (rank/total));
		writer.write(numTrainItems + " " + (correct/total) + " " + (rank/total) +"\n");
	}
		pento.closeConnection();		
	writer.close();
		
	}
	
	private boolean isTrainData(int j, int i) {
		return (j < (i-1) * foldSize) || (j > i * foldSize);
	}

	private boolean exceedsMax(int j) {
		return j > max;
	}

	
	
	public static void main(String[] args) {
		try {
			new PentoDemo().run();
		} 
		catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
}
