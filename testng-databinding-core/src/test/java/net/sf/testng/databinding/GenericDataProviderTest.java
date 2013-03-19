package net.sf.testng.databinding;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import net.sf.testng.databinding.error.MultipleConfigurationErrorsException;

import org.testng.annotations.Test;

public class GenericDataProviderTest {
	public static class TestBean {
		private String value;

		public String getValue() {
			return this.value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

	private void dataForMethod(String name, Class<?>[] parameterTypes) throws Exception {
		Method method = this.getClass().getMethod(name, parameterTypes);
		Iterator<Object[]> provider = GenericDataProvider.getDataProvider(method);
		if (provider.hasNext()) {
			provider.next();
		}
	}

	@Test
	public void test_SingleBean_ListOfBeans() throws Exception {
		String name = "method_SingleBean_ListOfBeans";
		Class<?>[] parameterTypes = new Class<?>[] { TestBean.class, List.class };
		this.dataForMethod(name, parameterTypes);
	}

	@DataBinding(propertiesPrefix = "tSB_LOB")
	public void method_SingleBean_ListOfBeans(@TestInput TestBean input, @TestOutput List<TestBean> output) {
	}

	@Test
	public void test_Primitive_ListOfBeans() throws Exception {
		String name = "method_Primitive_ListOfBeans";
		Class<?>[] parameterTypes = new Class<?>[] { String.class, List.class };
		this.dataForMethod(name, parameterTypes);
	}

	@DataBinding(propertiesPrefix = "tP_LOB")
	public void method_Primitive_ListOfBeans(@TestInput(name = "input") String input, @TestOutput List<TestBean> output) {
	}

	@Test(expectedExceptions = MultipleConfigurationErrorsException.class)
	public void test_NoParameters() throws Exception {
		String name = "method_NoParameters";
		Class<?>[] parameterTypes = new Class<?>[] {};
		this.dataForMethod(name, parameterTypes);
	}

	@DataBinding(propertiesPrefix = "tNP")
	public void method_NoParameters() {
	}
}