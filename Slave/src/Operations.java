/* Operations.java
 * 
 * 	Version:1.0
 *  
 *  Revision:1.0
 */

import java.util.*;
/**
 * Class to perform the two tasks.
 * @author Ayush Vora
 * @author Harshal Khandare
 * @author Pranav Dadlani
 *
 */
public class Operations {
	/**
	 * Sorts an array of integers
	 * @param numberList
	 * @return
	 */
	public ArrayList<Integer> sort(ArrayList<Integer> numberList){
		int max = numberList.get(0);
		for (int index = 1; index < numberList.size(); index++) {
			if (numberList.get(index) > max)
				max = numberList.get(index);
		}
		for (int exp = 1; max / exp > 0; exp *= 10)

			countSort(max, numberList, exp);

		return numberList;
		
		
		
	}
	/**
	 * Performs count sort.
	 * @param max
	 * @param numberList
	 * @param exp
	 */
	public static void countSort(int max, List<Integer> numberList, int exp) {

		int index;
		int outputArray[] = new int[numberList.size()];
		int[] count = new int[10];

		for (index = 0; index < numberList.size(); index++)
			count[(numberList.get(index) / exp) % 10]++;

		for (index = 1; index < 10; index++)
			count[index] += count[index - 1];

		for (index = numberList.size() - 1; index >= 0; index--) {

			outputArray[count[(numberList.get(index) / exp) % 10] - 1] = numberList
					.get(index);

			count[(numberList.get(index) / exp) % 10]--;
		}
		for (index = 0; index < numberList.size(); index++)
			numberList.set(index, outputArray[index]);
	}
	/**
	 * Finds average of a list of integers.
	 * @param numberList
	 * @return
	 */
	public static Average average(ArrayList<Integer> numberList) {
		float avg = 0;
		int total = numberList.size();
		for (int index = 0; index < total; index++) {

			avg = avg + (float) numberList.get(index) / total;
		}
		Average avgObject = new Average(numberList.size(),avg);
		return avgObject ;
	}

}
