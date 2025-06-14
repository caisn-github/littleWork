import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Scanner;

import action.*;
import action.GenerateChildKey;
import action.GenerateMasterKey;
import keyClass.*;
public class Main {
    final static int SEEDS_LEN=500;
    // 输入Seeds
    public static String inputBuffer() {
        Scanner scanner = new Scanner(System.in);
        String buffer;
        buffer = scanner.nextLine();
        buffer = buffer.replaceAll("0x", "")
                        .replaceAll("\s+", "");  // 删除掉16进制前面的0x表示，以及所有空格
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
    public static void main (String []args) throws Exception {
        // 初始化参数
        int n = 512; //维度512
        int q = 1 << 23; //模数2^23
        q = q-1; // 取模一般取的是素数
        double alpha = 0.01;

        // 获取seed
        System.out.println("请输入SEED:");
        String buffer = inputBuffer();
        byte []seeds = new byte[buffer.length()]; // seeds是32字节，所以后续要转化为bytes[]类型
        copyBuffer(buffer, seeds);
        System.out.println(Arrays.toString(seeds));

        // 通过seeds初始化random
        SecureRandom random = new SecureRandom(seeds);

        // 通过random初始化SecretKey和MasterKey
        SecretKey secretKey = new SecretKey(n, q, random);
        ErrorList errorList = new ErrorList(n, alpha, random);
        GenerateMasterKey gen= new GenerateMasterKey(secretKey, errorList);
        Masterkey master = new Masterkey(gen, n, q,  random);

        // 产生私钥
        secretKey.generateSk();
        int []sk = secretKey.getSk();
        System.err.println("主公钥：");
        System.err.println(Arrays.toString(sk));

        //产生公钥
        master.generatePK_m();
        byte[] pk_m = master.getPK_m();
        System.err.println("主公钥：");
        System.err.println(Arrays.toString(pk_m));

        // 然后通过pk_m计算chaincode
        master.getChainCode();
        byte[] chainCode = master.getChainCode();
        System.err.println("主ChainCode:");
        System.err.println(Arrays.toString(chainCode));

        
        //然后基于格基伪随机函数LWE-PRF实现密钥派生
        int index = 1; //第二个问题需要输入索引号
        GenerateChildKey gen_child = new GenerateChildKey(master, secretKey, errorList, index);
        gen_child.generateChildKey();
        Masterkey master_child = gen_child.getMasterChild();
        SecretKey secret_child = gen_child.getSecretChild();
        System.err.println("子私钥：");
        System.err.println(Arrays.toString(secret_child.getSk()));
        System.err.println("子公钥：");
        System.err.println(Arrays.toString(master_child.getPK_m()));
        System.err.println("子ChainNode：");
        System.err.println(Arrays.toString(master_child.getChainCode()));

        // 重复5次即可生成五层格密码， 待做


        // 计算公钥和私钥生成是否合理
        // CheckSafe safe = new CheckSafe(b, master.getA(), secretKey.getSk(), n);
        // safe.calculateE();
        // int e[] = safe.getE();
        // System.err.println(Arrays.toString(e));
    }

}