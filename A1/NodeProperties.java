import org.apache.thrift.transport.TSocket;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;

public class NodeProperties {
    private BcryptService.Client BENodeClient;
    private TTransport transport;
    private final String hostname;
    private final String port;
    private double load;
    private boolean occupied;
    public String nodeId;

    NodeProperties(String hostname, String port) {
        TSocket sock = new TSocket(hostname, Integer.parseInt(port));
        TTransport transport = new TFramedTransport(sock);
        TProtocol protocol = new TBinaryProtocol(transport);
        BcryptService.Client client = new BcryptService.Client(protocol);

        this.BENodeClient = client;
        this.transport = transport;
        this.nodeId = hostname + port;
        this.occupied = false;
        this.hostname = hostname;
        this.port = port;
    }

    public BcryptService.Client getClient() {
        return BENodeClient;
    }

    public TTransport getTransport() {
        return transport;
    }

    public void markOccupied() {
        occupied = true;
    }

    public void markFree() {
        occupied = false;
    }

    public boolean isNotOccupied() {
        return !occupied;
    }

    public void addLoad(int numPasswords, short logRounds) {
        load += numPasswords * Math.pow(2, logRounds);
    }

    public void reduceLoad(int numPasswords, short logRounds) {
        load -= numPasswords * Math.pow(2, logRounds);
    }

    public double getLoad() {
        return load;
    }

    public String getHostname() {
        return hostname;
    }

    public String getPort() {
        return port;
    }
}