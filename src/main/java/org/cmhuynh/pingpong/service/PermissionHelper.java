package org.cmhuynh.pingpong.service;

import com.google.appengine.api.oauth.OAuthServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import org.cmhuynh.pingpong.domain.ClubAdmin;
import org.cmhuynh.pingpong.resource.Constants;

import java.util.List;

/**
 * Little helper to check permission to manage data by this application
 *
 * @author Chau Huynh <cmhuynh at gmail dot com>
 */
public class PermissionHelper {

    private DatastoreHelper datastoreHelper;

    public boolean isAppAdmin() {
        boolean isAdmin = false;
        try {
            UserService userService = UserServiceFactory.getUserService();
            isAdmin = userService.isUserAdmin();
        } catch (IllegalStateException e1) {
            try {
                isAdmin = OAuthServiceFactory.getOAuthService().isUserAdmin(Constants.EMAIL_SCOPE);
            } catch (Exception e2) {}
        }
        return isAdmin;
    }

    public boolean isClubAdmin(String clubId, User user) {
        if (user == null || user.getEmail() == null) {
            return false;
        }
        List<ClubAdmin> clubAdmins = datastoreHelper.getClubAdmins(clubId);
        for (ClubAdmin clubAdmin : clubAdmins) {
            if (clubAdmin.getAdminEmail().equalsIgnoreCase(user.getEmail())) {
                return true;
            }
        }
        return false;
    }

    public boolean canManageClub(String clubId, User user) {
        return isClubAdmin(clubId, user) || isAppAdmin();
    }
}
