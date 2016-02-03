package org.cmhuynh.pingpong.service;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.KeyRange;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import org.cmhuynh.pingpong.domain.Club;
import org.cmhuynh.pingpong.domain.Match;
import org.cmhuynh.pingpong.domain.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Little helper to store POJO entity model to DataStore persistence Entity, or retrieve from the datastore.
 *
 * @author Chau Huynh <cmhuynh at gmail dot com>
 */
public class DatastoreHelper {
    private static final String CLUB_KIND = "C"; // ancestor path: Club
    private static final String PLAYER_KIND = "P"; // ancestor path: Club > Player (read as player in a club/league)
    private static final String MATCH_YEAR_KIND = "MY"; // ancestor path: Club > Player > Year (read as a match by player in a league)
    private static final String MATCH_DETAIL_KIND = "MD"; // ancestor path: Club > Player > Year > Match Id (a match itself)

    // Club properties
    private static final String CLUB_ID = "cId";
    private static final String CLUB_NAME = "cName";
    // Player properties
    private static final String PLAYER_ID = "pId";
    private static final String PLAYER_NAME = "pName";
    private static final String PLAYER_IMAGE_URL = "pImage";
    private static final String PLAYER_SCORE = "pScore";
    private static final String PLAYER_LAST_SCORE = "pLastScore";
    private static final String PLAYER_STATUS = "pStatus";
    // Match properties
    private static final String MATCH_DATE = "mDate";
    private static final String MATCH_NAME = "mName";
    private static final String MATCH_P1_ID = "p1Id";
    private static final String MATCH_P1_NAME = "p1Name";
    private static final String MATCH_P1_SET = "p1Set";
    private static final String MATCH_P1_SCORE = "p1Score";
    private static final String MATCH_P1_GAIN = "p1Gain";
    private static final String MATCH_P2_ID = "p2Id";
    private static final String MATCH_P2_NAME = "p2Name";
    private static final String MATCH_P2_SET = "p2Set";
    private static final String MATCH_P2_SCORE = "p2Score";
    private static final String MATCH_P2_GAIN = "p2Gain";

    private DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();

    public void saveClubs(List<Club> clubs) {
        List<Entity> entities = new ArrayList<>(clubs.size());
        for (Club club : clubs) {
            Key clubKey = clubKey(club.getClubId());
            entities.add(asEntity(clubKey, club));
        }
        datastoreService.put(entities);
    }

    private Key clubKey(String clubId) {
        return KeyFactory.createKey(CLUB_KIND, clubId);
    }

    private Entity asEntity(Key clubKey, Club input) {
        Entity club = new Entity(clubKey);
        club.setProperty(CLUB_ID, input.getClubId());
        club.setProperty(CLUB_NAME, input.getName());
        return club;
    }

    private Club asClub(Entity entity) {
        String clubId = (String) entity.getProperty(CLUB_ID);
        String name = (String) entity.getProperty(CLUB_NAME);
        return new Club(clubId, name);
    }

    public List<Club> getClubs() {
        Query query = new Query(CLUB_KIND);
        PreparedQuery pq = datastoreService.prepare(query);
        List<Club> clubs = new ArrayList<>();
        for (Entity result : pq.asIterable()) {
            clubs.add(asClub(result));
        }
        return clubs;
    }

    public void savePlayers(String clubId, List<Player> players) {
        List<Entity> entities = new ArrayList<>(players.size());
        for (Player player : players) {
            Key playerKey = playerKey(clubId, player.getPlayerId());
            entities.add(asEntity(playerKey, player));
        }
        datastoreService.put(entities);
    }

    private Key playerKey(String clubId, String playerId) {
        Key parentKey = clubKey(clubId);
        return KeyFactory.createKey(parentKey, PLAYER_KIND, playerId);
    }

    private Entity asEntity(Key playerKey, Player input) {
        Entity player = new Entity(playerKey);
        player.setProperty(PLAYER_ID, input.getPlayerId());
        player.setProperty(PLAYER_NAME, input.getName());
        player.setProperty(PLAYER_IMAGE_URL, input.getImageUrl());
        player.setProperty(PLAYER_SCORE, input.getScore());
        player.setProperty(PLAYER_LAST_SCORE, input.getLastScore());
        player.setProperty(PLAYER_STATUS, input.isStatus());
        return player;
    }

    private Player asPlayer(Entity entity) {
        String playerId = (String) entity.getProperty(PLAYER_ID);
        String playerName = (String) entity.getProperty(PLAYER_NAME);
        String imageUrl = (String) entity.getProperty(PLAYER_IMAGE_URL);
        int score = (int) entity.getProperty(PLAYER_SCORE);
        int lastScore = (int) entity.getProperty(PLAYER_LAST_SCORE);
        boolean status = (boolean) entity.getProperty(PLAYER_STATUS);
        return new Player(playerId, playerName, imageUrl, score, lastScore, status);
    }

    public List<Player> getPlayersByClub(String clubId) {
        Key clubKey = clubKey(clubId);
        Query query = new Query(PLAYER_KIND, clubKey);
        PreparedQuery pq = datastoreService.prepare(query);
        List<Player> players = new ArrayList<>();
        for (Entity result : pq.asIterable()) {
            players.add(asPlayer(result));
        }
        return players;
    }

    public List<Player> getPlayerByIds(String clubId, List<String> playerIds) {
        List<Key> playerKeys = new ArrayList<>();
        for (String playerId : playerIds) {
            playerKeys.add(playerKey(clubId, playerId));
        }
        Filter keyFilter = new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.IN, playerKeys);
        Query query =  new Query(PLAYER_KIND).setFilter(keyFilter);
        PreparedQuery pq = datastoreService.prepare(query);
        List<Player> players = new ArrayList<>();
        for (Entity result : pq.asIterable()) {
            players.add(asPlayer(result));
        }
        return players;
    }

    public void saveMatches(String clubId, String playerId, int year, List<Match> matches) {
        List<Entity> entities = new ArrayList<>(matches.size());
        Iterator<Key> matchKeys = matchKeys(clubId, playerId, year, matches.size()).iterator();
        for (Match match : matches) {
            Key matchKey = matchKeys.next();
            entities.add(asEntity(matchKey, match));
        }
        datastoreService.put(entities);
    }

    private KeyRange matchKeys(String clubId, String playerId, int year, int num) {
        Key parentKey = matchYearKey(clubId, playerId, year);
        return datastoreService.allocateIds(parentKey, MATCH_DETAIL_KIND, num);
    }

    private Key matchYearKey(String club, String playerId, int year) {
        Key playerKey = playerKey(club, playerId);
        return KeyFactory.createKey(playerKey, MATCH_YEAR_KIND, year);
    }

    private Entity asEntity(Key matchKey, Match input) {
        Entity match = new Entity(matchKey);
        match.setProperty(MATCH_DATE, input.getMatchDate());
        match.setProperty(MATCH_NAME, input.getMatchName());
        match.setProperty(MATCH_P1_ID, input.getP1Id());
        match.setProperty(MATCH_P1_NAME, input.getP1Name());
        match.setProperty(MATCH_P1_SET, input.getP1Set());
        match.setProperty(MATCH_P1_SCORE, input.getP1Score());
        match.setProperty(MATCH_P1_GAIN, input.getP1Gain());
        match.setProperty(MATCH_P2_ID, input.getP2Id());
        match.setProperty(MATCH_P2_NAME, input.getP2Name());
        match.setProperty(MATCH_P2_SET, input.getP2Set());
        match.setProperty(MATCH_P2_SCORE, input.getP2Score());
        match.setProperty(MATCH_P2_GAIN, input.getP2Gain());
        return match;
    }

    private Match asMatch(Entity entity) {
        long matchDate = (long) entity.getProperty(MATCH_DATE);
        String matchName = (String) entity.getProperty(MATCH_NAME);
        String p1Id = (String) entity.getProperty(MATCH_P1_ID);
        String p1Name = (String) entity.getProperty(MATCH_P1_NAME);
        int p1Set = (int) entity.getProperty(MATCH_P1_SET);
        int p1Score = (int) entity.getProperty(MATCH_P1_SCORE);
        int p1Gain = (int) entity.getProperty(MATCH_P1_GAIN);
        String p2Id = (String) entity.getProperty(MATCH_P2_ID);
        String p2Name = (String) entity.getProperty(MATCH_P2_NAME);
        int p2Set = (int) entity.getProperty(MATCH_P2_SET);
        int p2Score = (int) entity.getProperty(MATCH_P2_SCORE);
        int p2Gain = (int) entity.getProperty(MATCH_P2_GAIN);
        return new Match(matchDate, matchName, p1Id, p1Name, p1Set, p1Score, p1Gain, p2Id, p2Name, p2Set, p2Score, p2Gain);
    }

    public List<Match> getPlayerMatchesByYear(String playerId, String clubId, int year) {
        Key matchByYear = matchYearKey(clubId, playerId, year);
        Query query = new Query(MATCH_DETAIL_KIND, matchByYear);
        PreparedQuery pq = datastoreService.prepare(query);
        List<Match> matches = new ArrayList<>();
        for (Entity result : pq.asIterable()) {
            matches.add(asMatch(result));
        }
        return matches;
    }

}
