package net.sf.testng.databinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import net.sf.testng.databinding.properties.DoNothingPropertiesPrefixPreprocessor;
import net.sf.testng.databinding.properties.PropertiesPrefixPreprocessor;

/**
 * Annotation for {@link Test @Test} annotated methods, causing the
 * {@link GenericDataProvider} to be used for data provision.
 * <p>
 * Requires that the {@link TestAnnotationTransformer} be specified as a listener in the testng.xml
 * file like so:
 * 
 * <pre>
 * &lt;suite&gt;
 *     &lt;listeners&gt;
 *         &lt;listener class-name="net.sf.testng.databinding.TestAnnotationTransformer"/&gt;
 *     &lt;/listeners&gt;
 *     ...
 * &lt;/suite&gt;
 * </pre>
 * <p>
 * This annotation can also be used at class level. If used at class level it applies to all
 * test methods in the class so annotated. Any settings of this annotation's parameters will be
 * ignored if used at class level and defaults will be used. If parameter values other than the
 * defaults are needed for any test method in a test class annotated with this annotation that
 * test method needs to also be annotated with this annotation specifying the parameters as needed.
 * 
 * @author Matthias Rothe
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface DataBinding {
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