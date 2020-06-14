public class MultiThreadCheck implements Runnable {
    private List<String> passwords;
    private List<String> hashes;
    private Boolean[] res;
    int start;
    int end;
    CountDownLatch latch;

    public MultiThreadCheck(List<String> passwords, List<String> hashes, Boolean[] res, int start, int end, CountDownLatch latch) {
        this.passwords = passwords;
        this.hashes = hashes;
        this.res = res;
        this.start = start;
        this.end = end;
        this.latch = latch;
    }

    @Override
    public void run() {
        checkPassword(passwords, hashes, res, start, end);
        latch.countDown();
    }
}