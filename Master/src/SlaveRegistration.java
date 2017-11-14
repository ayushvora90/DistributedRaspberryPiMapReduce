/*
 * SlaveRegistration.java
 * 
 * Version:
 * 		1.0
 * Revision
 * 		1.0
 */

import java.util.*;
import java.util.Map.Entry;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * This is the Slave Registration thread class. It handles registration of the
 * slave programs with the master.
 * 
 * 
 * @author Ayush Vora
 * @author Harshal Khandare
 * @author Pranav Dadlani
 * 
 */

public class SlaveRegistration extends Thread {
	DatagramSocket registrationSocket;
	DatagramSocket masterACKPort;
	HashMap<String, Socket> createdSockets;
	DatagramSocket monitorSocket;
	HashMap<String, Boolean> currentlyAliveSockets;

	/**
	 * This constructor references important objects to be used by this class.
	 * 
	 * @param registrationSocket
	 *            socket for registration
	 * @param masterACKPort
	 *            socket for acknowledgments
	 * @param createdSockets
	 *            A map containing the socket connections for each Slave
	 *            program.
	 * @param monitorSocket
	 *            a dedicated datagram socket to receive ping messages.
	 */
	SlaveRegistration(DatagramSocket registrationSocket,
			DatagramSocket masterACKPort,
			HashMap<String, Socket> createdSockets, DatagramSocket monitorSocket) {
		this.registrationSocket = registrationSocket;
		this.masterACKPort = masterACKPort;
		this.createdSockets = createdSockets;
		this.monitorSocket = monitorSocket;
		currentlyAliveSockets = new HashMap<String, Boolean>();
	}

	public void run() {
		// start ping handler thread.
		new PingHandler(currentlyAliveSockets, monitorSocket, createdSockets)
				.start();

		while (true) {

			Socket slavePiThread = null;
			InetAddress slaveIP = null;
			try {
				byte infoBytes[] = new byte[50];
				DatagramPacket sdp = new DatagramPacket(infoBytes,
						infoBytes.length);

				// receive registration message from slave Pi
				registrationSocket.receive(sdp);
				System.out.print("Registration Received from----------- ");

				String slaveInfo = new String(sdp.getData());
				String infoSlave[] = slaveInfo.split("_");
				byte ackBytes[] = new String("ACK").getBytes();
				int ackBytesLeng = ackBytes.length;
				DatagramPacket ack = new DatagramPacket(ackBytes, ackBytesLeng,
						InetAddress.getByName(infoSlave[0]),
						Integer.parseInt(infoSlave[4].trim()));

				// send ack to slavePi before starting to send a job
				masterACKPort.send(ack);
				System.out.println("Slave IP = " + infoSlave[0]
						+ "\nSlave TCP Port 1 = " + infoSlave[1]
						+ "\nSlave TCP Port 2 = " + infoSlave[2]
						+ "\nSlave TCP Port 3 = " + infoSlave[3]
						+ "\nSlave UDP port = " + infoSlave[4]);

				String slaveIPPort[] = new String[3];
				slaveIPPort[0] = infoSlave[0] + ":" + infoSlave[1];
				slaveIPPort[1] = infoSlave[0] + ":" + infoSlave[2];
				slaveIPPort[2] = infoSlave[0] + ":" + infoSlave[3];
				System.out.println("---------------------------");

				slaveIP = InetAddress.getByName(infoSlave[0]);
				Thread.sleep(1000);

				// add the slave Pi by connecting and storing its socket in
				// createdSockets map
				for (int i = 1; i < 4; i++) {
					if (!createdSockets.containsKey(slaveIPPort[i - 1])) {
						slavePiThread = new Socket(infoSlave[0],
								Integer.parseInt(infoSlave[i]));
						createdSockets.put(slaveIPPort[i - 1], slavePiThread);
					}

				}
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				continue;
			} catch (IOException e) {

				// remove the socket from created sockets if IOExeption occurs.
				System.out.println("IOException occurred in registration");
				Set<String> keys = createdSockets.keySet();
				for (String IPPort : keys) {
					if (IPPort.contains(slaveIP.toString().replace("/", ""))) {
						createdSockets.remove(IPPort);
					}
				}
				continue;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
		}

	}

}
