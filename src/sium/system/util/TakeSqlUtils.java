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
import sium.nlu.multi.ObjectPoint;

public class TakeSqlUtils {
	
	protected Connection conn;
	protected Statement stat;

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


	public ArrayList<LingEvidence> getLingEvidence(String episode) throws SQLException {
		ArrayList<LingEvidence> ling = new ArrayList<LingEvidence>();
		
//		ResultSet utt = getUttForEpisode(episode);
		ResultSet utt = getUttForEpisode(episode);
		
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
	
	
	public Context<String, String> getCVArgMaxContext(String episode) throws SQLException {
		Context<String,String> context = new Context<String,String>();
		
		ResultSet pieces = getCVPiecesForEpisode(episode);
		
		while (pieces.next()) {
			String entity = pieces.getString("id");
			context.addPropertyToEntity(entity, pieces.getString("grid"));
			context.addPropertyToEntity(entity, pieces.getString("top_color"));
			context.addPropertyToEntity(entity, pieces.getString("top_type"));
//			context.addPropertyToEntity(entity, pieces.getString("row")+ "R");
//			context.addPropertyToEntity(entity, pieces.getString("col") + "C");
//			context.addPropertyToEntity(entity, pieces.getString("selected"));
		}
		
		return context;
	}
	
	public Context<String, String> getCVContext(String episode) throws SQLException {
		Context<String,String> context = new Context<String,String>();
		
		ResultSet pieces = getCVPiecesForEpisode(episode);
		
		while (pieces.next()) {
			String entity = pieces.getString("id");
			addProperties(entity, context, pieces.getString("color"));
			addProperties(entity, context, pieces.getString("type"));
//			context.addPropertyToEntity(entity, pieces.getString("type"));
			context.addPropertyToEntity(entity, pieces.getString("grid"));
//			context.addPropertyToEntity(entity, pieces.getString("row")+ "R");
//			context.addPropertyToEntity(entity, pieces.getString("col") + "C");
		}
		
		return context;
	}

	private void addProperties(String entity, Context<String, String> context,
			String string) {
//	System.out.println(entity+ " " +string);
		String[] pairs = string.split(",");
		for (int i=0; i<pairs.length; i++) {
			String[] p = pairs[i].split(":");
			Double prob = Double.parseDouble(p[1]);
			context.addPropertyToEntity(entity, p[0], prob);
		}
	}

	public Context<String, String> getContext(String episode) throws SQLException {
		
		Context<String,String> context = new Context<String,String>();
		
		ResultSet pieces = getPiecesForEpisode(episode);
		
		while (pieces.next()) {
			String entity = pieces.getString("id");
			context.addPropertyToEntity(entity, pieces.getString("color"));
			context.addPropertyToEntity(entity, pieces.getString("type"));
			context.addPropertyToEntity(entity, pieces.getString("grid"));
//			context.addPropertyToEntity(entity, pieces.getString("row")+ "R");
//			context.addPropertyToEntity(entity, pieces.getString("col") + "C");
		}
		
		pieces.close();
		return context;
	}

	public ResultSet getGoldPieceForEpisode(String episode) throws SQLException {
		Statement stat = createStatement();
		return stat.executeQuery(String.format("select * from referent where episode_id='%s'", episode));
	}
	
	public ResultSet getUttForEpisode(String episode) throws SQLException {
		Statement stat = createStatement();
		return stat.executeQuery(String.format("select * from hand where episode_id='%s' order by inc", episode));
	}
	
	public ResultSet getAsrForEpisode(String episode) throws SQLException {
		Statement stat = createStatement();
		return stat.executeQuery(String.format("select * from asr where episode_id='%s' order by inc", episode));
	}
	
	public List<String> getDeixisForEpisode(String episode, String type) throws SQLException {
		return getModalityForEpisode(episode, type, "deixis");
	}	
	
	public List<String> getModalityForEpisode(String episode, String type, String modality) throws SQLException {
		Statement stat = createStatement();
		ResultSet result = stat.executeQuery(String.format("select * from %s_%s where episode_id='%s'", type, modality, episode));
		if (result.isAfterLast()) return new ArrayList<String>();
		ArrayList<String> points = new ArrayList<String>();
		while (result.next()) {
			points.addAll(Arrays.asList(result.getString(modality).toLowerCase().split("\\s+")));
		}
		result.close();
		return points;
	}
	
	public List<String> getGazeForEpisode(String episode, String type) throws SQLException {
		return getModalityForEpisode(episode, type, "gaze");
	}		

	public ResultSet getPiecesForEpisode(String episode) throws SQLException {
		Statement stat = createStatement();
		return stat.executeQuery(String.format("select * from piece where episode_id='%s'", episode));
	}

	public ResultSet getCVPiecesForEpisode(String episode, String iteration) throws SQLException {
		Statement stat = createStatement();
		return stat.executeQuery(String.format("select * from cv_piece_"+iteration+" where episode_id='%s'", episode));
	}
	
	public ResultSet getCVPiecesForEpisode(String episode) throws SQLException {
		Statement stat = createStatement();
		return stat.executeQuery(String.format("select * from cv_piece where episode_id='%s'", episode));
	}

	public ArrayList<String> getAllEpisodes() throws SQLException {
		ArrayList<String> episodes = new ArrayList<String>();
		ResultSet utterances = getAllEpisodeIDs();
		while (utterances.next()) {
			episodes.add(utterances.getString("episode_id"));
		}
		utterances.close();
		return episodes;
	}
	
	public ResultSet getAllUtterances() throws SQLException {
		Statement stat = createStatement();
		return stat.executeQuery("select * from hand");
	}
	
	public ResultSet getAllEpisodeIDs() throws SQLException {
		Statement stat = createStatement();
		return stat.executeQuery("select distinct episode_id from hand order by episode_id");
	}
	
	public ResultSet getEvalEpisodeIDs() throws SQLException {
		Statement stat = createStatement();
		return stat.executeQuery("select distinct episode_id from cv_piece order by episode_id");
	}	
	
	
	public void createConnection(String path) throws SQLException {
		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		conn =  DriverManager.getConnection("jdbc:sqlite:" + path);
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

	public List<ObjectPoint> getObjectPoints(String episode) throws SQLException {
		
		Statement stat = createStatement();
		ResultSet result = stat.executeQuery(String.format("select id, x, y from piece where episode_id='%s'", episode));
		
		List<ObjectPoint> objects = new ArrayList<ObjectPoint>();
		
		while (result.next()) {
			objects.add(new ObjectPoint(result.getString("id"), result.getInt("x"), result.getInt("y")));
		}
		return objects;
	}

	public ArrayList<String> getEvalEpisodes() throws SQLException {
		ArrayList<String> episodes = new ArrayList<String>();
		ResultSet utterances = getAllEpisodeIDs();
		while (utterances.next()) {
			episodes.add(utterances.getString("episode_id"));
		}
		utterances.close();
		return episodes;
	}

	public double getColorEntropy(String iteration) throws NumberFormatException, SQLException {
		Statement stat = createStatement();
		ResultSet result = stat.executeQuery(String.format("select color from cv_piece_%s", iteration));
		
		double total = 0;
		double numObjects = 0;
		while (result.next()) {
			String colors = result.getString("color");
			String[] pairs = colors.split(",");
			double current = 0;
			for (int i=0; i<pairs.length; i++) {
				String[] p = pairs[i].split(":");
				double prob = Double.parseDouble(p[1]);
				current += prob * Math.log(prob);
			}
			total += current;
			numObjects++;
		}
		result.close();
		return -(total / numObjects);
	}


}
