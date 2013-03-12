package net.sf.testng.databinding;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines the names of the strategies a class implementing the {@link DataProviderStrategy}
 * interface supports. One of these names can then be used in a .data.properties file to define the
 * data source for a test method, e.g. <code>{prefix}.strategy=CSV</code>.
 * <p>
 * If this annotation is not present on a DataProviderStrategy implementation the name defaults to
 * the {@link Class#getSimpleName() simple name} of that class.
 * 
 * @author Matthias Rothe
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataProviderStrategyNames {

	/**
	 * Defines the names of the strategies a class implementing the {@link DataProviderStrategy}
	 * interface supports.
	 * 
	 * @return The strategy names
	 */
	String[] value();
}
