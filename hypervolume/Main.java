import multiobjective_data_structures.*;
import multiobjective_data_structures.implementations.*;
import java.util.ArrayList;
import java.util.Random; 
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Iterator; 
import java.util.ListIterator; 
import java.lang.management.*;

/**
 * Write a description of class Main here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Main
{
    private ThreadMXBean bean = ManagementFactory.getThreadMXBean( );

    private long callCounter=0;
    private Random rng = new Random(1L);
    private Random rngMC = new Random(2L);

    private PrintWriter solutionWriter;
    private PrintWriter objectiveWriter;
    private PrintWriter hypervolumeWriter;
    private PrintWriter countWriter;
    private PrintWriter timeWriter;
    private int hypervolumeSamples = 0;
    private double hypervolume = 0.0;
    private int comparedToArchive = 0;
    private int comparedToSingle = 0;
    private long domCalls = 0;
    private ArrayList<DTLZSolution> nondominatedList;
    private ArrayList<DTLZSolution> newSolutionList = new ArrayList<>();
    private ArrayList<Integer> processedIndices = new ArrayList<>();
    private DTLZSolution prevChild;
    private int prevProcessedIndex =0;
    
    boolean updateType = false;
    public static void main(String[] args) 
    throws IllegalNumberOfObjectivesException, FileNotFoundException
    {

        //HypeType t = HypeType.BASIC;
        //HypeType t = HypeType.INCREMENTAL;
        //HypeType t = HypeType.INCREMENTAL_SINGLE;
        //HypeType t = HypeType.DYNAMIC_1;
        HypeType t = HypeType.DYNAMIC_2;
        int its  = 100000;
        int[] objs = {3, 5, 10, 20};
        int problem = 2;

        for (int o : objs) {   
            int dim = (problem ==1) ? 5+o-1 : 10+o-1;
            for (int i=0; i<50; i++) {
                Main a = new Main();
                a.runOptimiser(its, o, dim, problem, t, i); 
            }
        }
        System.out.println("COMPLETED");
    }

    /** Get CPU time in nanoseconds. */
    public long getCPUTime( ) {
        return bean.getCurrentThreadCpuTime( );
    }

    /** Get user time in nanoseconds. */
    public long getUserTime( ) {
        return bean.isCurrentThreadCpuTimeSupported( ) ?
            bean.getCurrentThreadUserTime( ) : 0L;
    }

    /** Get system time in nanoseconds. */
    public long getSystemTime( ) {
        return bean.isCurrentThreadCpuTimeSupported( ) ?
            (bean.getCurrentThreadCpuTime( ) - bean.getCurrentThreadUserTime( )) : 0L;
    }

    public void writeSolution(DTLZSolution s,int problem,int numberOfObjectives,int numberOfDesignVariables, int archiveSize, long time) 
    {
        // write solutions
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<s.designVariables.length-1; i++) {
            sb.append(s.designVariables[i]);
            sb.append(", ");
        }
        sb.append(s.designVariables[s.designVariables.length-1]);
        sb.append("\n");
        solutionWriter.write(sb.toString());

        // write objectives
        sb = new StringBuilder();
        for (int i=0; i<s.getNumberOfObjectives()-1; i++) {
            sb.append(s.getFitness(i));
            sb.append(", ");
        }
        sb.append(s.getFitness(s.getNumberOfObjectives()-1));
        sb.append("\n");
        objectiveWriter.write(sb.toString());

        // write counts
        // 1 number compared to archive in time step, 2 number compared to single new member,
        // 3  archive size at time step, cumulative domination calls due to H estimation,
        // cumulative domination calls across optimisation
        sb = new StringBuilder();
        sb.append(comparedToArchive);
        sb.append(", ");
        sb.append(comparedToSingle);
        sb.append(", ");
        sb.append(archiveSize);
        sb.append(", ");
        sb.append(domCalls);
        sb.append(", ");
        sb.append(callCounter);
        if (nondominatedList!=null) {
            sb.append(", ");
            sb.append(nondominatedList.size());
        }
        sb.append("\n");
        countWriter.write(sb.toString());

        // write hypervolume
        sb = new StringBuilder();
        sb.append(hypervolume);
        sb.append(", ");
        sb.append(hypervolumeSamples);
        sb.append("\n");
        hypervolumeWriter.write(sb.toString());

        // write time
        sb = new StringBuilder();
        sb.append(time);
        sb.append("\n");
        timeWriter.write(sb.toString());
    }

    public void runOptimiser(int iterations, int numberOfObjectives, int numberOfDesignVariables, int problem, HypeType t, int fold) 
    throws IllegalNumberOfObjectivesException, FileNotFoundException
    {
        //rng = new Random((long) fold);
        rngMC = new Random((long) fold);

        solutionWriter = new PrintWriter(new File("sol_output_p" + problem + "_obj" + numberOfObjectives 
                + "_des" + numberOfDesignVariables + "_it" + iterations +"_"+ t+ "_fold" + fold+".csv"));
        objectiveWriter = new PrintWriter(new File("obj_output_p" + problem + "_obj" + numberOfObjectives 
                + "_des" + numberOfDesignVariables + "_it" + iterations +"_"+ t+ "_fold" + fold+".csv"));
        hypervolumeWriter = new PrintWriter(new File("hyp_output_p" + problem + "_obj" + numberOfObjectives 
                + "_des" + numberOfDesignVariables + "_it" + iterations +"_"+ t+ "_fold" + fold+".csv"));
        countWriter = new PrintWriter(new File("cnt_output_p" + problem + "_obj" + numberOfObjectives 
                + "_des" + numberOfDesignVariables + "_it" + iterations +"_"+ t+ "_fold" + fold+".csv"));
        timeWriter = new PrintWriter(new File("time_output_p" + problem + "_obj" + numberOfObjectives 
                + "_des" + numberOfDesignVariables + "_it" + iterations +"_"+ t+ "_fold" + fold+".csv"));

        // set up manager of Pareto set approximation                                            
        ParetoSetManager list = DominanceDecisionTreeManager.managerFactory(numberOfObjectives);
        // initial point
        DTLZSolution s = new DTLZSolution(numberOfObjectives, numberOfDesignVariables);
        evaluate(problem,s);
        list.add(s);
        int maxSamples = 5000;
        double[] lowerBound = new double[ numberOfObjectives];
        double[] upperBound = new double[ numberOfObjectives];
        for (int i=0; i<numberOfObjectives; i++) {
            lowerBound[i] = 0.0;
            upperBound[i] = 2.0;
        }
        long oldTime = getCPUTime();
        hypervolumeEstimate(list, maxSamples,lowerBound, upperBound,s,t,oldTime);
        writeSolution(s,problem,numberOfObjectives,numberOfDesignVariables, list.size(), getCPUTime()-oldTime);

        //evolve
        for (int i=1; i<iterations; i++) {
            DTLZSolution child = evolve(s);
            evaluate(problem,child);
            if (list.add(child)) {
                s = child;
                oldTime = getCPUTime();
                hypervolumeEstimate(list, maxSamples,lowerBound, upperBound,child,t,oldTime);
            } else {
                oldTime = getCPUTime();
                hypervolumeEstimate(list, maxSamples,lowerBound, upperBound,null,t, oldTime);
            }
            writeSolution(child,problem,numberOfObjectives,numberOfDesignVariables,list.size(),getCPUTime()-oldTime);
            if (i%1000 ==0)
                System.out.println("it: " +i  + ", size: " + list.size() + ", hyp: " + hypervolume + ", n " + (hypervolumeSamples+ ((nondominatedList != null) ? nondominatedList.size() : 0)) +", nanosecs " + (getCPUTime()-oldTime));
        }
        System.out.println("Archive size: " +  list.size() + ", dominance calls " + domCalls);
        solutionWriter.close();
        objectiveWriter.close();
        hypervolumeWriter.close();
        countWriter.close();
        timeWriter.close();
    }

    private void hypervolumeEstimate(ParetoSetManager list, int maxSamples,
    double[] lowerBound, double[] upperBound, DTLZSolution child, HypeType type, long startTime)
    throws IllegalNumberOfObjectivesException 
    {
        long maxTime = 100000L;
        switch (type) {
            case BASIC :
            hypervolumeBasic(list, maxSamples,lowerBound, upperBound);
            break;
            case INCREMENTAL :
            hypervolumeIncremental(list, maxSamples,lowerBound, upperBound,child,false);
            break;
            case INCREMENTAL_SINGLE :
            hypervolumeIncremental(list, maxSamples,lowerBound, upperBound,child,true);
            break;  
            case DYNAMIC_1 :
            hypervolumeDynamic1(list, lowerBound, upperBound,child, maxTime, startTime); //1ms allowed
            break; 
            case DYNAMIC_2 :
            hypervolumeDynamic2(list, lowerBound, upperBound,child, maxTime, startTime); //1ms allowed
            break; 
        }

    }

    private void hypervolumeDynamic1(ParetoSetManager list, 
    double[] lowerBound, double[] upperBound, DTLZSolution child, long maxTime, long startTime) 
    throws IllegalNumberOfObjectivesException
    {
        process1(startTime,child,list,lowerBound,upperBound,maxTime); 
    }

    private void process1(long startTime, DTLZSolution child, ParetoSetManager list, 
    double[] lowerBound, double[] upperBound,  long maxTime) 
    throws IllegalNumberOfObjectivesException
    {
        long currentCalls = callCounter;
        if (nondominatedList == null) {
            nondominatedList = new ArrayList<>(100);
            int h=0;
            while (getCPUTime()-startTime < maxTime) {
                DTLZSolution s = new DTLZSolution(lowerBound, upperBound);
                if (list.weaklyDominates(s)){
                    h++;
                } else {
                    nondominatedList.add(s); // record non dominated
                }

            }
            int g = h+nondominatedList.size();
            hypervolumeSamples = h;
            hypervolume = h/((double) g);
            comparedToArchive = g;
            domCalls += callCounter-currentCalls;
            return;
        } else {
            int h = 0;
            // first see if need to process point to list
            if (child != null) {
                Iterator<DTLZSolution> itr = nondominatedList.iterator();
                comparedToSingle = nondominatedList.size();
                while (itr.hasNext()) {
                    DTLZSolution s  = itr.next();
                    if (child.weaklyDominates(s)){ //only need to compare to child
                        h++;
                        itr.remove();
                    }
                }
            } else {
                comparedToSingle = 0;
            }
            // now do extra
            int g=0;
            while (getCPUTime()-startTime < maxTime) {
                DTLZSolution s = new DTLZSolution(lowerBound, upperBound);
                g++;
                if (list.weaklyDominates(s)){
                    h++;
                } else {
                    nondominatedList.add(s); // record non-dominated
                }
            }

            comparedToArchive = g;
            hypervolumeSamples += h;
            hypervolume = hypervolumeSamples/((double) hypervolumeSamples+ nondominatedList.size());
            //hypervolumeSamples += h;
            domCalls += callCounter-currentCalls;
        }
    }

    private void hypervolumeDynamic2(ParetoSetManager list, 
    double[] lowerBound, double[] upperBound, DTLZSolution child, long maxTime, long startTime) 
    throws IllegalNumberOfObjectivesException
    {
        process2(startTime,child,list,lowerBound,upperBound,maxTime); 
    }

    private void process2(long startTime, DTLZSolution child, ParetoSetManager list, 
    double[] lowerBound, double[] upperBound,  long maxTime) 
    throws IllegalNumberOfObjectivesException
    {
        long currentCalls = callCounter;
        if (nondominatedList == null) {
            nondominatedList = new ArrayList<>(100);
            int h=0;
            while (getCPUTime()-startTime < maxTime) {
                DTLZSolution s = new DTLZSolution(lowerBound, upperBound);
                if (list.weaklyDominates(s)){
                    h++;
                } else {
                    nondominatedList.add(s); // record non dominated
                }

            }
            int g = h+nondominatedList.size();
            hypervolumeSamples = h;
            hypervolume = h/((double) g);
            comparedToArchive = g;
            domCalls += callCounter-currentCalls;
            return;
        } else {
            int h = 0;
            comparedToSingle = 0;
            // are there still elements to process from last iteration
            if (prevChild != null){
                ListIterator<DTLZSolution> itr = nondominatedList.listIterator(prevProcessedIndex);
                comparedToSingle = nondominatedList.size()-prevProcessedIndex;
                if (itr.hasNext()) {
                    DTLZSolution s  = itr.next();
                    if (prevChild.weaklyDominates(s)){ 
                        h++;
                        itr.remove(); // clear dominated sample from nonDominatedList
                    }
                }
            }
            
            // now see if need to process point to list, and if time permits process
            if (child != null) {
                prevProcessedIndex = 0; // track how many processed
                Iterator<DTLZSolution> itr = nondominatedList.iterator();
                int toProcess = nondominatedList.size()/2;
                while (itr.hasNext() && (prevProcessedIndex<toProcess)) {
                        comparedToSingle++;
                        prevProcessedIndex++;
                        DTLZSolution s  = itr.next();
                        if (child.weaklyDominates(s)){ //only need to compare to child
                            h++;
                            itr.remove();
                        }
                }
                while (getCPUTime()-startTime < maxTime) {
                    while (itr.hasNext()) {
                        comparedToSingle++;
                        prevProcessedIndex++;
                        DTLZSolution s  = itr.next();
                        if (child.weaklyDominates(s)){ //only need to compare to child
                            h++;
                            itr.remove();
                        }
                    }
                }
            } else {
                prevChild = null;
                prevProcessedIndex = 0;
            }
            // now do extra if time permits
            int g=0;
            while (getCPUTime()-startTime < maxTime) {
                DTLZSolution s = new DTLZSolution(lowerBound, upperBound);
                g++;
                if (list.weaklyDominates(s)){
                    h++;
                } else {
                    nondominatedList.add(s); // record non-dominated
                }
            }

            comparedToArchive = g;
            hypervolumeSamples += h;
            hypervolume = hypervolumeSamples/((double) hypervolumeSamples+ nondominatedList.size());
            //hypervolumeSamples += h;
            domCalls += callCounter-currentCalls;
        }
    }

    
    
    private void hypervolumeDynamic(ParetoSetManager list, 
    double[] lowerBound, double[] upperBound, DTLZSolution child, long maxTime) 
    throws IllegalNumberOfObjectivesException
    {
        long startTime = getCPUTime(); 
        if (child != null) {
            newSolutionList.add(child);
            processedIndices.add(0);
        }
        process(startTime,list,lowerBound,upperBound,maxTime); 
    }

    private void process(long startTime, ParetoSetManager list, double[] lowerBound, double[] upperBound, long maxTime) 
    throws IllegalNumberOfObjectivesException
    {
        int domed = 0;
        if (nondominatedList == null) 
            nondominatedList = new ArrayList<>();
        //System.out.println(newSolutionList.size() + " " + processedIndices.size() + " " + nondominatedList.size());
        if (nondominatedList.size()>0){
            Iterator<DTLZSolution> itrNewSolList = newSolutionList.iterator();
            int i = 0;
            //System.out.println("L "+ newSolutionList.size() + " " + processedIndices.size() + " " + nondominatedList.size());
            while (itrNewSolList.hasNext()){
                int index = processedIndices.get(i);
                //System.out.println("N " + newSolutionList.size() + " " + processedIndices.size() + " " + nondominatedList.size() + " i " +i + " index " + index);
                boolean complete = false;
                DTLZSolution archiveMember = itrNewSolList.next();
                /*if (index>=nondominatedList.size()) {
                // special case when last removed in previous step
                complete = true;
                } else {
                 */  ListIterator<DTLZSolution> itr = nondominatedList.listIterator(index);
                while (getCPUTime()-startTime < maxTime) {
                    if (itr.hasNext()) {
                        DTLZSolution s  = itr.next();
                        if (archiveMember.weaklyDominates(s)){ 
                            domed++;
                            itr.remove(); // clear dominated sample from nonDominatedList
                            // shift all indices down one larger or equal to i, as ith
                            // now removed
                            //System.out.println("rem " + index);
                            for (int j=0; j<processedIndices.size(); j++)
                                if (i!=j)
                                    if (processedIndices.get(j) > index)
                                        processedIndices.set(j,processedIndices.get(j)-1);

                        } else {
                            index++;
                        }
                        comparedToSingle++;
                    } else {
                        //System.out.println("C " + newSolutionList.size() + " " + processedIndices.size() + " " + nondominatedList.size() + " i " +i);
                        complete = true;
                        break;
                    }
                }
                //}
                if (complete){
                    processedIndices.remove(i); //clear element i from processedIndices
                    itrNewSolList.remove(); // clear element i from newSolutionList
                    i--;
                } else { //ran out of time 
                    //System.out.println("update: " + i + " index " +index);
                    processedIndices.set(i,index);
                    break; // if not complete, ran out of time
                }
                i++;
            }
        } else {
            processedIndices = new ArrayList<>();
            newSolutionList = new ArrayList<>();
        }

        // If reached this point with time remaining newSolutionList and processedIndices are empty
        // so can add samples to nonDominatedList
        while (getCPUTime()-startTime < maxTime) {
            DTLZSolution s = new DTLZSolution(lowerBound, upperBound);
            if (list.weaklyDominates(s)){
                domed++;
            } else {
                nondominatedList.add(s); // record non-dominated
            }
            comparedToArchive++;
        }

        hypervolumeSamples = hypervolumeSamples+domed;
        hypervolume = hypervolumeSamples/((double) nondominatedList.size()+hypervolumeSamples);
    }

    private void hypervolumeIncremental(ParetoSetManager list, int maxSamples,
    double[] lowerBound, double[] upperBound, DTLZSolution child, boolean smart) 
    throws IllegalNumberOfObjectivesException
    {
        long currentCalls = callCounter;
        if (nondominatedList == null) {
            nondominatedList = new ArrayList<>(maxSamples);
            int h=0;
            for (int i=0; i<maxSamples; i++){
                DTLZSolution s = new DTLZSolution(lowerBound, upperBound);
                if (list.weaklyDominates(s)){
                    h++;
                } else {
                    nondominatedList.add(s); // record non dominated
                }
            }
            hypervolumeSamples = maxSamples-nondominatedList.size();
            hypervolume = h/(double) maxSamples;
            comparedToArchive = maxSamples;
            domCalls += callCounter-currentCalls;
            return;
        } else {
            int extra = 0;
            int h = 0;
            int steps =0;
            int toGenerate = Math.max(0,maxSamples-nondominatedList.size()); // calculate beforehand, as list may change

            if (smart){ // leveraging state knowledge and only comparing to new entrant
                if (child != null) {
                    Iterator<DTLZSolution> itr = nondominatedList.iterator();
                    comparedToSingle = nondominatedList.size();
                    while (itr.hasNext()) {
                        DTLZSolution s  = itr.next();
                        if (child.weaklyDominates(s)){ //only need to compare to child
                            h++;
                            itr.remove();
                        }
                    }
                } else {
                    comparedToSingle = 0;
                }
            } else { // not leveraging state knowledge 
                extra = nondominatedList.size();
                Iterator<DTLZSolution> itr = nondominatedList.iterator();
                while (itr.hasNext()) {
                    DTLZSolution s  = itr.next();
                    if (list.weaklyDominates(s)){ 
                        h++;
                        itr.remove();
                    }
                }
            }

            for (int i=0; i<toGenerate; i++){
                DTLZSolution s = new DTLZSolution(lowerBound, upperBound);
                if (list.weaklyDominates(s)){
                    h++;
                } else {
                    nondominatedList.add(s); // record non-dominated
                }
            }
            comparedToArchive = toGenerate+extra;
            hypervolume = (1/((double) maxSamples + hypervolumeSamples)) * (hypervolumeSamples + h);
            hypervolumeSamples += toGenerate;
            //hypervolumeSamples += h;
            domCalls += callCounter-currentCalls;
        }
    }

    private void hypervolumeBasic(ParetoSetManager list, int maxSamples,double[] lowerBound, double[] upperBound) 
    throws IllegalNumberOfObjectivesException
    {
        int h=0;
        long currentCalls = callCounter;
        for (int i=0; i<maxSamples; i++)
            if (list.weaklyDominates(new DTLZSolution(lowerBound, upperBound)))
                h++;
        hypervolume = h/(double) maxSamples;
        hypervolumeSamples = maxSamples;
        comparedToArchive = maxSamples;
        domCalls += callCounter-currentCalls;
    }

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

    private void evaluate(int problem,DTLZSolution s) {
        if (problem ==1)
            DTLZ1(s);
        else
            DTLZ2(s);
    }

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

    private class DTLZSolution implements Solution {
        private double[] objectives;

        double designVariables[];

        DTLZSolution(double[] lowerBound, double[] upperBound){
            objectives = new double[ lowerBound.length ];
            for (int i=0; i<lowerBound.length; i++)
                objectives[i] = lowerBound[i] + rngMC.nextDouble()*(upperBound[i] - lowerBound[i]);
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
            callCounter++;
            return Solution.super.dominates(s);
        }

        @Override 
        public boolean weaklyDominates(Solution s)
        {
            callCounter++;
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