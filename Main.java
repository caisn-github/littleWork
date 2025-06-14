import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Scanner;
public class Main {
    final static int SEEDS_LEN=500;
    // 输入Seeds
    public static String inputBuffer() {
        Scanner scanner = new Scanner(System.in);
        String buffer;
        buffer = scanner.nextLine();
        buffer = buffer.replaceAll("0x", "")
                        .replaceAll("\\s+", "");  // 删除掉16进制前面的0x表示，以及所有空格
        scanner.close();
        return buffer;
    }

    public static void copyBuffer(String buffer, byte[] seeds) {
        byte[] str = new byte[SEEDS_LEN];
        str = buffer.getBytes();        
        for ( int i =0 ;i < str.length;i++) {
            seeds[i] = str[i];
        }
        System.out.println(Arrays.toString(seeds));
    }
    public static void main (String []args) {
        int n = 512; //维度512
        int q = 1 << 23; //模数2^23
        q = q-1; // 取模一般取的是素数
        double sigma = 0.01;

        System.out.println("请输入SEED:");
        String buffer = inputBuffer();
        byte []seeds = new byte[buffer.length()]; // seeds是32字节，所以后续要转化为bytes[]类型

        copyBuffer(buffer, seeds);

        System.out.println(Arrays.toString(seeds));

        // 通过seeds初始化random
        SecureRandom random = new SecureRandom(seeds);

        // 通过random初始化SecretKey和MasterKey
        SecretKey secretKey = new SecretKey(n, q, random);
        Matserkey master = new Matserkey(n, q, sigma, random);

        secretKey.generateSk();
        int []sk = secretKey.getSk();

        master.generateA();
        master.generateE();

        System.out.println("输出SK:");
        System.out.println(Arrays.toString(sk));


    }

}