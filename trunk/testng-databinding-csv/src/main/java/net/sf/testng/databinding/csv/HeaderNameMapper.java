package net.sf.testng.databinding.csv;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import net.sf.testng.databinding.TestInput;
import net.sf.testng.databinding.TestOutput;
import net.sf.testng.databinding.core.error.ErrorCollector;
import net.sf.testng.databinding.core.error.MultipleSourceErrorsException;
import net.sf.testng.databinding.core.util.Types;
import net.sf.testng.databinding.util.Exceptions;
import net.sf.testng.databinding.util.MethodParameter;
import au.com.bytecode.opencsv.CSVReader;

/**
 * <p>
 * Maps the column names in the first line (header line) of the CSV file to the names of the
 * {@link MethodParameter method parameters}. Takes input and output column prefixes into account, mapping the
 * input and output columns to {@link TestInput test input} and {@link TestOutput test output} parameters.
 * </p>
 * <h3>Example</h3>
 * <p>
 * This example loads test input and output data for testing a <code>public static boolean isBetween(int value,
 * int lower, int upper)</code> application method. The test input values are bound to a Java Bean, the test
 * output value is a single boolean primitive value. Getters and setters are omitted in the Java Bean for
 * brevity in this example. They are however crucial in actual Java Beans, so you have to include them in any
 * Java Bean you actually want to bind data to.
 * </p>
 * <h4>Test Method</h4>
 * <pre>
 * &#64;DataBinding(propertiesPrefix = "isBetween")
 * public void testIsBetween(&#64;TestInput CheckData data, &#64;TestOutput(name = "expected") boolean expected) {
 *     assertEquals(isBetween(data.getValue(), data.getLower(), data.getUpper()), expected);
 * }
 * </pre>
 * <h4>Java Bean: CheckData</h4>
 * <pre>
 * public class CheckData {
 *     private int value;
 *     private int lower;
 *     private int upper;
 *     
 *     /* Getters and setters omitted for brevity &#42;/
 * }
 * </pre>
 * <h4>Data Properties File</h4>
 * <pre>
 * isBetween.dataSource=csv
 * isBetween.url=/data/isBetween.csv
 * </pre>
 * <h4>CSV Data Source File</h4>
 * <pre>
 * in_value,in_lower,in_upper,out_expected
 * -10,-5,10,false
 * -5,-5,10,true
 * 0,-5,10,true
 * 10,-5,10,true
 * 20,-5,10,false
 * </pre>
 * 
 * @author Matthias Rothe
 */
public class HeaderNameMapper extends Mapper {
	private String inputColumnPrefix;
	private String outputColumnPrefix;
	private List<String> headers = new ArrayList<String>();
	private List<MethodParameter> inputParameters = new ArrayList<MethodParameter>();
	private List<MethodParameter> outputParameters = new ArrayList<MethodParameter>();

	/**
	 * Constructor taking a {@link List list} of {@link MethodParameter method parameters} to bind the data to and
	 * {@link Properties configuration properties} specifying how the CSV file is defined and how to bind the data.
	 * 
	 * @param parameters The test method parameters
	 * @param properties The configuration properties
	 */
	public HeaderNameMapper(List<MethodParameter> parameters, Properties properties) {
		super(parameters, properties);

		this.inputColumnPrefix = properties.getProperty("inputColumnPrefix", "in_");
		this.outputColumnPrefix = properties.getProperty("outputColumnPrefix", "out_");

		for (MethodParameter parameter : parameters) {
			if (parameter.getAnnotation(TestInput.class) != null) {
				this.inputParameters.add(parameter);
			} else if (parameter.getAnnotation(TestOutput.class) != null) {
				this.outputParameters.add(parameter);
			}
		}
	}

	/**
	 * @return The input column prefix
	 */
	protected final String getInputColumnPrefix() {
		return this.inputColumnPrefix;
	}

	/**
	 * @return The output column prefix
	 */
	protected final String getOutputColumnPrefix() {
		return this.outputColumnPrefix;
	}

	/**
	 * @return The {@link List list} of {@link TestInput test input} {@link MethodParameter method parameters}
	 */
	protected final List<MethodParameter> getInputParameters() {
		return this.inputParameters;
	}

	/**
	 * @return The {@link List list} of {@link TestOutput test output} {@link MethodParameter method parameters}
	 */
	protected final List<MethodParameter> getOutputParameters() {
		return this.outputParameters;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<ErrorCollector> checkParameters(List<MethodParameter> parameters) {
		List<ErrorCollector> errorCollectors = new ArrayList<ErrorCollector>();

		for (MethodParameter parameter : parameters) {
			Type type = parameter.getType();

			if (Types.isEnumType(type) || Types.isListOfObjectsType(type)) {
				ErrorCollector errorCollector = new ErrorCollector(type);
				errorCollector.addError("Type " + type + " is not supported by this mapper: " + this.getClass());
				errorCollectors.add(errorCollector);
			}
		}

		return errorCollectors;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void init(CSVReader csvReader) throws Exception {
		String[] headerLine = csvReader.readNext();
		this.headers.addAll(Arrays.asList(headerLine));
		this.normalizeHeaders();

		List<ErrorCollector> errorCollectors = this.checkHeaders();
		if (errorCollectors != null && errorCollectors.size() > 0) {
			throw new MultipleSourceErrorsException(errorCollectors);
		}
	}

	private void normalizeHeaders() {
		List<String> normalizedHeaders = new ArrayList<String>();

		for (String header : this.headers) {
			normalizedHeaders.add(header.toLowerCase());
		}

		this.headers = normalizedHeaders;
	}

	/**
	 * Checks whether all required headers are contained in the CSV file. Returns a {@link List list} of
	 * {@link ErrorCollector error collectors} containing any errors found.
	 * 
	 * @return The list of error collectors
	 * @throws Exception if anything goes wrong during header checking
	 */
	protected List<ErrorCollector> checkHeaders() throws Exception {
		List<ErrorCollector> errorCollectors = new ArrayList<ErrorCollector>();

		errorCollectors.addAll(this.checkHeaders(this.inputParameters, this.inputColumnPrefix));
		errorCollectors.addAll(this.checkHeaders(this.outputParameters, this.outputColumnPrefix));

		return errorCollectors;
	}

	/**
	 * Checks whether all headers required by the given {@link List list} of {@link MethodParameter method parameters}
	 * are contained in the CSV file with the given prefix. Returns a {@link List list} of
	 * {@link ErrorCollector error collectors} containing any errors found.
	 * 
	 * @param parameters The test method parameters for which the headers are checked
	 * @param prefix The column name prefix of the headers to be checked
	 * @return The list of error collectors
	 * @throws IntrospectionException if anything goes wrong during Java Bean introspection
	 */
	protected List<ErrorCollector> checkHeaders(List<MethodParameter> parameters, String prefix)
			throws IntrospectionException {
		List<ErrorCollector> errorCollectors = new ArrayList<ErrorCollector>();

		for (MethodParameter parameter : parameters) {
			Type type = parameter.getType();
			ErrorCollector errorCollector = null;

			if (Types.isPrimitiveType(type)) {
				String name = parameter.getName();
				errorCollector = this.checkPrimitiveTypeHeaders(type, name, prefix);
			} else if (Types.isSingleBeanType(type)) {
				errorCollector = this.checkSingleBeanHeaders((Class<?>) type, prefix);
			}

			if (errorCollector != null && errorCollector.hasErrors()) {
				errorCollectors.add(errorCollector);
			}
		}

		return errorCollectors;
	}

	/**
	 * Checks whether the header for the given primitive type and name is contained in the CSV file with the given
	 * prefix. Returns an {@link ErrorCollector error collector} containing the error if the header cannot be found.
	 * 
	 * @param type The primitive type of the method parameter
	 * @param name The name of the method parameter
	 * @param prefix The column prefix of the header
	 * @return The error collector
	 */
	protected ErrorCollector checkPrimitiveTypeHeaders(Type type, String name, String prefix) {
		ErrorCollector errorCollector = new ErrorCollector(type, name);

		if (!this.headersContain(prefix, name)) {
			errorCollector.addError("header not found: " + prefix + name);
		}

		return errorCollector;
	}

	/**
	 * Checks whether all headers required by the given Java Bean type are contained in the CSV file with the given
	 * prefix. Returns an {@link ErrorCollector error collector} containing any error found.
	 * 
	 * @param type The Java Bean type of the method parameter
	 * @param prefix The column prefix of the header
	 * @return The error collector
	 * @throws IntrospectionException if anything goes wrong during Java Bean introspection
	 */
	protected ErrorCollector checkSingleBeanHeaders(Class<?> type, String prefix) throws IntrospectionException {
		ErrorCollector errorCollector = new ErrorCollector(type);

		BeanInfo info = Introspector.getBeanInfo(type);
		for (PropertyDescriptor descriptor : info.getPropertyDescriptors()) {
			if (this.isWriteable(descriptor) && !this.headersContain(prefix, descriptor.getName())) {
				errorCollector.addError("header not found: " + prefix + descriptor.getName());
			}
		}

		return errorCollector;
	}

	private boolean isWriteable(PropertyDescriptor descriptor) {
		return descriptor.getWriteMethod() != null;
	}

	/**
	 * Checks whether a header with the given prefix and name is contained in the CSV file.
	 * 
	 * @param prefix The column prefix
	 * @param name The header name
	 * @return <code>true</code>, if and only if a header with the given prefix and name is contained
	 * in the CSV file, <code>false</code> otherwise
	 */
	protected boolean headersContain(String prefix, String name) {
		return this.headers.contains(this.createHeader(prefix, name));
	}

	private String createHeader(String prefix, String name) {
		return (prefix + name).toLowerCase();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] createBeans(String[] line) {
		List<Object> objects = new ArrayList<Object>();

		for (MethodParameter parameter : this.getParameters()) {
			if (this.inputParameters.contains(parameter)) {
				objects.add(this.createObject(parameter, line, this.inputColumnPrefix));
			} else if (this.outputParameters.contains(parameter)) {
				objects.add(this.createObject(parameter, line, this.outputColumnPrefix));
			}
		}

		return objects.toArray();
	}

	/**
	 * Creates the object for the given {@link MethodParameter method parameter} taking the appropriate value(s) from
	 * the given {@link String string array} representing the current line from the CSV file. The given prefix is
	 * used to retrieve the correct header(s) corresponding to the given {@link MethodParameter method parameter}.
	 * 
	 * @param parameter The test method parameter for which to create the object
	 * @param line The current line from the CSV file
	 * @param prefix The column prefix
	 * @return The created object
	 */
	protected Object createObject(MethodParameter parameter, String[] line, String prefix) {
		Type type = parameter.getType();

		if (Types.isPrimitiveType(type)) {
			return this.createPrimitive(parameter, line, prefix);
		} else if (Types.isSingleBeanType(type)) {
			return this.createSingleBean(parameter, line, prefix);
		} else {
			// shouldn't happen cause check would have failed earlier
			return null;
		}
	}

	/**
	 * Creates the primitive value for the given {@link MethodParameter method parameter} taking the appropriate value
	 * from the given {@link String string array} representing the current line from the CSV file. The given prefix is
	 * used to retrieve the correct header corresponding to the given {@link MethodParameter method parameter}.
	 * 
	 * @param parameter The test method parameter for which to create the object
	 * @param line The current line from the CSV file
	 * @param prefix The column prefix
	 * @return The created primitive value
	 */
	protected Object createPrimitive(MethodParameter parameter, String[] line, String prefix) {
		final String value = line[this.getHeaderIndexFor(prefix, parameter.getName())];
		final Type type = parameter.getType();

		if (type.equals(String.class)) {
			return value;
		} else if (type == Integer.class || type == int.class) {
			return Integer.parseInt(value);
		} else if (type == Long.class || type == long.class) {
			return Long.parseLong(value);
		} else if (type == Float.class || type == float.class) {
			return Float.parseFloat(value);
		} else if (type == Double.class || type == double.class) {
			return Double.parseDouble(value);
		} else if (type == Boolean.class || type == boolean.class) {
			return Boolean.parseBoolean(value);
		}

		// can't happen
		throw new RuntimeException();
	}

	/**
	 * Creates the Java Bean for the given {@link MethodParameter method parameter} taking the appropriate values
	 * from the given {@link String string array} representing the current line from the CSV file. The given prefix is
	 * used to retrieve the correct header corresponding to the given {@link MethodParameter method parameter}.
	 * 
	 * @param parameter The test method parameter for which to create the object
	 * @param line The current line from the CSV file
	 * @param prefix The column prefix
	 * @return The created Java Bean
	 */
	protected Object createSingleBean(MethodParameter parameter, String[] line, String prefix) {
		try {
			Class<?> clazz = (Class<?>) parameter.getType();
			Object object = clazz.newInstance();
			BeanInfo info = Introspector.getBeanInfo(clazz);

			for (PropertyDescriptor descriptor : info.getPropertyDescriptors()) {
				if (this.isWriteable(descriptor) && isPrimitiveType(descriptor)) {
					Object value = createPrimitive(createMethodParameterForProperty(descriptor), line, prefix);
					Method writeMethod = descriptor.getWriteMethod();
					writeMethod.invoke(object, value);
				}
			}

			return object;
		} catch (Exception e) {
			throw Exceptions.softenIfNecessary(e);
		}
	}

	private boolean isPrimitiveType(PropertyDescriptor descriptor) {
		return Types.isPrimitiveType(descriptor.getPropertyType());
	}

	private MethodParameter createMethodParameterForProperty(final PropertyDescriptor descriptor) {
		final Method writeMethod = descriptor.getWriteMethod();
		final Annotation[][] annotations = writeMethod.getParameterAnnotations();
		final Type[] parameterTypes = writeMethod.getGenericParameterTypes();
		return new MethodParameter(Arrays.asList(annotations[0]), parameterTypes[0], descriptor.getName());
	}

	/**
	 * Retrieves the index of the header specified by the given prefix and name.
	 * 
	 * @param prefix The column prefix
	 * @param name The header name
	 * @return The index of the header, or -1 if no such header exists
	 */
	protected int getHeaderIndexFor(String prefix, String name) {
		return this.headers.indexOf((prefix + name).toLowerCase());
	}
}