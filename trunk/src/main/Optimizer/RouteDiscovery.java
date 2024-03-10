package main.Optimizer;

import main.services.Task;
import org.cloudbus.cloudsim.Cloudlet;

import java.util.*;

public class RouteDiscovery {

    private static RouteDiscovery ref=null;
    private List<Cloudlet>cloudlets;
    double[][] pheromones;
    double[][] distances;

    private double alpha;
    private double beta;
    private int initialPheromones;

    private double remainingFactor;
    private double Q;
    private double randomFactor;

    private int maxIterations;
    private int numberOfCities;

    private int numberOfAnts;
    private double antFactor;

    private List<Ant> ants = new ArrayList<>();
    private Random random = new Random();
    private double[] probabilities;

    private int currentIndex;

    private int[] bestTourOrder;
    private double bestTourLength;



    private RouteDiscovery(List<Cloudlet>cloudlets){
        this.cloudlets=cloudlets;
        this.initialPheromones=1;
        this.alpha=1;
        this.beta=5;
        this.remainingFactor=0.5;
        this.Q=500;
        this.antFactor=0.8;
        this.randomFactor=0.01;
        this.maxIterations=1000;
        this.numberOfCities=this.getTotalCities();
        this.numberOfAnts=this.numberOfCities;
        this.distances = generateRandomMatrix(numberOfCities);
        this.pheromones = new double[numberOfCities][numberOfCities];
        this.probabilities = new double[numberOfCities];


    }
    static public RouteDiscovery getInstance(List<Cloudlet> cloudlets){
        if(ref == null) {
            ref = new RouteDiscovery(cloudlets);
        }
        return ref;
    }
    static public RouteDiscovery getInstance(){
        return ref;
    }
    public double[][] generateRandomMatrix(int n) {
        double[][] randomMatrix = new double[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j)
                    randomMatrix[i][j] = 0;
                else
                    randomMatrix[i][j] = Math.abs(random.nextInt(100) + 1);
            }
        }
        return  randomMatrix;
    }
    private int getTotalCities() {
        HashSet<Integer>set = new HashSet<>();
        for(Cloudlet cloudLet : cloudlets){
            Task task = (Task) cloudLet;
            set.add(task.getGroupId());
        }
        return set.size();
    }
    
     public void initiateOptimization(){
        this.setupAnts();
        this.clearTrails();
        for(int i=0;i<this.maxIterations;i++)
        {
            ref.moveAnts();
            ref.updateTrails();
            ref.updateBest();
        }
    }

    public  HashMap<Integer,Integer> getPriorityMap(){

        HashMap<Integer,Integer> priorityMap = new HashMap<>();
        int priority = 1;
        for( int ele : this.bestTourOrder ) {
            priorityMap.put(ele,priority);
            priority++;
        }
        return priorityMap;
    }

    private  void updateBest() {
        if (bestTourOrder == null)
        {
            System.out.println(ants);
            bestTourOrder = ants.get(0).trail;
            bestTourLength = ants.get(0).trailLength(distances);
        }

        for (Ant a : ants)
        {
            if (a.trailLength(distances) < bestTourLength)
            {
                bestTourLength = a.trailLength(distances);
                bestTourOrder = a.trail.clone();
            }
        }
    }

    private  void updateTrails() {
        for (int i = 0; i < numberOfCities; i++)
        {
            for (int j = 0; j < numberOfCities; j++)
                pheromones[i][j] *= (int) remainingFactor;
        }
        for (Ant a : ants)
        {
            double contribution = Q / a.trailLength(distances);
            for (int i = 0; i < numberOfCities - 1; i++)
                pheromones[a.trail[i]][a.trail[i + 1]] += contribution;
            pheromones[a.trail[numberOfCities - 1]][a.trail[0]] += contribution;
        }
    }

    private void moveAnts()
    {
        for(int i=currentIndex;i<numberOfCities-1;i++)
        {
            for(Ant ant:ants)
            {
                ant.visitCity(currentIndex,this.selectNextCity(ant));
            }
            currentIndex++;
        }
    }

    private int selectNextCity(Ant ant)
    {
        int t = random.nextInt(numberOfCities - currentIndex);
        if (random.nextDouble() < randomFactor)
        {
            int cityIndex=-999;
            for(int i=0;i<numberOfCities;i++)
            {
                if(i==t && !ant.visited(i))
                {
                    cityIndex=i;
                    break;
                }
            }
            if(cityIndex!=-999)
                return cityIndex;
        }
        this.calculateProbabilities(ant);
        double r = random.nextDouble();
        double total = 0;
        for (int i = 0; i < numberOfCities; i++)
        {
            total += probabilities[i];
            if (total >= r)
                return i;
        }
        throw new RuntimeException("There are no other cities");
    }

    private void calculateProbabilities(Ant ant) {
        int i = ant.trail[currentIndex];
        double pheromone = 0.0;
        for (int l = 0; l < numberOfCities; l++)
        {
            if (!ant.visited(l))
                pheromone += Math.pow(pheromones[i][l], alpha) * Math.pow(1.0 / distances[i][l], beta);
        }
        for (int j = 0; j < numberOfCities; j++)
        {
            if (ant.visited(j))
                probabilities[j] = 0.0;
            else
            {
                double numerator = Math.pow(pheromones[i][j], alpha) * Math.pow(1.0 / distances[i][j], beta);
                probabilities[j] = numerator / pheromone;
            }
        }
    }

    private void clearTrails()
    {
        for(int i=0;i<numberOfCities;i++)
        {
            for(int j=0;j<numberOfCities;j++)
                pheromones[i][j]=initialPheromones;
        }
    }

    private void setupAnts()
    {
        for(int i=0;i<numberOfAnts;i++)
            ants.add(new Ant(numberOfCities));

        System.out.println(numberOfCities);
        for(int i=0;i<numberOfAnts;i++)
        {
            for(Ant ant:ants)
            {
                ant.clear();
                ant.visitCity(-1, random.nextInt(numberOfCities));
            }
        }
        currentIndex = 0;
        System.out.println(numberOfAnts);
        System.out.println(ants);
    }

}



