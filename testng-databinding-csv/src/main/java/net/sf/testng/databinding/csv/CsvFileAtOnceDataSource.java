package net.sf.testng.databinding.csv;

import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import net.sf.testng.databinding.AbstractDataSource;
import net.sf.testng.databinding.DataSource;
import net.sf.testng.databinding.IDataSource;
import net.sf.testng.databinding.TestInput;
import net.sf.testng.databinding.TestOutput;
import net.sf.testng.databinding.core.error.ErrorCollector;
import net.sf.testng.databinding.core.error.MissingPropertiesException;
import net.sf.testng.databinding.core.error.MultipleConfigurationErrorsException;
import net.sf.testng.databinding.core.error.MultipleSourceErrorsException;
import net.sf.testng.databinding.core.util.Types;
import net.sf.testng.databinding.util.MethodParameter;

/**
 * <p>
 * This {@link IDataSource data source} binds test data contained in CSV files to a test method parameter. It supports
 * only one {@link TestInput test input} parameter which needs to be a {@link List} of either primitives as defined
 * by {@link Types#isPrimitiveType(Type)} or objects of some Java Bean type. A {@link List} of primitives or Java
 * Beans is supported as a test output parameter with the appropriate <code>mapper</code>. More on that below.
 * </p><p>
 * Supposedly using this data source only makes sense if the test method getting its data this way has
 * just one fixed way of input and a predefined set of expected results. Then the list will contain
 * exactly these results as read from the CSV file.
 * </p>
 * <h3>Specifications</h3>
 * <h4>Data Properties</h4>
 * <p>
 * The following table gives an overview of the required and optional data property keys for this
 * data source.
 * </p><p>
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
 * <td>A {@link URL} conformant {@link String} for an absolute<br>
 * locator or a relative path starting with a<br>
 * slash (/)</td>
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
 * <td><code>headerNameMapper</code></td>
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
 * </p>
 * <h4>CSV Data Files</h4>
 * <p>
 * The structure of the CSV files depends a lot on the specified data properties and the selected <code>mapper</code>.
 * See the JavaDocs for the standard mappers {@link HeaderNameMapper} and {@link HeaderNameFileLinkingMapper}.
 * </p>
 * <h4>Java Beans</h4>
 * <p>
 * Java Beans that data is to be bound to need to have a standard constructor taking no arguments. Only primitive types and
 * {@link String Strings} are supported as Java Bean property types. Nested Java Beans are not supported either. If there
 * are any Java Bean properties for which no columns can be found a {@link MultipleSourceErrorsException} is thrown. Any
 * Java Bean properties having an unsupported type are just not set, leaving them unchanged. Any columns for which no Java
 * Bean properties of matching name and supported type can be found are skipped ignoring them.
 * </p>
 * <h3>Examples</h3>
 * <p>
 * All examples for binding CSV files are given within the standard mapper classes, as any CSV file binding is actually
 * mapper dependent.
 * </p>
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasNext() {
		return this.delegate.hasNext();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] next() {
		final List<Object> list = new ArrayList<Object>();

		do {
			list.add(this.delegate.next()[0]);
		} while (this.delegate.hasNext());

		return new Object[] { list };
	}
}