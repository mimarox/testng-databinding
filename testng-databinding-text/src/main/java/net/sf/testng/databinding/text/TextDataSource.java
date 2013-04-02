package net.sf.testng.databinding.text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;

import net.sf.testng.databinding.AbstractDataSource;
import net.sf.testng.databinding.DataSource;
import net.sf.testng.databinding.core.error.ErrorCollector;
import net.sf.testng.databinding.core.error.MissingPropertiesException;
import net.sf.testng.databinding.core.error.MultipleConfigurationErrorsException;
import net.sf.testng.databinding.util.MethodParameter;

@DataSource(name = "text")
public class TextDataSource extends AbstractDataSource {
	private List<TextFileReader> readers;
	private String encoding;

	public TextDataSource(final List<MethodParameter> parameters, final Properties properties) throws Exception {
		checkProperties(parameters, properties);
		encoding = properties.getProperty("encoding", "UTF-8");

		checkParameters(parameters);
		createReaders(parameters, properties);
	}

	private void checkProperties(final List<MethodParameter> parameters, final Properties properties) {
		final List<String> missingKeys = new ArrayList<String>();

		for (MethodParameter parameter : parameters) {
			if (!properties.containsKey(parameter.getName() + ".url")) {
				missingKeys.add(parameter.getName() + ".url");
			}
		}

		if (missingKeys.size() > 0) {
			throw new MissingPropertiesException(missingKeys);
		}
	}

	private void checkParameters(List<MethodParameter> parameters) {
		final List<ErrorCollector> errorCollectors = new ArrayList<ErrorCollector>();

		for (final MethodParameter parameter : parameters) {
			final Type type = parameter.getType();
			final ErrorCollector errorCollector = new ErrorCollector(type, parameter.getName());

			if (!type.equals(String.class)) {
				errorCollector.addError("Type " + type + " is not supported by this data source: " + this.getClass()
						+ ". This data source only supports type String.");
			}

			if (errorCollector.hasErrors()) {
				errorCollectors.add(errorCollector);
			}
		}

		if (errorCollectors.size() > 0) {
			throw new MultipleConfigurationErrorsException(errorCollectors);
		}
	}

	private void createReaders(List<MethodParameter> parameters, Properties properties) throws Exception {
		readers = new ArrayList<TextFileReader>();
		String boundary = properties.getProperty("boundary");

		for (MethodParameter parameter : parameters) {
			URL url = resolveURL(properties.getProperty(parameter.getName() + ".url"));
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream(), encoding));
			TextFileReader reader = new TextFileReader(bufferedReader, boundary);
			readers.add(reader);
		}
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

	@Override
	public boolean hasNext() {
		boolean hasNext = false;

		for (TextFileReader reader : readers) {
			try {
				if (reader.hasMoreData()) {
					hasNext = true;
					break;
				}
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}

		return hasNext;
	}

	@Override
	public Object[] next() {
		if (hasNext()) {
			try {
				Object[] next = new Object[readers.size()];

				for (int i = 0; i < readers.size(); i++) {
					next[i] = readers.get(i).readNextChunk();
				}

				return next;
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		} else {
			throw new NoSuchElementException();
		}
	}
}