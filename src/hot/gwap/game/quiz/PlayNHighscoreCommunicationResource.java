/*
 * This file is part of gwap, an open platform for games with a purpose
 *
 * Copyright (C) 2013
 * Project play4science
 * Lehr- und Forschungseinheit für Programmier- und Modellierungssprachen
 * Ludwig-Maximilians-Universität München
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package gwap.game.quiz;

import gwap.game.quiz.action.QuizHighscoreBean;
import gwap.model.QuizHighscore;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.servlet.ContextualHttpServletRequest;
import org.jboss.seam.transaction.Transaction;
import org.jboss.seam.web.AbstractResource;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * This servlet provides the highscores by returning a HttpServletRespone in
 * JSON-Format
 * 
 * @author Jonas Hölzler
 * 
 */
@Startup
@Scope(ScopeType.APPLICATION)
@Name("playNHighscoreCommunicationResource")
@BypassInterceptors
public class PlayNHighscoreCommunicationResource extends AbstractResource {

	private HttpServletResponse response;

	@In(create = true)
	EntityManager entityManager;

	private String action = "";

	private String mode;

	@Override
	public String getResourcePath() {
		return "/highscore";
	}

	@Override
	public void getResource(final HttpServletRequest request,
			final HttpServletResponse response) throws ServletException,
			IOException {
		new ContextualHttpServletRequest(request) {
			@Override
			public void process() throws IOException {
				doWork(request, response);
			}
		}.run();
	}

	private void doWork(HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		this.response = response;
		QuizHighscore quizHighscore = readOutJSONData(request);
		try {

			Transaction.instance().begin();

			QuizHighscoreBean quizHighscoreBean = (QuizHighscoreBean) Component
			.getInstance("quizHighscoreBean");
			
			int placeAllTime = 0;
			int placeThisWeek = 0;
			List<QuizHighscore> highscores = new ArrayList<QuizHighscore>();
			
			if(action.equals("GetEvaluation")){
				
				placeAllTime = quizHighscoreBean.getPlaceAllTime(quizHighscore.getScore()) + 1;
				placeThisWeek = quizHighscoreBean.getPlaceThisWeek(quizHighscore.getScore()) + 1;
			}else if(action.equals("Store")){
				if (quizHighscore != null) {
					quizHighscoreBean.addHighscore(quizHighscore);
					
				}
			}else if(action.equals("GetHighscores")){
				if(mode.equals("All Time")){
				 highscores = quizHighscoreBean.getHighScoresAllTime();	
				}else{
					highscores = quizHighscoreBean.getHighScoresThisWeek();	
				}
			}
				
			JSONObject jsonObject = createJSONForFeedback(highscores, placeAllTime, placeThisWeek);

			sendJSONObject(jsonObject);

			Transaction.instance().commit();

		} catch (NotSupportedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SystemException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RollbackException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (HeuristicMixedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (HeuristicRollbackException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void sendJSONObject(JSONObject jsonObject) {
		OutputStream outstream = null;

		try {
			response.setContentType("text/plain");
			outstream = response.getOutputStream();
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					outstream));
			jsonObject.writeJSONString(out);
			out.flush();
			outstream.flush();
			outstream.close();

		} catch (IOException e) {
			System.out.println("Exception!");
		}

	}

	private JSONObject createJSONForFeedback(List<QuizHighscore> highscore, int placeAllTime, int placeThisWeek) {
		JSONObject jsonResult = new JSONObject();

		int platz = 0;
		for (QuizHighscore h : highscore) {
			Map<String, Object> m = new HashMap<String, Object>();
			m.put("Name", h.getUsername());
			m.put("PunkteAnzahl", h.getScore());
			m.put("Joker", h.getJoker());
			m.put("Frage", h.getQuestion());

			jsonResult.put("Platz" + platz, m);
			platz++;
		}
		jsonResult.put("Anzahl", platz);
		jsonResult.put("placeAllTime", placeAllTime);
		jsonResult.put("placeThisWeek", placeThisWeek);

		return jsonResult;
	}

	private QuizHighscore readOutJSONData(HttpServletRequest request)
			throws IOException {
		QuizHighscore quizHighscore = null;
		BufferedReader reader = request.getReader();
		StringBuilder sb = new StringBuilder();
		String line = reader.readLine();
		while (line != null) {
			sb.append(line + "\n");
			line = reader.readLine();
		}
		reader.close();
		String data = sb.toString();
		if (data.equals("")) {
			return null;
		}

		JSONParser parser = new JSONParser();

		quizHighscore = new QuizHighscore();

		quizHighscore.setCreated(new Date());

		Object obj;
		try {
			obj = parser.parse(data);
			JSONObject jsonObject = (JSONObject) obj;

			for (Object k : jsonObject.keySet()) {

				Object value = jsonObject.get(k);
				String key = (String) k;

				if (key.equals("Name")) {
					quizHighscore.setUsername((String) value);
				} else if (key.equals("PunkteAnzahl")) {
					quizHighscore.setScore(((Long)value).intValue());
				} else if (key.equals("Joker")) {
					quizHighscore.setJoker(((Long)value).intValue());
				} else if (key.equals("Frage")) {
					quizHighscore.setQuestion(((Long)value).intValue());
				} else if (key.equals("Action")){
					this.action = (String) value;
				} else if (key.equals("HighScoreMode")){
					this.mode = (String) value;
				}

			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return quizHighscore;
	}
}
