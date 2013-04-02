package net.sf.testng.databinding.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

/**
 * This class contains utility methods pertaining to {@link Constructor constructors}.
 * 
 * @author Matthias Rothe
 */
public class Constructors {
	private Constructors() {
	}

	/**
	 * Retrieves and returns a {@link Constructor constructor} of the given {@link Class class} the parameter types of
	 * which match the ones defined in the given {@link ConstructorMatcher constructor matcher}. If no such constructor
	 * can be found a {@link NoSuchMethodException} is thrown.
	 * 
	 * @param clazz The class from which the constructor is to be returned
	 * @param matcher The constructor matcher defining the parameter types the constructor to be returned must have
	 * @return The constructor of the given class having matching parameter types
	 * @throws NoSuchMethodException if no constructor with matching parameter types can be found
	 */
	@SuppressWarnings("unchecked")
	public static <T> Constructor<T> getMatchingConstructor(Class<? extends T> clazz, ConstructorMatcher matcher)
			throws NoSuchMethodException {
		for (Constructor<?> constructor : clazz.getConstructors()) {
			Type[] parameterTypes = constructor.getGenericParameterTypes();
			if (matcher.matches(parameterTypes)) {
				return (Constructor<T>) constructor;
			}
		}

		throw new NoSuchMethodException("The expected constructor could not be found on the " + clazz);
	}

	/**
	 * Checks whether the given {@link Class class} has a {@link Constructor constructor} with parameter types matching
	 * the ones defined in the given {@link ConstructorMatcher constructor matcher}.
	 * 
	 * @param clazz The class to check for the constructor
	 * @param matcher The constructor matcher defining the parameter types the constructor must have
	 * @return <code>true</code>, if and only if the given class has a constructor with matching parameter types,
	 * <code>false</code> otherwise
	 */
	public static boolean hasMatchingConstructor(Class<?> clazz, ConstructorMatcher matcher) {
		for (Constructor<?> constructor : clazz.getConstructors()) {
			if (matcher.matches(constructor.getGenericParameterTypes())) {
				return true;
			}
		}

		return false;
	}
}