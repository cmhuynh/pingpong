package org.cmhuynh.pingpong.service;

import com.google.appengine.api.oauth.OAuthServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.repackaged.com.google.common.base.Optional;
import com.google.appengine.repackaged.com.google.common.base.Predicate;
import com.google.appengine.repackaged.com.google.common.collect.Iterables;
import org.cmhuynh.pingpong.domain.ClubAdmin;
import org.cmhuynh.pingpong.resource.Constants;

import java.util.List;

/**
 * Little helper to check permission to manage data by this application
 *
 * @author Chau Huynh <cmhuynh at gmail dot com>
 */
public class PermissionHelper {

    private DatastoreHelper datastoreHelper = new DatastoreHelper();

    private boolean isProductionEnvironment() {
        return (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production);
    }
    public boolean isAppAdmin() {
        if (!isProductionEnvironment()) {
            return true;
        }

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

    public boolean isClubAdmin(final String clubId, final User user) {
        if (!isProductionEnvironment()) {
            return true;
        }

        if (!Optional.fromNullable(user).isPresent() || !Optional.fromNullable(user.getEmail()).isPresent()) {
            return false;
        }

        List<ClubAdmin> clubAdmins = datastoreHelper.getClubAdminsByAdmin(user.getEmail());
        Optional<ClubAdmin> clubAdminOpt = Iterables.tryFind(clubAdmins, new Predicate<ClubAdmin>() {
            @Override
            public boolean apply(ClubAdmin clubAdmin) {
                return clubAdmin.getClubId().equalsIgnoreCase(clubId);
            }
        });

        if(clubAdminOpt.isPresent()) {
            return true;
        } else {
            return isAppAdmin();
        }
    }
}
