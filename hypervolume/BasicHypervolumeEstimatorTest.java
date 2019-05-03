

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
    long maxTime = 100000L;
    
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
    public void exampleRandomRun()
    throws IllegalNumberOfObjectivesException {
        int numberOfObjectives = 2;
        double[] lowerBounds = new double[ numberOfObjectives];
        double[] upperBounds = new double[ numberOfObjectives];
        // set box contstraints for MC sampling of objective vectors
        for (int i=0; i<numberOfObjectives; i++) {
            lowerBounds[i] = 0.0;
            upperBounds[i] = 2.0;
        }
        HypervolumeEstimator estimator[] = new HypervolumeEstimator[4];
        estimator[0] = new BasicHypervolumeEstimator(numberOfObjectives, lowerBounds, upperBounds);
        estimator[1] = new IncrementalHypervolumeEstimator(numberOfObjectives, lowerBounds, upperBounds);
        estimator[2] = new EfficientIncrementalHypervolumeEstimator(numberOfObjectives, lowerBounds, upperBounds);
        estimator[3] = new DynamicHypervolumeEstimator(numberOfObjectives, lowerBounds, upperBounds);
        
        
        estimator[0].setNumberOfSamplesToComparePerIteration(5000);
        estimator[1].setNumberOfSamplesToComparePerIteration(5000);
        estimator[2].setNumberOfSamplesToComparePerIteration(5000);
        estimator[3].setTimeLimit(maxTime);
        SharedTest.exampleRandomRun(estimator,new Random(0L),numberOfObjectives,10000);      
    }
    
    @Test(timeout=200000)
    public void exampleEvolvingRun()
    throws IllegalNumberOfObjectivesException {
        int numberOfObjectives = 2;
        double[] lowerBounds = new double[ numberOfObjectives];
        double[] upperBounds = new double[ numberOfObjectives];
        // set box contstraints for MC sampling of objective vectors
        for (int i=0; i<numberOfObjectives; i++) {
            lowerBounds[i] = 0.0;
            upperBounds[i] = 2.0;
        }
        HypervolumeEstimator estimator[] = new HypervolumeEstimator[4];
        estimator[0] = new BasicHypervolumeEstimator(numberOfObjectives, lowerBounds, upperBounds);
        estimator[1] = new IncrementalHypervolumeEstimator(numberOfObjectives, lowerBounds, upperBounds);
        estimator[2] = new EfficientIncrementalHypervolumeEstimator(numberOfObjectives, lowerBounds, upperBounds);
        estimator[3] = new DynamicHypervolumeEstimator(numberOfObjectives, lowerBounds, upperBounds);
        
        
        estimator[0].setNumberOfSamplesToComparePerIteration(5000);
        estimator[1].setNumberOfSamplesToComparePerIteration(5000);
        estimator[2].setNumberOfSamplesToComparePerIteration(5000);
        estimator[3].setTimeLimit(maxTime);
        SharedTest.exampleEvolvingRun(estimator,0L,numberOfObjectives,10000);      
    }
    
}
