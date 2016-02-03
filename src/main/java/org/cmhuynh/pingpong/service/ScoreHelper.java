package org.cmhuynh.pingpong.service;

/**
 * Helper to calculate expected score and upset score given a match result.
 * <a href="http://www.teamusa.org/USA-Table-Tennis/Ratings/How%20Does%20the%20USATT%20Rating%20System%20work.aspx">How does the USATT Ratings Processing System work?</a>
 *
 * @author Chau Huynh <cmhuynh at gmail dot com>
 */
public class ScoreHelper {
    private static final int[] boundaries = { 0, 13, 38, 63, 88, 113, 138, 163, 188, 213, 238 };
    private static final int[] expected = { 8, 7, 6, 5, 4, 3, 2, 2, 1, 1, 0 };
    private static final int[] upset = { 8, 10, 13, 16, 20, 25, 30, 35, 40, 45, 50 };

    /**
     * Process a match result given the current scores of the 2 players, return pair of score that Player 1 and 2 earned accordingly
     */
    public static int[] exchangeScore(int p1Score, int p2Score, boolean p1Win) {
        int index = getIndex(p1Score, p2Score);

        int[] score = new int[2];
        if (p1Score > p2Score) { // P1 is upper hand
            if (p1Win) {
                score[0] = expected[index];
                score[1] = -expected[index];
            } else {
                score[0] = -upset[index];
                score[1] = upset[index];
            }
        } else { // P2 is upper hand
            if (!p1Win) {
                score[0] = -expected[index];
                score[1] = expected[index];
            } else {
                score[0] = upset[index];
                score[1] = -upset[index];
            }
        }

        return score;
    }

    /**
     * Find the sub-range index that the point spread between the two player.
     * Each sub-range is [lower bound inclusive, upper bound exclusive)
     */
    private static int getIndex(int p1Score, int p2Score) {
        int pointSpread = Math.abs(p1Score - p2Score);
        int index = boundaries.length - 1;
        while (index > 0 && pointSpread < boundaries[index]) {
            index--;
        }
        return index;
    }
}
