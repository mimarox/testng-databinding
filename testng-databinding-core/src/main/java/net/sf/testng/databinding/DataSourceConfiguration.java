package net.sf.testng.databinding;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Configuration of data sources works via configuration objects. Any class whose objects should
 * be used as configuration objects must be annotated with this annotation with the
 * @link {@link #name() name} matching the configuration name set with
 * &#64;{@link DataBinding#configName() configuration name} and implement a data source
 * specific interface for its configuration objects.
 * <p>
 * See the individual data sources for their interface for configuration objects.
 * 
 * @author Matthias Rothe
 * @see DataBinding
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface DataSourceConfiguration {
	
	/**
	 * Specifies the name of this configuration.
	 * 
	 * @return the name
	 */
	String name();
}
