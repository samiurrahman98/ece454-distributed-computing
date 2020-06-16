import org.apache.thrift.transport.TTransport;

import java.time.Duration;

public class FENodeRunnable implements Runnable {
    private TTransport transport;
    private BcryptService.Client FENodeClient;
    private final String hostname;
    private final String port;

    private static final int MAX_ATTEMPTS = 100;
    private static final Duration RETRY_WAIT_TIME = Duration.ofMillis(200);

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
                Thread.sleep(Tracker.TIMEOUT.toMillis());
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (Tracker.isFENodeDown()) {
                establishConnectionToFENode();
            }
        }
    }

    /* 
    ** Function: establishConnectionToFENode
    ** Purpose: invoked by BE Node to attempt to establish a connection to a FE Node
    ** Parameters: none
    ** Returns: void
    */
    public void establishConnectionToFENode() {
        int numAttempts = 0;
        while (numAttempts < MAX_ATTEMPTS) {
            try {
                transport.open();
                FENodeClient.heartBeat(hostname, port);
                transport.close();
                
                return;
            } catch (Exception e) {
                numAttempts++;
                try {
                    Thread.sleep(RETRY_WAIT_TIME);
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            } finally {
                if (transport.isOpen()) transport.close();
            }
        }
    }
}