package net.sf.testng.databinding.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.NoSuchElementException;

import au.com.bytecode.opencsv.CSVReader;
import net.sf.testng.databinding.AbstractDataSource;
import net.sf.testng.databinding.DataSource;
import net.sf.testng.databinding.IDataSource;
import net.sf.testng.databinding.TestInput;
import net.sf.testng.databinding.TestOutput;
import net.sf.testng.databinding.core.error.MultipleSourceErrorsException;
import net.sf.testng.databinding.core.model.Configuration;
import net.sf.testng.databinding.core.util.DataSourceConfigurationLoader;
import net.sf.testng.databinding.core.util.Types;
import net.sf.testng.databinding.util.ConstructorMatcher;
import net.sf.testng.databinding.util.Constructors;
import net.sf.testng.databinding.util.MethodParameter;

/**
 * <p>
 * This {@link IDataSource data source} binds test data contained in CSV files to test method parameters. It supports
 * an arbitrary number of {@link TestInput test input} and {@link TestOutput test output} parameters and supports only
 * primitive and non-nested Java Beans with only primitive properties for test input and output parameters. Primitives
 * are defined in terms of {@link Types#isPrimitiveType(Type)}. A {@link List List} of primitives or Java Beans is
 * supported as a test output parameter with the appropriate <code>mapper</code>. More on that below.
 * </p>
 * <h3>Specifications</h3>
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

	public CsvDataSource(final List<MethodParameter> parameters, final Configuration configuration)
			throws Exception {
		CsvDataSourceConfiguration dataSourceConfiguration =
				DataSourceConfigurationLoader.loadDataSourceConfiguration(configuration,
						CsvDataSourceConfiguration.class);
		
		this.csvReader = this.createCsvReader(dataSourceConfiguration);
		this.mapper = this.createMapper(parameters, dataSourceConfiguration);
		this.mapper.init(this.csvReader);
	}

	public CsvDataSource(final List<MethodParameter> parameters,
			final CsvDataSourceConfiguration configuration) throws Exception {
		this.csvReader = this.createCsvReader(configuration);
		this.mapper = this.createMapper(parameters, configuration);
		this.mapper.init(this.csvReader);
	}

	private CSVReader createCsvReader(final CsvDataSourceConfiguration configuration)
			throws Exception {
		final URL url = configuration.getURL();
		final Charset charset = Charset.forName(configuration.getCharset());
		final char separator = configuration.getSeparator();
		final char quotechar = configuration.getQuoteChar();
		final char escape = configuration.getEscapeChar();
		final int line = configuration.getLinesToSkip();
		final boolean strictQuotes = configuration.useStrictQuotes();
		final boolean ignoreLeadingWhitespace = configuration.ignoreLeadingWhitespace();

		final BufferedReader rawReader = new BufferedReader(new InputStreamReader(url.openStream(), charset));
		return new CSVReader(rawReader, separator, quotechar, escape, line, strictQuotes, ignoreLeadingWhitespace);
	}

	private Mapper createMapper(final List<MethodParameter> parameters,
			final CsvDataSourceConfiguration configuration) throws Exception {
		final Class<?> mapperClass = configuration.getMapperClass();
		final ConstructorMatcher matcher = new MapperConstructorMatcher();
		final Constructor<?> constructor = Constructors.getMatchingConstructor(mapperClass, matcher);
		return (Mapper) constructor.newInstance(parameters, configuration);
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