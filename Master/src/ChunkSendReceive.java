/*
 * ChunkSendReceive.java
 * 
 * Version:
 * 		1.0
 * Revision
 * 		1.0
 */

import java.util.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * This is the Chunk Send Receive thread class. A thread of this class will be
 * associated with the sending of a chunk to the slave program and receiving a
 * sorted chunk in return. The sorted chunk is written to a file to be used by
 * the merge process.
 * 
 * @author Ayush Vora
 * @author Harshal Khandare
 * @author Pranav Dadlani
 * 
 */

public class ChunkSendReceive extends Thread {
	int jobIndex;
	Socket slavePi;
	JobCounter masterJobCounter;
	Queue<Integer> jobQueue;
	HashMap<String, Socket> createdSockets;
	HashSet<String> currentlyUsedSockets;
	ArrayList<Integer> finishedJobs;
	List<Average> listOfAverages;
	static String restartedPi = "";

	/**
	 * This constructor references important objects which will be used by this
	 * class.
	 * 
	 * @param createdSockets
	 *            A map containing the socket connection for each Slave program.
	 * @param jobIndex
	 *            This is the current job ID which the thread will use to create
	 *            a chunk from.
	 * @param slavePi
	 *            Slave socket to be used by the thread for communication with
	 *            the Slave program.
	 * @param masterJobCounter
	 *            counter counter to be incremented when the job is finished
	 *            processing
	 * @param jobQueue
	 *            This queue contains all pending jobs to be processed
	 * @param currentlyUsedSockets
	 *            A set of sockets which are currently in use
	 * @param finishedJobs
	 *            A list of jobs which have successfully processed.
	 * @param listOfAverages
	 *            Average for the current chunk will be added to this list.
	 */

	ChunkSendReceive(HashMap<String, Socket> createdSockets, int jobIndex,
			Socket slavePi, JobCounter masterJobCounter,
			Queue<Integer> jobQueue, HashSet<String> currentlyUsedSockets,
			ArrayList<Integer> finishedJobs, List<Average> listOfAverages) {
		this.currentlyUsedSockets = currentlyUsedSockets;
		this.jobIndex = jobIndex;
		this.slavePi = slavePi;
		this.masterJobCounter = masterJobCounter;
		this.jobQueue = jobQueue;
		this.createdSockets = createdSockets;
		this.finishedJobs = finishedJobs;
		this.listOfAverages = listOfAverages;

	}

	public void run() {
		try {
			System.out.println("\n[" + jobIndex
					+ "]Chunk Send Receive Started...."
					+ slavePi.getInetAddress() + " PORT=" + slavePi.getPort());

			// open job file using the job index. jobs file are stored as
			// intermediate .uvc object files.
			File listFile = new File(jobIndex + ".uvc");

			// read the object file of the job and store it as arraylist. This
			// is the job chunk list to be processed.
			ObjectInputStream fileObjInputStream = new ObjectInputStream(
					new BufferedInputStream(new FileInputStream(listFile)));
			ArrayList<Integer> uniqueChunkList = (ArrayList<Integer>) fileObjInputStream
					.readObject();

			// send the job chunk to the slave Pi by writing it to the slave
			// sockets output stream.
			ObjectOutputStream objOutputStream = new ObjectOutputStream(
					slavePi.getOutputStream());
			objOutputStream.writeObject(uniqueChunkList);
			objOutputStream.flush();

			System.out
					.println("Chunk (" + jobIndex + ") Sent to Pi "
							+ slavePi.getInetAddress() + " port = "
							+ slavePi.getPort());

			// receive the sorted chunk along with the average by reading from
			// the input stream of the slave Pi socket.
			ObjectInputStream objInputStream = new ObjectInputStream(
					slavePi.getInputStream());
			@SuppressWarnings("unchecked")
			List<Integer> solutionChunkList = (List<Integer>) objInputStream
					.readObject();
//			Average avgOfList = (Average) objInputStream.readObject();
			System.out.println("Sorted Chunk (" + jobIndex + ") "
					+ "Received from Pi " + slavePi.getInetAddress()
					+ " port = " + slavePi.getPort());

			// Increment the completed jobs counter
			// write the sorted chunk to a temporary txt file of this chunk.
			FileWriter writer = new FileWriter(
					(masterJobCounter.completedJobs++) + ".txt");
			for (int i = 0; i < solutionChunkList.size(); i++) {
				writer.write(solutionChunkList.get(i).toString() + "\n");
			}
			writer.close();

			// add the average of this chunk to the list of averages.
//			listOfAverages.add(avgOfList);
			System.out.println("Completed Job (" + jobIndex + ")\n");

			// add the job index i.e ID to the finished Jobs list.
			finishedJobs.add(jobIndex);

			// remove the current socket as it has completed processing.
			currentlyUsedSockets.remove(slavePi.getInetAddress().toString()
					.replace("/", "")
					+ ":" + slavePi.getPort());

		} catch (IOException e) {
			// in case of any IO Exception
			System.out.println("IOException Occurred for "
					+ slavePi.getInetAddress() + " port = " + slavePi.getPort()
					+ "\n");
			System.out.println("Recovering from error");

			// add the job currently being processed by this thread back to job
			// Queue as it did not finish.
			jobQueue.add(jobIndex);
			System.out
					.println("Job (" + jobIndex + ") of "
							+ slavePi.getInetAddress() + " port= "
							+ slavePi.getPort() + " placed back in job Queue "
							+ jobQueue.toString() + "\n");

			// close the associated socket and remove it from the created
			// sockets map.
			Set<String> keys = createdSockets.keySet();
			ArrayList<String> keyList = new ArrayList<String>();

			for (String IPPort : keys) {
				if (IPPort.contains(slavePi.getInetAddress().toString()
						.replace("/", ""))) {
					keyList.add(IPPort);
				}
			}
			for (String IPPort : keyList) {
				try {
					createdSockets.get(IPPort).close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					// e1.printStackTrace();
				}
				System.out.println("Removed Socket " + IPPort);
				createdSockets.remove(IPPort);
			}
			String piAddress = slavePi.getInetAddress().toString()
					.replace("/", "");
			if (!restartedPi.equals(piAddress)) {
				restartDeadPi(piAddress);
				restartedPi = piAddress;
			}

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}

	}

	public void restartDeadPi(String host) {
		boolean notExecuted = true;

		while (notExecuted) {
			JSch jsch = new JSch();
			Session session = null;
			try {
				session = jsch.getSession("pi", host, 22);

				session.setPassword("raspberry");
				Properties config = new Properties();

				config.put("StrictHostKeyChecking", "no");
				session.setConfig(config);

				session.connect();

				ChannelExec channel = null;

				channel = (ChannelExec) session.openChannel("exec");

				BufferedReader in = null;

				in = new BufferedReader(new InputStreamReader(
						channel.getInputStream()));

				// ping to check if machine is on
				channel.setCommand("cd slave5;javac *.java;java Slave");

				channel.connect();

				String msg = null;

				while ((msg = in.readLine()) != null) {
					System.out.println(msg);
				}

				channel.disconnect();
				session.disconnect();
				notExecuted = false;

			} catch (IOException e) {
				System.out.println("IO Exception in JSCH");
				// TODO Auto-generated catch block
				e.printStackTrace();

			} catch (JSchException e) {

				System.out.println("JSCH Exception");
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
}
