package hypervolume;

import java.lang.management.ThreadMXBean;
import java.lang.management.ManagementFactory;
import java.io.FileNotFoundException;


/**
 * HypervolumeEstimator provides an interface for all hyervolume
 * estimation objects.
 * 
 * @author Jonathan Fieldsend 
 * @version 09/05/2019
 */
public interface HypervolumeEstimator
{
    static ThreadMXBean bean = ManagementFactory.getThreadMXBean(); // object to track timings
    
    /**
     * Method sets number of samples to compare to per iteration -- enforces minumum of 1 sample
     * 
     * @param numberOfSamples number of samples to compare to per iteration
     * @throws UnsupportedOperationException if HypervolumeEstimator uses time
     * rather than sample number to limit its estimation
     */
    void setNumberOfSamplesToComparePerIteration(int numberOfSamples) throws UnsupportedOperationException; 
    
    /**
     * Method sets number of time limit per iteration -- enforces minumum of 1 nanosecond
     * 
     * @param nanoseconds time limit per iteration (in nanoseconds)
     * @throws UnsupportedOperationException if HypervolumeEstimator uses sample 
     * number rather than time to limit its estimation
     */
    void setTimeLimit(long nanoseconds) throws UnsupportedOperationException;
    
    /**
     * Method updates the Pareto set estimate managed by the hypervolume
     * estimator with the solution s. Returns true if Pareto set changed 
     * by update
     * 
     * @param s solution to update Pareto archive estimate with
     * @returns true if s is non-dominated, otherwise returns false
     * @throws IllegalNumberOfObjectivesException if number of objectives in solution
     * does not match that of solutions maintained in the archive
     */
    boolean updateWithNewSolution(Solution s) throws IllegalNumberOfObjectivesException;
    
    /**
     * Method updates the hypervolume calculation and returns it
     * 
     * @returns the reestimated hypervolume
     * @throws IllegalNumberOfObjectivesException
     */
    double getNewHypervolumeEstimate() throws IllegalNumberOfObjectivesException;
    
    /**
     * Method updates the hypervolume calculation and returns it. It also tracks the value, 
     * and records how long (in nanoseconds) the computation took, for later writing to 
     * file of object history
     * 
     * @returns the reestimated hypervolume
     * @throws IllegalNumberOfObjectivesException
     */
    double instrumentedGetNewHypervolumeEstimate() throws IllegalNumberOfObjectivesException;
    
    /**
     * Sets the filenames to be used when writing the hypervolumes and times if instrumented
     * object used
     * 
     * @param hypervolumeFilename name (and location) of file to save hypervolumes in
     * @param timeFilename name (and location) of file to save timings in
     */
    void setInstrumentationFilenames(String hypervolumeFilename, String timeFilename);
    
    /**
     * Method returns the most recently calculated hypervolume estimate (this may be stale
     * if updateWithNewSolution has been called in the intervening period)
     * 
     * @returns current hypervolume estimate
     */
    double getCurrentHypervolumeEstimate();
    
    /**
     * Returns state of current Pareto set estimate managed by this object (shallow copy)
     * 
     * @returns ParetoSetManager containing the non-dominated solutions approximating the
     * Pareto front as found so far
     */
    ParetoSetManager getCurrentParetoSetEstimate();
    
    /**
     * Writes out file tracking hypervolume statistic. Once written out the state tracking object will be reset.
     *
     * @throws FileNotFoundException
     */
    void writeOutHypervolume() throws FileNotFoundException;
    
    /**
     * Writes out file tracking timings in nanoseconds for computing the hypervolume estimate.
     * Once written out the state tracking object will be reset.
     *
     * @throws FileNotFoundException
     */
    void writeOutTimeInNanoseconds() throws FileNotFoundException;
    
    /**
     * Returns the number of Monte Carlo samples used to create current estimate of the
     * dominated hypervolume
     * 
     * @returns number of samples taken to generate estimate
     */
    int getNumberOfSamplesUsedForCurrentEstimate();
    
    /**
     * Method returns true if the most recent solution passed into the estimator to be 
     * compared to and update the archive was not dominated
     * 
     * @returns true if the last update call was with a non-dominated solution, otherwise 
     * returns false
     */
    boolean isMostRecentUpdateNondominated();
    
    /**
     * Archive is updated with new solution, and hypervolume reestimated and returned
     * 
     * @param s solution to update Pareto archive estimate with
     * @returns reestimated hypervolume
     * @throws IllegalNumberOfObjectivesException if number of objectives in solution
     * does not match that of solutions maintained in the archive 
     */
    default double updateAndReestimateHypervolume(Solution s) 
    throws IllegalNumberOfObjectivesException 
    {
        updateWithNewSolution(s);
        return getNewHypervolumeEstimate();
    }
    
    /**
     * Get CPU time in nanoseconds. 
     * 
     * @returns CPU time in nanoseconds of current thread
     */
    static long getCPUTime() {
        return bean.getCurrentThreadCpuTime();
    }
}
