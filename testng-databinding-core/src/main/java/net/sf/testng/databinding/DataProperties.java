package net.sf.testng.databinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import net.sf.testng.databinding.properties.DoNothingPropertiesPrefixPreprocessor;
import net.sf.testng.databinding.properties.PropertiesPrefixPreprocessor;

import org.testng.annotations.Test;

/**
 * Annotation to use for describing where a data properties file can be found for the annotated test
 * method.
 * <p>
 * Using this annotation only makes sense on {@link Test @Test} annotated methods also annotated
 * with {@link UseGenericDataProvider @UseGenericDataProvider}.
 * 
 * @author Matthias Rothe
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataProperties {

	/**
	 * The class this test method belongs to or should be deemed to belong to.
	 * <p>
	 * Data properties file resolution depends upon that class's full name. For example if the
	 * declaring class is <code>foo.bar.SampleTest</code> then the data properties file has to be
	 * accessible under <code>/foo/bar/SampleTest.data.properties</code>.
	 * <p>
	 * If not specified the declaring class will be retrieved by calling
	 * {@link Method#getDeclaringClass()} on the test method.
	 */
	public Class<?> declaringClass() default Object.class;

	/**
	 * The prefix of the data properties for this test method.
	 * <p>
	 * To distinguish between the properties belonging to the test methods of one
	 * {@link #declaringClass() declaring class} those properties need a common prefix per test
	 * method. It is expected that this prefix is followed by a dot (.). This dot must be omitted in
	 * the prefix.
	 * <p>
	 * If not specified the prefix will default to the method's name.
	 */
	public String propertiesPrefix() default "";
	
	/**
	 * The class of the object to be used to dynamically change the properties prefix, before
	 * this prefix is used.
	 * <p>
	 * The given class must have a no-args constructor.
	 * <p>
	 * If not specified, the properties prefix will be used without preprocessing.
	 */
	public Class<? extends PropertiesPrefixPreprocessor> prefixPreprocessor() default DoNothingPropertiesPrefixPreprocessor.class; 
}
