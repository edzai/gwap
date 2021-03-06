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

package gwap.game;

import gwap.ResourceBean;
import gwap.action.TaggingBean;
import gwap.model.GameRound;
import gwap.model.action.Action;
import gwap.model.action.Tagging;
import gwap.model.action.TaggingCorrection;
import gwap.model.resource.ArtResource;
import gwap.wrapper.MatchingTag;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;

/**
 * This is the backing bean for one game session. It handles all actions that
 * can be executed during a game session. The game session itself is organized
 * in a business process.
 * 
 * @author Christoph Wieser
 */

@Name("gameSessionBean")
@Scope(ScopeType.CONVERSATION)
public class GameSessionBean extends AbstractGameSessionBean {

	private static final int MAXIMUM_RETRY_LOAD_NEW_RESOURCE = 5;

	private static final long serialVersionUID = 1L;

	@In(create=true)         private TaggingBean taggingBean;
	@In                      private ResourceBean resourceBean;
	@In(create=true)		 private ArtResource resource;
	@In(create=true)         private GameRoundScoringBean gameRoundScoringBean;
	
	private Map<Integer, List<MatchingTag>> recommendedTags = new HashMap<Integer, List<MatchingTag>>();
	
	@Override
	public void startGameSession() {
		startGameSession("imageLabeler");
	}
	
	@Override
	protected void loadNewResource() {
		resourceBean.updateResource();
		
		// check if person already played with this resource in the last 6 months
		Query q = entityManager.createNamedQuery("artResource.hasBeenPlayedSince");
		q.setParameter("person", person);
		Calendar monthsAgo = GregorianCalendar.getInstance();
		monthsAgo.add(Calendar.MONTH, -6);
		q.setParameter("startDate", monthsAgo.getTime());
		q.setParameter("resource", resourceBean.getResource());
		
		int emergencyStop = MAXIMUM_RETRY_LOAD_NEW_RESOURCE;
		while (((Number)q.getSingleResult()).longValue() > 0 && emergencyStop > 0) {
			log.info("Load new resource because user already played with #0", resourceBean.getResource());
			resourceBean.updateResource();
			q.setParameter("resource", resourceBean.getResource());
			emergencyStop--;
		}
	}
	
	public String recommendTag() {
		if (!roundExpired()) {
			Tagging tagging = taggingBean.recommendTag(resource, false);
			if (tagging.getTag() == null) {
				log.info("Could not add tag to gameround as it is invalid.");
			} else {
				List<Action> actions = gameRound.getActions();
				actions.add(tagging);
				log.info("Added #0 to game round", tagging.getTag().getName());
			}
			recommendedTags.put(gameRound.getNumber(), taggingBean.getRecommendedTags());
			return null;
		} else {
			//facesMessages.add("#{messages['game.round.expired']}");
			return "next";
		}
	}
	
	public void acceptTagCorrection(String originalTag, String correctedTag) {
		 log.info("Correcting tag from #0 to #1", originalTag, correctedTag);
		 
		 MatchingTag correctedMatchingTag = taggingBean.correctTag(originalTag, correctedTag);
		 
		 if (correctedMatchingTag != null) {
			 correctedMatchingTag.setTagCorrectionCompleted(true);
			 updateScore(correctedMatchingTag);
		 }
	}

	public void rejectTagCorrection(String originalTag, String correctedTag) {
		log.info("Reject tag correction from #0 to #1", originalTag, correctedTag);
		
		MatchingTag matchingTag = taggingBean.getMatchingTag(originalTag);
		matchingTag.setAlternativeTags(null);
		matchingTag.setTagCorrectionCompleted(true);
		
		TaggingCorrection taggingCorrection = new TaggingCorrection();
		initializeAction(taggingCorrection);
		taggingCorrection.setOriginalTag(taggingBean.findOrCreateTag(originalTag));
		taggingCorrection.setCorrectedTag(taggingBean.findOrCreateTag(correctedTag));
		taggingCorrection.setAccepted(false);
		entityManager.persist(taggingCorrection);
	}
	
	@Override
	public void endRound() {
		gameRound.getResources().add(resource);
		super.endRound();
	}
	
	@Observer("updateScore")
	public void updateScore(MatchingTag newlyMatchedTag) {
		gameRoundScoringBean.updateScore(resource, newlyMatchedTag);
		currentRoundScore = taggingBean.getCurrentScore();
	}
	
	public List<MatchingTag> getTagsForRound(GameRound gameRound) {
		Integer roundNr = gameRound.getNumber();
		if (roundNr != null)
			return recommendedTags.get(roundNr);
		else
			return null;
	}
}
