package net.sf.testng.databinding.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

public class Constructors {
	private Constructors() {
	}

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

	public static boolean hasMatchingConstructor(Class<?> clazz, ConstructorMatcher matcher) {
		for (Constructor<?> constructor : clazz.getConstructors()) {
			if (matcher.matches(constructor.getGenericParameterTypes())) {
				return true;
			}
		}

		return false;
	}
}
