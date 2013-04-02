package net.sf.testng.databinding.core.properties;

import net.sf.testng.databinding.DataBinding;

/**
 * This interface defines a method for preprocessing a property prefix before using it.
 * <p>
 * A class <code>MyPropertiesPrefixPreprocessor</code> implementing this interface can be
 * defined as the preprocessor to use with {@link DataBinding#prefixPreprocessor()
 * &#64;DataBinding(prefixPreprocessor = MyPropertiesPrefixPreprocessor.class)}.
 * <p>
 * If the preprocessing involves substitution of some dynamic part of the prefix, the value
 * to be inserted must be made available to the preprocessor before its {@link #process(String)}
 * method is called. Since client code has actually never any access to the instance of the
 * preprocessor on which the TestNG Data Binding framework calls this method, the only option is
 * setting such a value statically, either via a static setter method or on a public static field
 * of the class.
 * 
 * @author Matthias Rothe
 */
public interface PropertiesPrefixPreprocessor {

	/**
	 * Processes the given prefix and returns the resulting prefix.
	 * 
	 * @param prefix The prefix to process
	 * @return The resulting prefix after processing is done
	 */
	public String process(String prefix);
}