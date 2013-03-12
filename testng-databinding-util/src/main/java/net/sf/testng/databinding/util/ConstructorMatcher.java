package net.sf.testng.databinding.util;

import java.lang.reflect.Type;

/**
 * This interface provides a method to determine whether the given parameter types of a constructor
 * match the expected parameter types.
 * 
 * @author Matthias Rothe
 */
public interface ConstructorMatcher {

	/**
	 * Determines whether the given parameter types of a constructor match the expected parameter
	 * types.
	 * 
	 * @param parameterTypes
	 *            The parameter types to match
	 * @return <code>true</code>, if and only if the given parameter types match the parameter types
	 *         expected, <code>false</code> otherwise
	 */
	public boolean matches(Type[] parameterTypes);
}