package net.sf.testng.databinding.csv;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.sf.testng.databinding.AbstractDataSource;
import net.sf.testng.databinding.DataSource;
import net.sf.testng.databinding.IDataSource;
import net.sf.testng.databinding.TestInput;
import net.sf.testng.databinding.TestOutput;
import net.sf.testng.databinding.core.error.ErrorCollector;
import net.sf.testng.databinding.core.error.MultipleConfigurationErrorsException;
import net.sf.testng.databinding.core.error.MultipleSourceErrorsException;
import net.sf.testng.databinding.core.model.Configuration;
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
	public CsvFileAtOnceDataSource(final List<MethodParameter> parameters,
			final Configuration configuration)
			throws Exception {
		this.checkParameters(parameters);
		final List<MethodParameter> adjustedParameters = this.prepareParameters(parameters);
		this.delegate = new CsvDataSource(adjustedParameters, configuration);
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