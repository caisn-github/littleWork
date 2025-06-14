package action;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

import keyClass.*;
public class GenerateMasterKey{  //产生公钥
    SecretKey secret;
    ErrorList errorList;

    public GenerateMasterKey(SecretKey secret, ErrorList errorList) {
        this.secret = secret;
        this.errorList = errorList;
    }
    

    public int[] generateB(int [][]A) {
        int sk[] = this.secret.getSk();
        int n = secret.getN();
        int q = secret.getQ();
        int []b = new int[n];
        int []e = this.errorList.generateE();
        // 计算B, B = A * sk // 一个个乘起来
        for (int i = 0 ; i < n;i++) {
            b[i] = 0;
                double tmp = 0;
            for (int j = 0;j<n;j++) {
                tmp = (A[i][j] * sk[j] + e[j]) %q;
            }
            b[i] += tmp;
        }
        return b;
        
    }

    public int[][] generateA() {
        int n = secret.getN();
        int q = secret.getQ();
        int [][]A = new int[n][n];
        SecureRandom random = this.errorList.getRandom();
        for (int i=0; i < n; i++) {
            for (int j=0; j < n; j++) {
                A[i][j] = random.nextInt(q);
            }
        }
        return A;
    }

    public byte[] serializePublicKey(int [][]A,int[] b, int len) {
       // 先写入A矩阵，
       ByteBuffer buffer = ByteBuffer.allocate(len);
       int n = secret.getN();
       for (int i =0; i < n;i++) {
            for (int j=0; j< n;j++) {
                buffer.putInt(A[i][j]);
            }
       }

       for (int i=0;i<n;i++) {
        buffer.putInt(b[i]);
       }
       byte[] pubkeyBytes = buffer.array();
       // 再写入b向量
       return pubkeyBytes;
        
    }


    public byte[] generate_ChainCode(byte[] pubkeyBytes) throws GeneralSecurityException {
        MessageDigest sah256 = MessageDigest.getInstance("SHA_256");
        byte[] hash = sah256.digest(pubkeyBytes);
        byte[] chainCode = Arrays.copyOf(hash,32); // 取前32个作为chainCode
        return chainCode;
    }
}