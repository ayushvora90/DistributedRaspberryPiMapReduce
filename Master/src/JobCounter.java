/*
 * JobsCounter.java
 * 
 * Version:
 * 		1.0
 * Revision
 * 		1.0
 */

/**
 * This is the Job Counter class. It contains counters for created and completed
 * jobs.
 * 
 * 
 * @author Ayush Vora
 * @author Harshal Khandare
 * @author Pranav Dadlani
 * 
 */

public class JobCounter {
	int completedJobs;
	int createdJobs;
	boolean fileReadIndicator = false;

	/**
	 * This constructor references the counters.
	 * 
	 * @param completedJobs
	 *            counter to denote the completed jobs
	 * @param createdJobs
	 *            counter to denote the created jobs
	 */

	JobCounter(int completedJobs, int createdJobs) {
		this.completedJobs = completedJobs;
		this.createdJobs = createdJobs;
	}

}
