package sium.system.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sium.nlu.context.Context;
import sium.nlu.language.LingEvidence;

public class PentoSqlUtils {
	
	private Connection conn;
	private Statement stat;

	public String getGoldPiece(String episode) throws SQLException {
		return getGold(episode, "object");
	}
	
	public String getGold(String episode, String slot) throws SQLException {
		String gold = new String();
		ResultSet sem = getGoldPieceForEpisode(episode);
		sem.next();
		gold = sem.getString(slot);
		sem.close();
		return gold;
	}
	
	public ArrayList<LingEvidence> getDynSynEvidence(String episode) throws SQLException {

		ArrayList<LingEvidence> ling = new ArrayList<LingEvidence>();
		
		ResultSet ds = getDynSynForEpisode(episode);
		
		if (ds.isAfterLast()) {
			ds.close();
			return new ArrayList<LingEvidence>(); // if empty
		}
		
		String prevPred = "<s>";
		String prevType = "<t>";
		String prevPrevPred = "<s>";
		String prevPrevType = "<t>";		
		
		while (ds.next()) {
			LingEvidence e = new LingEvidence();
			String val = null;
			
			String arg1_type = ds.getString("arg1_type");
			String arg2_type = ds.getString("arg2_type");
			
			val =  ds.getString("pred_type");
			if (val != null) {  
				if (!val.equals("t") && 
						!(arg1_type != null && arg1_type.equals("t")) && 
						!(arg2_type != null && arg2_type.equals("t"))) continue; // only deal with t types, if anywhere
//				e.addEvidence("pred_type",val.toLowerCase());
			}
			
			e.addEvidence("prev_pred", prevPred);
			e.addEvidence("prev_type", prevType);
			e.addEvidence("prev_prev_pred", prevPrevPred);
			e.addEvidence("prev_prev_type", prevPrevType);
			
			prevPrevType = prevType;
			prevType = val;
			val =  ds.getString("pred");
			if (val != null)  e.addEvidence("pred",val.toLowerCase());
			prevPred = val;
			
			prevPrevType = prevType;
			val =  ds.getString("arg1");
			if (val != null)  e.addEvidence("arg1",val.toLowerCase());			
//			if (arg1_type != null)  e.addEvidence("arg1_type",arg1_type.toLowerCase());
//			
			val =  ds.getString("arg2");
			if (val != null)  e.addEvidence("arg2",val.toLowerCase());
//			if (arg2_type != null)  e.addEvidence("arg2_type",arg2_type.toLowerCase());
			val =  ds.getString("handle");
//			
//			if (val != null)  e.addEvidence("handle",val.toLowerCase().substring(0,1));
			ling.add(e);
			
		}
		
		ds.close();
		return ling;
	}


	public ArrayList<LingEvidence> getLingEvidence(String episode) throws SQLException {

		ArrayList<LingEvidence> ling = new ArrayList<LingEvidence>();
		
		ResultSet utt = getUttForEpisode(episode);
		
		if (utt.isAfterLast()) {
			utt.close();
			return new ArrayList<LingEvidence>(); // if empty
		}
		
		String prev = "<s>";
		String prevPrev = prev;
		
		while (utt.next()) {
			LingEvidence e = new LingEvidence();
			String word = utt.getString("word").toLowerCase();
			e.addEvidence("w1", word);
			e.addEvidence("w2", prev);
			e.addEvidence("w3", prevPrev);
			ling.add(e);
			prevPrev = prev;
			prev = word;
		}
		
		utt.close();
		
		return ling;
	}

	
	public ArrayList<String> getRelativeIDs() throws SQLException {
		
		Statement stat = createStatement();
		ResultSet result = stat.executeQuery(String.format("select * from relative"));
		
		ArrayList<String> ids = new ArrayList<String>();
		while (result.next()) {
			ids.add(result.getString("episode_id"));
		}
		
		stat.close();
		return ids;
	}

	public Context<String, String> getContext(String episode) throws SQLException {
		
		Context<String,String> context = new Context<String,String>();
		
		ResultSet pieces = getPiecesForEpisode(episode);
		
		while (pieces.next()) {
			String entity = pieces.getString("id");
			context.addPropertyToEntity(entity, pieces.getString("color").toLowerCase());
			context.addPropertyToEntity(entity, pieces.getString("type").toLowerCase());
			context.addPropertyToEntity(entity, pieces.getString("grid").toLowerCase());
//			context.addPropertyToEntity(entity, pieces.getString("row")+ "R");
//			context.addPropertyToEntity(entity, pieces.getString("col") + "C");
//			if ( pieces.getString("selected").equals("selected"))
				context.addPropertyToEntity(entity, pieces.getString("selected"));
		}
		pieces.close();
		return context;
	}

	public ResultSet getGoldPieceForEpisode(String episode) throws SQLException {
		Statement stat = createStatement();
		return stat.executeQuery(String.format("select * from sem where episode_id='%s'", episode));
	}
	
	public ResultSet getUttForEpisode(String episode) throws SQLException {
		Statement stat = createStatement();
		return stat.executeQuery(String.format("select * from hand where episode_id='%s' order by inc", episode));
	}
	
	public ResultSet getDynSynForEpisode(String episode) throws SQLException {
		Statement stat = createStatement();
		return stat.executeQuery(String.format("select * from dynsyn where episode_id='%s' order by inc", episode));
	}	

	public ResultSet getPiecesForEpisode(String episode) throws SQLException {
		Statement stat = createStatement();
		return stat.executeQuery(String.format("select * from piece where episode_id='%s'", episode));
	}

	public ArrayList<String> getAllEpisodes() throws SQLException {
		ArrayList<String> episodes = new ArrayList<String>();
		ResultSet utterances = getAllUtterances();
		while (utterances.next()) {
			episodes.add(utterances.getString("episode_id"));
		}
		utterances.close();
		return episodes;
	}
	
	
	public ResultSet getAllUtterances() throws SQLException {
		Statement stat = createStatement();
		return stat.executeQuery("select * from hand where episode_id not in (select episode_id from flagged); ");
	}
	
	
	public void createConnection() throws SQLException {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		conn =  DriverManager.getConnection("jdbc:sqlite:domains/pento/pento.db");
//		conn =  DriverManager.getConnection("jdbc:sqlite:src/demo/inpro/system/refres/pento.db");
	}
	
	public Statement createStatement() throws SQLException {
		if (stat == null)
			stat = conn.createStatement();
		return stat;
	}
	
	public void closeConnection() throws SQLException {
		conn.close();
	}


	public String getGoldAction(String episode) throws SQLException {
		return getGold(episode, "action");
	}

}
