import java.lang.management.ThreadMXBean;
import java.lang.management.ManagementFactory;
import java.io.FileNotFoundException;


/**
 * HypervolumeEstimator provides an interface for all hyervolume
 * estimation objects.
 * 
 * @author Jonathan Fieldsend 
 * @version 02/05/2019
 */
public interface HypervolumeEstimator
{
    static ThreadMXBean bean = ManagementFactory.getThreadMXBean(); // object to track timings
    
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
     * Method updates the hypervolume calculation and returns it. It also tracks the value, 
     * and records how long (in nanoseconds) the computation took, for later writing to 
     * file of object history
     */
    double instrumentedGetNewHypervolumeEstimate() throws IllegalNumberOfObjectivesException;
    
    /**
     * Sets the filenames to be used when writing the hypervolumes and times if instrumented
     * object used
     */
    void setInstrumentationFilenames(String hypervolumeFilename, String timeFilename);
    
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
     * Writes out file tracking hypervolume statistic. Once written out the state tracking object will be reset.
     */
    void writeOutHypervolume() throws FileNotFoundException;
    
    /**
     * Writes out file tracking timings in nanoseconds for computing the hypervolume estimate.
     * Once written out the state tracking object will be reset.
     */
    void writeOutTimeInNanoseconds() throws FileNotFoundException;
    
    /**
     * Returns the number of Monte Carlo samples used to create current estimate of the
     * dominated hypervolume
     */
    int getNumberOfSamplesUsedForCurrentEstimate();
    
    /**
     * Get CPU time in nanoseconds. 
     */
    static long getCPUTime() {
        return bean.getCurrentThreadCpuTime();
    }
}
