package net.sf.testng.databinding.csv;

import java.beans.IntrospectionException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import net.sf.testng.databinding.TestInput;
import net.sf.testng.databinding.TestOutput;
import net.sf.testng.databinding.core.error.ErrorCollector;
import net.sf.testng.databinding.core.util.Types;
import net.sf.testng.databinding.util.Exceptions;
import net.sf.testng.databinding.util.MethodParameter;

/**
 * <p>
 * Maps the column names in the first line (header line) of the CSV file to the names of the
 * {@link MethodParameter method parameters}. Takes input and output column prefixes into account, mapping the
 * input and output columns to {@link TestInput test input} and {@link TestOutput test output} parameters, and
 * supports a linking column prefix. The linking column allows binding a dependent CSV file per line of the main
 * CSV file to a {@link List} of Java Beans for a {@link TestOutput test output} parameter.
 * </p><p>
 * As all configuration properties are reused for binding any dependent CSV file all dependent CSV files need to
 * have the same structure as the main CSV file. Dependent CSV files cannot have another linking column set again.
 * All dependent CSV files need to be placed in the same location as the main CSV file, as the path to the main
 * CSV file is prepended to the name of the dependent CSV file to locate it. So if the path to the main CSV file
 * is /path/to/main.csv and it contains a link to a dependent.csv file, the path to the linked file must be
 * /path/to/dependent.csv.
 * </p>
 * <h3>Example</h3>
 * <p>
 * This example loads test input and output data for testing selected ranges of a <code>public static
 * List&lt;Integer&gt; getPrimesInRange(int lowerBound, int upperBound, boolean includingBounds)</code> application
 * method. The bounds are loaded into a Java Bean, the <code>includingBounds</code> parameter is loaded as a boolean
 * primitive and the expected values for each range are loaded from a dependent CSV file as a {@link List list} of
 * {@link Integer integers}. Getters and setters are omitted in the Java Bean for brevity in this example. They are
 * however crucial in actual Java Beans, so you have to include them in any Java Bean you actually want to bind data
 * to.
 * </p>
 * <h4>Test Method</h4>
 * <pre>
 * &#64;DataBinding(propertiesPrefix = "primesInRange")
 * public void testGetPrimesInRange(&#64;TestInput Bounds bounds, &#64;TestInput(name = "includingBounds") boolean includingBounds,
 *         &#64;TestOutput(name = "primes") List&lt;Integer&gt; primes) {
 *     assertEquals(getPrimesInRange(bounds.getLower(), bounds.getUpper(), includingBounds), primes);
 * }
 * </pre>
 * <h4>Java Bean: Bounds</h4>
 * <pre>
 * public class Bounds {
 *     private int lower;
 *     private int upper;
 *     
 *     /* Getters and setters omitted for brevity &#42;/
 * }
 * </pre>
 * <h4>Data Properties File</h4>
 * <pre>
 * primesInRange.dataSource=csv
 * primesInRange.url=/data/primeRanges.csv
 * primesInRange.mapper=headerNameFileLinkingMapper
 * </pre>
 * <h4>CSV Data Files</h4>
 * <h5>primeRanges.csv</h5>
 * <pre>
 * in_lower,in_upper,in_includingBounds,link_primes
 * 2,11,true,2-to-11-primes.csv
 * 13,37,false,13-to-37-primes.csv
 * </pre>
 * <h5>2-to-11-primes.csv</h5>
 * <pre>
 * out_primes
 * 2
 * 3
 * 5
 * 7
 * 11
 * </pre>
 * <h5>13-to-37-primes.csv</h5>
 * <pre>
 * out_primes
 * 17
 * 19
 * 23
 * 29
 * 31
 * </pre>
 * 
 * @author Matthias Rothe
 */
public class HeaderNameFileLinkingMapper extends HeaderNameMapper {
	private final String linkingColumnPrefix;

	/**
	 * Constructor taking a {@link List list} of {@link MethodParameter method parameters} to bind the data to and
	 * {@link Properties configuration properties} specifying how the CSV file is defined and how to bind the data.
	 * 
	 * @param parameters The test method parameters
	 * @param properties The configuration properties
	 */
	public HeaderNameFileLinkingMapper(final List<MethodParameter> parameters,
			final CsvDataSourceConfiguration configuration) {
		super(parameters, configuration);
		this.linkingColumnPrefix = configuration.getLinkingColumnPrefix();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<ErrorCollector> checkParameters(final List<MethodParameter> parameters) {
		final List<ErrorCollector> errorCollectors = new ArrayList<ErrorCollector>();

		for (final MethodParameter parameter : parameters) {
			final Type type = parameter.getType();

			final ErrorCollector errorCollector = new ErrorCollector(type);
			this.checkIsNotEnumType(type, errorCollector);

			if (errorCollector.hasErrors()) {
				errorCollectors.add(errorCollector);
			}
		}

		return errorCollectors;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<ErrorCollector> checkHeaders() throws Exception {
		final List<ErrorCollector> errorCollectors = new ArrayList<ErrorCollector>();

		errorCollectors.addAll(this.checkInputHeaders());
		errorCollectors.addAll(this.checkOutputHeaders());

		return errorCollectors;
	}

	private List<ErrorCollector> checkInputHeaders() throws IntrospectionException {
		return this.checkHeaders(this.getInputParameters(), this.getInputColumnPrefix());
	}

	private List<ErrorCollector> checkOutputHeaders() throws IntrospectionException {
		final List<ErrorCollector> errorCollectors = new ArrayList<ErrorCollector>();
		final String prefix = this.getOutputColumnPrefix();

		for (final MethodParameter parameter : this.getOutputParameters()) {
			final Type type = parameter.getType();
			ErrorCollector errorCollector = null;

			if (Types.isPrimitiveType(type)) {
				errorCollector = this.checkPrimitiveTypeHeaders(type, parameter.getName(), prefix);
			} else if (Types.isSingleBeanType(type)) {
				errorCollector = this.checkSingleBeanHeaders((Class<?>) type, prefix);
			} else if (Types.isListOfPrimitivesType(type)) {
				errorCollector = this.checkListOfPrimitivesHeaders(type, parameter.getName(), prefix);
			} else if (Types.isListOfBeansType(type)) {
				errorCollector = this.checkListOfBeansHeaders((ParameterizedType) type, prefix);
			}

			if (errorCollector != null && errorCollector.hasErrors()) {
				errorCollectors.add(errorCollector);
			}
		}

		return errorCollectors;
	}

	private ErrorCollector checkListOfPrimitivesHeaders(final Type type, final String name, final String prefix) {
		final ErrorCollector errorCollector = new ErrorCollector(type, name);

		if (!this.headersContain(this.linkingColumnPrefix, name)) {
			errorCollector.addError("no link column found");
		}

		return errorCollector;
	}

	private ErrorCollector checkListOfBeansHeaders(final ParameterizedType type, final String prefix) {
		final ErrorCollector errorCollector = new ErrorCollector(type);

		final Class<?> beanClass = (Class<?>) type.getActualTypeArguments()[0];
		if (!this.headersContain(this.linkingColumnPrefix, beanClass.getSimpleName())) {
			errorCollector.addError("no link column found");
		}

		return errorCollector;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] createBeans(final String[] line) {
		final List<Object> objects = new ArrayList<Object>();

		for (final MethodParameter parameter : this.getParameters()) {
			if (this.getInputParameters().contains(parameter)) {
				objects.add(this.createInputObject(parameter, line));
			} else if (this.getOutputParameters().contains(parameter)) {
				objects.add(this.createOutputObject(parameter, line));
			}
		}

		return objects.toArray();
	}

	private Object createInputObject(final MethodParameter parameter, final String[] line) {
		return this.createObject(parameter, line, this.getInputColumnPrefix());
	}

	private Object createOutputObject(final MethodParameter parameter, final String[] line) {
		final Type type = parameter.getType();

		if (Types.isListOfObjectsType(type)) {
			return this.createListOfObjects(parameter, line);
		} else {
			return this.createObject(parameter, line, this.getOutputColumnPrefix());
		}
	}

	private Object createListOfObjects(final MethodParameter parameter, final String[] line) {
		try {
			final List<Object> list = new ArrayList<Object>();

			final String name = parameter.getName();
			final String linkKey = line[this.getHeaderIndexFor(this.linkingColumnPrefix, name)];

			final CsvDataSourceConfiguration configuration =
					getConfiguration().getConfiguration(linkKey);
			
			final List<MethodParameter> parameters = this.createParameters(parameter);

			final CsvDataSource provider = new CsvDataSource(parameters, configuration);
			while (provider.hasNext()) {
				list.add(provider.next()[0]);
			}

			return list;
		} catch (final Exception e) {
			throw Exceptions.softenIfNecessary(e);
		}
	}

	private List<MethodParameter> createParameters(final MethodParameter parameter) {
		return Arrays.asList(Types.unwrapIfPossible(parameter));
	}
}