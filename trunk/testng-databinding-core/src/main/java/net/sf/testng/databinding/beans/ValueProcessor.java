package net.sf.testng.databinding.beans;

/**
 * Interface for classes providing value processing / transformation.
 * 
 * @author Matthias Rothe
 */
public interface ValueProcessor {

	/**
	 * Takes an object, processes / transforms it in some way and returns the result.
	 * 
	 * @param input
	 *            The input to be processed
	 * @return The processed value
	 */
	Object process(Object input);
}
