package action;
// 用来做验证，看公钥以及私钥的产生是否一致
// 即验证： b = A*sk 与 公钥实际的 b' 的差值是否为一个极小值
public class CheckSafe {
    int []master_b;
    int [][]A;
    int []sk;
    int e[]; //用来存放实际的误差结果
    int n;

    public CheckSafe(int[]master_b, int [][]A, int []sk, int n) {
        this.master_b = master_b;
        this.A = A;
        this.sk = sk;
        this.n = n;
        this.e  = new int[n]; //e是用来计算 A * sk 与 master_b之间的差值的 
    }

    public void calculateE() {
        for (int i = 0;i < this.n;i++) {
            int tmp = 0;
            for (int j=0;j<this.n;j++) {
                tmp += A[i][j] * this.sk[j];
            }
            this.e[i] = this.master_b[i] - tmp;
        }
    }

    public int[] getE() {
        return this.e;
    }
}