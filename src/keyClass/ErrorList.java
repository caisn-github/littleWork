
package keyClass;
import java.security.SecureRandom;

public class ErrorList {
    int n;
    double alpha;
    SecureRandom random;
    int bound;

    public ErrorList(int n ,double alpha, SecureRandom random) {
        this.n=n;
        this.alpha=alpha;
        this.random = random;
        this.bound = (int)Math.ceil(6 * this.alpha);
    }
    public int sample() {
        while (true) {
            // 均匀采样
            int x = this.random.nextInt((2 * bound + 1) - bound);
            // 接受概率
            double p = Math.exp(-x * x/(2 * this.alpha * this.alpha));
            // 均匀采样一个浮点数
            double u = this.random.nextDouble();

            if (u<p) {
                return x;
            }
        }
        
    }

    public int[] generateE(){
        int[] e = new int[this.n];
        for (int i=0; i<this.n; i++) {
            e[i] = sample();
        }
        return e;
    } 

    public int getBound() {
        return this.bound;
    }

    public SecureRandom getRandom() {
        return this.random;
    }
}