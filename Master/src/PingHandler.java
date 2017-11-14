/*
 * PingHandler.java
 * 
 * Version:
 * 		1.0
 * Revision
 * 		1.0
 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * This is the Ping handler thread class. It will receive ping messages from all
 * the slave Pi's and will accordingly update their state.
 * 
 * 
 * @author Ayush Vora
 * @author Harshal Khandare
 * @author Pranav Dadlani
 * 
 */

public class PingHandler extends Thread {
	HashMap<String, Boolean> currentlyAliveSockets;
	DatagramSocket monitorSocket;
	HashMap<String, Socket> createdSockets;

	/**
	 * This constructor references important objects to be used by this class.
	 * 
	 * @param currentlyAliveSockets
	 *            A map to denote the state of sockets.
	 * @param monitorSocket
	 *            a dedicated datagram socket to receive ping messages.
	 * @param createdSockets
	 *            A map containing the socket connections for each Slave
	 *            program.
	 */

	PingHandler(HashMap<String, Boolean> currentlyAliveSockets,
			DatagramSocket monitorSocket, HashMap<String, Socket> createdSockets) {
		this.currentlyAliveSockets = currentlyAliveSockets;
		this.monitorSocket = monitorSocket;
		this.createdSockets = createdSockets;
	}

	public void run() {
		new Monitor(currentlyAliveSockets, createdSockets).start();

		while (true) {
			DatagramPacket receivedIsAlive = new DatagramPacket(new byte[75],
					75);

			try {
				// receive ping
				monitorSocket.receive(receivedIsAlive);

				// extract info from the message.
				String receivedInfo = new String(receivedIsAlive.getData());
				String infoIsAlive[] = receivedInfo.split("_");
				infoIsAlive[3] = infoIsAlive[3].trim();

				// update the state of the sockets
				for (int i = 1; i < infoIsAlive.length; i++) {

					currentlyAliveSockets.put(infoIsAlive[0] + ":"
							+ infoIsAlive[i], true);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}

		}
	}
}