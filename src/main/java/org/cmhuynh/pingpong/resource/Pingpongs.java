package org.cmhuynh.pingpong.resource;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.response.ForbiddenException;
import com.google.appengine.api.users.User;
import org.cmhuynh.pingpong.domain.Club;
import org.cmhuynh.pingpong.domain.ClubAdmin;
import org.cmhuynh.pingpong.domain.Match;
import org.cmhuynh.pingpong.domain.Player;
import org.cmhuynh.pingpong.service.PermissionHelper;
import org.cmhuynh.pingpong.service.PingpongService;

import javax.inject.Named;
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
  private PermissionHelper permissionHelper = new PermissionHelper();

  @ApiMethod(name = "pingpongs.getClubs", path = "player/club")
  public List<Club> getClubs() {
    return pingpongService.getClubs();
  }

  @ApiMethod(name = "admin.saveClub", httpMethod = "post", path = "admin/club")
  public Club saveClub(User user, Club club) throws ServiceException {
    if (permissionHelper.isClubAdmin(club.getClubId(), user)) {
      pingpongService.saveClub(club);
      return club;
    } else {
      throw new ForbiddenException("No permission to manage Club");
    }
  }

  @ApiMethod(name = "admin.getClubsByAdmin", path = "admin/club")
  public List<Club> getClubsByAdmin(User user) {
    if (user != null) {
      return pingpongService.getClubsByAdmin(user.getEmail());
    } else {
      return Collections.emptyList();
    }
  }

  @ApiMethod(name = "admin.saveClubAdmin", httpMethod = "post", path = "admin/clubAdmin")
  public ClubAdmin saveClubAdmin(User user, ClubAdmin clubAdmin) throws ServiceException {
    if (permissionHelper.isClubAdmin(clubAdmin.getClubId(), user)) {
      pingpongService.saveClubAdmin(clubAdmin);
      return clubAdmin;
    } else {
      throw new ForbiddenException("No permission to manage Club Admin");
    }
  }

  @ApiMethod(name = "pingpongs.getPlayers", path = "player/player")
  public List<Player> getPlayersByClub(@Named("clubId") String clubId) {
    return pingpongService.getPlayersByClub(clubId);
  }

  @ApiMethod(name = "admin.savePlayer", httpMethod = "post", path = "admin/player")
  public Player savePlayer(User user, @Named("clubId") String clubId, Player player) throws ServiceException {
    if (permissionHelper.isClubAdmin(clubId, user)) {
      pingpongService.savePlayer(clubId, player);
      return player;
    } else {
      throw new ForbiddenException("No permission to manage Player");
    }
  }

  @ApiMethod(name = "pingpongs.getPlayerMatches", path="player/match")
  public List<Match> getPlayerMatchesByYear(@Named("playerId") String playerId, @Named("clubId") String clubId, @Named("year") int year) {
    return pingpongService.getPlayerMatchesByYear(playerId, clubId, year);
  }

  @ApiMethod(name = "admin.savePlayerMatch", httpMethod = "post", path="admin/match")
  public Match savePlayerMatch(User user, @Named("clubId") String clubId, Match match) throws ServiceException {
    if (permissionHelper.isClubAdmin(clubId, user)) {
      pingpongService.saveMatch(clubId, match);
      return match;
    } else {
      throw new ForbiddenException("No permission to manage Match");
    }
  }

  @ApiMethod(name = "admin.initSampleData", httpMethod = "post", path="admin/init")
  public void initSampleData(User user) throws ServiceException {
    if (permissionHelper.isAppAdmin()) {
      String clubId = "clb1q9";
      pingpongService.saveClub(new Club(clubId, "CLB1 Q9", true));

      pingpongService.savePlayer(clubId, new Player("cmhuynh@gmail.com", "Chau Huynh", null, 1600, 1600, true, "A"));
      pingpongService.savePlayer(clubId, new Player("teogamingcenter@gmail.com", "Tri Le", null, 1600, 1600, true, "A"));

      Date today = new Date();
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(today);
      calendar.add(Calendar.DATE, -1);
      long yesterday = calendar.getTimeInMillis();
      pingpongService.saveMatch(clubId, new Match(yesterday, "Friendly match 1", "cmhuynh@gmail.com", "Chau Huynh", 0, 1600, -8, "teogamingcenter@gmail.com", "Tri Le", 3, 1600, 8));
      pingpongService.saveMatch(clubId, new Match(today.getTime(), "Friendly match 2", "cmhuynh@gmail.com", "Chau Huynh", 0, 1592, -7, "teogamingcenter@gmail.com", "Tri Le", 3, 1608, 7));

      pingpongService.saveClubAdmin(new ClubAdmin(clubId, "cmhuynh@gmail.com"));
      pingpongService.saveClubAdmin(new ClubAdmin(clubId, "teogamingcenter@gmail.com"));
      pingpongService.saveClubAdmin(new ClubAdmin(clubId, "example@example.com")); // OAuth on local yield this magic email
    } else {
      throw new ForbiddenException("No permission to manage Club");
    }
  }
}
