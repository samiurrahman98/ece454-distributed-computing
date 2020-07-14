import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.zookeeper.WatchedEvent;

import javax.xml.crypto.dsig.keyinfo.KeyValue;

public class NodeWatcher implements CuratorWatcher {
    private KeyValueHandler kvHandler;
    private CuratorFramework curClient;
    private KeyValueService.Client siblingClient;
    private String zkName;

    public NodeWatcher(CuratorFramework curClient, KeyValueHandler kvHandler, String zkName) {
        this.curClient = curClient;
        this.kvHandler = kvHandler;
        this.zkName = zkName;
    }

    public void classifyNode(int size) {
        if (kvHandler.getRole().equals(KeyValueHandler.ROLE.PRIMARY))
            return;

        if (size == 1)
            kvHandler.setRole(KeyValueHandler.ROLE.PRIMARY);
        else if (size == 2)
            kvHandler.setRole(KeyValueHandler.ROLE.BACKUP);
        else
            throw new RuntimeException(String.format("There are %d child Nodes", size));
    }

    synchronized public void process(WatchedEvent watchedEvent) {
        try {
            List<String> children = curClient.getChildren().usingWatcher(this).forPath(zkName);
            Collections.sort(children);
            classifyNode(children.size());

            if (children.size() > 1) {
                InetSocketAddress address = ClientUtils.extractSiblingInfo(children, kvHandler.getZkNode(), kvHandler.getRole(), curClient);
                int cap = kvHandler.getRole().equals(KeyValueHandler.ROLE.BACKUP) ? ClientUtils.BACKUP_POOL_NUM : ClientUtils.PRIMARY_POOL_NUM;
                ClientUtils.populateClientObjectPool(address.getHostName(), address.getPort(), cap);
                kvHandler.setAlone(false);

                if (kvHandler.getRole().equals(KeyValueHandler.ROLE.PRIMARY))
                    kvHandler.transferMap();
            } else
                kvHandler.setAlone(true);
        } catch (Exception e) {
            System.out.println("Unable to determine the primary replica: " + e.getMessage());
        }
    }
}