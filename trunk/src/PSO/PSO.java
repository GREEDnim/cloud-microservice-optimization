package PSO;

import net.sourceforge.jswarm_pso.Swarm;
import utils.Constants;

public class PSO {
    private static Swarm swarm;
    private static SchedulerParticle particles[];
    private static SchedulerFitnessFunction ff = new SchedulerFitnessFunction();

    public PSO() {
        initParticles();
    }


    /**
     * 粒子群算法迭代过程
     * @return
     */
    public double[] run() {
        swarm = new Swarm(Constants.POPULATION_SIZE, new SchedulerParticle(), ff);

        swarm.setMinPosition(0);
        swarm.setMaxPosition(Constants.NO_OF_VMS - 1);
        swarm.setMaxMinVelocity(0.5);
        swarm.setParticles(particles);
        swarm.setParticleUpdate(new SchedulerParticleUpdate(new SchedulerParticle()));

        for (int i = 0; i < Constants.NO_OF_Iterations; i++) {
            swarm.evolve();
            if (i % 10 == 0) {
                System.out.printf("Gloabl best at iteration (%d): %f\n", i, swarm.getBestFitness());
            }
            //swarm.setInertia();
        }

        System.out.println("\nThe best fitness value: " + swarm.getBestFitness() + "\nBest makespan: " + ff.calcMakespan(swarm.getBestParticle().getBestPosition()));
        System.out.println("The best calcTotalTime:"+ff.calcTotalTime(swarm.getBestParticle().getPosition()));
        System.out.println("loadBalance"+ff.loadBalance(swarm.getBestParticle().getBestPosition()));
        for (int i = 0; i < Constants.NO_OF_VMS; i++){
            System.out.println("vmSerTime "+i+"  "+ff.vmSerTime(swarm.getBestParticle().getBestPosition())[i]);
        }
        System.out.println("calcLoadCost"+ff.calcLoadCost(swarm.getBestParticle().getBestPosition()));
        System.out.println("The best solution is: ");
        SchedulerParticle bestParticle = (SchedulerParticle) swarm.getBestParticle();
        System.out.println(bestParticle.toString());

        return swarm.getBestPosition();
    }

    private static void initParticles() {
        particles = new SchedulerParticle[Constants.POPULATION_SIZE];
        for (int i = 0; i < Constants.POPULATION_SIZE; ++i)
            particles[i] = new SchedulerParticle();
    }

    public void printBestFitness() {
        System.out.println("\nBest fitness value: " + swarm.getBestFitness() +
                "\nBest makespan: " + ff.calcMakespan(swarm.getBestParticle().getBestPosition()));
    }
}
