package net.sf.testng.databinding.text;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.fail;

import java.lang.reflect.Method;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import net.sf.testng.databinding.DataSource;
import net.sf.testng.databinding.GenericDataProvider;
import net.sf.testng.databinding.TestInput;
import net.sf.testng.databinding.TestOutput;
import net.sf.testng.databinding.core.model.Configuration;
import net.sf.testng.databinding.text.datasource.config.TextDataSourceConfigurations;
import net.sf.testng.databinding.util.MethodParameter;

public class TextDataSourceTest {
	private Method methodParametersCreator;

	@BeforeClass
	public void initMethodParametersCreator() throws SecurityException, NoSuchMethodException {
		methodParametersCreator = GenericDataProvider.class.getDeclaredMethod("createMethodParameters", Method.class);
		methodParametersCreator.setAccessible(true);
	}

	@Test
	public void shouldHaveDataSourceNameText() {
		DataSource dataSource = TextDataSource.class.getAnnotation(DataSource.class);
		assertNotEquals(dataSource, null);
		assertEquals(dataSource.name(), "text");
	}

	@Test
	public void shouldReadAllDataWithBoundary() throws Exception {
		TextDataSource dataSource = createTextDataSource(true);
		Object[][] expecteds = createMultiValueWithBoundaryExpecteds();
		assertContentsEqual(dataSource, expecteds);
	}

	@Test
	public void shouldReadAllDataWithoutBoundary() throws Exception {
		TextDataSource dataSource = createTextDataSource(false);
		Object[][] expecteds = createMultiValueWithoutBoundaryExpecteds();
		assertContentsEqual(dataSource, expecteds);
	}

	private void assertContentsEqual(final TextDataSource dataSource, final Object[][] expecteds) {
		for (int i = 0; i < expecteds.length; i++) {
			if (dataSource.hasNext()) {
				Object[] actualRow = dataSource.next();
				assertEquals(actualRow, expecteds[i]);
			} else {
				fail("Expected " + expecteds.length + " value sets, but only found " + i);
			}
		}
	}

	private TextDataSource createTextDataSource(final boolean useBoundary) throws Exception {
		List<MethodParameter> parameters = createMethodParameters("multiValueConsumer");

		Configuration configuration = useBoundary ?
				new Configuration(TextDataSourceConfigurations.class, "withBoundaryConfig") :
					new Configuration(TextDataSourceConfigurations.class, "withoutBoundaryConfig");
		
		return new TextDataSource(parameters, configuration);
	}

	private Object[][] createMultiValueWithBoundaryExpecteds() {
		return new Object[][] { { "123\n456", "abc\ndef", "321\n654", "cba\nfed" },
				{ "789\nabc", "ghi\njkl", "987\ncba", "ihg\nlkj" }, { "def\n012", "mno\npqr", "fed\n210", "onm\nrqp" },
				{ "123\n456", "abc\ndef", null, "cba\nfed" }, { "789\nabc", null, null, "ihg\nlkj" },
				{ "def\n012", null, null, null }, };
	}

	private Object[][] createMultiValueWithoutBoundaryExpecteds() {
		return new Object[][] { {
				"123\n456\n---123---\n789\nabc\n---123---\ndef\n012\n---123---\n123\n456\n---123---\n789\nabc\n---123---\ndef\n012",
				"abc\ndef\n---123---\nghi\njkl\n---123---\nmno\npqr\n---123---\nabc\ndef",
				"321\n654\n---123---\n987\ncba\n---123---\nfed\n210",
				"cba\nfed\n---123---\nihg\nlkj\n---123---\nonm\nrqp\n---123---\ncba\nfed\n---123---\nihg\nlkj" } };
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

	public void multiValueConsumer(@TestInput(name = "input1") final String input1,
			@TestInput(name = "input2") final String input2, @TestOutput(name = "output1") final String output1,
			@TestOutput(name = "output2") final String output2) {
	}
}