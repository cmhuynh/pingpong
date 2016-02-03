package org.cmhuynh.pingpong.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * Model a match between 2 players in a club / league
 *
 * Each row of a match in a data store is stored by first player
 *
 * @author Chau Huynh <cmhuynh at gmail dot com>
 */
public class Match implements Serializable {
    private long matchDate;
    private String matchName;

    // player 1
    private String p1Id; // de-normalize for the sake of simplicity on retrieving details of one player
    private String p1Name;
    private Integer p1Set; // number of set player 1 won in the match
    private Integer p1Score; // his score when he played the match
    private Integer p1Gain; // score he earned on winning the match

    // player 2 // de-normalize to trade off storage for lookup (see ancestor path) and computation
    private String p2Id;
    private String p2Name;
    private Integer p2Set;
    private Integer p2Score;
    private Integer p2Gain;

    public Match() {
    }

    public Match(long matchDate, String matchName, String p1Id, String p1Name, Integer p1Set, Integer p1Score, Integer p1Gain, String p2Id, String p2Name, Integer p2Set, Integer p2Score, Integer p2Gain) {
        this.matchDate = matchDate;
        this.matchName = matchName;
        this.p1Id = p1Id;
        this.p1Name = p1Name;
        this.p1Set = p1Set;
        this.p1Score = p1Score;
        this.p1Gain = p1Gain;
        this.p2Id = p2Id;
        this.p2Name = p2Name;
        this.p2Set = p2Set;
        this.p2Score = p2Score;
        this.p2Gain = p2Gain;
    }

    public long getMatchDate() {
        return matchDate;
    }

    public void setMatchDate(long matchDate) {
        this.matchDate = matchDate;
    }

    public String getMatchName() {
        return matchName;
    }

    public void setMatchName(String matchName) {
        this.matchName = matchName;
    }

    public String getP1Id() {
        return p1Id;
    }

    public void setP1Id(String p1Id) {
        this.p1Id = p1Id;
    }

    public String getP1Name() {
        return p1Name;
    }

    public void setP1Name(String p1Name) {
        this.p1Name = p1Name;
    }

    public Integer getP1Set() {
        return p1Set;
    }

    public void setP1Set(Integer p1Set) {
        this.p1Set = p1Set;
    }

    public Integer getP1Score() {
        return p1Score;
    }

    public void setP1Score(Integer p1Score) {
        this.p1Score = p1Score;
    }

    public Integer getP1Gain() {
        return p1Gain;
    }

    public void setP1Gain(Integer p1Gain) {
        this.p1Gain = p1Gain;
    }

    public String getP2Id() {
        return p2Id;
    }

    public void setP2Id(String p2Id) {
        this.p2Id = p2Id;
    }

    public String getP2Name() {
        return p2Name;
    }

    public void setP2Name(String p2Name) {
        this.p2Name = p2Name;
    }

    public Integer getP2Set() {
        return p2Set;
    }

    public void setP2Set(Integer p2Set) {
        this.p2Set = p2Set;
    }

    public Integer getP2Score() {
        return p2Score;
    }

    public void setP2Score(Integer p2Score) {
        this.p2Score = p2Score;
    }

    public Integer getP2Gain() {
        return p2Gain;
    }

    public void setP2Gain(Integer p2Gain) {
        this.p2Gain = p2Gain;
    }
}
