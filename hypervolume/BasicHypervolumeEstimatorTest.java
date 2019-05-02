

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.Random;

/**
 * The test class BasicHypervolumeEstimatorTest.
 *
 * @author  (your name)
 * @version (a version number or a date)
 */
public class BasicHypervolumeEstimatorTest
{
    /**
     * Default constructor for test class BasicHypervolumeEstimatorTest
     */
    public BasicHypervolumeEstimatorTest()
    {
    }

    /**
     * Sets up the test fixture.
     *
     * Called before every test case method.
     */
    @Before
    public void setUp()
    {
    }

    /**
     * Tears down the test fixture.
     *
     * Called after every test case method.
     */
    @After
    public void tearDown()
    {
    }
    
    
    @Test(timeout=200000)
    public void exampleRun()
    throws IllegalNumberOfObjectivesException {
        int numberOfObjectives = 2;
        double[] lowerBounds = new double[ numberOfObjectives];
        double[] upperBounds = new double[ numberOfObjectives];
        // set box contstraints for MC sampling of objective vectors
        for (int i=0; i<numberOfObjectives; i++) {
            lowerBounds[i] = 0.0;
            upperBounds[i] = 2.0;
        }
        HypervolumeEstimator estimator1 = new BasicHypervolumeEstimator(numberOfObjectives, lowerBounds, upperBounds);
        HypervolumeEstimator estimator2 = new IncrementalHypervolumeEstimator(numberOfObjectives, lowerBounds, upperBounds);
        estimator1.setNumberOfSamplesToComparePerIteration(5000);
        estimator2.setNumberOfSamplesToComparePerIteration(5000);
        
        
        SharedTest.exampleRun(estimator1,estimator2,new Random(0L),numberOfObjectives,1000);      
    }
}
