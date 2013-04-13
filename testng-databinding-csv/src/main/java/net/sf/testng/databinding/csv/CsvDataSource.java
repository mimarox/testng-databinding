package net.sf.testng.databinding.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;

import net.sf.testng.databinding.AbstractDataSource;
import net.sf.testng.databinding.DataSource;
import net.sf.testng.databinding.IDataSource;
import net.sf.testng.databinding.TestInput;
import net.sf.testng.databinding.TestOutput;
import net.sf.testng.databinding.core.error.MissingPropertiesException;
import net.sf.testng.databinding.core.error.MultipleSourceErrorsException;
import net.sf.testng.databinding.core.util.MethodParametersAndPropertiesConstructorMatcher;
import net.sf.testng.databinding.core.util.Types;
import net.sf.testng.databinding.util.ConstructorMatcher;
import net.sf.testng.databinding.util.Constructors;
import net.sf.testng.databinding.util.MethodParameter;
import au.com.bytecode.opencsv.CSVReader;

/**
 * <p>
 * This {@link IDataSource data source} binds test data contained in CSV files to test method parameters. It supports
 * an arbitrary number of {@link TestInput test input} and {@link TestOutput test output} parameters and supports only
 * primitive and non-nested Java Beans with only primitive properties for test input and output parameters. Primitives
 * are defined in terms of {@link Types#isPrimitiveType(Type)}. A {@link List List} of primitives or Java Beans is
 * supported as a test output parameter with the appropriate <code>mapper</code>. More on that below.
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
 * <td><code>csv</code></td>
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
 * @see HeaderNameMapper
 * @see HeaderNameFileLinkingMapper
 */
@DataSource(name = "csv")
public class CsvDataSource extends AbstractDataSource {
	private final CSVReader csvReader;
	private final Mapper mapper;
	private String[] nextLine;

	public CsvDataSource(final List<MethodParameter> parameters, final Properties properties) throws Exception {
		this.checkProperties(properties);
		this.csvReader = this.createCsvReader(properties);
		this.mapper = this.createMapper(parameters, properties);
		this.mapper.init(this.csvReader);
	}

	private void checkProperties(final Properties properties) {
		final List<String> missingKeys = new ArrayList<String>();

		if (!properties.containsKey("url")) {
			missingKeys.add("url");
		}

		if (missingKeys.size() > 0) {
			throw new MissingPropertiesException(missingKeys);
		}
	}

	private CSVReader createCsvReader(final Properties properties) throws Exception {
		final URL url = resolveURL(properties.getProperty("url"));
		final Charset charset = Charset.forName(properties.getProperty("charset", "UTF-8"));
		final char separator = properties.getProperty("separator", ",").charAt(0);
		final char quotechar = properties.getProperty("quoteChar", "\"").charAt(0);
		final char escape = properties.getProperty("escapeChar", "\\").charAt(0);
		final int line = Integer.parseInt(properties.getProperty("linesToSkip", "0"));
		final boolean strictQuotes = Boolean.parseBoolean(properties.getProperty("strictQuotes", "false"));
		final boolean ignoreLeadingWhiteSpace = Boolean.parseBoolean(properties.getProperty("ignoreLeadingWhiteSpace",
			"false"));

		final BufferedReader rawReader = new BufferedReader(new InputStreamReader(url.openStream(), charset));
		return new CSVReader(rawReader, separator, quotechar, escape, line, strictQuotes, ignoreLeadingWhiteSpace);
	}

	private URL resolveURL(final String urlString) {
		URL url;

		try {
			url = new URL(urlString);
		} catch (final MalformedURLException e) {
			url = getClass().getResource(urlString);
		}

		return url;
	}

	private Mapper createMapper(final List<MethodParameter> parameters, final Properties properties) throws Exception {
		String mapperDefinition = properties.getProperty("mapper", "headerNameMapper");
		Mapper mapper;

		if ("headerNameMapper".equals(mapperDefinition)) {
			mapper = new HeaderNameMapper(parameters, properties);
		} else if ("headerNameFileLinkingMapper".equals(mapperDefinition)) {
			mapper = new HeaderNameFileLinkingMapper(parameters, properties);
		} else {
			final Class<?> mapperClass = Class.forName(mapperDefinition);
			final ConstructorMatcher matcher = new MethodParametersAndPropertiesConstructorMatcher();
			final Constructor<?> constructor = Constructors.getMatchingConstructor(mapperClass, matcher);
			mapper = (Mapper) constructor.newInstance(parameters, properties);
		}

		return mapper;
	}

	@Override
	public boolean hasNext() {
		try {
			return (this.nextLine = this.csvReader.readNext()) != null;
		} catch (final IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public Object[] next() {
		if (this.nextLine != null) {
			return this.mapper.createBeans(this.nextLine);
		} else {
			throw new NoSuchElementException();
		}
	}
}