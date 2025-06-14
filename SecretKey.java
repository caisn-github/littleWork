import java.security.SecureRandom;

public class SecretKey{
    // 元素： sk短向量
    int n;
    // private int []sk;
    private int []sk;
    int q;
    SecureRandom random;
    

    public SecretKey(int n, int q, SecureRandom random) {
        this.n = n;
        this.sk = new int[n];
        this.q = q;
        this.random = random;
    }

    public boolean generateSk() {
        for (int i = 0; i < this.n; i++) {
            this.sk[i] = this.random.nextInt(q);
        }
        return true;
    }

    public int[] getSk() {
        return this.sk;
    }
}
