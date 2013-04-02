package net.sf.testng.databinding.core.properties;

import net.sf.testng.databinding.DataBinding;

/**
 * A no-operation implementation of the {@link PropertiesPrefixPreprocessor} interface.
 * <p>
 * This class is used as the default value of {@link DataBinding#prefixPreprocessor()} signifying that the prefix
 * should not be modified before being used.
 * 
 * @author Matthias Rothe
 */
public class DoNothingPropertiesPrefixPreprocessor implements PropertiesPrefixPreprocessor {

	/**
	 * Returns the given prefix without touching it, effectively doing nothing.
	 * 
	 * @return The prefix as given
	 */
	@Override
	public String process(String prefix) {
		return prefix;
	}
}