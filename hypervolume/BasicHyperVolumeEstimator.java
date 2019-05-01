
/**
 * BasicHyperVolumeEstimator.
 * 
 * @author Jonathan Fieldsend 
 * @version 01/05/2019
 */
public class BasicHyperVolumeEstimator implements HypervolumeEstimator
{
    private int numberOfSamples;
    private ParetoSetManager list;
    private boolean instrument = false; 
    private double hypervolume = 0.0; // initially don't dominate any hypervolume 
    private double[] lowerBounds;
    private double[] upperBounds;
    
    /**
     * Generates an instance of BasicHyperVolumeEstimator to track the
     * hypervolume for a numberOfObjectives dimensional problem, with the
     * hypervolume estimated by Monte Carlo samples from the box constrained
     * hyperrectangle defined in objective space by lowerBounds and upperBounds
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
    public BasicHyperVolumeEstimator(int numberOfObjectives, double[] lowerBounds, double[] upperBounds)
    throws IllegalNumberOfObjectivesException
    {
        if (numberOfObjectives < 1)
            throw new IllegalNumberOfObjectivesException("Number of objectives must be at least 1");
        if (lowerBounds.length != numberOfObjectives)
            throw new IllegalNumberOfObjectivesException("Number of objectives does not match number of lower bound values");
        if (upperBounds.length != numberOfObjectives)
            throw new IllegalNumberOfObjectivesException("Number of objectives does not match number of upper bound values");
        list = DominanceDecisionTreeManager.managerFactory(numberOfObjectives);
        this.lowerBounds = lowerBounds;
        this.upperBounds = upperBounds;
    }
    
    
    public void setNumberOfSamplesToComparePerIteration(int numberOfSamples) 
    throws UnsupportedOperationException
    {
        this.numberOfSamples = numberOfSamples;
    }
    
    public void setTimeLimt(long milliseconds) 
    throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException("BasicHyperVolumeEstimators are defined in terms of samples per iteration, not time");
    }
    
    public boolean updateWithNewSolution(Solution s)
    throws IllegalNumberOfObjectivesException
    {
        return list.add(s);
    }
    
    public double getNewHypervolumeEstimate()
    throws IllegalNumberOfObjectivesException
    {
        int h = 0;
        for (int i=0; i<numberOfSamples; i++)
            if (list.weaklyDominates(new MonteCarloSolution(lowerBounds, upperBounds)))
                h++;
        hypervolume = h/(double) numberOfSamples;
        return hypervolume;
    }
    
    public double getCurrentHypervolumeEstimate()
    {
        return hypervolume;
    }
    
    public ParetoSetManager getCurrentParetoSetEstimate()
    {
        return list;
    }
    
    public void setInstrumented(boolean instrument)
    {
        // if switching instrumentation off, and currently on, then write to file before changing state 
        if (instrument==false) {
            this.writeOutFiles();
        }
        this.instrument = instrument;
    }
    
    public boolean isInstrumented()
    {
        return instrument;
    }
    
    public boolean writeOutFiles()
    {
        if (instrument == false)
            return false;
            
            
        return true;
    }
}
