package net.sf.testng.databinding.core.beans;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines the {@link ValueProcessor ValueProcessors} to be applied before using the value of the field or the
 * return value of the method annotated with this annotation.
 * <p>
 * Using this annotation is only possible within beans used as test data containers.
 * <p>
 * It's the responsibility of the receiving test method or intermediate framework code to apply
 * the specified value processors.
 * 
 * @author Matthias Rothe
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Preprocessors {

	/**
	 * @return The value processors to be applied.
	 */
	Class<? extends ValueProcessor>[] value();
}
