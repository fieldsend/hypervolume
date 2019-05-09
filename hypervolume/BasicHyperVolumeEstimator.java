package hypervolume;

import java.util.ArrayList;
import java.io.File;
import java.io.PrintWriter;
import java.io.FileNotFoundException;

/**
 * BasicHyperVolumeEstimator.
 * 
 * @author Jonathan Fieldsend 
 * @version 09/05/2019
 */
public class BasicHypervolumeEstimator implements HypervolumeEstimator
{
    int numberOfSamples; //number of MC samples to take at each query
    ParetoSetManager list;
    double hypervolume = 0.0; // initially don't dominate any hypervolume 
    double[] lowerBounds;
    double[] upperBounds;
    String hypervolumeFilename = "estimated_hypervolumes.txt";
    String timeFilename = "hypervolume_calculation_times_in_nanoseconds.txt";
    ArrayList<Double> hypervolumeHistory = new ArrayList<>();
    ArrayList<Long> hypervolumeTimingHistoryInNanoseconds  = new ArrayList<>();
    long startTime;
    boolean lastUpdateNondominated = false;

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
    public BasicHypervolumeEstimator(int numberOfObjectives, double[] lowerBounds, double[] upperBounds)
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

    @Override
    public void setNumberOfSamplesToComparePerIteration(int numberOfSamples) 
    throws UnsupportedOperationException
    {
        this.numberOfSamples = Math.max(numberOfSamples,1);
    }

    @Override
    public void setTimeLimit(long nanoseconds) 
    throws UnsupportedOperationException
    {
        throw new UnsupportedOperationException("BasicHyperVolumeEstimators are defined in terms of samples per iteration, not time");
    }

    @Override
    public boolean updateWithNewSolution(Solution s)
    throws IllegalNumberOfObjectivesException
    {
        lastUpdateNondominated = list.add(s);
        return lastUpdateNondominated;
    }

    @Override
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
    public double getCurrentHypervolumeEstimate()
    {
        return hypervolume;
    }

    @Override
    public ParetoSetManager getCurrentParetoSetEstimate()
    {
        return list;
    }

    @Override
    public void setInstrumentationFilenames(String hypervolumeFilename, String timeFilename)
    {
        this.hypervolumeFilename = hypervolumeFilename;
        this.timeFilename = timeFilename;
    }

    @Override
    public void writeOutHypervolume()
    throws FileNotFoundException
    {
        PrintWriter hypervolumeWriter = new PrintWriter(new File(hypervolumeFilename));

        StringBuilder sb = new StringBuilder();
        for (double d : hypervolumeHistory) {
            sb.append(d);
            sb.append("\n");
        }
        hypervolumeWriter.write(sb.toString());
        hypervolumeWriter.close();

        // reset tracker
        hypervolumeHistory = new ArrayList<>();
    }

    @Override
    public void writeOutTimeInNanoseconds()
    throws FileNotFoundException
    {
        PrintWriter timeWriter = new PrintWriter(new File(timeFilename));

        StringBuilder sb = new StringBuilder();
        for (long t : hypervolumeTimingHistoryInNanoseconds) {
            sb.append(t);
            sb.append("\n");
        }
        timeWriter.write(sb.toString());
        timeWriter.close();

        // reset tracker
        hypervolumeTimingHistoryInNanoseconds  = new ArrayList<>();
    }

    @Override
    public int getNumberOfSamplesUsedForCurrentEstimate()
    {
        return numberOfSamples;
    }

    @Override
    public boolean isMostRecentUpdateNondominated()
    {
        return lastUpdateNondominated;
    }

    /**
     * Keeps track of current time
     */
    void logTimeIn()
    {
        startTime = HypervolumeEstimator.getCPUTime();
    }

    /**
     * Tracks difference of current time and time when logTimeIn() was last called
     * and saves in tracker for latter writing to file
     */
    void logTimeOut()
    {
        hypervolumeTimingHistoryInNanoseconds.add(HypervolumeEstimator.getCPUTime()-startTime);
    }
}
