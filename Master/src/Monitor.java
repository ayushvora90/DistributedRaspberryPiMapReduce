/*
 * Monitor.java
 * 
 * Version:
 * 		1.0
 * Revision
 * 		1.0
 */

import java.security.KeyStore.Entry;
import java.util.*;
import java.io.*;
import java.net.DatagramSocket;
import java.net.Socket;

/**
 * This is the Monitor thread class. This thread will periodically monitor the
 * state of the sockets. It will remove any inactive sockets from the created
 * sockets map.
 * 
 * @author Ayush Vora
 * @author Harshal Khandare
 * @author Pranav Dadlani
 * 
 */

public class Monitor extends Thread {

	HashMap<String, Boolean> currentlyAliveSockets;
	HashMap<String, Socket> createdSockets;

	/**
	 * This constructor references important objects which will be used by this
	 * class.
	 * 
	 * @param currentlyAliveSockets
	 *            A map to denote the state of sockets.
	 * @param createdSockets
	 *            A map containing the socket connections for each Slave
	 *            program.
	 */

	Monitor(HashMap<String, Boolean> currentlyAliveSockets,
			HashMap<String, Socket> createdSockets) {
		this.currentlyAliveSockets = currentlyAliveSockets;
		this.createdSockets = createdSockets;
	}

	public void run() {
		while (true) {
			try {
				Thread.sleep(10000);
				System.out.println("MONITORING ");
				List<String> currentlyDeadSockets = new ArrayList<String>();

				// check the state of the sockets. true indicates active
				// sockets.
				for (Map.Entry<String, Boolean> e : currentlyAliveSockets
						.entrySet()) {

					// if the state is false remove the associated socket.
					if (!e.getValue()) {

						if (createdSockets.get(e.getKey()) != null) {
							//createdSockets.get(e.getKey()).close();
							//createdSockets.remove(e.getKey());
							currentlyDeadSockets.add(e.getKey());
							// currentlyAliveSockets.remove(e.getKey());
						}
					}
				}
				for (String key : currentlyDeadSockets) {
					currentlyAliveSockets.remove(key);
				}

				// set the state of all the sockets as false. It will be true as
				// soon as it receives a ping from that slave Pi.
				for (Map.Entry<String, Boolean> e : currentlyAliveSockets
						.entrySet()) {
					currentlyAliveSockets.put(e.getKey(), false);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			} 

		}

	}

}
