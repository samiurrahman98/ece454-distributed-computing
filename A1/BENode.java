import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;

import org.apache.thrift.transport.TSocket;
import org.apache.thrift.TProcessorFactory;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TFramedTransport;

import java.net.InetAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class BENode {
    static Logger log;

    private static ExecutorService executorService = Executors.newCachedThreadPool(new ThreadFactory() {
        private AtomicInteger threadCounter = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setDaemon(false);
            t.setPriority(Thread.NORM_PRIORITY);
            t.setName("worker-thread-" + threadCounter.incrementAndGet());
            return t;
        }
    });

    public static void main(String [] args) throws Exception {
        if (args.length != 3) {
            System.err.println("Usage: FEhost FEport BEport");
            System.exit(-1);
        }

		BasicConfigurator.configure();
		log = Logger.getLogger(BENode.class.getName());

		String hostFE = args[0];
		String hostBE = getHostName();
		int portFE = Integer.parseInt(args[1]);
		int portBE = Integer.parseInt(args[2]);
		log.info("Launching BE node on port " + portBE + " at host " + hostBE);

		// Create a client to the FENode
		TSocket sock = new TSocket(hostFE, portFE);
		TTransport transport = new TFramedTransport(sock);
		TProtocol protocol = new TBinaryProtocol(transport);
		BcryptService.Client client = new BcryptService.Client(protocol);

        // FENodeRunnable FENodeConnection = new FENodeRunnable(transport, client, hostBE, args[2]);
        FENodeRunnable FENodeConnection = new FENodeRunnable(transport, hostBE, args[2]);
        FENodeConnection.establishConnectionToFENode();
        executorService.submit(FENodeConnection);

		// launch Thrift server
		BcryptService.Processor processor = new BcryptService.Processor(new BcryptServiceHandler(true));
		TServerSocket socket = new TServerSocket(portBE);
        TThreadPoolServer.Args sargs = new TThreadPoolServer.Args(socket);
        sargs.protocolFactory(new TBinaryProtocol.Factory());
        sargs.transportFactory(new TFramedTransport.Factory());
        sargs.processorFactory(new TProcessorFactory(processor));
        TThreadPoolServer server = new TThreadPoolServer(sargs);
        server.serve();
    }

	static String getHostName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (Exception e) {
			return "localhost";
		}
	}
 }