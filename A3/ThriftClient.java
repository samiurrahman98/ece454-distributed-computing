import java.util.Map;
import java.util.List;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransport;

public class ThriftClient {
    public String host;
    public Integer port;

    private TTransport transport;
    private KeyValueService.Client client;

    public ThriftClient(KeyValueService.Client client, TTransport transport, String host, Integer port) {
        this.client = client;
        this.transport = transport;
        this.host = host;
        this.port = port;
    }

    public void forward(String key, String value, int sequence) throws TException {
        this.client.forward(key, value, sequence);
    }

    public String get(String key) throws TException {
        return this.client.get(key);
    }

    public Map<String, String> getDataDump() throws TException {
        return this.client.getDataDump();
    }

    public void setMyMap(List<String> keys, List<String> values) throws TException{
        this.client.setMyMap(keys, values);
    }

    public void closeTransport() {
        this.transport.close();
    }
}