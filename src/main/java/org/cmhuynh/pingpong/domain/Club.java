package org.cmhuynh.pingpong.domain;

import java.io.Serializable;

/**
 * Model a club / league that matches are organized and competed in
 *
 * @author Chau Huynh <cmhuynh at gmail dot com>
 */
public class Club implements Serializable {
    private String clubId;
    private String name;
    private boolean status;

    public Club() {
    }

    public Club(String clubId, String name, boolean status) {
        this.clubId = clubId;
        this.name = name;
        this.status = status;
    }

    public String getClubId() {
        return clubId;
    }

    public void setClubId(String clubId) {
        this.clubId = clubId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
