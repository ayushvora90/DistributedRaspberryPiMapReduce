/*
 * JobsGenerator.java
 * 
 * Version:
 * 		1.0
 * Revision
 * 		1.0
 */

import java.util.*;
import java.io.*;

/**
 * This is the jobs generator thread class. It will create jobs and add the jobs
 * IDs to the job queue. The created jobs have unique elements.
 * 
 * @author Ayush Vora
 * @author Harshal Khandare
 * @author Pranav Dadlani
 * 
 */

public class JobsGenerator extends Thread {
	Queue<Integer> jobQueue;
//	HashMap<Integer, Integer> countsMap;
	Boolean fileReadIndicator;
	JobCounter masterJobCounter;
	String dataset;

	// specify the chunk size
	int chunkSize = 25000;

	/**
	 * This constructor references important objects to be used by this class.
	 * @param dataset 
	 * 
	 * @param countsMap
	 *            hashmap of unique value with their counts of the original file
	 * @param jobQueue
	 *            This queue contains all pending jobs to be processed
	 * @param fileReadIndicator
	 *            to indicate file has been read
	 * @param masterJobCounter
	 *            the count the created jobs.
	 */

	JobsGenerator(String dataset, HashMap<Integer, Integer> countsMap, Queue<Integer> jobQueue,
			Boolean fileReadIndicator, JobCounter masterJobCounter) {
		this.dataset = dataset;
//		this.countsMap = countsMap;
		this.jobQueue = jobQueue;
		this.fileReadIndicator = fileReadIndicator;
		this.masterJobCounter = masterJobCounter;
	}

	public void run() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(dataset));
			ArrayList<Integer> uniqueValuesChunk = new ArrayList<Integer>();
			String line;
			int counter = 0;
			int startIndex = 0;

			// read file
			while ((line = br.readLine()) != null) {
				int value = Integer.parseInt(line);
//				if (countsMap.get(value) != null) {
//					countsMap.put(value, countsMap.get(value) + 1);
//
//				} else {
//					countsMap.put(value, 1);
					uniqueValuesChunk.add(value);
					counter++;

					// create chunk of specified chunk size
					if (counter % chunkSize == 0) {
						startIndex = counter - chunkSize;

						// write chunk .uvc file
						ObjectOutputStream oos = new ObjectOutputStream(
								new BufferedOutputStream(new FileOutputStream(
										startIndex + ".uvc")));
						oos.writeObject(uniqueValuesChunk);
						oos.close();
						uniqueValuesChunk.clear();
						uniqueValuesChunk = new ArrayList<Integer>();

						// add job index to the job queue
						jobQueue.add(startIndex);
						masterJobCounter.createdJobs++;
						System.out.println("Job "
								+ masterJobCounter.createdJobs
								+ " created at sIndex " + startIndex);
						startIndex = counter;
					}
				}
//			}

			// if file is smaller than chunk size
			if (counter < chunkSize) {
				ObjectOutputStream oos = new ObjectOutputStream(
						new BufferedOutputStream(new FileOutputStream("0.uvc")));
				oos.writeObject(uniqueValuesChunk);
				oos.close();
				jobQueue.add(0);

				// if file contains any extra lines write it to the last chunk
			} else if (!(counter % chunkSize == 0)) {
				ObjectOutputStream oos = new ObjectOutputStream(
						new BufferedOutputStream(new FileOutputStream(
								startIndex + ".uvc")));
				oos.writeObject(uniqueValuesChunk);
				oos.close();
				jobQueue.add(startIndex);
				masterJobCounter.createdJobs++;
				System.out.println("Job " + masterJobCounter.createdJobs
						+ " created at sIndex " + startIndex);
			}
			masterJobCounter.fileReadIndicator = true;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
