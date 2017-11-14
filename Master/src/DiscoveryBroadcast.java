/*
 * DiscoveryBroadcast.java
 * 
 * Version:
 * 		1.0
 * Revision
 * 		1.0
 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * This is the Discovery Broadcast thread class. It will periodically broadcast
 * its own IP to all the nodes in the network. It uses datagram packet to send
 * this broadcast.
 * 
 * @author Ayush Vora
 * @author Harshal Khandare
 * @author Pranav Dadlani
 * 
 */

public class DiscoveryBroadcast extends Thread {
	InetAddress broadCastIP;
	DatagramSocket masterDiscoverySocket;
	int regPort;
	int bPort;
	int rUDPPort;
	int ackUDPPort;
	int requestPort;
	int monitorPort;
	String masterIP;

	/**
	 * This constructor references the necessary sockets and port details from
	 * the master class.
	 * 
	 * @param masterIP
	 *            IP address of the master Pi. Included in the broadcast
	 *            message.
	 * @param regPort
	 *            port where the slave Pi can send its registration message on.
	 *            Included in the broadcast message.
	 * @param bPort
	 *            broadcast port. This is the port where all the slaves listen
	 *            on for a broadcast.
	 * @param masterDiscoverySocket
	 *            datagram socket used by the master to send its broadcast.
	 * @param ackUDPPort
	 *            port to be used by the slave to send acknowledgement messages.
	 *            Included in the broadcast message.
	 * @param requestPort
	 *            port to be used by the slave to request for a job at the
	 *            master. Included in the broadcast
	 * @param monitorPort
	 *            port to be used by the slave to periodically send a ping
	 *            message on. Included in the broadcast.
	 * @throws UnknownHostException
	 */
	DiscoveryBroadcast(String masterIP, int regPort, int bPort,
			DatagramSocket masterDiscoverySocket, int ackUDPPort,
			int requestPort, int monitorPort) throws UnknownHostException {
		broadCastIP = InetAddress.getByName("255.255.255.255");
		this.bPort = bPort;
		this.regPort = regPort;
		this.masterDiscoverySocket = masterDiscoverySocket;
		this.ackUDPPort = ackUDPPort;
		this.requestPort = requestPort;
		this.monitorPort = monitorPort;
		this.masterIP = masterIP;
	}

	public void run() {
		try {
			System.out.println("BROADCAST Started");
			// periodically broadcast this message
			while (true) {
				// this string is present within the broadcast message and it
				// contains the master IP along with different port details
				// to be used by the slaves to request messages.
				String sourceInfo = masterIP + "_" + ackUDPPort + "_" + regPort
						+ "_" + requestPort + "_" + monitorPort;
				byte[] infoBytes = sourceInfo.getBytes();
				DatagramPacket bdp = new DatagramPacket(infoBytes,
						infoBytes.length, broadCastIP, bPort);
				masterDiscoverySocket.send(bdp);
				Thread.sleep(4000);
			}
		} catch (SocketException e) {
			masterDiscoverySocket.close();
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}