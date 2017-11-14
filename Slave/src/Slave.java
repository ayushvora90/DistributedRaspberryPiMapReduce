/* Slave.java
 * 
 * 	Version:1.0
 *  
 *  Revision:1.0
 */
import java.util.*;
import java.io.*;
/**
 * Class to start the slave.
 * @author Ayush Vora
 * @author Harshal Khandare
 * @author Pranav Dadlani
 *
 */
public class Slave {
	/**
	 * Starts the response handler.
	 * @param args				ignore command args
	 * @throws IOException
	 */
	public static void main(String args[]) throws IOException{
		ResponseHandlerSlave slave = new ResponseHandlerSlave();
		slave.start();
	}
	

}
