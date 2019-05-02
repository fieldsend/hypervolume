import java.util.Random; 
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
/**
 * Simple (1+1)--ES for use in examples (and unit tests)
 * 
 * @author Jonathan Fieldsend
 * @version 02/05/2019
 */
public class OnePlusOneES
{
    private Random rng;
    private ThreadMXBean bean = ManagementFactory.getThreadMXBean( ); // object to track timings
    
    /**
     * Creates an optimiser instance to run
     */
    OnePlusOneES(long seed) 
    {
        rng = new Random(seed);
    }
    
    /**
     * runs optimiser on problem for given number of generations
     */
    void runOptimiser(int problem, int iterations, int numberOfObjectives, HypervolumeEstimator estimator) 
    throws IllegalNumberOfObjectivesException
    {
        // initial point
        int numberOfDesignVariables = (problem ==1) ? 5+numberOfObjectives-1 : 10+numberOfObjectives-1;
        DTLZSolution s = new DTLZSolution(numberOfObjectives, numberOfDesignVariables);
        evaluate(problem,s);
        estimator.updateWithNewSolution(s);
        double hypervolume = estimator.getNewHypervolumeEstimate();
        
        long oldTime = bean.getCurrentThreadCpuTime();
        
        for (int i=1; i<iterations; i++) {
            DTLZSolution child = evolve(s);
            evaluate(problem,child);
            if (estimator.updateWithNewSolution(s)) 
                s = child;
            
            hypervolume = estimator.getNewHypervolumeEstimate();
            if (i%1000 ==0)
                System.out.println("it: " +i  + ", size: " + estimator.getCurrentParetoSetEstimate().size() + ", hyp: " + hypervolume + ", nanosecs " + (bean.getCurrentThreadCpuTime()-oldTime));
        }
        
    }
    
    /*
     * Method evaluate solutioon argument under either the DTLZ1 or DTLZ2 problem
     */
    private void evaluate(int problem,DTLZSolution s) {
        if (problem ==1)
            DTLZ1(s);
        else
            DTLZ2(s);
    }
    
    /*
     * Evolves arguement to make child returned
     */
    private DTLZSolution evolve(DTLZSolution s) {
        // select dimension at random
        DTLZSolution child = new DTLZSolution(s);
        int dimension = rng.nextInt(child.designVariables.length);
        // perturb
        do {
            child.designVariables[dimension] = s.designVariables[dimension];
            child.designVariables[dimension] += rng.nextGaussian()*0.1;
        } while ((child.designVariables[dimension] <0.0) || (child.designVariables[dimension]>1.0));

        return child;
    }
    
   

    /*
     * Method evaluates argument under the DTLZ1 problem
     */
    private void DTLZ1(DTLZSolution s) {
        double[] f = new double[s.getNumberOfObjectives()];
        double g = 0.0;
        int k = s.designVariables.length - s.getNumberOfObjectives() + 1;

        for (int i = s.designVariables.length - k; i < s.designVariables.length; i++) {
            g += Math.pow(s.designVariables[i] - 0.5, 2.0)
            - Math.cos(20.0 * Math.PI * (s.designVariables[i] - 0.5));
        }
        g = 100.0 * (k + g);

        for (int i = 0; i < s.getNumberOfObjectives(); i++) {
            f[i] = 0.5 * (1.0 + g);

            for (int j = 0; j < s.getNumberOfObjectives() - i - 1; j++) 
                f[i] *= s.designVariables[j];

            if (i != 0) 
                f[i] *= 1 - s.designVariables[s.getNumberOfObjectives() - i - 1];

        }
        for (int i=0; i<s.getNumberOfObjectives(); i++)
            s.setFitness(i, f[i]);
    }

    /*
     * Method evaluates argument under the DTLZ2 problem
     */
    private void DTLZ2(DTLZSolution s) {
        double[] f = new double[s.getNumberOfObjectives()];
        double g = 0.0;
        int k = s.designVariables.length - s.getNumberOfObjectives() + 1;

        for (int i = s.designVariables.length- k; i < s.designVariables.length; i++) 
            g += Math.pow(s.designVariables[i] - 0.5, 2.0);

        for (int i = 0; i < s.getNumberOfObjectives(); i++) {
            f[i] = 1.0 + g;

            for (int j = 0; j < s.getNumberOfObjectives()- i - 1; j++) 
                f[i] *= Math.cos(0.5 * Math.PI * s.designVariables[j]);

            if (i != 0) 
                f[i] *= Math.sin(0.5 * Math.PI * s.designVariables[s.getNumberOfObjectives() - i - 1]);
        }
        for (int i=0; i<s.getNumberOfObjectives(); i++)
            s.setFitness(i, f[i]);
    }

    /*
     * Inner class representing the Solutions to the DTLZ problems
     */
    private class DTLZSolution implements Solution {
        private double[] objectives;

        double designVariables[];

        /**
         * Generates an instance of DTLZSolution whose design variables are null
         * and whose assigned quality is box constrained with the lowerBound and 
         * upperBound double arrays --- used for Monte Carlo sampling the objective
         * space
         */
        DTLZSolution(double[] lowerBound, double[] upperBound){
            objectives = new double[ lowerBound.length ];
            for (int i=0; i<lowerBound.length; i++)
                objectives[i] = lowerBound[i] + rng.nextDouble()*(upperBound[i] - lowerBound[i]);
        }

        DTLZSolution(int numberOfObjectives, int numberOfDesignVariables){
            objectives = new double[ numberOfObjectives ];
            designVariables = new double[ numberOfDesignVariables ];
            for (int i=0; i<numberOfDesignVariables; i++)
                designVariables[i] = rng.nextDouble();
        }

        DTLZSolution(DTLZSolution toCopy) {
            objectives = new double[ toCopy.objectives.length ];
            designVariables = new double[ toCopy.designVariables.length ];
            for (int i=0; i<toCopy.designVariables.length; i++)
                designVariables[i] = toCopy.designVariables[i];
        }

        @Override 
        public boolean dominates(Solution s)
        {
            return Solution.super.dominates(s);
        }

        @Override 
        public boolean weaklyDominates(Solution s)
        {
            return Solution.super.weaklyDominates(s);
        }

        @Override
        public double getFitness(int index){
            return objectives[index];
        }

        @Override
        public void setFitness(int index, double value){
            objectives[index] = value;
        }

        @Override
        public void setFitness(double[] fitnesses){
            objectives = fitnesses;
        }

        @Override
        public int getNumberOfObjectives(){
            return objectives.length;
        }

        /**
         * Equal if fitness the same
         */
        @Override
        public boolean equals(Object o){
            //System.out.println("In equals check " + this + " "+ o);
            if (o instanceof Solution) {
                //  System.out.println(getFitness());
                //  System.out.println(((Solution) o).getFitness());
                return isFitnessTheSame((Solution) o);
            }
            return false;
        }

        @Override 
        public String toString() {
            String s = "Objective Values-- ";
            for (double d : objectives)
                s+= " : " + d;
            return s;    
        }
    }
    
}
