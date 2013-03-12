package net.sf.testng.databinding.util;

import net.sf.testng.databinding.beans.Preprocessors;
import net.sf.testng.databinding.beans.ValueProcessor;

/**
 * This class contains some static methods for applying {@link Preprocessors} and
 * {@link ValueProcessor} classes to specific value instances.
 * 
 * @author Matthias Rothe
 */
public class PreprocessorsUtil {
	private PreprocessorsUtil() {
	}

	/**
	 * Applies the {@link ValueProcessor} classes set as the value of the given
	 * {@link Preprocessors} annotation instance to the given value and returns the resulting
	 * processed value.
	 * 
	 * @param value
	 *            The value to process
	 * @param preprocessors
	 *            The annotation instance defining the value processors to be applied
	 * @return The processed value
	 */
	public static Object process(final Object value, final Preprocessors preprocessors) {
		Object newValue;

		if (preprocessors != null) {
			newValue = process(value, preprocessors.value());
		} else {
			newValue = value;
		}

		return newValue;
	}

	/**
	 * Applies the given array of {@link ValueProcessor} classes to the given value and returns the
	 * resulting processed value.
	 * 
	 * @param value
	 *            The value to process
	 * @param classes
	 *            The classes of value processors to be applied
	 * @return The processed value
	 */
	public static Object process(final Object value, final Class<? extends ValueProcessor>[] classes) {
		Object newValue = value;

		try {
			for (final Class<? extends ValueProcessor> clazz : classes) {
				final ValueProcessor processor = clazz.newInstance();
				newValue = processor.process(newValue);
			}
		} catch (final Exception e) {
			System.err.println("Processing value " + value + " failed:");
			e.printStackTrace();
			System.err.println("The whole processing queue will be skipped.");
		}

		return newValue;
	}
}
