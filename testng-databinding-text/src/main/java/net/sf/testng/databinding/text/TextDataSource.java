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
import net.sf.testng.databinding.IDataSource;
import net.sf.testng.databinding.TestInput;
import net.sf.testng.databinding.TestOutput;
import net.sf.testng.databinding.core.error.ErrorCollector;
import net.sf.testng.databinding.core.error.MissingPropertiesException;
import net.sf.testng.databinding.core.error.MultipleConfigurationErrorsException;
import net.sf.testng.databinding.util.MethodParameter;

/**
 * <p>
 * This {@link IDataSource data source} binds test data contained in text files to
 * test method parameters. It supports an arbitrary number of {@link TestInput test input} and
 * {@link TestOutput test output} parameters, but supports only {@link String} parameters.
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
 * <td><code>text</code></td>
 * <td>N/A</td>
 * <td>The name of this data source</td>
 * <td>Yes</td>
 * </tr>
 * <tr>
 * <td>{parameterName}.url</td>
 * <td>A {@link URL} conformant {@link String} for an absolute<br>
 * locator or a relative path starting with a<br>
 * slash (/)</td>
 * <td>N/A</td>
 * <td>The locator of the actual data source file</td>
 * <td>Yes</td>
 * </tr>
 * <tr>
 * <td>boundary</td>
 * <td>any {@link String}</td>
 * <td><code>null</code></td>
 * <td>The boundary separating the text chunks</td>
 * <td>No</td>
 * </tr>
 * <tr>
 * <td>encoding</td>
 * <td>Any name of a supported charset<br>
 * (e.g. UTF-8, ISO-8859-1, or US-ASCII)</td>
 * <td>UTF-8</td>
 * <td>The encoding of the data source file</td>
 * <td>No</td>
 * </tr>
 * </table>
 * </p>
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