package action;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.KeyStore.SecretKeyEntry;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import keyClass.*;
import keyClass.Masterkey;
public class GenerateChildKey { //用来实现密钥派生
    Masterkey masterParent;
    SecretKey secretParent;
    ErrorList errorList;
    int index;

  
    Masterkey master_child;
    SecretKey secret_child;
    ErrorList error_child;


    // 将上一次的公钥，私钥输入进来，作为下一轮的计算
    public GenerateChildKey(Masterkey matserKey, SecretKey secretParent, ErrorList errorList, int index) {
        this.masterParent = matserKey;
        this.secretParent = secretParent;
        this.errorList = errorList;
        this.index = index;
    }

    public byte[] hmaxSha256(byte []chainCode, byte[] data) throws GeneralSecurityException {
        Mac hmac = Mac.getInstance("HmacSHA256");
        hmac.init(new SecretKeySpec(chainCode, "HmacSHA256"));
        return hmac.doFinal(chainCode);
    }

    public int[] generate_child_sk(byte []sha) {
        int n = secretParent.getN();
        int q = this.secretParent.getQ();
        int child[] = new int[n];
        for (int i=0;i<n;i++) {
            int val = ((sha[i % sha.length] & 0xFF) + (i * 31)) % q;
            child[i] = val;
        }
        return child;
    }
    
    public void generateChildKey() throws Exception {
        // 先将index转化为字节，然后拼接成data

        byte []indexBytes = new byte[4]; //因为int是4个字节
        indexBytes[0] = (byte) ((this.index >> 24) & 0xFF);
        indexBytes[1] = (byte) ((this.index >> 16) & 0xFF);
        indexBytes[2] = (byte) ((this.index >> 8) & 0xFF);
        indexBytes[3] = (byte) ((this.index ) & 0xFF);


        // 首先拼接 pk_m 以及 index形成data
        byte[] pk_m = masterParent.getPK_m();
        byte[] chainCode = masterParent.getChainCode();
        byte[] data = new byte[pk_m.length + indexBytes.length];
        int q = secretParent.getQ();
        int n = secretParent.getN();
        System.arraycopy(pk_m, 0, data, 0, pk_m.length);
        System.arraycopy(indexBytes, 0, data, pk_m.length, indexBytes.length);

        // HMAC-SHA256构造随机函数PRF输出，用于从父密钥生成子密钥，也是“密钥派生函数”的核心步骤
        byte []sha = hmaxSha256(chainCode, data);
        // 对sha取模q生成 子sk
        this.secret_child = new SecretKey(n, q, null); //因为之后不是使用random生成密钥，而是使用父公钥，父私钥生成新的密钥
        secret_child.setSk(generate_child_sk(sha));

        
        // 生成新的离散高斯误差数组 e
        int []e_child = this.errorList.generateE();

        GenerateMasterKey gen = new GenerateMasterKey(secret_child, errorList);
        // 对 (A*sk + e) mod q 生成子公钥
        int [][]A_child =gen.generateA();
        this.master_child.setA(A_child);
        this.masterParent.setPk_m(gen.serializePublicKey(A_child, e_child, n));
        // 生成新的子链码
        this.master_child.setChainCode(gen.generate_ChainCode(sha));

        // 于是所有的派生完成

    }

    public Masterkey getMasterChild() {
        return this.master_child;
    }

    public SecretKey getSecretChild() {
        return this.secret_child;
    }

}