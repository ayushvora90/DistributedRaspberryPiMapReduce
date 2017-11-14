/* IsAlive.java
 * 
 *  Version:1.0
 *  
 *  Revision:1.0
 */
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Thread class to periodically ping master.
 * @author Ayush Vora
 * @author Harshal Khandare
 * @author Pranav Dadlani
 *
 */
public class IsAlive extends Thread {
	String piAddress;
	int rTCPPort[];
	DatagramSocket isAliveSocket;
	int monitorPort;
	InetAddress masterIP;
	SlaveState stateOfSlave;
	/**
	 * Parameterized constructor
	 * @param isAliveSocket
	 * @param piAdress
	 * @param rTCPPort
	 * @param masterIP
	 * @param monitorPort
	 * @param stateOfSlave
	 * @throws SocketException
	 */
	IsAlive(DatagramSocket isAliveSocket, String piAdress,int rTCPPort[], InetAddress masterIP, int monitorPort, SlaveState stateOfSlave) throws SocketException{
		this.piAddress = piAdress;
		this.rTCPPort = rTCPPort;
		this.monitorPort = monitorPort;
		this.masterIP = masterIP;
		this.isAliveSocket = isAliveSocket;
		this.stateOfSlave = stateOfSlave;
	}
	/**
	 * Run method of thread isAlive
	 */
	public void run(){
		String slaveRegInfo = piAddress + "_" + rTCPPort[0] + "_" + rTCPPort[1] + "_" + rTCPPort[2];
		byte sBytes[] = slaveRegInfo.getBytes();
		DatagramPacket srp = new DatagramPacket(sBytes, sBytes.length,
				masterIP,monitorPort);
		while(stateOfSlave.goodState){
			try {
				isAliveSocket.send(srp);
				Thread.sleep(4000);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
	}
}
