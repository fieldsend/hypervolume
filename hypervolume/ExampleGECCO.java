import java.io.FileNotFoundException;

/**
 * ExampleGECCO class permits the regneration of results presented in GECCO 2019
 * 
 * Jonathan E. Fieldsend. 2019. 
 * Efficient Real-Time Hypervolume Estimation with Monotonically Reducing Error. 
 * In Genetic and Evolutionary Computation Conference (GECCO ’19), 
 * July 13–17, 2019, Prague, Czech Republic. ACM, New York, NY, USA.
 * 
 * @author Jonathan Fieldsend
 * @version 03/05/2019
 */
public class ExampleGECCO
{
    public static void main(String[] args) 
    throws IllegalNumberOfObjectivesException, FileNotFoundException
    {
        if (args.length<6) {
            System.out.println("Not enough input arguments, six arguments expected:\n"
                    + " Hypervolume estimate update type (B, I, S OR D),\n" 
                    + " Number of samples compared per new estimate (B, I and S), or max nanoseconds for new samples (D_)\n"
                    + " number of iterations (minimum 0 applied),\n"
                    + " number of objectives (minumum 2 applied) and\n"
                    + " seed\n"
                    + " instrument? (true or false), if true hypervolume and timings are written to a file or each iteration\n");
            return;
        }
        
        System.out.println("Using hypervolume estimate update type " + args[0]);
        System.out.println("Using repititions/nanosecond limit " + args[1]);
        System.out.println("Running for " + args[2] + " iterations of the (1+1)-ES");
        System.out.println("Using " + args[3] + " objectives");
        System.out.println("Using " + args[4] + " as seed");
        System.out.println("Instrumented? " + args[5]);
        
        
        
        HypeType t = null;
        HypervolumeEstimator estimator;
        
        
        int numberOfObjectives = Math.max(2,Integer.parseInt(args[3])); // number of objective dimensions
        
        double[] lowerBounds = new double[ numberOfObjectives];
        double[] upperBounds = new double[ numberOfObjectives];
        // set box contstraints for MC sampling of objective vectors
        for (int i=0; i<numberOfObjectives; i++) {
            lowerBounds[i] = 0.0;
            upperBounds[i] = 2.0;
        }
        int numberOfSamplesOrTime = Integer.parseInt(args[1]); // how accurate
        
        switch(args[0]) {
            case "B" :
            t = HypeType.BASIC;
            estimator = new BasicHypervolumeEstimator(numberOfObjectives, lowerBounds, upperBounds);
            estimator.setNumberOfSamplesToComparePerIteration(numberOfSamplesOrTime);
            break;
            case "I" :
            t = HypeType.INCREMENTAL;
            estimator = new IncrementalHypervolumeEstimator(numberOfObjectives, lowerBounds, upperBounds);
            estimator.setNumberOfSamplesToComparePerIteration(numberOfSamplesOrTime);
            break;
            case "S" :
            t = HypeType.INCREMENTAL_SINGLE;
            estimator = new EfficientIncrementalHypervolumeEstimator(numberOfObjectives, lowerBounds, upperBounds);
            estimator.setNumberOfSamplesToComparePerIteration(numberOfSamplesOrTime);
            break;
            default :
            t = HypeType.DYNAMIC;
            estimator = new DynamicHypervolumeEstimator(numberOfObjectives, lowerBounds, upperBounds);
            estimator.setTimeLimit(numberOfSamplesOrTime);
        }
        
        /*if (t==null){
            System.out.println("Using hypervolume update type " + args[1] 
                                + " is invalid. Use only B, I, S or D" );
            return;
        }*/
        
        int its  = Math.max(0,Integer.parseInt(args[2])); // number of iterations of (1+1)-ES
        int seed = Math.max(1,Integer.parseInt(args[4])); // number of folds
        
        boolean instrumented = Boolean.parseBoolean(args[5]);
        String details = "_num_objs_" + numberOfObjectives + "_seed_" + seed;
        estimator.setInstrumentationFilenames("hypervolumes_" + details + ".txt", "times_" + details + ".txt");
        
        int dim =  10+numberOfObjectives-1; // get the number of design dimensions depending on problem number
        OnePlusOneES optimiser = new OnePlusOneES(seed);
        MonteCarloSolution.setRandomSeed((long) seed);
            
        optimiser.runOptimiser(numberOfObjectives,its,numberOfObjectives,estimator);
        System.out.println("SEED : " + seed);
        System.out.println("COMPLETED");
    }
}
