package keyClass;
import java.security.SecureRandom;
import java.util.Arrays;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import action.*;
public class Masterkey {
    public static final int MAX_LEN = 1000;
    // ?? matserkey具有什么元素呢？
    private int n; 
    private int [][]A;  // 因为后续要使用模q进行计算，所以
    GenerateMasterKey gen;
    
    // 矩阵A, 以及整形向量b,
    private int []b;
    SecureRandom random;
    int q;
    byte[] pubkeyBytes;
    byte[] chainCode;


    /// 然后数据的init：
    public Masterkey(GenerateMasterKey gen, int n, int q, SecureRandom random) {
        this.gen = gen;
        this.n = n;
        // this.b = new int[this.n];
        // this.A = new int[this.n][this.n];
        this.q = q ;
        this.random = random;
    }

    /// 主公钥的这里 要能生成A
    
    public void generatePK_m() throws Exception {
        int n = this.n;
        int len = n*n*4 + n*8; //因为int是4字节，而n是8字节
        int [][]A = this.gen.generateA();
        int []b = this.gen.generateB(A);
        byte[] pubkeyBytes = this.gen.serializePublicKey(A, b, len);
        this.pubkeyBytes = pubkeyBytes;
        this.chainCode = gen.generate_ChainCode(pubkeyBytes);
    }

    
    public int getN() {
        return this.n;   
    }
    public void setN(int n) {
        this.n = n;
        return ; 
    }

    public int[] getb() {
        return this.b;
    }

    public void setb(int []b) {
        this.b = b;
    }
    public int [][] getA() {
        return this.A;
    }

    public void setA(int [][]A) {
        this.A = A;
    }

    public byte[] getPK_m(){
        return this.pubkeyBytes;
    }

    public void setPk_m(byte []pubkeyBytes) {
        this.pubkeyBytes = pubkeyBytes;
    }

    public byte[] getChainCode() {
        return this.chainCode;
    }

    public void setChainCode(byte []chainCode) {
        this.chainCode = chainCode;
    }
} 