package sium.nlu.language.mapping;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;

import opennlp.maxent.BasicEventStream;
import opennlp.maxent.GIS;
import opennlp.maxent.PlainTextByLineDataStream;
import opennlp.maxent.io.SuffixSensitiveGISModelWriter;
import opennlp.model.AbstractModel;
import opennlp.model.AbstractModelWriter;
import opennlp.model.EventStream;
import opennlp.model.GenericModelReader;
import opennlp.model.MaxentModel;
import opennlp.model.RealValueFileEventStream;
import sium.nlu.context.Properties;
import sium.nlu.context.Property;
import sium.nlu.language.LingEvidence;
import sium.nlu.stat.Distribution;
import sium.system.SimpleUpdateModelException;
import sium.system.util.FileUtils;

public class MaxEntMapping implements Mapping<String> {
	
	static Logger log = Logger.getLogger(MaxEntMapping.class.getName());
	
	private String path;
	private AbstractModel model;
	FileWriter writer;// append setting
	
	public MaxEntMapping(String path) {
		setPath(path);
		try {
			writer = new FileWriter(new File(path), false);
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	public void clear() {
		FileUtils.deleteFile(getPath());
		FileUtils.deleteFile(getPath().substring(0,path.lastIndexOf('.')) + "Model.txt");
		model = null;
	}

	@Override
	public void addEvidenceToTrain(LingEvidence evidence, Properties<Property<String>> properties) {

		try {
			String evidenceString = evidence.getEvidenceString();
			for (Property<String> property : properties) {
				writer.write(String.format("%s %s\n", evidenceString, property.getProperty()));
			}			
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void train() {
		
		  try {
			writer.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		  
		  String modelFileName = path.substring(0,path.lastIndexOf('.')) + "Model.txt";
	      File outputFile = new File(modelFileName);
	      AbstractModelWriter writer = null;
	      try {
	    	  
	        FileReader datafr = new FileReader(new File(path));
	        EventStream es = new BasicEventStream(new PlainTextByLineDataStream(datafr));
	        GIS.SMOOTHING_OBSERVATION = 0.001;
	        
//	        GIS.trainModel() args: EventStream, num iterations, cutoff, use smoothing, show verbose output
	        AbstractModel model = GIS.trainModel(es, 100, 1, false, true);
	        writer =  new SuffixSensitiveGISModelWriter(model, outputFile);
	        writer.persist();
	      } 
	      catch (Exception e) {
	    	String msg = "Unable to create model due to exception: " + e; 
	        log.info(msg);
	        e.printStackTrace();
	        throw new SimpleUpdateModelException(msg);
	      }
	}
	
	private boolean modelIsNotLoaded() {
		return model == null;
	}

	@Override
	public Distribution<String> applyEvidenceToContext(LingEvidence evidence) {
		
		
		if (modelIsNotLoaded()) {
			
			String modelFileName = path.substring(0,path.lastIndexOf('.')) + "Model.txt";
			log.debug("Loading model: " + modelFileName);
			try {
				model = new GenericModelReader(new File(modelFileName)).getModel();
			} 
			catch (IOException e) {
				e.printStackTrace();
				throw new SimpleUpdateModelException("Unable to load model: " + modelFileName);
			}
		}
		
		String results = eval(model, evidence.getEvidenceString());
		String[] resultsSplit = results.split(" ");

		//this next part converts the output into one of our Distributions
		Distribution<String> dist = new Distribution<String>();
		for (int i=0; i<resultsSplit.length; i++) {
			String cur = resultsSplit[i];
			if (cur.isEmpty()) continue;
			String value = cur.substring(0,cur.indexOf("["));
			cur = cur.replaceAll(",","."); // not sure why this is necessary
			double prob = Double.parseDouble(cur.substring(cur.indexOf("[")+1, cur.indexOf("]")));
			if (prob == 0.0) prob = 0.0001; // smoothing hack, TODO: fix this
			dist.addProbability(value, prob);
		}
		
		return dist;
	}
	
	public static String eval(MaxentModel model, String predicates) {
	      boolean real = false;
	      String[] contexts = predicates.split(" ");
	      double[] ocs;
	      if (!real) {
	        ocs = model.eval(contexts);
	      }
	      else {
	        float[] values = RealValueFileEventStream.parseContexts(contexts);
	        ocs = model.eval(contexts,values);
	      }
	      return model.getAllOutcomes(ocs).trim();
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}


}
