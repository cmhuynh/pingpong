package org.cmhuynh.pingpong.domain;

/**
 * Model a club admin who has permission to manage club data
 *
 * @author Chau Huynh <cmhuynh at gmail dot com>
 */
public class ClubAdmin {
    String clubId;
    String adminEmail;

    public ClubAdmin() {
    }

    public ClubAdmin(String clubId, String adminEmail) {
        this.clubId = clubId;
        this.adminEmail = adminEmail;
    }

    public String getClubId() {
        return clubId;
    }

    public void setClubId(String clubId) {
        this.clubId = clubId;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }
}
