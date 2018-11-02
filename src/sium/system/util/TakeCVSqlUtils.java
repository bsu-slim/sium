package sium.system.util;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import sium.nlu.context.Context;
import sium.nlu.language.LingEvidence;

public class TakeCVSqlUtils extends TakeSqlUtils {
	
	public void createConnection() throws SQLException {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		conn =  DriverManager.getConnection("jdbc:sqlite:domains/takecv/takecv.db");
	}


	public Context<String,String> getRawFeatures(String episode) throws SQLException {
		
		Context<String,String> context = new Context<String,String>();
		
		ResultSet pieces = this.getRawFeaturesForEpisode(episode);
		
		while (pieces.next()) {
			String entity = pieces.getString("id");
			context.addPropertyToEntity(entity, "r:"+pieces.getString("r"));
			context.addPropertyToEntity(entity, "g:"+pieces.getString("g"));
			context.addPropertyToEntity(entity, "b:"+pieces.getString("b"));
			context.addPropertyToEntity(entity, "h:"+pieces.getString("h"));
			context.addPropertyToEntity(entity, "s:"+pieces.getString("s"));
			context.addPropertyToEntity(entity, "v:"+pieces.getString("v"));
//			context.addPropertyToEntity(entity, "o:"+pieces.getString("orientation"));
//			context.addPropertyToEntity(entity, "hs:"+pieces.getString("h_skew"));
//			context.addPropertyToEntity(entity, "vs:"+pieces.getString("v_skew"));
			context.addPropertyToEntity(entity, "ne:"+pieces.getString("num_edges"));
			context.addPropertyToEntity(entity, "x:"+pieces.getString("pos_x"));
			context.addPropertyToEntity(entity, "y:"+pieces.getString("pos_y"));
			
		}
		
		pieces.close();
		
		
		return context;
	}
	
	
	public ResultSet getAsrForEpisode(String episode, String tags) throws SQLException {
		Statement stat = createStatement();
		return stat.executeQuery(String.format("select * from asr where episode_id='%s' and tags like '%s%s%s' order by inc", episode, "%",tags, "%"));
	}
	
	public ResultSet getGoldPieceForEpisode(String episode, String table) throws SQLException {
		Statement stat = createStatement();
		return stat.executeQuery(String.format("select * from %s where episode_id='%s'", table, episode));
	}
	
	public String getGold(String episode, String slot, String table) throws SQLException {
		String gold = new String();
		ResultSet sem = getGoldPieceForEpisode(episode, table);
		sem.next();
		gold = sem.getString(slot);
		sem.close();
		return gold;
	}
	
	
	public String getGoldPiece(String episode, String table) throws SQLException {
		return getGold(episode, "object", table);
	}
	
	public ArrayList<LingEvidence> getLingEvidence(String episode, String tags) throws SQLException {
		ArrayList<LingEvidence> ling = new ArrayList<LingEvidence>();
		
//		ResultSet utt = getUttForEpisode(episode);
		ResultSet utt = getAsrForEpisode(episode, tags);
		
		if (utt.isAfterLast()) {
			utt.close();
			return new ArrayList<LingEvidence>(); // if empty
		}
		
		String prev = "<s>";
		String prevPrev = prev;
		
		while (utt.next()) {
			LingEvidence e = new LingEvidence();
			String word = utt.getString("word");
			if (word.equals("<sil>")) continue;
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

	
	public ResultSet getRawFeaturesForEpisode(String episode) throws SQLException {
		Statement stat = createStatement();
		return stat.executeQuery(String.format("select * from cv_piece_raw where episode_id='%s'", episode));
	}

	
	public ResultSet getLandmarkEpisodeIDs() throws SQLException {
		Statement stat = createStatement();
		return stat.executeQuery("select distinct episode_id from landmark order by episode_id");
	}
	
	public ResultSet getTargetOnlyEpisodeIDs() throws SQLException {
		Statement stat = createStatement();
		return stat.executeQuery("select distinct episode_id from referent where episode_id not in (select episode_id from landmark) order by episode_id");
	}

	public ArrayList<String> getLandmarkEpisodes() throws SQLException {
		
		ArrayList<String> episodes = new ArrayList<String>();
		ResultSet utterances = getLandmarkEpisodeIDs();
		while (utterances.next()) {
			episodes.add(utterances.getString("episode_id"));
		}
		utterances.close();
		return episodes;
	}
	
	
	public ArrayList<String> getTargetOnlyEpisodes() throws SQLException {
		
		ArrayList<String> episodes = new ArrayList<String>();
		ResultSet utterances = getTargetOnlyEpisodeIDs();
		while (utterances.next()) {
			episodes.add(utterances.getString("episode_id"));
		}
		utterances.close();
		return episodes;
	}
	
	


}
