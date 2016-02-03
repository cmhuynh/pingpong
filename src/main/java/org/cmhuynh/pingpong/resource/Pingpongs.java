package org.cmhuynh.pingpong.resource;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.appengine.api.users.User;
import org.cmhuynh.pingpong.domain.Club;
import org.cmhuynh.pingpong.domain.Match;
import org.cmhuynh.pingpong.domain.Player;
import org.cmhuynh.pingpong.service.PingpongService;

import javax.inject.Named;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Api(
    name = "pingpong",
    version = "v1",
    scopes = {Constants.EMAIL_SCOPE},
    clientIds = {Constants.WEB_CLIENT_ID, Constants.ANDROID_CLIENT_ID, Constants.IOS_CLIENT_ID, Constants.API_EXPLORER_CLIENT_ID},
    audiences = {Constants.ANDROID_AUDIENCE}
)
public class Pingpongs {

  private PingpongService pingpongService = new PingpongService();

  @ApiMethod(name = "pingpongs.getClubs")
  public List<Club> getClubs() {
    return Collections.singletonList(new Club("clb1q9", "CLB1 Q9")); // return (ArrayList<Club>) pingpongService.getClubs();
  }

  @ApiMethod(name = "pingpongs.saveClub", httpMethod = "post")
  public Club saveClubs(User user, Club club) {
    pingpongService.saveClub(club);
    return club;
  }

  @ApiMethod(name = "pingpongs.getPlayers")
  public List<Player> getPlayersByClub(@Named("clubId") String clubId) {
//    return pingpongService.getPlayersByClub(clubId);
    List<Player> players = Arrays.asList(
            new Player("chau", "Chau", null, 1592, 1600, true),
            new Player("tri", "Tri", null, 1608, 1600, true)
    );
    return players;
  }

  @ApiMethod(name = "pingpongs.getPlayerMatches")
  public List<Match> getPlayerMatchesByYear(@Named("playerId") String playerId, @Named("clubId") String clubId, @Named("year") int year) {
//    return pingpongService.getPlayerMatchesByYear(playerId, clubId, year);
    Date today = new Date();
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(today);
    calendar.add(Calendar.DATE, -1);
    long yesterday = calendar.getTimeInMillis();
    List<Match> matches = Arrays.asList(
            new Match(yesterday, "Friendly match 1", "chau", "Chau Huynh", 0, 1592, -8, "tri", "Tri Le", 3, 1608, 8),
            new Match(today.getTime(), "Friendly match 2", "chau", "Chau Huynh", 0, 1586, -8, "tri", "Tri Le", 3, 1614, 8)
    );
    return matches;
  }
}
