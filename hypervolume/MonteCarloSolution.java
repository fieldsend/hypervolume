import java.util.Random; 

/**
 * MonteCarloSolution implements the Solution class to represent a random
 * Monte Carlo sample in objective space. Assumes minimisation.
 * 
 * @author Jonathan Fieldsend
 * @version 01/05/2019
 */
public class MonteCarloSolution implements Solution
{
    private double[] fitnesses;
    private static Random rng = new Random(0L);

    /**
     * MonteCarloSolution implements the Solution class to represent a random
     * Monte Carlo sample in objective space. Assumes minimisation.
     * 
     *  @param lowerBounds array of values of lower bound for objectives for MC sampling
     *  @param upperBounds array of values of upper bound for objectives for MC sampling
     */
    public MonteCarloSolution(double[] lowerBounds, double[] upperBounds)
    {
        fitnesses = new double[ lowerBounds.length ];
        for (int i=0; i<lowerBounds.length; i++)
            fitnesses[i] = lowerBounds[i] + rng.nextDouble()*(upperBounds[i] - lowerBounds[i]);
    }
    
    public double getFitness(int index) 
    {
        return fitnesses[index];
    }
    
    public void setFitness(int index, double value)
    {
        fitnesses[index] = value;
    }
    
    public void setFitness(double[] fitnesses)
    {
        this.fitnesses = fitnesses;
    }
    
    public int getNumberOfObjectives() 
    {
        return fitnesses.length;
    }
    
    public static void setRandomSeed(long seed) 
    {
        rng = new Random(seed);
    }
}
