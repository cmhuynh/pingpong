package org.cmhuynh.pingpong.service;

import com.google.appengine.repackaged.com.google.common.base.Function;
import com.google.appengine.repackaged.com.google.common.base.Predicate;
import com.google.appengine.repackaged.com.google.common.collect.Iterables;
import com.google.appengine.repackaged.com.google.common.collect.Lists;
import com.google.appengine.repackaged.com.google.common.collect.Maps;
import org.cmhuynh.pingpong.domain.Club;
import org.cmhuynh.pingpong.domain.ClubAdmin;
import org.cmhuynh.pingpong.domain.Match;
import org.cmhuynh.pingpong.domain.Player;

import java.util.*;

/**
 * Manage entities including Player, Club, and Match.
 * Process ratings on match results.
 *
 * @author Chau Huynh <cmhuynh at gmail dot com>
 */
public class PingpongService {
    private DatastoreHelper datastoreHelper = new DatastoreHelper();

    public void saveClub(Club club) {
        datastoreHelper.saveClubs(Collections.singletonList(club));
    }

    public List<Club> getClubs() {
        return datastoreHelper.getClubs();
    }

    public void saveClubAdmin(ClubAdmin clubAdmin) {
        datastoreHelper.createClubAdmins(Collections.singletonList(clubAdmin));
    }

    public List<Club> getClubsByAdmin(String adminEmail) {
        List<ClubAdmin> clubsAdmins = datastoreHelper.getClubAdminsByAdmin(adminEmail);
        final List<String> clubIds = Lists.transform(clubsAdmins, new Function<ClubAdmin, String>() {
            @Override
            public String apply(ClubAdmin clubAdmin) {
                return clubAdmin.getClubId();
            }
        });
        List<Club> clubs = getClubs();
        Iterable<Club> filteredClubs = Iterables.filter(clubs, new Predicate<Club>() {
            @Override
            public boolean apply(Club club) {
                return clubIds.contains(club.getClubId());
            }
        });
        return Lists.newArrayList(filteredClubs);
    }

    public void savePlayer(String clubId, Player player) {
        datastoreHelper.savePlayers(clubId, Collections.singletonList(player));
    }

    public List<Player> getPlayersByClub(String clubId) {
        return datastoreHelper.getPlayersByClub(clubId);
    }

    public void saveMatch(String clubId, Match match) {
        List<String> playerIds = Arrays.asList(match.getP1Id(), match.getP2Id());
        Map<String, Player> idPlayerMap = getIdPlayerMap(clubId, playerIds);
        Player player1 = idPlayerMap.get(match.getP1Id());
        Player player2 = idPlayerMap.get(match.getP2Id());

        processRatingScore(player1, player2, match);

        int year = getYear(new Date(match.getMatchDate()));
        datastoreHelper.saveMatches(clubId, player1.getPlayerId(), year, Collections.singletonList(match));
        Match p2Match = p2Match(match);
        datastoreHelper.saveMatches(clubId, player2.getPlayerId(), year, Collections.singletonList(p2Match));

        datastoreHelper.savePlayers(clubId, Arrays.asList(player1, player2));
    }

    private Map<String, Player> getIdPlayerMap(String clubId, List<String> playerIds) {
        List<Player> players = datastoreHelper.getPlayerByIds(clubId, playerIds);
        return Maps.uniqueIndex(players, new Function<Player, String>() {
            @Override
            public String apply(Player player) {
                return player.getPlayerId();
            }
        });
    }

    private void processRatingScore(Player p1, Player p2, Match match) {
        boolean p1Win = match.getP1Set() > match.getP2Set();
        int[] scoreExchanged = ScoreHelper.exchangeScore(p1.getScore(), p2.getScore(), p1Win);

        match.setP1Score(p1.getScore());
        match.setP1Gain(scoreExchanged[0]);
        match.setP2Score(p2.getScore());
        match.setP2Gain(scoreExchanged[1]);

        p1.setLastScore(p1.getScore());
        p1.setScore(p1.getScore() + scoreExchanged[0]);

        p2.setLastScore(p2.getScore());
        p2.setScore(p2.getScore() + scoreExchanged[1]);

        populateMatchWithDenormalizedData(p1, p2, match);
    }

    private void populateMatchWithDenormalizedData(Player p1, Player p2, Match match) {
        match.setP1Name(p1.getName());
        match.setP2Name(p2.getName());
    }

    private int getYear(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }

    private Match p2Match(Match p1Match) {
        return new Match(
                p1Match.getMatchDate(),
                p1Match.getMatchName(),
                p1Match.getP2Id(), p1Match.getP2Name(), p1Match.getP2Set(), p1Match.getP2Score(), p1Match.getP2Gain(),
                p1Match.getP1Id(), p1Match.getP1Name(), p1Match.getP1Set(), p1Match.getP1Score(), p1Match.getP1Gain()
        );
    }

    public List<Match> getPlayerMatchesByYear(String playerId, String clubId, int year) {
        return datastoreHelper.getPlayerMatchesByYear(playerId, clubId, year);
    }
}
