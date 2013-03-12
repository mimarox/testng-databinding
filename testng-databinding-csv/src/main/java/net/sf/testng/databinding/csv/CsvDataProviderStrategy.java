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

import net.sf.testng.databinding.AbstractDataProviderStrategy;
import net.sf.testng.databinding.DataProviderStrategyNames;
import net.sf.testng.databinding.TestInput;
import net.sf.testng.databinding.error.MissingPropertiesException;
import net.sf.testng.databinding.util.ConstructorMatcher;
import net.sf.testng.databinding.util.Constructors;
import net.sf.testng.databinding.util.MethodParameter;
import net.sf.testng.databinding.util.MethodParametersAndPropertiesConstructorMatcher;

import au.com.bytecode.opencsv.CSVReader;


/**
 * Reads a CSV file line by line and returns the contents of one line per call to {@link #next()}.
 * <p>
 * The data to be provided can either be {@link TestInput}
 * 
 * @author Matthias Rothe
 */
@DataProviderStrategyNames({ "CSV", "csv" })
public class CsvDataProviderStrategy extends AbstractDataProviderStrategy {
	private final CSVReader csvReader;
	private final MappingStrategy mapper;
	private String[] nextLine;

	public CsvDataProviderStrategy(final List<MethodParameter> parameters, final Properties properties) throws Exception {
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
		if (!properties.containsKey("mapper")) {
			missingKeys.add("mapper");
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
		final boolean ignoreLeadingWhiteSpace = Boolean.parseBoolean(properties.getProperty("ignoreLeadingWhiteSpace", "false"));

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

	private MappingStrategy createMapper(final List<MethodParameter> parameters, final Properties properties) throws Exception {
		final Class<?> mapperClass = Class.forName(properties.getProperty("mapper"));
		final ConstructorMatcher matcher = new MethodParametersAndPropertiesConstructorMatcher();
		final Constructor<?> constructor = Constructors.getMatchingConstructor(mapperClass, matcher);
		return (MappingStrategy) constructor.newInstance(parameters, properties);
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