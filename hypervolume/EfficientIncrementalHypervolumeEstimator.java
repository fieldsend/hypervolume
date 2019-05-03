import java.util.Iterator; 

/**
 * EfficientIncrementalHypervolumeEstimator uses past history to incrementally
 * improve hypervolume estimation over time, and is smart in terms of comparing
 * previously non-dominated samples only to new entrants to the archive.
 * 
 * @author Jonathan Fieldsend 
 * @version 02/05/2019
 */
public class EfficientIncrementalHypervolumeEstimator extends IncrementalHypervolumeEstimator
{
    private Solution improvingEntrant; // track if last entrant was improving
    
    public EfficientIncrementalHypervolumeEstimator(int numberOfObjectives, double[] lowerBounds, double[] upperBounds)
    throws IllegalNumberOfObjectivesException
    {
        super(numberOfObjectives,lowerBounds,upperBounds);
    }
    
    @Override
    public double getNewHypervolumeEstimate()
    throws IllegalNumberOfObjectivesException
    {
        if (nondominatedSamples == null)  // first time called, special case
            return updateFirstTime();
        int toGenerate = Math.max(0,numberOfSamples-nondominatedSamples.size()); // calculate beforehand, as list may change
        int h = compareToStoredListEfficient();
        h += generateNewMCSamples(toGenerate); // now generate new MC samples up to limit
        
        hypervolume = (1/((double) numberOfSamples + hypervolumeSamplesDominated)) * (hypervolumeSamplesDominated + h);
        hypervolumeSamplesDominated += h;
        return hypervolume;
    }
    
    @Override
    public double instrumentedGetNewHypervolumeEstimate()
    throws IllegalNumberOfObjectivesException
    {
        logTimeIn();
        getNewHypervolumeEstimate();
        logTimeOut();
        hypervolumeHistory.add(hypervolume);
        return hypervolume;
    }
    
    /**
     * Compares the previously non-dominated solutions to the current
     * Pareto set estimate, return the number dominated (removed from 
     * the sample list)
     */
    int compareToStoredListEfficient() 
    throws IllegalNumberOfObjectivesException
    {
        int numberDominated = 0;
        if (improvingEntrant != null) {
            // first iterate over samples which haven't been dominated in previous iterations
            Iterator<Solution> itr = nondominatedSamples.iterator();
            while (itr.hasNext()) {
                Solution s  = itr.next();
                if (improvingEntrant.weaklyDominates(s)){ 
                    numberDominated++;
                    itr.remove();
                }
            }
        }
        return numberDominated;
    }
    
    @Override
    public boolean updateWithNewSolution(Solution s)
    throws IllegalNumberOfObjectivesException
    {
        // want to track if new solution improves the Pareto set estimate,
        // for use in efficient hypervolume calculation
        boolean improvement = super.updateWithNewSolution(s);
        if (improvement)
            improvingEntrant = s;
        else
            improvingEntrant = null;
        return improvement;    
    }
    
}
