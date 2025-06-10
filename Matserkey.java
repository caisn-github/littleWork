import java.security.SecureRandom;

public class Matserkey {
    public static final int MAX_LEN = 1000;
    // ?? matserkey具有什么元素呢？
    private int n; 
    private int [][]A;  // 因为后续要使用模q进行计算，所以

    // 矩阵A, 以及整形向量b,
    private int []b;
    SecureRandom random;
    int q;
    private double[]e;
    double sigma;
    int bound;


    /// 然后数据的init：
    public Matserkey(int n, int q,double sigma, SecureRandom random) {
        this.n = n;
        this.b = new int[this.n];
        this.A = new int[this.n][this.n];
        this.e = new double[this.n];
        this.q = q;
        this.random = random;
        this.sigma = sigma;
        this.bound = (int)Math.ceil(6 * sigma);
    }

    /// 主公钥的这里 要能生成A
    public void generateA() {
        for (int i=0; i < this.n; i++) {
            for (int j=0; j < this.n; j++) {
                A[i][j] = this.random.nextInt(q);
            }
        }
    }

    public int sample() {
        while (true) {
            // 均匀采样
            int x = this.random.nextInt((2 * this.bound + 1) - this.bound);
            // 接受概率
            double p = Math.exp(-x * x/(2 * sigma * sigma));
            // 均匀采样一个浮点数
            double u = this.random.nextDouble();

            if (u<p) {
                return x;
            }
        }
        
    }

    public void generateE(){
        for (int i=0; i<this.n; i++) {
            e[i] = sample();
        }
    } 
    
    /// 然后数据的get和set
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
} 