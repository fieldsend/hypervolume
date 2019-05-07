

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * The test class BasicHypervolumeEstimatorTest.
 *
 * Class provides tests for BasicHypervolumeEstimator class.
 *
 * @author  Jonathan Fieldsend
 * @version 07/05/2019
 */
public class BasicHypervolumeEstimatorTest
{
    BasicHypervolumeEstimator estimator;
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
    throws IllegalNumberOfObjectivesException 
    {
        int numberOfObjectives = 2;
        double[] lowerBounds = new double[ numberOfObjectives];
        double[] upperBounds = new double[ numberOfObjectives];
        // set box contstraints for MC sampling of objective vectors
        for (int i=0; i<numberOfObjectives; i++) {
            lowerBounds[i] = 0.0;
            upperBounds[i] = 2.0;
        }
        estimator = new BasicHypervolumeEstimator(numberOfObjectives, lowerBounds, upperBounds);
    }

    /**
     * Tears down the test fixture.
     *
     * Called after every test case method.
     */
    @After
    public void tearDown()
    {
        estimator = null;
    }
    
    @Test(timeout=200000)
    public void setNumberOfSamplesToComparePerIterationTest()
    {
        estimator.setNumberOfSamplesToComparePerIteration(1000);
        assertEquals(estimator.numberOfSamples,1000);
        estimator.setNumberOfSamplesToComparePerIteration(0);
        assertEquals(estimator.numberOfSamples,1);
    }
    
    @Test(timeout=200000, expected = UnsupportedOperationException.class)
    public void setTimeLimitTest()
    {
        estimator.setTimeLimit((long) 1000);
    }

    @Test(timeout=200000)
    public void getNewHypervolumeEstimateTest()
    {
    
    }
    
    @Test(timeout=200000)
    public void instrumentedGetNewHypervolumeEstimateTest()
    {
    
    }
    
    @Test(timeout=200000)
    public void getCurrentHypervolumeEstimateTest()
    {
        assertEquals(estimator.getCurrentHypervolumeEstimate(),0.0,0.0);
    }
    
    @Test(timeout=200000)
    public void getCurrentParetoSetEstimateTest()
    {
        assertEquals(estimator.getCurrentParetoSetEstimate().size(),0);
    }
    
    @Test(timeout=200000)
    public void setInstrumentationFilenamesTest()
    {
    
    }
    
    @Test(timeout=200000)
    public void getNumberOfSamplesUsedForCurrentEstimateTest()
    {
        assertEquals(estimator.getNumberOfSamplesUsedForCurrentEstimate(),0);
    }
    
    
    
}
