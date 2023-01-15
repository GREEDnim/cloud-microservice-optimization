
package utils;

/**
 *
 * @author User
 */
public class VmType {
    public class Type1{
        public static final int ram = 1000; // 1GB
        public static final int disk = 4000; // 4GB
        public static final int mips = 1000;
        public static final int cost = 5000; // per hour
        public static final int size = 10000;
        public static final int pesNumber = 1;
        public static final int bw = 1000;
    }
    public class Type2{
        public static final int ram = 750; // 1GB
        public static final int disk = 3000; // 3GB
        public static final int mips = 500;
        public static final int cost = 2000;// per hour
        public static final int size = 10000;
        public static final int pesNumber = 1;
        public static final int bw = 1000;
    }
    public class Type3{
        public static final int ram = 500; // 1GB
        public static final int disk = 1500; // 4GB
        public static final int mips = 300;
        public static final int cost = 1000; // per hour
        public static final int size = 10000;
        public static final int pesNumber = 1;
        public static final int bw = 1000;
    }
    public class Type4{
        public static final int ram = 250; // 1GB
        public static final int disk = 700; // 4GB
        public static final int mips = 200; //
        public static final int cost = 250; // per hour
        public static final int size = 10000;
        public static final int pesNumber = 1;
        public static final int bw = 1000;
    }
    public class Type5{
        public static final int ram = 100; // 1GB
        public static final int disk = 500; // 4GB
        public static final int mips = 200;
        public static final int cost = 100;// per hour
        public static final int size = 10000;
        public static final int pesNumber = 1;
        public static final int bw = 1000;
    }
    public final static int[][] type = {{Type1.ram, Type1.disk, Type1.mips, Type1.cost,Type1.size,Type1.pesNumber,Type1.bw}
                            ,{Type2.ram, Type2.disk, Type2.mips, Type2.cost,Type2.size,Type2.pesNumber,Type2.bw},
                            {Type3.ram, Type3.disk, Type3.mips, Type3.cost,Type3.size,Type3.pesNumber,Type3.bw},
                            {Type4.ram, Type4.disk, Type4.mips, Type4.cost,Type4.size,Type4.pesNumber,Type4.bw},
                            {Type5.ram, Type5.disk, Type5.mips, Type5.cost,Type5.size,Type5.pesNumber,Type5.bw}
                            };
}
