import java.security.SecureRandom;
import java.util.Scanner;
public class Main {
    public static void main (String []args) {
        int []seeds = new int[500]; // seeds是32字节，所以后续要转化为bytes[]类型
        int n = 512; //维度512
        int q = 1 << 23; //模数2^23
        
        Scanner sacnner = new Scanner(System.in);

        SecureRandom random = new SecureRandom(seeds);

        Matserkey master = new Matserkey(n, q, q, null)

    }

}