package sium.app;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeSet;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.varia.NullAppender;

import sium.nlu.context.Context;
import sium.nlu.grounding.Grounder;
import sium.nlu.language.LingEvidence;
import sium.nlu.language.mapping.Mapping;
import sium.nlu.language.mapping.MaxEntMapping;
import sium.nlu.language.mapping.NaiveBayesMapping;
import sium.nlu.stat.Distribution;

public class ATIS {
	
	
	
	ArrayList<Instance> data;
	TreeSet<String> attributes;
	Instance current;
	String currentType;
	String path = "/home/casey/corpora/ATIS/atoms/";

//	Mapping<String> goalMapping = new MaxEntMapping("goal.txt");
	Mapping<String> slotMapping = new MaxEntMapping("slot.txt");
	
	Mapping<String> goalMapping = new NaiveBayesMapping();
//	Mapping<String> slotMapping = new NaiveBayesMapping();
	
	Context<String,String> slots = new Context<String, String>(); // keeps track of all slots
	Context<String,String> goals = new Context<String, String>(); // keeps track of all the goals

	
	private void train() throws IOException {
		
		BasicConfigurator.configure(new NullAppender()); // turn logging off for the demo
		
		attributes = new TreeSet<String>();
		data = new ArrayList<Instance>();
		
		gatherData(path+"trainAtis.atoms");
		gatherData(path+"devAtis.train.atoms");

		cleanup(); // run this for the final eval
		
		System.out.println("num instances for training: " + data.size());
		
		trainModel();
//		evaluate(path+"atis_2_3_dev.atoms", path+"atis.guess");
		evaluate(path+"testAtis.atoms", path+"atis.guess");
	}

	private void cleanup() {
		if (current != null) {
			current.clearEnds();
			data.add(current);
		}
		current = null;		
	}

	private void trainModel() {
		for (Instance instance : data) {
			
			String pWord = "<s1>";
			String ppWord = "<s2>";
			String pppWord = "<s3>";
			String pPos  = "<t1>";
			String ppPos = "<t2>";
			String pSlot = "<p1>";
			String ppSlot = "<p2>";
			
			
//			step through each instance incrementally, word by word
			for (Integer i : instance.getIndeces()) {
				
//				the main goal is to map the word to the tag
				String goldTag = instance.getSlotsMap().get(i);
				String word = instance.getWordsMap().get(i);
				
				Context<String,String> context = new Context<String,String>();
				
				addProperties(context, goldTag);
				String pos = instance.getPOSMap().get(i);
				
				addProperties(goals, instance.goal);
				addProperties(slots, goldTag);
				
				LingEvidence ling = new LingEvidence();

				ling.addEvidence("w1", word);
				ling.addEvidence("w2", pWord);
				ling.addEvidence("w3", ppWord);
//				ling.addEvidence("tag1", pSlot);
//				ling.addEvidence("pos1", pos + "_" + word);
				
				slotMapping.addEvidenceToTrain(ling, context.getPropertiesForEntity(goldTag));

//				ling.addEvidence("tag2", ppSlot);
//				ling.addEvidence("pos2", pPos + "_" + pWord);
//				ling.addEvidence("pos3", ppPos + "_" + ppWord);
				ling.addEvidence("w4", pppWord);

				goalMapping.addEvidenceToTrain(ling, goals.getPropertiesForEntity(instance.goal));
				
				pppWord = ppWord;
				ppWord = pWord;
				pWord = word;
				ppPos = pPos;
				pPos = pos;
				ppSlot = pSlot;
				pSlot = goldTag;
			}
		}
		goalMapping.train();
		slotMapping.train();
	}
	
	private void addProperties(Context<String, String> goals, String goal) {
		goals.addPropertyToEntity(goal, goal);
	}

	private void evaluate(String evalpath, String outpath) throws IOException {
		data.clear();
		gatherData(evalpath);
		cleanup();
		
		FileWriter writer = new FileWriter(new File(outpath));
		HashMap<String,Double> confusionMatrix = new HashMap<String,Double>();
		double correct = 0.0, total = 0.0, flights = 0.0;
		
		for (Instance instance : data) {
			
			writer.write("=== " + instance.getSentence() +"\n");
			
			String pWord = "<s1>";
			String ppWord = "<s2>";
			String pppWord = "<s3>";
			String pPos  = "<t1>";
			String ppPos = "<t2>";
			String pSlot = "<p1>";
			String ppSlot = "<p2>";
			
			Grounder<String, String> goal = new Grounder<String, String>();
		
			ArrayList<String> guesses = new ArrayList<String> ();
			ArrayList<String> words = new ArrayList<String>();
			
			String prevGuess = new String();
			
			for (Integer i : instance.getIndeces()) {
				
				Grounder<String, String> slot = new Grounder<String, String>();
				
				String word = instance.getWordsMap().get(i);
				String pos = instance.getPOSMap().get(i);
				String goldTag = instance.getSlotsMap().get(i);

				LingEvidence ling = new LingEvidence();
				ling.addEvidence("w1", word);
				ling.addEvidence("w2", pWord);
				ling.addEvidence("w3", ppWord);
				ling.addEvidence("tag1", pSlot);
				ling.addEvidence("pos1", pos + "_" + word);
				
				Distribution<String> slotDist = slotMapping.applyEvidenceToContext(ling);
				slot.groundIncrement(slots, slotDist);
				
				String guess = slot.getPosterior().getArgMax().getEntity();
				
				ppSlot = pSlot;
				pSlot = guess;

				ling.addEvidence("tag2", ppSlot);
				ling.addEvidence("pos2", pPos + "_" + pWord);
				ling.addEvidence("pos3", ppPos + "_" + ppWord);
				ling.addEvidence("w4", pppWord);
				
				Distribution<String> goalDist = goalMapping.applyEvidenceToContext(ling);
				goal.groundIncrement(goals, goalDist);
				
				guesses.add(guess);
				words.add(word);
				
				pppWord = ppWord;
				ppWord = pWord;
				pWord = word;
				ppPos = pPos;
				pPos = pos;
				
				if (!goldTag.equals(guess) && !guess.equals("OSLOT")) {
					
					String con = goldTag+":"+guess;
					if (!confusionMatrix.containsKey(con)) {
						confusionMatrix.put(con,  0.0);
					}
					double temp = confusionMatrix.get(con);
					temp++;
					confusionMatrix.put(con, temp);
				}
			}
			
			
//			Now, we have to write the guesses to a file
			String guess = goal.getPosterior().getArgMax().getEntity();
			if (instance.goal.equals("FLIGHT"))
				flights++;
			if (guess.equals(instance.goal))
				correct++;
			total++;
			
			writer.write("GOAL:" + guess + "\n");
			
//			collect all guesses throughout the sentence, combining ones that have the same guessed tag
			prevGuess = new String();
			String prevWord = new String();
			String slotWords = new String();
			ArrayList<String> foundSlots = new ArrayList<String>();
			for (int j=0; j<words.size(); j++) {
				String g = guesses.get(j);
				String w = words.get(j);
				
				if (prevGuess.equals(g)) {
					slotWords += " " + w;
				}
				else {
					if (!"".equals(prevWord)) {
						foundSlots.add(prevGuess.toUpperCase() + "=" +slotWords);
						slotWords = new String();
					}
					slotWords += w;
				}
				
				prevGuess = g;
				prevWord = w;
			}
			if (!"".equals(prevWord)) {
				foundSlots.add(prevGuess.toUpperCase() + "=" + slotWords);
			}
//			now write them filter out OSLOT
			for (String f : foundSlots) {
				if (f.startsWith("OSLOT")) continue;
				writer.write(f.trim()+"\n");
			}
			
			writer.write("*\n");
		}
		
		System.out.println(correct + " " + total + " %correct:" + correct/total + " %flights:" + flights/total);
//		for (String key : confusionMatrix.keySet()) {
//			System.out.println(key + " " + confusionMatrix.get(key));
//		}
		
		writer.close();
		goalMapping.clear();
		slotMapping.clear();
		
	}

	private void gatherData(String string) throws FileNotFoundException {
		Scanner scan = new Scanner(new File(string));
		String line = "";
		while (scan.hasNext()){
			line = scan.nextLine();
			process(line);
		}
		scan.close();	

	}

	private void process(String line) {
		if (line.isEmpty()) return;
		if (line.equals(">>")) {
			if (current != null) {
				current.clearEnds();
				data.add(current);
			}
			current = new Instance();
		}
		else if (line.contains(">slot")) {
			currentType = "slot";
			return;
		}
		else if (line.contains(">pos")) {
			currentType = "pos";
			return;			
		}		
		else if (line.contains(">cass")) {
			currentType = "cass";
			return;			
		}			
		else if (line.contains(">word")) {
			currentType = "word";
			return;			
		}		
		else if (line.contains(">goal")) {
			currentType = "goal";
			return;			
		}
		else if (line.startsWith(">")) {
			currentType = null;
			return;			
		}
		
		if (currentType != null)
			current.add(currentType, line);			

	}
	
	public static void main(String[] args) {
		try {
			new ATIS().train();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public class Instance {
		HashMap<String, HashMap<Integer,String>> maps = new HashMap<String, HashMap<Integer,String>>();
		String goal;
		
		public Instance() {
			maps.put("word", new HashMap<Integer,String>());
		}
		
		public void add(String type, String line) {
			if (type.equals("goal")) {
				this.goal = clean(line);
				return;
			}
			
			String[] split = line.split("\\s+");
			Integer i = Integer.parseInt(clean(split[0]));
			if (!maps.containsKey(type)) {
				maps.put(type, new HashMap<Integer,String>());
			}
			HashMap<Integer,String> temp = maps.get(type);
			temp.put(i, clean(split[1]));
			maps.put(type, temp);
		}
		
		public void clearEnds() {
			int start = 0;
			int end = 0;
			for (Integer i : getWordsMap().keySet()) {
				String word = getWordsMap().get(i);
				if (word.equals("END")) 
					end = i;
			}
			
			for (String type : maps.keySet()) {
				maps.get(type).remove(start);
				maps.get(type).remove(end);
			}
		}
		
		private String clean(String line) {
			String newLine = line.trim();
			newLine = newLine.replace("\"", "");			
			return newLine;
		}
		
		public String toString() {
			return goal + " " + maps;
		}

		public HashMap<Integer, String> getWordsMap() {
			return maps.get("word");
		}
		
		public HashMap<Integer, String> getSlotsMap() {
			return maps.get("slot");
		}
		
		public HashMap<Integer, String> getPOSMap() {
			return maps.get("pos");
		}		
		
		public HashMap<Integer, String> getCassMap() {
			return maps.get("cass");
		}	
		
		public TreeSet<Integer> getIndeces() {
			TreeSet<Integer> orderedWords = new TreeSet<Integer>();
			orderedWords.addAll(getWordsMap().keySet());
			return orderedWords;
		}		
		
		public String getSentence() {
			String temp = "";
			for (Integer i : getIndeces()) {
				temp += getWordsMap().get(i) + " ";				
			}
			return temp.trim();
		}
		
	}
	
}
