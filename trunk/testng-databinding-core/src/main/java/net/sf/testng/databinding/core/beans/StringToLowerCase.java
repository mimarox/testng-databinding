package net.sf.testng.databinding.core.beans;

/**
 * A {@link String#toLowerCase()} implementation of a {@link ValueProcessor}.
 * 
 * @author Matthias Rothe
 */
public class StringToLowerCase implements ValueProcessor {

	/**
	 * Takes an object and returns its {@link String#toLowerCase()} representation.
	 * 
	 * @param input
	 *            The input to be processed
	 * @return The processed value
	 */
	@Override
	public String process(final Object input) {
		return input.toString().toLowerCase();
	}
}