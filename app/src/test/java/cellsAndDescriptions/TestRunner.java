package cellsAndDescriptions;

/**
 * Created by kirtsim on 13/02/2017.
 */

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class TestRunner {
    public static void main(String[] args) {
        Result res = JUnitCore.runClasses(PuzzleArraysControllerTest.class);

        for(Failure f : res.getFailures())
            System.out.println(f.toString());
        System.out.println(res.wasSuccessful());
    }
}