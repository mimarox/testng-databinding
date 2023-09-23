package net.sf.testng.databinding.text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;

import net.sf.testng.databinding.AbstractDataSource;
import net.sf.testng.databinding.DataSource;
import net.sf.testng.databinding.IDataSource;
import net.sf.testng.databinding.TestInput;
import net.sf.testng.databinding.TestOutput;
import net.sf.testng.databinding.core.error.ErrorCollector;
import net.sf.testng.databinding.core.error.MissingPropertiesException;
import net.sf.testng.databinding.core.error.MultipleConfigurationErrorsException;
import net.sf.testng.databinding.core.model.Configuration;
import net.sf.testng.databinding.core.util.DataSourceConfigurationLoader;
import net.sf.testng.databinding.util.MethodParameter;

/**
 * <p>
 * This {@link IDataSource data source} binds test data contained in text files to
 * test method parameters. It supports an arbitrary number of {@link TestInput test input} and
 * {@link TestOutput test output} parameters, but supports only {@link String} parameters.
 * </p>
 * <h3>Specifications</h3>
 * <h4>Text Data Files</h4>
 * <p>
 * The text files to be bound can have several chunks of text separated by a boundary. Each chunk can span several
 * lines and is bound to one {@link String} parameter. Therefore there will be as many test method invocations as there
 * are chunks in the text file. Each {@link String} parameter is bound to a chunk from its individual text file. If
 * there are more chunks in one text file, than there are in others, the {@link String} parameters for which there are no
 * chunks available are set to <code>null</code>. If no boundary is given the whole text file is bound as a single chunk
 * and only one test method invocation will be the result.
 * </p>
 * <h3>Example</h3>
 * <p>
 * To make issues clearer, here is an example of this data source in use. It binds data to two input and one output
 * parameter, simulating a transformation from the two input data chunks into the output data chunk. The actual test code
 * is pseudocode that does not work, and is not part of the TestNG Data Binding framework, but clearly conveys the intent.
 * </p>
 * <h4>Test Method</h4>
 * <pre>
 * &#64;DataBinding(propertiesPrefix = "transform")
 * public void testTransform(&#64;TestInput(name = "source") final String source,
 *         &#64;TestInput(name = "mergedIn") final String mergedIn, &#64;TestOutput(name = "target") final String target) {
 *     assertEquals(merge(source, mergedIn), target);
 * }
 * </pre>
 * <h4>Data Properties File</h4>
 * <pre>
 * transform.dataSource=text
 * transform.source.url=/data/transform/source.txt
 * transform.mergedIn.url=/data/transform/mergedIn.txt
 * transform.target.url=/data/transform/target.txt
 * transform.boundary=---a_boundary---
 * </pre>
 * <h4>Text Data Sources</h4>
 * <h5>source.txt</h5>
 * <pre>
 * 01234
 * ABCDE
 * KLMNO
 * UVWXY
 * ---a_boundary---
 * 11235
 * 81321
 * 21138
 * 53211
 * </pre>
 * <h5>mergedIn.txt</h5>
 * <pre>
 * 56789
 * FGHIJ
 * PQRST
 * ---a_boundary---
 * ilike
 * texts
 * </pre>
 * <h5>target.txt</h5>
 * <pre>
 * 01234
 * 56789
 * ABCDE
 * FGHIJ
 * KLMNO
 * PQRST
 * UVWXY
 * ---a_boundary---
 * 11235
 * ilike
 * 81321
 * texts
 * 21138
 * ilike
 * 53211
 * texts
 * </pre>
 * 
 * @author Matthias Rothe
 */
@DataSource(name = "text")
public class TextDataSource extends AbstractDataSource {
	private List<TextFileReader> readers;
	private String encoding;

	/**
	 * Constructs a new instance of this class, setting the {@link MethodParameter test method parameters} to load the
	 * data for and the {@link Properties properties} describing where to load the data from.
	 * 
	 * @param parameters The test method parameters for which data is to be loaded
	 * @param properties The properties describing where to load the data from
	 * @throws Exception If anything goes wrong during the creation of this instance
	 */
	public TextDataSource(final List<MethodParameter> parameters,
			final Configuration configuration) throws Exception {
		TextDataSourceConfiguration dataSourceConfiguration =
				DataSourceConfigurationLoader.loadDataSourceConfiguration(configuration,
						TextDataSourceConfiguration.class);
		
		checkConfiguration(parameters, dataSourceConfiguration);
		encoding = dataSourceConfiguration.getEncoding();

		checkParameters(parameters);
		createReaders(parameters, dataSourceConfiguration);
	}

	private void checkConfiguration(final List<MethodParameter> parameters,
			final TextDataSourceConfiguration configuration) {
		Map<String, URL> urls = configuration.getURLs();
		
		final List<String> missingKeys = new ArrayList<String>();

		for (MethodParameter parameter : parameters) {
			if (!urls.containsKey(parameter.getName())) {
				missingKeys.add(parameter.getName());
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

	private void createReaders(final List<MethodParameter> parameters,
			final TextDataSourceConfiguration configuration) throws Exception {
		readers = new ArrayList<TextFileReader>();
		String boundary = configuration.getBoundary();

		Map<String, URL> urls = configuration.getURLs();
		
		for (MethodParameter parameter : parameters) {
			URL url = urls.get(parameter.getName());
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(url.openStream(), encoding));
			TextFileReader reader = new TextFileReader(bufferedReader, boundary);
			readers.add(reader);
		}
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
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