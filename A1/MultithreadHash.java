import java.util.*;

public class MultiThreadHash implements Runnable {
    private List<String> passwords;
    private short logRounds;
    private String[] res;
    private int start;
    private int end;
    private CountDownLatch latch;

    public MultiThreadHash(List<String> passwords, short logRounds, String[] res, int start, int end, CountDownLatch latch) {
        this.logRounds = logRounds;
        this.passwords = passwords;
        this.res = res;
        this.start = start;
        this.end = end;
        this.latch = latch;
    }

    @Override
    public void run() {
        hashPassword(passwords, logRounds, res, start, end);
        latch.countDown();
    }
}