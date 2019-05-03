import java.lang.management.ThreadMXBean;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Iterator; 

/**
 * DynamicHypervolumeEstimator uses past history to incrementally
 * improve hypervolume estimation over time, and is smart in terms of comparing
 * previously non-dominated samples only to new entrants to the archive.
 * 
 * After comparing new Archive entrants to those samples previously non-dominated, 
 * it then checks how much time it has remaining to perform new samples and comparisons
 * and conducts these until the limit is reached
 * 
 * @author Jonathan Fieldsend 
 * @version 02/05/2019
 */
public class DynamicHypervolumeEstimator extends EfficientIncrementalHypervolumeEstimator
{
    private long nanoseconds = 0; // maximum number of time spent on _new_ MC samples
    private ThreadMXBean bean = ManagementFactory.getThreadMXBean( ); // object to track timings
    
    public DynamicHypervolumeEstimator(int numberOfObjectives, double[] lowerBounds, double[] upperBounds)
    throws IllegalNumberOfObjectivesException
    {
        super(numberOfObjectives,lowerBounds,upperBounds);
    }
    
    @Override
    public void setNumberOfSamplesToComparePerIteration(int numberOfSamples) 
    throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException("DynamicHyperVolumeEstimators are defined in terms of max new sample time, not number of samples");
    }
    
    @Override
    public void setTimeLimit(long nanoseconds) 
    throws UnsupportedOperationException
    {
        this.nanoseconds = nanoseconds;
    }
    
    @Override
    public double getNewHypervolumeEstimate()
    throws IllegalNumberOfObjectivesException
    {
        long startTime = getCPUTime();
        if (nondominatedSamples == null) 
            return updateFirstTime(startTime);
        // not first time, so need to compare new entrant to archive  
        int h = compareToStoredListEfficient();
        h += generateNewMCSamples(startTime,nanoseconds); // now generate new MC samples up to time limit
        
        hypervolumeSamplesDominated += h; // update number of MC samples that have been dominated in the history
        hypervolume = hypervolumeSamplesDominated/((double) hypervolumeSamplesDominated + nondominatedSamples.size());
        return hypervolume;
    }
    
    @Override
    public int getNumberOfSamplesUsedForCurrentEstimate() 
    {
        return hypervolumeSamplesDominated + nondominatedSamples.size();
    }
    
    
    /* Get CPU time in nanoseconds. */
    private long getCPUTime( ) {
        return bean.getCurrentThreadCpuTime( );
    }
    
    private double updateFirstTime(long startTime)
    throws IllegalNumberOfObjectivesException
    {
        nondominatedSamples = new ArrayList<>(100); // initial max list length is arbitrary
        hypervolumeSamplesDominated = generateNewMCSamples(startTime,nanoseconds);
        hypervolume = hypervolumeSamplesDominated/(double) (hypervolumeSamplesDominated + nondominatedSamples.size());    
        return hypervolume;
    }
    
    private int generateNewMCSamples(long startTime, long nanoseconds) 
    throws IllegalNumberOfObjectivesException
    {
        int numberDominated = 0;
        while (getCPUTime()-startTime < nanoseconds){
            MonteCarloSolution s = new MonteCarloSolution(lowerBounds, upperBounds);
            if (list.weaklyDominates(s)){
                numberDominated++;
            } else {
                nondominatedSamples.add(s); // record non-dominated
            }
        }
        return numberDominated;
    }
    
    
}
