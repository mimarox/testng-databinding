package net.sf.testng.databinding;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.testng.IAnnotationTransformer;
import org.testng.annotations.ITestAnnotation;

/**
 * {@link IAnnotationTransformer} implementation enabling the behavior of the
 * {@link UseGenericDataProvider} annotation.
 * <p>
 * You need to add this class as a listener to your testng.xml file like so:
 * 
 * <pre>
 * &lt;suite&gt;
 *     &lt;listeners&gt;
 *         &lt;listener class-name="net.sf.testng.databinding.TestAnnotationTransformer"/&gt;
 *     &lt;/listeners&gt;
 *     ...
 * &lt;/suite&gt;
 * </pre>
 * 
 * to make it known to and used by TestNG.
 * 
 * @author Matthias Rothe
 */
public class TestAnnotationTransformer implements IAnnotationTransformer {

	@Override
	@SuppressWarnings("rawtypes")
	public void transform(final ITestAnnotation test, final Class testClass, final Constructor testConstructor,
			final Method testMethod) {
		if (testMethod != null && testMethod.getAnnotation(UseGenericDataProvider.class) != null) {
			test.setDataProviderClass(GenericDataProvider.class);
			test.setDataProvider("DataProvider");
		}
	}
}