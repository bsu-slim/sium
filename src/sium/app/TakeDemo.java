package sium.app;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;

import sium.nlu.context.Context;
import sium.nlu.grounding.Grounder;
import sium.nlu.language.LingEvidence;
import sium.nlu.language.mapping.CoocurrenceMapping;
import sium.nlu.language.mapping.Mapping;
import sium.nlu.language.mapping.MaxEntMapping;
import sium.nlu.multi.DistanceDurationClassifier;
import sium.nlu.multi.ObjectPoint;
import sium.nlu.stat.Distribution;
import sium.system.util.TakeSqlUtils;


public class TakeDemo {
	
	static Logger log = Logger.getLogger(TakeDemo.class.getName());

	private int max = 1000;
	private int numFolds = 10;
	private int foldSize = 100;
	
	
	
	private TakeSqlUtils take;
	
	FileWriter writer;

	public TakeDemo() throws IOException {
		writer = new FileWriter(new File("/home/casey/Desktop/sium_take_learning"));
		BasicConfigurator.configure(new NullAppender()); // turn logging off for the demo
		
	}
	
	private void run() throws SQLException, IOException {
		
		double topAvgRank = 100;
		double topWeight = 0;
		double topWeightb = 0;
		
//
//		for (double b=0.0; b<=1.0; b+=0.01) {
//		for (double a=0.0; a<=1.0; a+=0.01) {
		take = new TakeSqlUtils();
		take.createConnection("/home/casey/git/008_pdc_corpus_maintenance/Scripts/Pento/take.db");
		double entropy = 0.0;
		double rank = 0.0;
		
		ArrayList<String> episodes = take.getAllEpisodes();
		
//		ArrayList<String> evalEpisodes = new ArrayList<String> ();
//		Scanner scan = new Scanner(new File("/home/casey/git/dissertation/comparison/eval_ids.txt"));
//		while (scan.hasNext()) evalEpisodes.add(scan.nextLine().trim());
//		scan.close();
		
//		for (String eid : evalEpisodes) episodes.remove(eid);
//		System.out.println(evalEpisodes);
		
		
		
		for (int numTrainItems = 1; numTrainItems < 900; numTrainItems++) {
			
			double total = 0.0;
			double correct = 0.0;
			rank = 0.0;
		for (int i=1; i<=numFolds; i++) {
			int j = 1;
			
//			System.out.println("Processing fold " + i + " out of " + numFolds);
			
			Mapping<String> mapping = new MaxEntMapping("takedall.txt");
//			setup the training data
			int used = 0;
			for (String episode : episodes) {
				
				if (exceedsMax(j)) break;
			
				if (isTrainData(j, i)) {
//					System.out.println(episode);
					Context<String,String> context = take.getContext(episode);
					ArrayList<LingEvidence> ling = take.getLingEvidence(episode);
					if (ling == null) continue;
					String gold = take.getGoldPiece(episode);

					for (LingEvidence evidence : ling) {
						mapping.addEvidenceToTrain(evidence, context.getPropertiesForEntity(gold));
					}
					
					used++;
					if (used >= numTrainItems) break;
					
				}
				j++;
			}
		
			mapping.train();
//			now, run evaluation on this fold
			j = 0;
			for (String episode : episodes) {
				if (exceedsMax(j)) break;
			
				if (!isTrainData(j, i)) {
//					System.out.println(episode);
					Context<String,String> context = take.getContext(episode);
					ArrayList<LingEvidence> ling = take.getLingEvidence(episode);
					
					if (ling.size() == 0) continue;
					
					String gold = take.getGoldPiece(episode);
 
//					step through the utterance one word at a time
					Grounder<String, String> grounder = new Grounder<String, String>();
					String line = "";
					for (LingEvidence evidence : ling) {
						
						Distribution<String> currentDist = mapping.applyEvidenceToContext(evidence);
						grounder.groundIncrement(context, currentDist);
						Distribution<String> temp = grounder.getPosterior();
						
						String t = "False";
						if (gold.equals(temp.getArgMax().getEntity())) {
							t = "True";
						}
						line += t + " ";
					}
					
					Distribution<String> post = grounder.getPosterior();
					
//					combineDeixis(episode, post, 0.35);
//					combineGaze(episode, post, 0.91);
					
					entropy += post.getEntropy();
					int r = post.findRank(gold);
					if (r == -1) System.out.println(gold);
					rank += 1.0/r;
//					writer.write(r + "\n");
					
//					System.out.println(episode + " " + gold+" "+ post.getArgMax().getEntity() + " " + r);
					
					if (gold.equals(post.getArgMax().getEntity())) {
						correct++;
//						writer.write("True\n");
					}
					
					total++;
				}
				j++;
			}
			
			mapping.clear();
			break;
		}
		System.out.println(numTrainItems + " "+ correct/total + " " + rank / 100);
		writer.write(numTrainItems + " "+ correct/total + " " + rank / 100 + "\n");
		
		}
		 
		
		writer.close();
//			System.out.println("Accuracy: " + correct + "/" + total + " " + (correct / total));
//			System.out.println("MRR: " + (rank / total));
//			if ((rank/total) < topAvgRank) {
//				topAvgRank = (rank/total);
//				topWeight = a;
//				topWeightb = b;
//				System.out.println("top weight: "  + a + ", top weight b: " +b+", top avg rank:" + topAvgRank);
//			}
			
			take.closeConnection();
			
			
//		}
//		}
	
//		System.out.println("top weight: "  + topWeight + ", top weight b: " +topWeightb+", top avg rank:" + topAvgRank);
	}
	
	private void combineGaze(String episode, Distribution<String> post, double weight) throws SQLException {
		combineModality(episode, post, take.getGazeForEpisode(episode, "hand"), weight);
	}

	private void combineDeixis(String episode, Distribution<String> post, double weight) throws SQLException {
		combineModality(episode, post, take.getDeixisForEpisode(episode, "hand"), weight);
	}

	private void combineModality(String episode, Distribution<String> post, List<String> points, double weight) throws SQLException {
		
		List<ObjectPoint> targetPoints = createTargetPointsList(points);
		List<ObjectPoint> objectPoints = take.getObjectPoints(episode);
		
		DistanceDurationClassifier ddc = new DistanceDurationClassifier(objectPoints);
		
		Distribution<String> result = ddc.classifyPointsWithDuration(targetPoints);

		post.combineDistribution(result, weight);

	}

	private List<ObjectPoint> createTargetPointsList(List<String> points) throws SQLException {
		ArrayList<ObjectPoint> targetPoints = new ArrayList<ObjectPoint>();
		
		for (String point : points) {
			List<String> parts = Arrays.asList(point.split(","));
			try {
				targetPoints.add(new ObjectPoint("", (int)Double.parseDouble(parts.get(0)), (int) Double.parseDouble(parts.get(1)), Double.parseDouble(parts.get(2))));
			}
			catch (NumberFormatException e) {
				
			}
		}
		
		return targetPoints;
	}


	private boolean isTrainData(int j, int i) {
		return (j < (i-1) * foldSize) || (j > i * foldSize);
	}

	private boolean exceedsMax(int j) {
		return j > max;
	}
	
	public static void main(String[] args) {
		try {
			new TakeDemo().run();
		} 
		catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
}
