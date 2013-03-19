package net.sf.testng.databinding.properties;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Properties;

import net.sf.testng.databinding.GenericDataProvider;
import net.sf.testng.databinding.TestInput;
import net.sf.testng.databinding.TestOutput;
import net.sf.testng.databinding.properties.PropertiesDataSource;
import net.sf.testng.databinding.properties.beans.TestBean;
import net.sf.testng.databinding.properties.beans.TestEnum;
import net.sf.testng.databinding.util.MethodParameter;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class PropertiesDataSourceTest {
	private Method methodParametersCreator;

	@BeforeClass
	public void initMethodParametersCreator() throws SecurityException, NoSuchMethodException {
		methodParametersCreator = GenericDataProvider.class.getDeclaredMethod("createMethodParameters", Method.class);
		methodParametersCreator.setAccessible(true);
	}

	@Test
	public void testPrimitiveValues() throws Exception {
		final List<MethodParameter> parameters = createMethodParameters("primitivesConsumer");

		final Properties properties = new Properties();
		properties.setProperty("url", "/primitive.properties");

		final PropertiesDataSource provider = new PropertiesDataSource(parameters, properties);

		assertTrue(provider.hasNext());
		assertEquals(provider.next(), new Object[] { "Hello World!", 0.6 });
		assertFalse(provider.hasNext());
	}

	@Test
	public void testPrimitivesAndEnum() throws Exception {
		final List<MethodParameter> parameters = createMethodParameters("primitivesAndEnumConsumer");

		final Properties properties = new Properties();
		properties.setProperty("url", "/primitivesAndEnum.properties");

		final PropertiesDataSource provider = new PropertiesDataSource(parameters, properties);

		assertTrue(provider.hasNext());
		assertEquals(provider.next(), new Object[] { 5, TestEnum.one, true });
		assertFalse(provider.hasNext());
	}

	@Test
	public void testBeanAllValuesSet() throws Exception {
		final List<MethodParameter> parameters = createMethodParameters("beanConsumer");

		final Properties properties = new Properties();
		properties.setProperty("url", "/beanAllValuesSet.properties");

		final PropertiesDataSource provider = new PropertiesDataSource(parameters, properties);

		assertTrue(provider.hasNext());
		assertEquals(provider.next(), new Object[] { new TestBean("Hello World!", 10, 5.3f, TestEnum.one) });
		assertFalse(provider.hasNext());
	}

	@Test
	public void testBeanSomeValuesSet() throws Exception {
		final List<MethodParameter> parameters = createMethodParameters("beanConsumer");

		final Properties properties = new Properties();
		properties.setProperty("url", "/beanSomeValuesSet.properties");

		final PropertiesDataSource provider = new PropertiesDataSource(parameters, properties);

		assertTrue(provider.hasNext());
		assertEquals(provider.next(), new Object[] { new TestBean("Hello World!", 0, 5.3f, null) });
		assertFalse(provider.hasNext());
	}

	@SuppressWarnings("unchecked")
	private List<MethodParameter> createMethodParameters(final String methodName) throws Exception {
		for (final Method method : getClass().getMethods()) {
			if (method.getName().equals(methodName)) {
				return (List<MethodParameter>) methodParametersCreator.invoke(null, method);
			}
		}

		throw new NoSuchMethodException(methodName);
	}

	public void primitivesConsumer(@TestInput(name = "string") final String inString,
			@TestOutput(name = "double") final double outDouble) {
	}

	public void primitivesAndEnumConsumer(@TestInput(name = "integer") final int inInt, @TestInput final TestEnum testEnum,
			@TestOutput(name = "boolean") final boolean outBoolean) {
	}

	public void beanConsumer(@TestInput final TestBean testBean) {
	}
}
