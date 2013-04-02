package net.sf.testng.databinding.core.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * This class contains some static methods for working with annotations used within the TestNG
 * DataBinding framework.
 * <p>
 * <b>Note:</b> The methods contained within this class are not part of the public API and should
 * only be used internally within the TestNG DataBinding framework.
 * 
 * @author Matthias Rothe
 */
public class Annotations {
	private Annotations() {
	}

	/**
	 * Determines whether the given annotation object has a <code>name</code> attribute the value of
	 * which is the empty string or <code>null</code>.
	 * 
	 * @param annotation
	 *            The annotation object to check
	 * @return <code>true</code>, if and only if the annotation object has a <code>name</code>
	 *         attribute the value of which is the empty string or <code>null</code>,
	 *         <code>false</code> if and only if the annotation object has a <code>name</code>
	 *         attribute the value of which is a non-empty string
	 * @throws IllegalArgumentException
	 *             if the given annotation object doesn't have a <code>name</code> attribute of type
	 *             {@link String}.
	 */
	public static boolean nameNotSet(final Annotation annotation) {
		try {
			final Method method = annotation.getClass().getMethod("name");
			final String name = (String) method.invoke(annotation);
			return name == null || name.trim().length() == 0;
		} catch (final Exception e) {
			throw new IllegalArgumentException("The given annotation doesn't have a name method returning a String value", e);
		}
	}

	/**
	 * Retrieves the value of the name attribute of the given annotation object.
	 * 
	 * @param annotation
	 *            The annotation object to retrieve the value of the name attribute from
	 * @return The value of the name attribute of the given annotation object or <code>null</code>
	 *         in case the given annotation object doesn't have a name attribute or the name
	 *         attribute is not of type {@link String}.
	 */
	public static String getName(final Annotation annotation) {
		try {
			final Method method = annotation.getClass().getMethod("name");
			return (String) method.invoke(annotation);
		} catch (final Exception e) {
			return null;
		}
	}
}
