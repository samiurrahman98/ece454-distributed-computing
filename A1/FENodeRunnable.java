import org.apache.thrift.transport.TTransport;

import java.time.Duration;

public class FENodeRunnable implements Runnable {
    private TTransport transport;
    private BcryptService.Client FENodeClient;
    private final String hostname;
    private final String port;

    private static final int MAX_ATTEMPTS = 100;
    private static final Duration RETRY_WAIT_TIME = Duration.ofSeconds(3);

    public FENodeRunnable (TTransport transport, BcryptService.Client client, String hostname, String port) {
        this.transport = transport;
        this.FENodeClient = client;
        this.port = port;
        this.hostname = hostname;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(BatchTracker.TIMEOUT.toMillis());
            } catch (Exception e) {
                System.out.println("interrupted thread on FENodeRunnable");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }

            if (BatchTracker.isFENodeDown()) {
                System.out.println("FENode is down!");
                establishConnectionToFENode();
            }
        }
    }

    public void establishConnectionToFENode() {
        int numAttempts = 0;

        while (numAttempts < MAX_ATTEMPTS) {
            try {
                transport.open();
                FENodeClient.heartBeat(hostname, port);
                transport.close();

                System.out.println("Successfully found FENode");
                return;
            } catch (Exception e) {
                numAttempts++;
                try {
                    Thread.sleep(RETRY_WAIT_TIME.toMillis());
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            } finally {
                if (transport.isOpen()) transport.close();
            }
        }
        System.out.println("Failed to connect " + MAX_ATTEMPTS + " times to the FENode");
    }
}