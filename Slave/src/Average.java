/* Average.java
 * 
 *  Version:1.0
 *  
 *  Revision:1.0
 */
import java.io.Serializable;

/**
 * Class to develop an average object.
 * @author Ayush Vora
 * @author Harshal Khandare
 * @author Pranav Dadlani
 *
 */
public class Average implements Serializable{
	/**
	 * Parameterized constructor for average
	 * @param numberinChunk 		Size of the chunk
	 * @param averageValue			Average of the chunk
	 */
	Average(int numberinChunk,double averageValue)
	{
		this.numberInChunk=numberinChunk;
		this.averageValue=averageValue;
		
	}
	private int numberInChunk;
	private double averageValue;
	
	/**
	 * Get size of the chunk
	 * @return Size as an int
	 */
	public int getNumberInChunk() {
		return numberInChunk;
	}
	/**
	 * Set size of the chunk
	 * @param numberInChunk		Set a size of chunk
	 */
	public void setNumberInChunk(int numberInChunk) {
		this.numberInChunk = numberInChunk;
	}
	/**
	 * Get the average value of this chunk
	 * @return	Returns the average value of the chunk as a double
	 */
	public double getAverageValue() {
		return averageValue;
	}
	/**
	 * Set the average value of this chunk
	 * @param averageValue	Set average value as a double
	 */
	public void setAverageValue(double averageValue) {
		this.averageValue = averageValue;
	}
	
	
}
