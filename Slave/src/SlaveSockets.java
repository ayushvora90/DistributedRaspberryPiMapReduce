/* SlaveSockets.java
 * 
 * 	Version:1.0
 *  
 *  Revision:1.0
 */
import java.net.Socket;
/**
 * Class to define slave sockets.
 * @author Ayush Vora
 * @author Harshal Khandare
 * @author Pranav Dadlani
 *
 */
public class SlaveSockets {
	Socket slaveSocket;
	int slaveServerSocketPort;
	/**
	 * Parameterized constructor
	 * @param slaveSocket
	 * @param slaveServerSocketPort
	 */
	SlaveSockets(Socket slaveSocket, int slaveServerSocketPort){
		this.slaveSocket=slaveSocket;
		this.slaveServerSocketPort = slaveServerSocketPort;
	}
}
