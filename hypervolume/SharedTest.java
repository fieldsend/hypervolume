import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.Random;
import java.util.Collection;

/**
 * SharedTest has test functionality shared by the unit tests across a number of classes.
 * 
 * @author Jonathan Fieldsend 
 * @version 02/05/2019
 */
public class SharedTest
{
    public static void exampleRun(HypervolumeEstimator estimator1, HypervolumeEstimator estimator2, Random rng, int numberOfObjectives, int numberOfQueries) 
    throws IllegalNumberOfObjectivesException {

        for (int i=0; i<numberOfQueries; i++){
            System.out.println("QUERY: " + i);

            double[] toAdd = new double[numberOfObjectives];
            for (int ii=0; ii < numberOfObjectives; ii++)
                toAdd[ii] = (double) rng.nextInt(100);
            /*System.out.print("Query point: ");
            for (int ii=0; ii < OBJECTIVE_NUMBER; ii++)
            System.out.print(toAdd[ii]+ "  ");

            System.out.println();*/
            //System.out.println(toAdd[0]+ "  " + toAdd[1]);
            System.out.println("Test manager 1 added "+estimator1.updateWithNewSolution(new ProxySolution(toAdd)));
            System.out.println("Manager size: " + estimator1.getCurrentParetoSetEstimate().size());
            
            System.out.println("Test manager 2 added "+estimator2.updateWithNewSolution(new ProxySolution(toAdd)));
            System.out.println("Manager size: " + estimator2.getCurrentParetoSetEstimate().size());
            
            Collection<? extends Solution> set1 = estimator1.getCurrentParetoSetEstimate().getContents();
            Collection<? extends Solution> set2 = estimator2.getCurrentParetoSetEstimate().getContents();
            
            assertTrue(estimator2.getCurrentParetoSetEstimate().size()==estimator1.getCurrentParetoSetEstimate().size());
            assertTrue(set1.size()==set2.size());
            assertTrue(set2.containsAll(set1));
            assertTrue(set1.containsAll(set2));
        }
        System.out.println(estimator1.getCurrentParetoSetEstimate().size());
    }

    private static class ProxySolution implements Solution
    {
        private double[] objectives;

        ProxySolution(double[] objectivesToCopy){
            objectives = new double[ objectivesToCopy.length ];
            for (int i=0; i< objectivesToCopy.length; i++)
                objectives[i] = objectivesToCopy[i];
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
