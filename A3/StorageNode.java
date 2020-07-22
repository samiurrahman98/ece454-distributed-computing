import java.io.*;
import java.util.*;

import org.apache.thrift.*;
import org.apache.thrift.server.*;
import org.apache.thrift.transport.*;
import org.apache.thrift.protocol.*;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.*;
import org.apache.curator.*;
import org.apache.curator.retry.*;
import org.apache.curator.framework.*;
import org.apache.curator.utils.*;

import org.apache.log4j.*;

public class StorageNode {
    static Logger log;

    public static void main(String [] args) throws Exception {
		BasicConfigurator.configure();
		log = Logger.getLogger(StorageNode.class.getName());

		if (args.length != 4) {
			System.err.println("Usage: java StorageNode host port zkconnectstring zknode");
			System.exit(-1);
		}

		CuratorFramework curClient =
			CuratorFrameworkFactory.builder()
			.connectString(args[2])
			.retryPolicy(new RetryNTimes(10, 1000))
			.connectionTimeoutMs(1000)
			.sessionTimeoutMs(10000)
			.build();

		curClient.start();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				curClient.close();
			}
			});

		KeyValueHandler kvHandler = new KeyValueHandler(args[0], Integer.parseInt(args[1]), curClient, args[3]);
		KeyValueService.Processor<KeyValueService.Iface> processor = new KeyValueService.Processor<>(new KeyValueHandler(args[0], Integer.parseInt(args[1]), curClient, args[3]));
		TServerSocket socket = new TServerSocket(Integer.parseInt(args[1]));
		TThreadPoolServer.Args sargs = new TThreadPoolServer.Args(socket);
		sargs.protocolFactory(new TBinaryProtocol.Factory());
		sargs.transportFactory(new TFramedTransport.Factory());
		sargs.processorFactory(new TProcessorFactory(processor));
		sargs.maxWorkerThreads(64);
		TServer server = new TThreadPoolServer(sargs);
		log.info("Launching server");

		new Thread(new Runnable() {
			public void run() {
				server.serve();
			}
			}).start();

		new Thread(new Runnable() {
			public void run() {
				String zkNode = args[3];

				try {
					List<String> children = new ArrayList<String>();
					while (children.size() == 0) {
						curClient.sync();
						children = curClient.getChildren().forPath(zkNode);
					}

					if (children.size() == 1)
						return;

					Collections.sort(children);
					byte[] backupData = curClient.getData().forPath(zkNode + "/" + children.get(children.size() - 1));
					String strBackupData = new String(backupData);
					String[] backup = strBackupData.split(":");
					String backupHost = backup[0];
					int backupPort = Integer.parseInt(backup[1]);

					byte[] primaryData = curClient.getData().forPath(zkNode + "/" + children.get(children.size() - 2));
					String strPrimaryData = new String(primaryData);
					String[] primary = strPrimaryData.split(":");
					String primaryHost = primary[0];
					int primaryPort = Integer.parseInt(primary[1]);

					TSocket sock = new TSocket(primaryHost, primaryPort);
					TTransport transport = new TFramedTransport(sock);
					transport.open();
					TProtocol protocol = new TBinaryProtocol(transport);
					KeyValueService.Client primaryClient = new KeyValueService.Client(protocol);

					while (true) {
						try {
							Thread.sleep(50);
							primaryClient.setPrimary(true);
							continue;
						} catch (Exception e) {
							System.out.println("Backup lost connection to primary");
							break;
						}
					}
					
					curClient.delete().forPath(zkNode + "/" + children.get(children.size() - 2));

					sock = new TSocket(backupHost, backupPort);
					transport = new TFramedTransport(sock);
					transport.open();
					protocol = new TBinaryProtocol(transport);
					KeyValueService.Client backupClient = new KeyValueService.Client(protocol);

					backupClient.setPrimary(true);

				} catch (Exception e) {
					e.printStackTrace();
				}
			};
		}).start();
    }
}
