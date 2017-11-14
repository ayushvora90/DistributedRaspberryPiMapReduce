/*
 * Master.java
 * 
 * Version:
 * 		1.0
 * Revision
 * 		1.0
 */

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;

/**
 * This is the main Master program class. It initiates the periodic broadcast,
 * handles registration of the slave programs and starts the request handler
 * which will coordinate entire communication with the slave programs.
 * 
 * 
 * @author Ayush Vora
 * @author Harshal Khandare
 * @author Pranav Dadlani
 * 
 */

public class Master {
	int regPort;
	int discoveryPort;
	int ackUDPPort;
	int requestPort;
	int monitorPort;
	DatagramSocket masterDiscoverySocket;
	DatagramSocket registrationSocket;
	DatagramSocket masterACKPort;
	DatagramSocket requestSocket;
	DatagramSocket monitorSocket;
	ServerSocket masterSolutionReceiver;
	String masterPiIP;
	HashMap<String, HashSet<Integer>> workerTable;
	HashMap<String, Socket> createdSockets;
	JobCounter masterJobCounter = new JobCounter(0, 0);

	/**
	 * This constructor initiates several ports and sockets to be used by the
	 * master program.
	 * 
	 * @throws IOException
	 */
	Master() throws IOException {
		requestPort = 4500;
		regPort = 3000;
		discoveryPort = 3500;
		ackUDPPort = 12200;
		monitorPort = 6570;
		masterPiIP = getPiInterfaceAddress().replace("/", "");
		masterDiscoverySocket = new DatagramSocket(discoveryPort);
		masterACKPort = new DatagramSocket(ackUDPPort);
		registrationSocket = new DatagramSocket(regPort);
		requestSocket = new DatagramSocket(requestPort);
		monitorSocket = new DatagramSocket(monitorPort);
		createdSockets = new HashMap<String, Socket>();
		workerTable = new HashMap<String, HashSet<Integer>>();
	}

	// Master(int transmissionPort, int receptionPort) throws
	// UnknownHostException{
	// masterPiIP = InetAddress.getLocalHost();
	// }

	/**
	 * This function is used to retrieve the IP address of the Pi where the
	 * master program is running.
	 * 
	 * @return String IP address of the current master Pi
	 */
	public String getPiInterfaceAddress() {

		System.out.println("Getting Pi Interface");
		NetworkInterface ni = null;
		try {
			ni = NetworkInterface.getByName("eth0");
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
		String piAddress = null;
		while (inetAddresses.hasMoreElements()) {
			InetAddress ia = inetAddresses.nextElement();
			if (!ia.isLinkLocalAddress()) {
				piAddress = ia.getHostAddress();
				System.out.println("Got Address " + piAddress);
			}
		}
		return piAddress;
	}

	/**
	 * This is the main method. It creates several new threads such as Discovery
	 * Broadcast, Slave Registration and Request Handler and starts them.
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String args[]) throws IOException {
		String dataset = args[0];
		Master myPi = new Master();
		//new InvokeScript().start();
		new DiscoveryBroadcast(myPi.masterPiIP, myPi.regPort, 6000,
				myPi.masterDiscoverySocket, myPi.ackUDPPort, myPi.requestPort,
				myPi.monitorPort).start();
		new SlaveRegistration(myPi.registrationSocket, myPi.masterACKPort,
				myPi.createdSockets, myPi.monitorSocket).start();
		new RequestHandlerMaster(dataset, myPi.workerTable, myPi.createdSockets,
				myPi.requestSocket, myPi.masterJobCounter, myPi.masterACKPort)
				.start();
	}

}
