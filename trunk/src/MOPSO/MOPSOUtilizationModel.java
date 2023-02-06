package MOPSO;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.cloudbus.cloudsim.UtilizationModel;

public class MOPSOUtilizationModel implements UtilizationModel{
    private Random randomGenerator;
    private Map<Double, Double> history;

    public MOPSOUtilizationModel() {
        this.setHistory(new HashMap());
        this.setRandomGenerator(new Random());
    }

    public MOPSOUtilizationModel(long seed) {
        this.setHistory(new HashMap());
        this.setRandomGenerator(new Random(seed));
    }

    public double getUtilization(double time) {
        if (this.getHistory().containsKey(time)) {
            return (Double)this.getHistory().get(time);
        } else {
            double utilization = this.getRandomGenerator().nextDouble();
            this.getHistory().put(time, utilization);
            return utilization;
        }
    }

    protected Map<Double, Double> getHistory() {
        return this.history;
    }

    protected void setHistory(Map<Double, Double> history) {
        this.history = history;
    }

    public void saveHistory(String filename) throws Exception {
        FileOutputStream fos = new FileOutputStream(filename);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(this.getHistory());
        oos.close();
    }

    public void loadHistory(String filename) throws Exception {
        FileInputStream fis = new FileInputStream(filename);
        ObjectInputStream ois = new ObjectInputStream(fis);
        this.setHistory((Map)ois.readObject());
        ois.close();
    }

    public void setRandomGenerator(Random randomGenerator) {
        this.randomGenerator = randomGenerator;
    }

    public Random getRandomGenerator() {
        return this.randomGenerator;
    }
}
