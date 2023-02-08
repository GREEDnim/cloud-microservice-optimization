package utils;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.lists.VmList;

import java.io.*;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;

public class GenerateMatrices {
    private static double[][] commMatrix, execMatrix;//传输转移消耗矩阵，执行时间矩阵
    private File commFile = new File("CommunicationTimeMatrix.txt");//传输消耗矩阵
    private File execFile = new File("ExecutionTimeMatrix.txt"); //执行消耗矩阵
    private String filePath = "cloudlets.txt";
    private List<Vm> vmlist;

    public GenerateMatrices() {
        commMatrix = new double[Constants.NO_OF_TASKS][Constants.NO_OF_VMS];
        execMatrix = new double[Constants.NO_OF_TASKS][Constants.NO_OF_VMS];
        try {
            if (commFile.exists() && execFile.exists()) {
                readCostMatrix();
            } else {
                initCostMatrix();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //基于已有虚拟机列表的各矩阵初始化
    public GenerateMatrices(List<Vm> vmlist){
        this.vmlist = vmlist;
        commMatrix = new double[Constants.NO_OF_TASKS][Constants.NO_OF_VMS];
        execMatrix = new double[Constants.NO_OF_TASKS][Constants.NO_OF_VMS];
        try {
            if (commFile.exists() && execFile.exists()) {
                readCostMatrix();
            } else {
                initCostMatrix(filePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initCostMatrix() throws IOException {
        System.out.println("Initializing new Matrices...");
        BufferedWriter commBufferedWriter = new BufferedWriter(new FileWriter(commFile));
        BufferedWriter execBufferedWriter = new BufferedWriter(new FileWriter(execFile));
        DecimalFormat df = new DecimalFormat("0");

        //生成传输矩阵和执行矩阵，
        for (int i = 0; i < Constants.NO_OF_TASKS; i++) {
            for (int j = 0; j < Constants.NO_OF_VMS; j++) {
                commMatrix[i][j] = Math.random()*10 + 10;
                execMatrix[i][j] = Math.random()*100 + 500;
                commBufferedWriter.write(df.format(commMatrix[i][j]) + ' ');
                execBufferedWriter.write(df.format(execMatrix[i][j]) + ' ');
            }
            commBufferedWriter.write('\n');
            execBufferedWriter.write('\n');
        }
        commBufferedWriter.close();
        execBufferedWriter.close();
    }

    private void readCostMatrix() throws IOException {
        System.out.println("Reading the Matrices...");
        BufferedReader commBufferedReader = new BufferedReader(new FileReader(commFile));

        int i = 0, j = 0;
        do {
            String line = commBufferedReader.readLine();
            for (String num : line.split(" ")) {
                commMatrix[i][j++] = new Double(num);
            }
            ++i;
            j = 0;
        } while (commBufferedReader.ready());


        BufferedReader execBufferedReader = new BufferedReader(new FileReader(execFile));

        i = j = 0;
        do {
            String line = execBufferedReader.readLine();
            for (String num : line.split(" ")) {
                execMatrix[i][j++] = new Double(num);
            }
            ++i;
            j = 0;
        } while (execBufferedReader.ready());
    }

    public static double[][] getCommMatrix() {
        return commMatrix;
    }

    public static double[][] getExecMatrix() {
        return execMatrix;
    }

    //一般public方法
    public double[][] getcommMatrix() {
        return commMatrix;
    }

    public double[][] getexecMatrix(){
        return execMatrix;
    }

    //基于样本生成任务初始化矩阵的方法
    private void initCostMatrix(String filePath) throws IOException
    {
        @SuppressWarnings("resource")
        BufferedReader br= new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
        String data = null;
        int index=0;
        double[] LengthGroup = new double[Constants.NO_OF_TASKS];
        while ((data = br.readLine()) != null)
        {
            System.out.println(data);
            String[] taskLength=data.split("\t");//tasklength[i]是任务执行的耗费（指令数量）
            for(int j=0;j<taskLength.length;j++){
                LengthGroup[index+j] = Double.parseDouble(taskLength[j]);
                if((index+j+1)==Constants.NO_OF_TASKS)
                {
                    br.close();
                    initMatrix(LengthGroup);
                    return;
                }
            }
            //20 cloudlets each line in the file cloudlets.txt.
            index+=taskLength.length;
        }
    }

    /**
     * 初始化矩阵
     * @param LengthGroup
     * @throws IOException
     */
    private void initMatrix(double[] LengthGroup) throws IOException
    {
        System.out.println("Initializing new Matrices...by sampel");
        BufferedWriter commBufferedWriter = new BufferedWriter(new FileWriter(commFile));
        BufferedWriter execBufferedWriter = new BufferedWriter(new FileWriter(execFile));
        //Random rm = new Random();
        for (int i = 0; i < Constants.NO_OF_TASKS; i++) {
            for (int j = 0; j < Constants.NO_OF_VMS; j++) {
                // 通信消耗 = image size / bw = 10000/1000 = 10,20,5
                commMatrix[i][j] = Calculator.div(VmList.getById(vmlist,j).getSize(),VmList.getById(vmlist, j).getBw());
                // 执行消耗 = 随机任务长度 / mips = (1000~2000)/1000
                //execMatrix[i][j] = Calculator.div(LengthGroup[rm.nextInt(Constants.NO_OF_TASKS)],VmList.getById(vmlist, j).getMips());
                execMatrix[i][j] = Calculator.div(LengthGroup[i],VmList.getById(vmlist, j).getMips());
                 //execMatrix[i][j] = Calculator.div(LengthGroup[i],VmList.getById(vmlist, j).getHost().getTotalAllocatedMipsForVm(VmList.getById(vmlist, j)));
                commBufferedWriter.write(String.valueOf(commMatrix[i][j]) + ' ');
                execBufferedWriter.write(String.format("%.2f",execMatrix[i][j]) + ' ');
                //execBufferedWriter.write(String.format("%.2f",execMatrix[i][j]) + ' ');
            }
            commBufferedWriter.write('\n');
            execBufferedWriter.write('\n');
        }
        commBufferedWriter.close();
        execBufferedWriter.close();
    }


    public void SetVmList(List<Vm> vmlist)
    {
        this.vmlist = vmlist;
    }

    public List<Vm> GetVmList()
    {
        return vmlist;
    }
}
