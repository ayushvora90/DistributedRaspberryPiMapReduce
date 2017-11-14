/* ChunkReceiveSend.java
 * 
 *  Version:1.0
 *  
 *  Revision:1.0
 */
import java.util.*;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Thread class to receive, process and send a chunk
 * @author Ayush Vora
 * @author Harshal Khandare
 * @author Pranav Dadlani
 *
 */
public class ChunkReceiveSend extends Thread {
	String piAddress;
	int serverSocketPort;
	int masterRequestPort;
	int slaveRequestSocketPort;
	int masterAckUDPPort;
	int masterRequestUDPPort;
	InetAddress masterIP;
	DatagramSocket slaveRequestSocket;
	ServerSocket slaveServerSocket;
	Queue<SlaveSockets> freePortsQueue;
	SlaveState stateOfSlave;
	HashSet<Integer> inUsePort;
	/**
	 * Parameterized constructor
	 * @param masterIP 					IP of the master node
	 * @param masterRequestPort			Receiving datagram socket port of master
	 * @param piAddress					IP address of the slave
	 * @param freePortsQueue			Queue of currently available slave sockets
	 * @param slaveRequestSocket		Transmitting datagram socket for sending slave requests
	 * @param slaveRequestSocketPort	Port number of slaves request socket					
	 * @param stateOfSlave				Indicates the state of the slave
	 * @throws UnknownHostException
	 */
	ChunkReceiveSend(InetAddress masterIP, int masterRequestPort,
			String piAddress, Queue<SlaveSockets> freePortsQueue,
			DatagramSocket slaveRequestSocket, int slaveRequestSocketPort,
			SlaveState stateOfSlave) throws UnknownHostException {
		this.masterIP = masterIP;
		this.masterRequestPort = masterRequestPort;
		this.freePortsQueue = freePortsQueue;
		this.slaveRequestSocket = slaveRequestSocket;
		this.slaveRequestSocketPort = slaveRequestSocketPort;
		this.piAddress = piAddress;
		this.stateOfSlave = stateOfSlave;

	}
	/**
	 * Checks the availability of the sockets in the queue. If sockets available code moves forward otherwise loops till a socket is free.
	 * @param freePortsQueue					Queue of currently available slave sockets
	 * @param masterPiSocketConnection			The socket connection with the master after registration
	 * @param newSlaveThread					The Slave socket associated with a particular masterPiSocketConnection and its port number
	 * @param slavePiServerSocketPort			The port number of the socket connection with the master after registration
	 * @return 
	 */
	public SlaveSockets checkQueueAvailability(Queue<SlaveSockets> freePortsQueue) {
		while (true) {
			if (freePortsQueue.size() > 0) {
				try {
					return freePortsQueue.remove();
				} catch (NoSuchElementException e) {
					e.printStackTrace();
					continue;
				}
			}
		}
	}
	/**
	 *	Send a request and wait for an acknowledgment, if socket times out send a duplicate request.
	 * @param slaveRequestSocket	Transmitting datagram socket for sending slave requests
	 * @param requestPacket			Datagram packet that carries this threads IP, port and request information
	 * @param masterRequestPort		Receiving datagram socket port of master.
	 * @param ackPacket				Acknowledgment packet from master for request
	 * @throws IOException
	 */
	public void sendRequest(DatagramSocket slaveRequestSocket,
			DatagramPacket requestPacket, int masterRequestPort,
			DatagramPacket ackPacket) throws IOException {
		while (true) {
			slaveRequestSocket.send(requestPacket);
			System.out.println("Job request sent to master at "
					+ masterRequestPort);
			try {
				slaveRequestSocket.setSoTimeout(5000);
				slaveRequestSocket.receive(ackPacket);
				System.out.println("Job ACK received from master port = "
						+ ackPacket.getPort());

				break;
			} catch (SocketTimeoutException e) {
				continue;
			}
		}

	}
	/**
	 * Run method for thread
	 */
	public void run() {
		System.out.println("Chunk Receive Send started.... ");
		SlaveSockets newSlaveThread = null;
		
		
		try {
			//Checks queue availability
			newSlaveThread = checkQueueAvailability(freePortsQueue);
			Socket masterPiSocketConnection = newSlaveThread.slaveSocket;
			int slavePiServerSocketPort = newSlaveThread.slaveServerSocketPort;

			String slaveRequestInfo = piAddress + "_" + slavePiServerSocketPort
					+ "_" + slaveRequestSocketPort;
			byte req[] = slaveRequestInfo.getBytes();
			DatagramPacket requestPacket = new DatagramPacket(req, req.length,
					masterIP, masterRequestPort);

			// need an ack from master? what if requestPacket gets lost?

			DatagramPacket ackPacket = new DatagramPacket(new byte[50], 50);
			//Send request
			sendRequest(slaveRequestSocket, requestPacket,
					masterRequestPort, ackPacket);
			// if code reaches here it means ack received
			// start streams as the connection is already existing

			// connection created if code reaches here

			System.out.println("Start Receive at PORT ="
					+ slavePiServerSocketPort);
			ObjectInputStream objInputStream = new ObjectInputStream(
					masterPiSocketConnection.getInputStream());
			ArrayList<Integer> chunkList = (ArrayList<Integer>) objInputStream
					.readObject();

			System.out.println("Chunk Received from Master at Port = "
					+ slavePiServerSocketPort + " with size= "
					+ chunkList.size());
			new ChunkReceiveSend(masterIP, masterRequestPort, piAddress,
					freePortsQueue, slaveRequestSocket, slaveRequestSocketPort, stateOfSlave)
					.start();
			
			Operations operation = new Operations();
			chunkList = operation.sort(chunkList);
//			Average avgOfList = operation.average(chunkList);
			System.out.println("Chunk Sorted");
			ObjectOutputStream objOutputStream = new ObjectOutputStream(
					masterPiSocketConnection.getOutputStream());
			objOutputStream.writeObject(chunkList);
			objOutputStream.flush();
//			objOutputStream.writeObject(avgOfList);
//			objOutputStream.flush();

			System.out.println("Solution Returned");

			freePortsQueue.add(newSlaveThread);

		} catch (UnknownHostException e) {

		} catch (IOException e1) {
			System.out.println("IOException occurred at port = "
					+ newSlaveThread.slaveServerSocketPort);
			stateOfSlave.goodState = false;
			// TODO Auto-generated catch block
			// e1.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
