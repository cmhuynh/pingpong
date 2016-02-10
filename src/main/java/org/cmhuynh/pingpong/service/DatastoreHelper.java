package org.cmhuynh.pingpong.service;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.repackaged.com.google.common.base.Function;
import com.google.appengine.repackaged.com.google.common.collect.Iterables;
import com.google.appengine.repackaged.com.google.common.collect.Lists;
import org.cmhuynh.pingpong.domain.Club;
import org.cmhuynh.pingpong.domain.ClubAdmin;
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
    private static final String ADMIN_KIND = "A";
    private static final String CLUB_ADMIN_KIND = "CA";
    private static final String PLAYER_KIND = "P"; // ancestor path: Club > Player (read as player in a club/league)
    private static final String MATCH_YEAR_KIND = "MY"; // ancestor path: Club > Player > Year (read as a match by player in a league)
    private static final String MATCH_DETAIL_KIND = "MD"; // ancestor path: Club > Player > Year > Match Id (a match itself)
    // Club properties
    private static final String CLUB_ID = "cId";
    private static final String CLUB_NAME = "cName";
    private static final String CLUB_STATUS = "cStatus";

    // Club Admin
    private static final String CLUB_ADMIN_CID = "caId";
    private static final String CLUB_ADMIN_EMAIL = "caEmail";

    // Player properties
    private static final String PLAYER_ID = "pId";
    private static final String PLAYER_NAME = "pName";
    private static final String PLAYER_IMAGE_URL = "pImage";
    private static final String PLAYER_SCORE = "pScore";
    private static final String PLAYER_LAST_SCORE = "pLastScore";
    private static final String PLAYER_STATUS = "pStatus";
    private static final String PLAYER_LEVEL = "pLevel";

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
        List<Entity> entities = Lists.transform(clubs, clubAsEntity());
        datastoreService.put(entities);
    }

    private Key clubKey(String clubId) {
        return KeyFactory.createKey(CLUB_KIND, clubId);
    }

    private Function<Club, Entity> clubAsEntity() {
        return new Function<Club, Entity>() {
            @Override
            public Entity apply(Club club) {
                Key clubKey = clubKey(club.getClubId());
                Entity entity = new Entity(clubKey);
                entity.setProperty(CLUB_ID, club.getClubId());
                entity.setProperty(CLUB_NAME, club.getName());
                entity.setProperty(CLUB_STATUS, club.isStatus());
                return entity;
            }
        };
    }

   private  Function<Entity, Club> entityAsClub() {
        return new Function<Entity, Club>() {
            @Override
            public Club apply(Entity entity) {
                String clubId = (String) entity.getProperty(CLUB_ID);
                String name = (String) entity.getProperty(CLUB_NAME);
                boolean status = (boolean) entity.getProperty(CLUB_STATUS);
                return new Club(clubId, name, status);
            }
        };
    }

    public List<Club> getClubs() {
        Query query = new Query(CLUB_KIND);
        PreparedQuery pq = datastoreService.prepare(query);
        Iterable<Club> clubs = Iterables.transform(pq.asIterable(), entityAsClub());
        return Lists.newArrayList(clubs);
    }

    public void createClubAdmins(List<ClubAdmin> clubAdmins) {
        List<Entity> entities = Lists.transform(clubAdmins, clubAdminAsEntity());
        datastoreService.put(entities);
    }

    private Key adminKey(String adminEmail) {
        return KeyFactory.createKey(ADMIN_KIND, adminEmail);
    }

    private Function<ClubAdmin, Entity> clubAdminAsEntity() {
        return new Function<ClubAdmin, Entity>() {
            @Override
            public Entity apply(ClubAdmin clubAdmin) {
                Key parentKey = adminKey(clubAdmin.getAdminEmail());
                Key clubAdminKey = KeyFactory.createKey(parentKey, CLUB_ADMIN_KIND, clubAdmin.getClubId());
                Entity entity = new Entity(clubAdminKey);
                entity.setProperty(CLUB_ADMIN_CID, clubAdmin.getClubId());
                entity.setProperty(CLUB_ADMIN_EMAIL, clubAdmin.getAdminEmail());
                return entity;
            }
        };
    }

    private Function<Entity, ClubAdmin> entityAsClubAdmin() {
        return new Function<Entity, ClubAdmin>() {
            @Override
            public ClubAdmin apply(Entity entity) {
                String clubId = (String) entity.getProperty(CLUB_ADMIN_CID);
                String adminEmail = (String) entity.getProperty(CLUB_ADMIN_EMAIL);
                return new ClubAdmin(clubId, adminEmail);
            }
        };
    }

    public List<ClubAdmin> getClubAdminsByAdmin(String adminEmail) {
        Key adminKey = adminKey(adminEmail);
        Query query = new Query(CLUB_ADMIN_KIND, adminKey);
        PreparedQuery pq = datastoreService.prepare(query);
        Iterable<ClubAdmin> clubAdmins = Iterables.transform(pq.asIterable(), entityAsClubAdmin());
        return Lists.newArrayList(clubAdmins);
    }

    public void savePlayers(String clubId, List<Player> players) {
        List<Entity> entities = Lists.transform(players, playerAsEntity(clubId));
        datastoreService.put(entities);
    }

    private Key playerKey(String clubId, String playerId) {
        Key parentKey = clubKey(clubId);
        return KeyFactory.createKey(parentKey, PLAYER_KIND, playerId);
    }

    private Function<Player, Entity> playerAsEntity(final String clubId) {
        return new Function<Player, Entity>() {
            @Override
            public Entity apply(Player player) {
                Key playerKey = playerKey(clubId, player.getPlayerId());
                Entity entity = new Entity(playerKey);
                entity.setProperty(PLAYER_ID, player.getPlayerId());
                entity.setProperty(PLAYER_NAME, player.getName());
                entity.setProperty(PLAYER_IMAGE_URL, player.getImageUrl());
                entity.setProperty(PLAYER_SCORE, player.getScore());
                entity.setProperty(PLAYER_LAST_SCORE, player.getLastScore());
                entity.setProperty(PLAYER_STATUS, player.isStatus());
                entity.setProperty(PLAYER_LEVEL, player.getLevel());
                return entity;
            }
        };
    }

    private Function<Entity, Player> entityAsPlayer() {
        return new Function<Entity, Player>() {
            @Override
            public Player apply(Entity entity) {
                String playerId = (String) entity.getProperty(PLAYER_ID);
                String playerName = (String) entity.getProperty(PLAYER_NAME);
                String imageUrl = (String) entity.getProperty(PLAYER_IMAGE_URL);
                int score = ((Long) entity.getProperty(PLAYER_SCORE)).intValue();
                int lastScore = ((Long) entity.getProperty(PLAYER_LAST_SCORE)).intValue();
                boolean status = (boolean) entity.getProperty(PLAYER_STATUS);
                String level = (String) entity.getProperty(PLAYER_LEVEL);
                return new Player(playerId, playerName, imageUrl, score, lastScore, status, level);
            }
        };
    }

    public List<Player> getPlayersByClub(String clubId) {
        Key clubKey = clubKey(clubId);
        Query query = new Query(PLAYER_KIND, clubKey);
        PreparedQuery pq = datastoreService.prepare(query);
        Iterable<Player> players = Iterables.transform(pq.asIterable(), entityAsPlayer());
        return Lists.newArrayList(players);
    }

    public List<Player> getPlayerByIds(final String clubId, final List<String> playerIds) {
        List<Key> playerKeys = Lists.transform(playerIds, new Function<String, Key>() {
            @Override
            public Key apply(String playerId) {
                return playerKey(clubId, playerId);
            }
        });
        Filter keyFilter = new FilterPredicate(Entity.KEY_RESERVED_PROPERTY, FilterOperator.IN, playerKeys);
        Query query =  new Query(PLAYER_KIND).setFilter(keyFilter);
        PreparedQuery pq = datastoreService.prepare(query);
        Iterable<Player> players = Iterables.transform(pq.asIterable(), entityAsPlayer());
        return Lists.newArrayList(players);
    }

    public void saveMatches(String clubId, String playerId, int year, List<Match> matches) {
        Iterator<Key> matchKeys = matchKeys(clubId, playerId, year, matches.size()).iterator();
        List<Entity> entities = new ArrayList<>(matches.size());
        for (Match match : matches) {
            Key matchKey = matchKeys.next();
            entities.add(matchAsEntity(matchKey).apply(match));
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

    private Function<Match, Entity> matchAsEntity(final Key matchKey) {
        return new Function<Match, Entity>() {
            @Override
            public Entity apply(Match match) {
                Entity entity = new Entity(matchKey);
                entity.setProperty(MATCH_DATE, match.getMatchDate());
                entity.setProperty(MATCH_NAME, match.getMatchName());
                entity.setProperty(MATCH_P1_ID, match.getP1Id());
                entity.setProperty(MATCH_P1_NAME, match.getP1Name());
                entity.setProperty(MATCH_P1_SET, match.getP1Set());
                entity.setProperty(MATCH_P1_SCORE, match.getP1Score());
                entity.setProperty(MATCH_P1_GAIN, match.getP1Gain());
                entity.setProperty(MATCH_P2_ID, match.getP2Id());
                entity.setProperty(MATCH_P2_NAME, match.getP2Name());
                entity.setProperty(MATCH_P2_SET, match.getP2Set());
                entity.setProperty(MATCH_P2_SCORE, match.getP2Score());
                entity.setProperty(MATCH_P2_GAIN, match.getP2Gain());
                return entity;
            }
        };
    }

    private Function<Entity, Match> entityAsMatch() {
        return new Function<Entity, Match>() {
            @Override
            public Match apply(Entity entity) {
                long matchDate = (long) entity.getProperty(MATCH_DATE);
                String matchName = (String) entity.getProperty(MATCH_NAME);
                String p1Id = (String) entity.getProperty(MATCH_P1_ID);
                String p1Name = (String) entity.getProperty(MATCH_P1_NAME);
                int p1Set = ((Long) entity.getProperty(MATCH_P1_SET)).intValue();
                int p1Score = ((Long) entity.getProperty(MATCH_P1_SCORE)).intValue();
                int p1Gain = ((Long) entity.getProperty(MATCH_P1_GAIN)).intValue();
                String p2Id = (String) entity.getProperty(MATCH_P2_ID);
                String p2Name = (String) entity.getProperty(MATCH_P2_NAME);
                int p2Set = ((Long) entity.getProperty(MATCH_P2_SET)).intValue();
                int p2Score = ((Long) entity.getProperty(MATCH_P2_SCORE)).intValue();
                int p2Gain = ((Long) entity.getProperty(MATCH_P2_GAIN)).intValue();
                return new Match(matchDate, matchName, p1Id, p1Name, p1Set, p1Score, p1Gain, p2Id, p2Name, p2Set, p2Score, p2Gain);
            }
        };
    }

    public List<Match> getPlayerMatchesByYear(String playerId, String clubId, int year) {
        Key matchByYear = matchYearKey(clubId, playerId, year);
        Query query = new Query(MATCH_DETAIL_KIND, matchByYear);
        PreparedQuery pq = datastoreService.prepare(query);
        Iterable<Match> matches = Iterables.transform(pq.asIterable(), entityAsMatch());
        return Lists.newArrayList(matches);
    }

}
