package net.sf.testng.databinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for TestNG <a href="http://testng.org/javadocs/org/testng/annotations/Test.html" target="_blank">&#64;Test</a>
 * annotated methods, causing the {@link GenericDataProvider} to be used for data provision.
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
 * test methods in the class so annotated. Only the {@link #dataSource()} and the
 * {@link #configClass()} can be configured on the class level. If parameter values other than the
 * default for the {@link #configMethod()} and the values set on the class level are needed for any
 * test method in a test class annotated with this annotation that test method needs to also be
 * annotated with this annotation specifying the parameters as needed. Values set on the method level
 * always override those set on the class level.
 * <p>
 * Join the <a href="http://facebook.com/TestNGDataBinding" target="_blank">TestNG Data Binding
 * community on Facebook</a> to always stay up to date and discuss issues with other users!
 * 
 * @author Matthias Rothe
 * @see GenericDataProvider
 * @see TestInput
 * @see TestOutput
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface DataBinding {
	/**
	 * Sets the data source.
	 * <p>
	 * May be the name of any data source available on the class path. See the individual
	 * data sources for the name their identified by.
	 * <p>
	 * This parameter may be set on the class level or the method level. If set on both, the
	 * value set on the method level takes precedence.
	 * 
	 * @return the data source
	 */
	String dataSource() default "";
	
	/**
	 * Sets the class containing the <code>public static</code> methods returning the configuration
	 * objects for the chosen {@link #dataSource()}.
	 * <p>
	 * This parameter may be set on the class level or the method level. If set on both, the
	 * value set on the method level takes precedence.
	 * 
	 * @return the configuration class
	 */
	Class<?> configClass() default Object.class;
	
	/**
	 * Sets the name of the method returning the configuration object for the chosen
	 * {@link #dataSource()}.
	 * <p>
	 * MUST be the name of any <code>public static</code> no-args method implemented within the
	 * set {@link #configClass()}. This method MUST have the return type set to the configuration
	 * object type of the chosen {@link #dataSource()}. See the individual data source implementations
	 * to find out which configuration object type they provide and expect to get returned.
	 * <p>
	 * This parameter may only be set on the method level. If set on the class level, it is silently
	 * ignored.
	 * <p>
	 * Defaults to &lt;name of the annotated test method&gt;Config. So if the annotated test method
	 * is called 'foo', the default configMethod will be 'public static &lt;return type&gt;
	 * <strong>fooConfig</strong>()'. 
	 * 
	 * @return the configuration method's name
	 */
	String configMethod() default "";
}