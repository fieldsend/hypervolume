
/**
 * HypervolumeEstimator provides an interface for all hyervolume
 * estimation objects.
 * 
 * @author Jonathan Fieldsend 
 * @version 30/04/2019
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
    void setTimeLimt(long milliseconds) throws UnsupportedOperationException;
    
    /**
     * Method updates the Pareto set estimate managed by the hypervolume
     * estimator with the solution s. Returns true if Pareto set changed 
     * by update
     */
    boolean updateWithNewSolution(Solution s);
    
    /**
     * Method updates the hypervolume calculation and returns it
     */
    double getNewHypervolumeEstimate();
    
    /**
     * Method returns the most recently calculated hypervolume estimate (this may be stale
     * if updateWithNewSolution has been called in the intervening period)
     */
    double getCurrentHypervolumeEstimate();
    
    /**
     * Returns state of current Pareto set estimate managed by this object (shallow copy)
     */
    ParetoSetManager getCurrentParetoSetEstimate();
}
