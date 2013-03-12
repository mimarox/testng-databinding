package net.sf.testng.databinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a test method parameter to be used for test input.
 * <p>
 * Which types of data are permissible for test input parameters depends on the data source used.
 * 
 * @author Matthias Rothe
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface TestInput {

	/**
	 * The name of the parameter. Since the actual parameter names are lost at runtime they must be
	 * made known to the TestNG Data Binding framework this way.
	 * <p>
	 * However it's only necessary to provide it for primitive types (e.g. {@link String}, int,
	 * etc.) and lists of primitives. For beans and enums their camelCased simple names are used
	 * instead.
	 * 
	 * @return the parameter's name
	 */
	public String name() default "";
}