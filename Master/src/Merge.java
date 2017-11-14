/*
 * Merge.java
 * 
 * Version:
 * 		1.0
 * Revision
 * 		1.0
 */

import java.util.*;
import java.io.*;

/**
 * This is the Merge class. It will merge individual solution chunks files and
 * form a single output file.
 * 
 * 
 * @author Ayush Vora
 * @author Harshal Khandare
 * @author Pranav Dadlani
 * 
 */

public class Merge {
	HashMap<Integer, Integer> countsMap;

	/**
	 * This constructor references important objects to be used by this class.
	 * 
	 * @param countsMap
	 *            hashmap of unique value with their counts of the original file
	 */
	Merge(HashMap<Integer, Integer> countsMap) {
		this.countsMap = countsMap;
	}

	public void mergeCompletedJobs(int numOfCompJobs)
			throws NumberFormatException, IOException {
		System.out.println("WRITING TO FILE");

		String filename[] = new String[numOfCompJobs];

		for (int i = 0; i < numOfCompJobs; i++) {
			filename[i] = i + ".txt";
		}

		merge(filename);
	}

	public void merge(String filename[]) throws NumberFormatException,
			IOException {

		File file[] = new File[filename.length];
		BufferedWriter out = new BufferedWriter(new FileWriter("Sol.txt"));
		ArrayList<BufferedReader> br = new ArrayList<BufferedReader>();
		int pointer[] = new int[filename.length];

		for (int i = 0; i < filename.length; i++) {
			br.add(new BufferedReader(new FileReader(filename[i])));
		}
		for (int i = 0; i < br.size(); i++) {

			pointer[i] = Integer.parseInt(br.get(i).readLine());
			//System.out.println(pointer[i]);
		}

		int chunks = pointer.length;
		boolean first = true;

		// write the final solution file using individual chunk solution files.
		while (chunks > 0) {
			int index = -1;
			int min = Integer.MAX_VALUE;
			for (int i = 0; i < pointer.length; i++) {
				int no = pointer[i];

				if (no >= 0) {
					if (no < min) {
						min = no;
						index = i;
					}
				}

			}
//			for (int k = 0; k < countsMap.get(min); k++) {
				out.write(new Integer(min).toString());
				out.write("\n");

//			}
			String line;
			if ((line = br.get(index).readLine()) == null) {
				pointer[index] = -1;
				chunks--;
			} else {
				pointer[index] = Integer.parseInt(line);
			}
		}

		out.close();
		System.out.println("WRITTEN TO FILE");

	}

}
