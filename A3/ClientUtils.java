import java.util.List;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;

import java.net.InetSocketAddress;

import org.apache.curator.framework.CuratorFramework;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

public class ClientUtils {
    public static final int BACKUP_POOL_NUM = 0;
    public static final int PRIMARY_POOL_NUM = 16;

    private static LinkedBlockingQueue<ThriftClient> clientObjectPool;

    static public void populateClientObjectPool(String host, Integer port, int threshold) {
        if (threshold == 0) { return; }

        if (clientObjectPool != null) {
            for (ThriftClient client : clientObjectPool) {
                client.closeTransport();
            }
        }

        clientObjectPool = new LinkedBlockingQueue<ThriftClient>(threshold);

        for (int i = 0; i < threshold; i++) {
            ThriftClient client = generateRPCClient(host, port);
            try {
                clientObjectPool.put(client);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static public ThriftClient getAvailable() throws InterruptedException {
        return clientObjectPool.poll(50L, TimeUnit.SECONDS);
    }

    static public void makeAvailable(ThriftClient client) {
        try {
            clientObjectPool.put(client);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static public ThriftClient generateRPCClient(String host, Integer port) {
        try {
            TSocket sock = new TSocket(host, port);
            TTransport transport = new TFramedTransport(sock);
            transport.open();
            TProtocol protocol = new TBinaryProtocol(transport);
            return new ThriftClient(new KeyValueService.Client(protocol), transport, host, port);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static public InetSocketAddress extractSiblingDetails(List<String> children, String zkNode, KeyValueHandler.ROLE role, CuratorFramework curClient) throws Exception {
        String siblingZnode = determineSiblingZnode(children, role);
        byte[] data = curClient.getData().forPath(zkNode + "/" + siblingZnode);

        String strData = new String(data);
        String[] primary = strData.split(":");

        System.out.println(String.format("%s found connection details for sibling at %s:%s", role, primary[0], primary[1]));

        return new InetSocketAddress(primary[0], Integer.parseInt(primary[1]));
    }

    static private String determineSiblingZnode(List<String> children, KeyValueHandler.ROLE role) {
        if (children.size() != 2) {
            String msg = String.format("Incorrect number of child nodes, expected 2 got :%d", children.size());
            throw new RuntimeException(msg);
        }

        Collections.sort(children);

        if (role.equals(KeyValueHandler.ROLE.BACKUP)) { return children.get(0); }

        if (role.equals(KeyValueHandler.ROLE.PRIMARY)) { return children.get(1); }

        // (should never happen but keeping here just in case)
        throw new RuntimeException(String.format("role expected PRIMARY or BACKUP got: %s", role));
    }
}