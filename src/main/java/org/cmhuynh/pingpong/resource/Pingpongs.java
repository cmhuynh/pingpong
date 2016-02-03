package org.cmhuynh.pingpong.resource;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.response.ForbiddenException;
import com.google.appengine.api.users.User;
import org.cmhuynh.pingpong.domain.Club;
import org.cmhuynh.pingpong.domain.Match;
import org.cmhuynh.pingpong.domain.Player;
import org.cmhuynh.pingpong.service.PermissionHelper;
import org.cmhuynh.pingpong.service.PingpongService;

import javax.inject.Named;
import java.util.Calendar;
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

  @ApiMethod(name = "pingpongs.getClubs")
  public List<Club> getClubs() {
    return pingpongService.getClubs();
  }

  @ApiMethod(name = "pingpongs.saveClub", httpMethod = "post")
  public Club saveClubs(User user, Club club) throws ServiceException {
    if (permissionHelper.canManageClub(club.getClubId(), user)) {
      pingpongService.saveClub(club);
      return club;
    } else {
      throw new ForbiddenException("No permission to manage Club");
    }
  }

  @ApiMethod(name = "pingpongs.getPlayers")
  public List<Player> getPlayersByClub(@Named("clubId") String clubId) {
    return pingpongService.getPlayersByClub(clubId);
  }

  @ApiMethod(name = "pingpongs.getPlayerMatches")
  public List<Match> getPlayerMatchesByYear(@Named("playerId") String playerId, @Named("clubId") String clubId, @Named("year") int year) {
    return pingpongService.getPlayerMatchesByYear(playerId, clubId, year);
  }

  @ApiMethod(name = "pingpongs.initSampleData", httpMethod = "post")
  public void initSampleData(User user) throws ServiceException {
    if (permissionHelper.isAppAdmin()) {
      String clubId = "clb1q9";
      pingpongService.saveClub(new Club(clubId, "CLB1 Q9", true));

      pingpongService.savePlayer(clubId, new Player("chau", "Chau Huynh", null, 1600, 1600, true));
      pingpongService.savePlayer(clubId, new Player("tri", "Tri Le", null, 1600, 1600, true));

      Date today = new Date();
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(today);
      calendar.add(Calendar.DATE, -1);
      long yesterday = calendar.getTimeInMillis();
      pingpongService.saveMatch(clubId, new Match(yesterday, "Friendly match 1", "chau", "Chau Huynh", 0, 1600, -8, "tri", "Tri Le", 3, 1600, 8));
      pingpongService.saveMatch(clubId, new Match(today.getTime(), "Friendly match 2", "chau", "Chau Huynh", 0, 1592, -7, "tri", "Tri Le", 3, 1608, 7));
    } else {
      throw new ForbiddenException("No permission to manage Club");
    }
  }
}
