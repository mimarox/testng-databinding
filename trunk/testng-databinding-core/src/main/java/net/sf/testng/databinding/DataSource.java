package net.sf.testng.databinding;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines the name of the data source a class implementing the {@link IDataSource}
 * interface supports. This name can then be used in a .data.properties file to define the
 * data source for a test method, e.g. <code>{prefix}.dataSource=csv</code>.
 * <p>
 * If this annotation is not present on a {@link IDataSource} implementation the name defaults to
 * the {@link Class#getSimpleName() simple name} of that class.
 * 
 * @author Matthias Rothe
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataSource {

	/**
	 * Defines the name of the data source a class implementing the {@link IDataSource}
	 * interface supports.
	 * 
	 * @return The data source name
	 */
	String name();
}
