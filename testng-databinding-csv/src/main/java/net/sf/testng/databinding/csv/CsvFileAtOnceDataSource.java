package net.sf.testng.databinding.csv;

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import net.sf.testng.databinding.AbstractDataSource;
import net.sf.testng.databinding.DataSource;
import net.sf.testng.databinding.TestInput;
import net.sf.testng.databinding.TestOutput;
import net.sf.testng.databinding.error.ErrorCollector;
import net.sf.testng.databinding.error.MissingPropertiesException;
import net.sf.testng.databinding.error.MultipleConfigurationErrorsException;
import net.sf.testng.databinding.util.MethodParameter;
import net.sf.testng.databinding.util.Types;

/**
 * Reads a whole CSV file into the first and only object contained in the first and only Object
 * array returned by calling {@link #next()}. This object needs to be a {@link List} of either
 * primitives as defined by {@link Types#isPrimitiveType(Type)} or objects of some Java Bean type.
 * <p>
 * Supposedly using this data source only makes sense if the test method getting its data this way has
 * just one fixed way of input and a predefined set of expected results. Then the list will contain
 * exactly these results as read from the CSV file.
 * <p>
 * The default mapper for this data source is the {@link HeaderNameMapper}. It
 * may be overridden by the mapper property.
 * <p>
 * The following table gives an overview of the required and optional data property keys for this
 * data source.
 * <p>
 * <table border="1">
 * <tr>
 * <td><b>Key</b></td>
 * <td><b>Possible Values</b></td>
 * <td><b>Default Value</b></td>
 * <td><b>Description</b></td>
 * <td><b>Required</b></td>
 * </tr>
 * <tr>
 * <td>dataSource</td>
 * <td><code>csv-file-at-once</code></td>
 * <td>N/A</td>
 * <td>The name of this data source</td>
 * <td>Yes</td>
 * </tr>
 * <tr>
 * <td>url</td>
 * <td>An absolute or relative source file locator<br>
 * A relative locator must start with a slash (/)</td>
 * <td>N/A</td>
 * <td>The locator of the actual data source file</td>
 * <td>Yes</td>
 * </tr>
 * <tr>
 * <td>mapper</td>
 * <td>The fully qualified class name of a class<br>
 * extending the {@link Mapper} abstract class<br>
 * or one of the short names for the predefined<br>
 * mappers:<br><br>
 * <code>headerNameMapper</code><br>
 * <code>headerNameFileLinkingMapper</code></td>
 * <td><code>headerName</code></td>
 * <td>The mapper implementation defining how the data within the<br>
 * CSV source file will be mapped to the test method parameters</td>
 * <td>No</td>
 * </tr>
 * <tr>
 * <td>charset</td>
 * <td>Any charset name or alias deemed to be legal by<br>
 * {@link Charset#forName(String)}</td>
 * <td>UTF-8</td>
 * <td>The character set of the CSV source file</td>
 * <td>No</td>
 * </tr>
 * <tr>
 * <td>separator</td>
 * <td>Any single character</td>
 * <td>, (Comma)</td>
 * <td>The character used as the value separator within the CSV source file</td>
 * <td>No</td>
 * </tr>
 * <tr>
 * <td>quoteChar</td>
 * <td>Any single character</td>
 * <td>" (Quotation mark)</td>
 * <td>The character used for quotes within the CSV source file</td>
 * <td>No</td>
 * </tr>
 * <tr>
 * <td>escapeChar</td>
 * <td>Any single character</td>
 * <td>\ (Backslash)</td>
 * <td>The character used for escapes within the CSV source file</td>
 * <td>No</td>
 * </tr>
 * <tr>
 * <td>linesToSkip</td>
 * <td>Any integer &gt;= 0</td>
 * <td>0</td>
 * <td>The number of lines to skip at the beginning of the CSV source file</td>
 * <td>No</td>
 * </tr>
 * <tr>
 * <td>strictQuotes</td>
 * <td><code>true</code>, <code>false</code></td>
 * <td><code>false</code></td>
 * <td>Whether to use strict quotes or not</td>
 * <td>No</td>
 * </tr>
 * <tr>
 * <td>ignoreLeadingWhiteSpace</td>
 * <td><code>true</code>, <code>false</code></td>
 * <td><code>false</code></td>
 * <td>Whether to ignore leading white space or not</td>
 * <td>No</td>
 * </tr>
 * <tr>
 * <td>inputColumnPrefix</td>
 * <td>Any arbitrary {@link String} different from the value<br>
 * of outputColumnPrefix and linkingColumnPrefix</td>
 * <td>in_</td>
 * <td>The prefix to signify test data input columns, i.e. columns<br>
 * containing data for test method parameters annotated with<br>
 * {@link TestInput}</td>
 * <td>No</td>
 * </tr>
 * <tr>
 * <td>outputColumnPrefix</td>
 * <td>Any arbitrary {@link String} different from the value<br>
 * of inputColumnPrefix and linkingColumnPrefix</td>
 * <td>out_</td>
 * <td>The prefix to signify test data output columns, i.e. columns<br>
 * containing data for test method parameters annotated with<br>
 * {@link TestOutput}</td>
 * <td>No</td>
 * </tr>
 * <tr>
 * <td>linkingColumnPrefix</td>
 * <td>Any arbitrary {@link String} different from the value<br>
 * of inputColumnPrefix and outputColumnPrefix</td>
 * <td>link_</td>
 * <td>The prefix to signify the linking column. Only applicable if the<br>
 * {@link HeaderNameFileLinkingMapper} is used as the<br>
 * mapper</td>
 * <td>No</td>
 * </tr>
 * </table>
 * 
 * @author Matthias Rothe
 */
@DataSource(name = "csv-file-at-once")
public class CsvFileAtOnceDataSource extends AbstractDataSource {
	private final CsvDataSource delegate;

	/**
	 * Creates a new object of this class.
	 * 
	 * @param parameters
	 *            The test method's parameters
	 * @param properties
	 *            The data properties
	 * @throws Exception
	 *             if anything goes wrong during initialization
	 */
	public CsvFileAtOnceDataSource(final List<MethodParameter> parameters, final Properties properties)
			throws Exception {
		this.checkProperties(properties);
		this.checkParameters(parameters);
		final List<MethodParameter> adjustedParameters = this.prepareParameters(parameters);
		final Properties adjustedProperties = this.prepareProperties(properties);
		this.delegate = new CsvDataSource(adjustedParameters, adjustedProperties);
	}

	private void checkProperties(final Properties properties) {
		if (!properties.containsKey("url")) {
			throw new MissingPropertiesException("url");
		}
	}

	private void checkParameters(final List<MethodParameter> parameters) {
		final List<ErrorCollector> errorCollectors = new ArrayList<ErrorCollector>();

		if (parameters.size() > 1) {
			final ErrorCollector errorCollector = new ErrorCollector("too many parameters");
			errorCollector.addError("The method specifies too many parameters for this data provider strategy.");
			errorCollectors.add(errorCollector);
		} else {
			final MethodParameter parameter = parameters.get(0);
			final Type type = parameter.getType();
			final ErrorCollector errorCollector = new ErrorCollector(type);

			if (parameter.getAnnotation(TestOutput.class) == null) {
				errorCollector.addError("The parameter must be annotated with @TestOutput.");
			}

			if (!Types.isListOfObjectsType(type)) {
				errorCollector.addError("The parameter must either be a list of primitives"
						+ " or of a Java Bean type.");
			}

			if (errorCollector.hasErrors()) {
				errorCollectors.add(errorCollector);
			}
		}

		if (errorCollectors.size() > 0) {
			throw new MultipleConfigurationErrorsException(errorCollectors);
		}
	}

	private List<MethodParameter> prepareParameters(final List<MethodParameter> parameters) {
		return Arrays.asList(Types.unwrapIfPossible(parameters.get(0)));
	}

	private Properties prepareProperties(final Properties properties) {
		if (!properties.containsKey("mapper")) {
			properties.setProperty("mapper", HeaderNameMapper.class.getName());
		}
		return properties;
	}

	@Override
	public boolean hasNext() {
		return this.delegate.hasNext();
	}

	@Override
	public Object[] next() {
		final List<Object> list = new ArrayList<Object>();

		do {
			list.add(this.delegate.next()[0]);
		} while (this.delegate.hasNext());

		return new Object[] { list };
	}
}