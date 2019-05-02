
/**
 * HypervolumeEstimator provides an interface for all hyervolume
 * estimation objects.
 * 
 * @author Jonathan Fieldsend 
 * @version 02/05/2019
 */
public interface HypervolumeEstimator
{
    /**
     * Method sets number of samples to compare to per iteration
     */
    void setNumberOfSamplesToComparePerIteration(int numberOfSamples) throws UnsupportedOperationException; 
    
    /**
     * Method sets number of time limit per iteration
     */
    void setTimeLimit(long nanoseconds) throws UnsupportedOperationException;
    
    /**
     * Method updates the Pareto set estimate managed by the hypervolume
     * estimator with the solution s. Returns true if Pareto set changed 
     * by update
     */
    boolean updateWithNewSolution(Solution s) throws IllegalNumberOfObjectivesException;
    
    /**
     * Method updates the hypervolume calculation and returns it
     */
    double getNewHypervolumeEstimate() throws IllegalNumberOfObjectivesException;
    
    /**
     * Method returns the most recently calculated hypervolume estimate (this may be stale
     * if updateWithNewSolution has been called in the intervening period)
     */
    double getCurrentHypervolumeEstimate();
    
    /**
     * Returns state of current Pareto set estimate managed by this object (shallow copy)
     */
    ParetoSetManager getCurrentParetoSetEstimate();
    
    /**
     * Sets instrumentation. If currently true will write out tracked stats to 
     * file before switching off tracking
     */
    void setInstrumented(boolean instrument);
    
    /**
     * Returns true if this object is instrumented
     */
    boolean isInstrumented();
    
    /**
     * Writes out files tracking statistics. Returns true if successful. Will return false
     * if instrumentation not set to true.
     */
    boolean writeOutFiles(String filename);
    
    /**
     * Returns the number of Monte Carlo samples used to create current estimate of the
     * dominated hypervolume
     */
    int getNumberOfSamplesUsedForCurrentEstimate();
}
