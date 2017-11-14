/*
 * RequestHandlerMaster.java
 * 
 * Version:
 * 		1.0
 * Revision
 * 		1.0
 */
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Thread class to handle job requests from slave
 * 
 * @author Ayush Vora
 * @author Harshal Khandare
 * @author Pranav Dadlani
 * 
 */
public class RequestHandlerMaster extends Thread {
	HashMap<Integer, Integer> countsMap;
	Queue<Integer> jobQueue;
	ArrayList<Integer> finishedJobs;
	HashMap<String, Socket> createdSockets;
	HashSet<String> currentlyUsedSockets;
	JobCounter masterJobCounter;
	DatagramSocket masterACKPort;
	List<Average> listOfAverages;
	Boolean fileReadIndicator = false;
	DatagramSocket requestSocket;
	String dataset;

	/**
	 * Parameterized constructor of master
	 * @param dataset 
	 * 
	 * @param workerTable
	 * @param createdSockets
	 * @param requestSocket
	 * @param masterJobCounter
	 * @param masterACKPort
	 */
	RequestHandlerMaster(String dataset, HashMap<String, HashSet<Integer>> workerTable,
			HashMap<String, Socket> createdSockets,
			DatagramSocket requestSocket, JobCounter masterJobCounter,
			DatagramSocket masterACKPort) {
		this.dataset = dataset;
		this.masterACKPort = masterACKPort;
		this.requestSocket = requestSocket;
		this.createdSockets = createdSockets;
		this.masterJobCounter = masterJobCounter;
		jobQueue = new LinkedBlockingQueue<Integer>();
		countsMap = new HashMap<Integer, Integer>();
		currentlyUsedSockets = new HashSet<String>();
		finishedJobs = new ArrayList<Integer>();
		listOfAverages = new ArrayList<Average>();
	}

	/**
	 * Run method for this thread.
	 */
	public void run() {
		try {
			new JobsGenerator(dataset, countsMap, jobQueue, fileReadIndicator,
					masterJobCounter).start();

			while (!masterJobCounter.fileReadIndicator
					|| (masterJobCounter.completedJobs != masterJobCounter.createdJobs)) {
				try {

					byte requestBytes[] = new byte[50];

					// Get request messages
					DatagramPacket requestPacket = new DatagramPacket(
							requestBytes, requestBytes.length);
					requestSocket.receive(requestPacket);
					String slaveInfo = new String(requestPacket.getData());
					String[] infoSlave = slaveInfo.split("_");
					System.out.println("Request Received from " + slaveInfo);
					System.out.println("Created count ="
							+ masterJobCounter.createdJobs);
					System.out.println("Completed count ="
							+ masterJobCounter.completedJobs);
					System.out.println("JOB QUEUE >>>> " + jobQueue.toString()
							+ " <<<<");
					System.out.println(masterJobCounter.fileReadIndicator);
					if (masterJobCounter.fileReadIndicator
							&& (masterJobCounter.createdJobs == masterJobCounter.completedJobs))
						break;
					String slaveIPPort = infoSlave[0] + ":" + infoSlave[1];
					// If jobs available in queue and socket not already in use
					if (jobQueue.size() > 0
							&& !currentlyUsedSockets.contains(slaveIPPort)) {
						// Send Ack
						byte ackBytes[] = new String("ACK").getBytes();
						int ackBytesLeng = ackBytes.length;
						DatagramPacket ack = new DatagramPacket(ackBytes,
								ackBytesLeng,
								InetAddress.getByName(infoSlave[0]),
								Integer.parseInt(infoSlave[2].trim()));
						requestSocket.send(ack);
						System.out.println("Sending ACK for chunk request at "
								+ infoSlave[0]);
						Socket slavePiThread = createdSockets.get(slaveIPPort);
						currentlyUsedSockets.add(slaveIPPort);

						// dequeue
						// call chunk send receive and pass
						int jobIndex = jobQueue.remove();
						// Start servicing a job request
						new ChunkSendReceive(createdSockets, jobIndex,
								slavePiThread, masterJobCounter, jobQueue,
								currentlyUsedSockets, finishedJobs,
								listOfAverages).start();
						System.out.println("FINISHED JOBS >>>>>>> "
								+ finishedJobs.toString());
					}

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			System.out.println("EXITED >>>>>>>>>>>>>>>>>>>");
			System.out.println("FINISHED =================");
			Merge merger = new Merge(countsMap);
			merger.mergeCompletedJobs(masterJobCounter.completedJobs);
			BufferedReader br = new BufferedReader(new FileReader("Sol.txt"));
			String line="";
			int prev = -1;
			double total = 0;
			int count = 0;
			while((line = br.readLine())!=null){
				int num = Integer.parseInt(line);
				
				if(num==prev){
					continue;
				}
				else{
					total = total + num;
					count++;
				}
				
				
				prev = num;
			}
			
			double average = total / count;
			
			
			
//			double avg = mergeAverage.getAvg(listOfAverages);
			
			System.out.println("\nAverage : " + average);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
