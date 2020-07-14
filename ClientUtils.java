import java.util.*;
import java.net.InetSocketAddress;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

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

    static public void populateClientObjectPool(String host, Integer port, int cap) {
        if (cap == 0)
            return;

        if (clientObjectPool != null) {
            for (ThriftClient client: clientObjectPool)
                client.closeTransport();
        }

        clientObjectPool = new LinkedBlockingQueue<ThriftClient>(cap);

        for (int i = 0; i < cap; i++) {
            ThriftClient client = generateClient(host, port);

            try {
                clientObjectPool.put(client);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        static public ThriftClient getAvailable() throws InterruptedException {
            return ClientObjectPool.poll(50L, TimeUnit.SECONDS);
        }

        static public void makeAvailable(ThriftClient client) {
            try {
                clientObjectPool.put(client);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        static public ThriftClient generateClient(String host, Integer port) {
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

        static public InetSocketAddress extractSiblingInfo(List<String> children, String zkNode, KeyValueHandler.ROLE role, CuratorFramework curClient) throws Exception {
            String siblingZnode = determineSiblingZnode(children, role);
            byte[] data = curClient.getData().forPath(zkNode + "/" + siblingZnode);

            String strData = new String(data);
            String[] primary = strData.split(":");

            return new InetSocketAddress(primary[0], Integer.parseInt(primary[1]));
        }

        static private String determineSiblingZnode(List<String> children, KeyValueHandler.ROLE role) {
            if (children.size() != 2)
                throw new RuntimeException(String.format("Wrong number of child nodes, expected 2 got :%d", children.size()));

            Collections.sort(children);

            if (role.equals(KeyValueHandler.ROLE.BACKUP))
                return children.get(0);

            if (role.equals(KeyValueHandler.ROLE.PRIMARY))
                return children.get(1);

            throw new RuntimeException(String.format("role expected PRIMARY or BACKUP got: %s", role));
        }
    }
}