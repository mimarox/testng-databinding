package net.sf.testng.databinding.xml;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import net.sf.testng.databinding.GenericDataProvider;
import net.sf.testng.databinding.TestInput;
import net.sf.testng.databinding.TestOutput;
import net.sf.testng.databinding.util.MethodParameter;
import net.sf.testng.databinding.xml.XMLDataProviderStrategy;
import net.sf.testng.databinding.xml.beans.BeanWithMap;
import net.sf.testng.databinding.xml.beans.IESTestBean;
import net.sf.testng.databinding.xml.beans.InnerTestBean;
import net.sf.testng.databinding.xml.beans.TestBean;
import net.sf.testng.databinding.xml.beans.TestEnum;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class XMLDataProviderStrategyTest {
	private Method methodParametersCreator;

	@BeforeClass
	public void initMethodParametersCreator() throws SecurityException, NoSuchMethodException {
		methodParametersCreator = GenericDataProvider.class.getDeclaredMethod("createMethodParameters", Method.class);
		methodParametersCreator.setAccessible(true);
	}

	@Test(groups = { "singleRow", "singleInputValue", "primitiveInputValue" }, timeOut = 1000)
	public void testSingleRowSingleStringInputValueTestData() throws Exception {
		final List<MethodParameter> parameters = createMethodParameters("singleStringInputValueConsumer");

		final Properties properties = new Properties();
		properties.setProperty("url", "/singleRowSingleStringInputValueTestData.xml");

		final XMLDataProviderStrategy provider = new XMLDataProviderStrategy(parameters, properties);

		assertTrue(provider.hasNext());
		assertEquals(provider.next(), new Object[] { "Hello World!" });
		assertFalse(provider.hasNext());
	}

	@Test(groups = { "singleRow", "singleInputValue", "enumInputValue" }, timeOut = 1000)
	public void testSingleRowSingleEnumInputValueTestData() throws Exception {
		final List<MethodParameter> parameters = createMethodParameters("singleEnumInputValueConsumer");

		final Properties properties = new Properties();
		properties.setProperty("url", "/singleRowSingleEnumInputValueTestData.xml");

		final XMLDataProviderStrategy provider = new XMLDataProviderStrategy(parameters, properties);

		assertTrue(provider.hasNext());
		assertEquals(provider.next(), new Object[] { TestEnum.one });
		assertFalse(provider.hasNext());
	}

	@Test(groups = { "multiRow", "singleInputValue", "primitiveInputValue" }, dependsOnGroups = "singleRow", timeOut = 1000)
	public void testMultiRowSingleStringInputValueTestData() throws Exception {
		final List<MethodParameter> parameters = createMethodParameters("singleStringInputValueConsumer");

		final Properties properties = new Properties();
		properties.setProperty("url", "/multiRowSingleStringInputValueTestData.xml");

		final XMLDataProviderStrategy provider = new XMLDataProviderStrategy(parameters, properties);

		assertTrue(provider.hasNext());

		while (provider.hasNext()) {
			assertEquals(provider.next(), new Object[] { "Hello World!" });
		}

		assertFalse(provider.hasNext());
	}

	@Test(groups = { "multiRow", "singleInputValue", "singleBeanInputValue" }, dependsOnGroups = { "singleRow",
			"primitiveInputValue" }, timeOut = 1000)
	public void testMultiRowComplexBeanInputValueTestData() throws Exception {
		final List<TestBean> expecteds = Arrays.asList(setupComplexTestBean(), new TestBean());

		final List<MethodParameter> parameters = createMethodParameters("complexBeanInputValueConsumer");

		final Properties properties = new Properties();
		properties.setProperty("url", "/multiRowComplexBeanInputValueTestData.xml");

		final XMLDataProviderStrategy provider = new XMLDataProviderStrategy(parameters, properties);

		assertTrue(provider.hasNext());

		for (int i = 0; provider.hasNext(); i++) {
			assertEquals(provider.next(), new Object[] { expecteds.get(i) });
		}

		assertFalse(provider.hasNext());
	}

	@Test(groups = { "multiRow", "multiInputValue" }, dependsOnGroups = { "singleRow", "singleInputValue" }, timeOut = 1000)
	public void testMultipleInputValuesTestData() throws Exception {
		final List<TestBean> expecteds = Arrays.asList(setupComplexTestBean(), new TestBean());

		final List<MethodParameter> parameters = createMethodParameters("multipleInputValuesConsumer");

		final Properties properties = new Properties();
		properties.setProperty("url", "/multipleInputValuesTestData.xml");

		final XMLDataProviderStrategy provider = new XMLDataProviderStrategy(parameters, properties);

		assertTrue(provider.hasNext());

		for (int i = 0; provider.hasNext(); i++) {
			assertEquals(provider.next(), new Object[] { "Hello World!", expecteds.get(i) });
		}

		assertFalse(provider.hasNext());
	}

	@Test(groups = { "multiRow", "inputOutputValue" }, dependsOnGroups = { "singleRow", "multiInputValue" }, timeOut = 1000)
	public void testInputOutputValuesTestData() throws Exception {
		final Object[][] expecteds = new Object[][] {
				{ true, new TestBean(), TestEnum.one, Arrays.asList("a", "b", "c"), Arrays.asList("1", "2", "3") },
				{ false, setupComplexTestBean(), TestEnum.three, Arrays.asList("9", "8", "7"), Arrays.asList("z", "y", "x") } };

		final List<MethodParameter> parameters = createMethodParameters("inputOutputValuesConsumer");

		final Properties properties = new Properties();
		properties.setProperty("url", "/inputOutputValuesTestData.xml");

		final XMLDataProviderStrategy provider = new XMLDataProviderStrategy(parameters, properties);

		assertTrue(provider.hasNext());

		for (int i = 0; provider.hasNext(); i++) {
			assertEquals(provider.next(), expecteds[i]);
		}

		assertFalse(provider.hasNext());
	}

	@Test(timeOut = 1000)
	public void testBeanWithMapTestData() throws Exception {
		final List<MethodParameter> parameters = createMethodParameters("beanWithMapConsumer");

		final Properties properties = new Properties();
		properties.setProperty("url", "/beanWithMapTestData.xml");

		final XMLDataProviderStrategy provider = new XMLDataProviderStrategy(parameters, properties);

		assertTrue(provider.hasNext());
		assertEquals(provider.next(), new Object[] { new BeanWithMap() });
		assertFalse(provider.hasNext());
	}

	@Test(/* timeOut = 1000 */)
	public void testListWithIESTestData() throws Exception {
		final List<String> entries = Arrays.asList("1", "2", "3");
		final IESTestBean bean = new IESTestBean();
		bean.setEntries(entries);

		final List<MethodParameter> parameters = createMethodParameters("listWithIESConsumer");

		final Properties properties = new Properties();
		properties.setProperty("url", "/listWithIESTestData.xml");

		final XMLDataProviderStrategy provider = new XMLDataProviderStrategy(parameters, properties);

		assertTrue(provider.hasNext());
		assertEquals(provider.next(), new Object[] { entries, bean });
		assertFalse(provider.hasNext());
	}

	private TestBean setupComplexTestBean() {
		final TestBean testBean = new TestBean();
		testBean.setTestEnum(TestEnum.one);
		testBean.setTestString("Hello World!");
		testBean.setTestDouble(5.5);
		testBean.setTestBooleans(Arrays.asList(true, false, true));
		testBean.setInnerTestBean(new InnerTestBean("test value"));
		testBean.setTestBeans(Arrays.asList(new InnerTestBean("list entry 1"), new InnerTestBean("list entry 2"),
				new InnerTestBean("list entry 3")));
		return testBean;
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

	public void singleStringInputValueConsumer(@TestInput(name = "testValue") final String testValue) {
	}

	public void singleEnumInputValueConsumer(@TestInput final TestEnum testEnum) {
	}

	public void complexBeanInputValueConsumer(@TestInput final TestBean testBean) {
	}

	public void multipleInputValuesConsumer(@TestInput(name = "testValue") final String testValue,
			@TestInput final TestBean testBean) {
	}

	public void inputOutputValuesConsumer(@TestInput(name = "testChoice") final boolean testChoice,
			@TestOutput final TestBean testBean, @TestInput final TestEnum testEnum,
			@TestOutput(name = "strings") final List<String> outStrings, @TestInput(name = "strings") final List<String> inStrings) {
	}

	public void beanWithMapConsumer(@TestInput final BeanWithMap testBean) {
	}

	public void listWithIESConsumer(@TestInput(name = "entries") final List<String> entries, @TestInput final IESTestBean bean) {
	}
}
