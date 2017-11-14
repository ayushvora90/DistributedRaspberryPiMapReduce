/* ResponseHandlerSlave.java
 * 
 *  Version:1.0
 *  
 *  Revision:1.0
 */
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
/**
 * Thread class to get a broadcast from master, initiate requests and handle responses for requests.
 * @author Ayush Vora
 * @author Harshal Khandare
 * @author Pranav Dadlani
 *
 */
public class ResponseHandlerSlave extends Thread {
	DatagramSocket discoveryBroadcastReceiver;
	DatagramSocket isAliveSocket;
	DatagramSocket slaveRequestSocket;
	ServerSocket slaveJobReceiver[];
	Socket masterSoc[];
	Queue<SlaveSockets> freePortsQueue;
	String piAddress;
	int masterRequestPort;
	SlaveState stateOfSlave;
	int rTCPPort[];
	int slaveRequestSocketPort;
	String masterInfo[];
	int masterMonitorPort;
	int masterAckUDPPort;
	int masterRegPort;
	InetAddress masterIP;
	/**
	 * Default constructor
	 */
	ResponseHandlerSlave()  {
		piAddress = getPiInterfaceAddress();
		generateSockets();
		slaveRequestSocketPort = 11250;
		masterSoc = new Socket[3];
		stateOfSlave = new SlaveState();
		freePortsQueue = new LinkedBlockingQueue<SlaveSockets>();
	}
	/**
	 * Displays master information from broadcast
	 * @param masterInfo		Master information
	 * @throws UnknownHostException
	 */
	public void displayInfo(String[] masterInfo) throws UnknownHostException{
		System.out.println("Master IP = " + masterInfo[0] + "\n"
				+ "Master ACK UDP Port = " + masterInfo[1] + "\n"
				+ "Master Reg Port = " + masterInfo[2] + "\nMaster Req Port "+ masterInfo[3]+"\nMaster Monitor Port "+ masterInfo[4]);

		System.out.println("--------");

		masterIP = InetAddress.getByName(masterInfo[0]);
		masterAckUDPPort = Integer.parseInt(masterInfo[1]);		//for 2nd handshake
		masterRegPort = Integer.parseInt(masterInfo[2]);
		masterRequestPort = Integer.parseInt(masterInfo[3]);
		masterMonitorPort = Integer.parseInt(masterInfo[4].trim());
		
	}
	
	
	
	/**
	 * Send a registration message and register socket connections after receiving acknowledgment
	 * @param piAddress					IP address of slave
	 * @param rTCPPort					Receiving TCP ports for slave server sockets
	 * @param masterIP					Master IP address
	 * @param masterRegPort				Master registration datagram socket port
	 * @param slaveRequestSocket		Slaves request datagram socket
	 * @param slaveJobReceiver			Slaves server sockets
	 * @param masterSoc					Socket connections with master
	 * @param freePortsQueue			Queue of available slave sockets
	 * @throws IOException
	 */
	public void sendRegistration(String piAddress,int rTCPPort[],InetAddress masterIP,int masterRegPort,DatagramSocket slaveRequestSocket, ServerSocket[] slaveJobReceiver, Socket[] masterSoc, Queue<SlaveSockets> freePortsQueue) throws IOException{
		String slaveRegInfo = piAddress + "_" + rTCPPort[0] + "_" + rTCPPort[1] + "_" + rTCPPort[2] + "_"
				+ slaveRequestSocketPort;
		byte sBytes[] = slaveRegInfo.getBytes();
		DatagramPacket srp = new DatagramPacket(sBytes, sBytes.length,
				masterIP, masterRegPort);
		DatagramPacket ack = new DatagramPacket(new byte[75], 75);

		while (true) {
			slaveRequestSocket.send(srp);		//send registration
			System.out.println("Registration Sent");
			try {
				slaveRequestSocket.setSoTimeout(5000);
				slaveRequestSocket.receive(ack);
				System.out.println("Registration Successful");
				break;
			} catch (SocketTimeoutException e) {
				continue;
			}
		}
		slaveJobReceiver[0].setSoTimeout(5000);
		slaveJobReceiver[1].setSoTimeout(5000);
		slaveJobReceiver[2].setSoTimeout(5000);

		masterSoc[0] = slaveJobReceiver[0].accept();
		masterSoc[1] = slaveJobReceiver[1].accept();
		masterSoc[2] = slaveJobReceiver[2].accept();

		System.out.println("Connection established with master");
		SlaveSockets newSlaveThread[] = new SlaveSockets[3];
		newSlaveThread[0] = new SlaveSockets(masterSoc[0],rTCPPort[0]);
		newSlaveThread[1] = new SlaveSockets(masterSoc[1],rTCPPort[1]);
		newSlaveThread[2] = new SlaveSockets(masterSoc[2],rTCPPort[2]);
		freePortsQueue.add(newSlaveThread[0]);
		freePortsQueue.add(newSlaveThread[1]);
		freePortsQueue.add(newSlaveThread[2]);

		System.out.println("Port Queue ready for Jobs");
		System.out.println("SLAVE READY");
		
	}
	/**
	 * Gets the IP address of the ethernet interface of pi
	 * @return
	 */
	public String getPiInterfaceAddress(){

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
			if (!ia.isLinkLocalAddress()){
				piAddress = ia.getHostAddress();
				System.out.println("Got Address "+piAddress);
			}

		}
		return piAddress;
	}
	/**
	 * Generates random server socket ports
	 */
	public void generateSockets(){
		System.out.println("Generating Sockets");
		//rTCPPort[0] = 12000;
		slaveJobReceiver = new ServerSocket[3];
		rTCPPort = new int[3]	;
		freePortsQueue = new LinkedBlockingQueue<SlaveSockets>();
		int seedPort = (int)(Math.random()*9000)+1000;
		int counter = 0;

		while(counter<3){
			try {


				seedPort = seedPort + 1000;
				slaveJobReceiver[counter] = new ServerSocket(seedPort);
				rTCPPort[counter] = seedPort;
				System.out.println("ServerSocket created at port "+seedPort);
				//				freePortsQueue.add(newSlaveSocket);
				counter++;
			} catch (IOException e) {
				continue;
			}
		}

	}
	/**
	 * Run method of response handler thread.
	 */
	public void run() {
		try {
			isAliveSocket =  new DatagramSocket(3745);
			discoveryBroadcastReceiver = new DatagramSocket(6000);
			slaveRequestSocket = new DatagramSocket(slaveRequestSocketPort);

			byte infoBytes[] = new byte[75];
			//Get discovery broadcast
			DatagramPacket mdp = new DatagramPacket(infoBytes, infoBytes.length);
			discoveryBroadcastReceiver.receive(mdp);		//receive from broadcast of master
			System.out.println("Received BROADCAST from --------");
			String destInfo = new String(mdp.getData());
			masterInfo = destInfo.split("_");
			displayInfo(masterInfo);
			//End discovery broadcast
			
			//start isAlive thread to send ping messages to master for status
			new IsAlive(isAliveSocket,piAddress,rTCPPort,masterIP,masterMonitorPort,stateOfSlave).start();
			//send a registration request and wait for ack
			sendRegistration(piAddress, rTCPPort, masterIP, masterRegPort, slaveRequestSocket, slaveJobReceiver, masterSoc,freePortsQueue);
			//Registration ends

			//Start requesting for jobs				
			new ChunkReceiveSend(masterIP, masterRequestPort, piAddress, freePortsQueue, slaveRequestSocket, slaveRequestSocketPort,stateOfSlave).start();  //not sure of slavereqsocket
			
			
			//Monitor status of all threads, if one fails restart
			while(true){
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(!stateOfSlave.goodState){
					System.err.println("Some error occurred HERE ?");
					masterSoc[0].close();
					masterSoc[1].close();
					masterSoc[2].close();
					System.err.println("HARD RESET");
					throw new IOException();
					//break;
				}
			}
		} catch (SocketTimeoutException e) {
			System.err.println("Timeout exception occured here");
			System.out.println("REBOOTING SLAVE");
			try {
				slaveRequestSocket.close();
				isAliveSocket.close();
				discoveryBroadcastReceiver.close();
				new ResponseHandlerSlave().start();
			} catch (Exception e2) {
				System.out.println("ANOTHER INSTANCE OF SLAVE ALREADY RUNNING");
				System.out.println("EXIT");
			}
		} catch (IOException e) {
			System.out.println("REBOOTING SLAVE");
			try {
				slaveRequestSocket.close();
				isAliveSocket.close();
				discoveryBroadcastReceiver.close();
				new ResponseHandlerSlave().start();
			} catch (Exception e2) {
				System.out.println("ANOTHER INSTANCE OF SLAVE ALREADY RUNNING");
				System.out.println("EXIT");
			}
		}
	}
}
