/*
 * mergeAverage.java
 * 
 * Version:
 * 		1.0
 * Revision
 * 		1.0
 */

import java.util.List;
/**
 * Class to compute final average
 * @author Ayush Vora
 * @author Harshal Khandare
 * @author Pranav Dadlani
 *
 */
public class mergeAverage {
	/**
	 * Gets average of all the averages of all the lists.
	 * @param completedJobList
	 * @return
	 */
	public static double getAvg(List<Average> completedJobList) {
		int denom = getTotalNum(completedJobList);
		System.out.println(denom);
		double avg = 0;
		for (int index = 0; index < completedJobList.size(); index++) {
			System.out.println((double) completedJobList.get(index)
					.getAverageValue());
			System.out.println(completedJobList.get(index).getNumberInChunk());
			System.out.println(avg);
			avg = avg
					+ ((double) completedJobList.get(index).getAverageValue()
							* completedJobList.get(index).getNumberInChunk() / denom);
		}

		return avg;
	}
	/**
	 * Gets the total number of integers
	 * @param completedJobList
	 * @return
	 */
	private static int getTotalNum(List<Average> completedJobList) {
		int sum = 0;
		for (int index = 0; index < completedJobList.size(); index++) {
			sum = sum + completedJobList.get(index).getNumberInChunk();
		}

		return sum;
	}

}
