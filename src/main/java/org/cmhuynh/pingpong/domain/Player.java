package org.cmhuynh.pingpong.domain;

import java.io.Serializable;

/**
 * Model a player in a club
 *
 * @author Chau Huynh <cmhuynh at gmail dot com>
 */
public class Player implements Serializable {
    private String playerId;
    private String name;
    private String imageUrl;
    private Integer score = 1600;
    private Integer lastScore = 1600;
    private boolean status = true;

    public Player() {
    }

    public Player(String playerId, String name, String imageUrl, Integer score, Integer lastScore, boolean status) {
        this.playerId = playerId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.score = score;
        this.lastScore = lastScore;
        this.status = status;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getLastScore() {
        return lastScore;
    }

    public void setLastScore(Integer lastScore) {
        this.lastScore = lastScore;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
