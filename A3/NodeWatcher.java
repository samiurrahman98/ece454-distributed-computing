import java.util.List;
import java.util.Arrays;
import java.util.Collections;

import java.net.InetSocketAddress;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.zookeeper.WatchedEvent;

import javax.xml.crypto.dsig.keyinfo.KeyValue;

public class NodeWatcher implements CuratorWatcher {
    private String zkName;

    private KeyValueHandler keyValueHandler;
    private CuratorFramework curClient;
    private KeyValueService.Client siblingClient;

    public NodeWatcher(CuratorFramework curClient, KeyValueHandler handler, String zkName) {
        this.curClient = curClient;
        this.keyValueHandler = handler;
        this.zkName = zkName;
    }

    public void classifyNode(int size) {
        if (this.keyValueHandler.getRole().equals(KeyValueHandler.ROLE.PRIMARY)) { return; }

        if (size == 1) {
            this.keyValueHandler.setRole(KeyValueHandler.ROLE.PRIMARY);
        } else if (size == 2) {
            this.keyValueHandler.setRole(KeyValueHandler.ROLE.BACKUP);
        } else {
            String msg = String.format("There are %d child nodes. There should not be more than 2 child nodes.", size);
            throw new RuntimeException(msg);
        }
    }

    synchronized public void process(WatchedEvent event) {
        try {
            List<String> children = this.curClient.getChildren().usingWatcher(this).forPath(zkName);
            Collections.sort(children);
            classifyNode(children.size());

            if (children.size() > 1) {
                InetSocketAddress address = ClientUtils.extractSiblingDetails(children, this.keyValueHandler.getZkNode(), this.keyValueHandler.getRole(), this.curClient);
                int threshold = this.keyValueHandler.getRole().equals(KeyValueHandler.ROLE.BACKUP) ? ClientUtils.BACKUP_POOL_NUM : ClientUtils.PRIMARY_POOL_NUM;
                ClientUtils.populateClientObjectPool(address.getHostName(), address.getPort(), threshold);
                this.keyValueHandler.setAlone(false);

                if (this.keyValueHandler.getRole().equals(KeyValueHandler.ROLE.PRIMARY))
                    this.keyValueHandler.transferMap();
            } else {
                keyValueHandler.setAlone(true);
            }

        } catch (Exception e) {
            System.out.println("Unable to determine primary " + e);
        }
    }
}