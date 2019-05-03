import java.util.ArrayList;
import java.util.Iterator; 

/**
 * IncrementalHypervolumeEstimator uses past history to incrementally
 * improve hypervolume estimation over time.
 * 
 * @author Jonathan Fieldsend 
 * @version 02/05/2019
 */
public class IncrementalHypervolumeEstimator extends BasicHypervolumeEstimator
{
    ArrayList<Solution> nondominatedSamples; // track which samples not yet dominated
    int hypervolumeSamplesDominated = 0; // track how many samples made over time 
    /**
     * Generates an instance of a HypervolumeEstimator to track the
     * hypervolume for a numberOfObjectives dimensional problem, with the
     * hypervolume estimated by Monte Carlo samples from the box constrained
     * hyperrectangle defined in objective space by lowerBounds and upperBounds, and
     * through tracking history of dominated samples 
     * 
     * Instance initially has instrumentation switched off.
     * 
     *  @param numberOfObjectives number of objectives
     *  @param lowerBounds array of values of lower bound for objectives for MC sampling
     *  @param upperBounds array of values of upper bound for objectives for MC sampling
     *  @throws IllegalNumberOfObjectivesException if the length of a bounds array does 
     *          not match the number of objectives, or if the number of objectives is 
     *          less than 1 (see message in exception)
     */
    public IncrementalHypervolumeEstimator(int numberOfObjectives, double[] lowerBounds, double[] upperBounds)
    throws IllegalNumberOfObjectivesException
    {
        super(numberOfObjectives,lowerBounds,upperBounds);
    }

    /**
     * Uses past dominated history and list of non-dominated samples to incrementally
     * improve fidelity of hypervolume estimate over time
     */
    @Override
    public double getNewHypervolumeEstimate()
    throws IllegalNumberOfObjectivesException
    {
        if (nondominatedSamples == null)  // first time called, special case
            return updateFirstTime();
        
        int toGenerate = Math.max(0,numberOfSamples-nondominatedSamples.size()); // calculate beforehand, as list may change
        int h = compareToStoredList();
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

    @Override
    public int getNumberOfSamplesUsedForCurrentEstimate()
    {
        return hypervolumeSamplesDominated + numberOfSamples;
    }
    
    
    /**
     * Compares the previously non-dominated solutions to the current
     * Pareto set estimate, return the number dominated (removed from 
     * the sample list)
     */
    private int compareToStoredList() 
    throws IllegalNumberOfObjectivesException
    {
        int numberDominated = 0;
        // first iterate over samples which haven't been dominated in pervious iterations
        Iterator<Solution> itr = nondominatedSamples.iterator();
        while (itr.hasNext()) {
            Solution s  = itr.next();
            if (list.weaklyDominates(s)){ 
                numberDominated++;
                itr.remove();
            }
        }
        return numberDominated;
    }
    
    /**
     * Generates toGenerate number Monte Carlo samples, and returns the
     * number dominated (with those not dominated added to the list of samples
     * nont dominated)
     */
    int generateNewMCSamples(int toGenerate) 
    throws IllegalNumberOfObjectivesException
    {
        int numberDominated = 0;
        for (int i=0; i<toGenerate; i++){
            MonteCarloSolution s = new MonteCarloSolution(lowerBounds, upperBounds);
            if (list.weaklyDominates(s)){
                numberDominated++;
            } else {
                nondominatedSamples.add(s); // record non-dominated
            }
        }
        return numberDominated;
    }
    
    
    /**
     * Calculates hypervolume on first occasion
     */
    double updateFirstTime()
    throws IllegalNumberOfObjectivesException
    {
        nondominatedSamples = new ArrayList<>(numberOfSamples); // initial max list length is simply number of samples in an iteration
        int h = generateNewMCSamples(numberOfSamples);
        hypervolumeSamplesDominated = h;
        hypervolume = h/(double) numberOfSamples;    
        return hypervolume;
    }

}
