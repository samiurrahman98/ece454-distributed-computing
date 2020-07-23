import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.thrift.*;
import org.apache.thrift.server.*;
import org.apache.thrift.transport.*;
import org.apache.thrift.protocol.*;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.*;
import org.apache.curator.*;
import org.apache.curator.retry.*;
import org.apache.curator.framework.*;

public class KeyValueHandler implements KeyValueService.Iface {
    private int port;
    private String host;
    private String zkNode;
    private boolean alone = true;

    private ROLE role = ROLE.UNDEFINED;
    
    private static AtomicInteger sequence;
    private static final int MAX_MAP_SIZE = 500000;

    private Map<String, String> myMap;
    private Map<String, Integer> sequenceMap;
    
    private CuratorFramework curClient;
    
    enum ROLE {
        PRIMARY,
        BACKUP,
        UNDEFINED
    }

    public void setRole(ROLE newRole) {
        this.role = newRole;
    }

    public ROLE getRole() {
        return this.role;
    }

    public void setAlone(boolean alone) {
        this.alone = alone;
    }

    public String getZkNode() {
        return this.zkNode;
    }

    public KeyValueHandler(String host, int port, CuratorFramework curClient, String zkNode) {
        this.host = host;
        this.port = port;
        this.curClient = curClient;
        this.zkNode = zkNode;

        this.sequenceMap = new ConcurrentHashMap<String, Integer>();
        this.myMap = new ConcurrentHashMap<String, String>();
        this.sequence = new AtomicInteger(0);
    }

    public String get(String key) throws org.apache.thrift.TException {
        String ret = this.myMap.get(key);

        if (ret == null) { return ""; }
        else { return ret; }
    }

    public void put(String key, String value) throws org.apache.thrift.TException {
        this.myMap.put(key, value);

        if (this.role.equals(ROLE.PRIMARY) && !this.alone)
            forwardData(key, value, sequence.addAndGet(1));
    }

    public void forward(String key, String value, int sequence) {
        if (this.sequenceMap.containsKey(key)) {
            if (sequence >= this.sequenceMap.get(key)) {
                this.myMap.put(key, value);
                this.sequenceMap.put(key, sequence);
            }
        } else {
            this.myMap.put(key, value);
            this.sequenceMap.put(key, sequence);
        }
    }

    public void forwardData(String key, String value, int sequence) throws org.apache.thrift.TException {
        ThriftClient tClient = null;
        try {
            tClient = ClientUtils.getAvailable();
            tClient.forward(key, value, sequence);
        } catch (org.apache.thrift.TException | InterruptedException e) {
            e.printStackTrace();
            tClient = ClientUtils.generateRPCClient(tClient.host, tClient.port);
        } finally {
            if (tClient != null) {
                ClientUtils.makeAvailable(tClient);
            }
        }
    }

    public void transferMap() throws org.apache.thrift.TException {
        List<String> keys = new ArrayList<String>(this.myMap.keySet());
        List<String> values = new ArrayList<String>(keys.size());
        for (int i = 0; i < keys.size(); i++) {
            values.add(i, this.myMap.get(keys.get(i)));
        }

        if (this.myMap.size() > MAX_MAP_SIZE) {
            int index = 0;
            int end = 0;

            while (end != keys.size()) {
                end = Math.min(index + MAX_MAP_SIZE, keys.size());
                setSiblingMap(keys.subList(index, end), values.subList(index, end));
                index = end;
            }
        } else {
            setSiblingMap(keys, values);
        }
    }

    public void setSiblingMap(List<String> keys, List<String> values) {
        ThriftClient tClient = null;
        try {
            tClient = ClientUtils.getAvailable();
            tClient.setMyMap(keys, values);
        } catch (org.apache.thrift.TException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (tClient != null) {
                ClientUtils.makeAvailable(tClient);
            }
        }
    }

    public void setMyMap(List<String> keys, List<String> values) {
        for (int i = 0; i < keys.size(); i++) {
            if (!this.myMap.containsKey(keys.get(i))) {
                this.myMap.put(keys.get(i), values.get(i));
            }
        }
    }

    public void fetchDataDump() throws org.apache.thrift.TException {
        if (!this.role.equals(ROLE.BACKUP)) {
            throw new RuntimeException(String.format("fetchDataDump() should only be called by BACKUP, called by: ", role));
        }

        ThriftClient tClient = null;

        try {
            tClient = ClientUtils.getAvailable();
            Map<String, String> tempMap = tClient.getDataDump();
            for (String key: tempMap.keySet()) {
                if (!this.myMap.containsKey(key)) {
                    this.myMap.put(key, tempMap.get(key));
                }
            }
        } catch (org.apache.thrift.TException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (tClient != null) {
                ClientUtils.makeAvailable(tClient);
            }
        }
    }

    public Map<String, String> getDataDump() throws org.apache.thrift.TException {
        if (!this.role.equals(ROLE.PRIMARY)) {
            throw new RuntimeException(String.format("Should only be implemented by PRIMARY, implemented by: %s", role));
        }
        return myMap;
    }
}