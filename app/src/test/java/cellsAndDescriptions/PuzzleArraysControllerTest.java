package cellsAndDescriptions;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by kirtsim on 13/02/2017.
 */

public class PuzzleArraysControllerTest {

    byte[][] unsettledDescriptions = {};

    @Test
    public void testSettleHorizontalDescriptions() {
        setUnsettledDescriptions();
        PuzzleArraysController.settleHorizontalDescriptions(unsettledDescriptions);
        Assert.assertArrayEquals(getSolutionForHorizontalSettling(), unsettledDescriptions);
    }

    @Test
    public void testSettleVerticalDescriptions() {
        setUnsettledDescriptions();
        PuzzleArraysController.settleVerticalDescriptions(unsettledDescriptions);
        Assert.assertArrayEquals(getSolutionForVerticalSettling(), unsettledDescriptions);
    }

    private void setUnsettledDescriptions() {
        this.unsettledDescriptions=  new byte[][] {
                {3, 0, 1, 2, 3},
                {2, 1, 2, 0, 0},
                {4, 1, 2, 3, 4},
                {1, 0, 0, 0, 1},
                {2, 0, 1, 0, 2},
                {1, 0, 1, 0, 0}};
    }

    private byte[][] getSolutionForHorizontalSettling() {
        return new byte[][] {
                {3, 0, 1, 2, 3},
                {2, 0, 0, 1, 2},
                {4, 1, 2, 3, 4},
                {1, 0, 0, 0, 1},
                {2, 0, 0, 1, 2},
                {1, 0, 0, 0, 1}};
    }

    private byte[][] getSolutionForVerticalSettling() {
        return new byte[][] {
                {3, 0, 0, 0, 0},
                {2, 0, 1, 0, 0},
                {4, 0, 2, 0, 3},
                {1, 0, 2, 0, 4},
                {2, 1, 1, 2, 1},
                {1, 1, 1, 3, 2}};
    }


}
