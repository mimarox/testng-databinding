package net.sf.testng.databinding.csv;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Properties;

import net.sf.testng.databinding.core.error.ErrorCollector;
import net.sf.testng.databinding.core.error.MultipleConfigurationErrorsException;
import net.sf.testng.databinding.core.util.Types;
import net.sf.testng.databinding.util.MethodParameter;
import au.com.bytecode.opencsv.CSVReader;

/**
 * A mapper defines how each line of a CSV file is bound to Java objects.
 * 
 * @author Matthias Rothe
 */
public abstract class Mapper {
	private List<MethodParameter> parameters;
	private Properties properties;

	/**
	 * Constructor taking a {@link List list} of {@link MethodParameter method parameters} to bind the data to and
	 * {@link Properties configuration properties} specifying how the CSV file is defined and how to bind the data.
	 * 
	 * @param parameters The test method parameters
	 * @param properties The configuration properties
	 */
	public Mapper(List<MethodParameter> parameters, Properties properties) {
		List<ErrorCollector> errorCollectors = this.checkParameters(parameters);
		if (errorCollectors.size() > 0) {
			throw new MultipleConfigurationErrorsException(errorCollectors);
		}

		this.parameters = parameters;
		this.properties = properties;
	}

	/**
	 * Returns the {@link List list} of {@link MethodParameter method parameters}.
	 * 
	 * @return The list of method parameters
	 */
	protected List<MethodParameter> getParameters() {
		return this.parameters;
	}

	/**
	 * Checks whether the given {@link Type type} is not an {@link Enum enum type}. If the given type is an enum type
	 * an error is added to the given {@link ErrorCollector error collector}.
	 * 
	 * @param type The type to check
	 * @param errorCollector The error collector to add the error to in case the check fails
	 */
	protected void checkIsNotEnumType(Type type, ErrorCollector errorCollector) {
		if (Types.isEnumType(type)) {
			errorCollector.addError("Type " + type + " is not supported " + "by this mapper: " + this.getClass());
		}
	}

	/**
	 * Returns the {@link Properties configuration properties}.
	 * 
	 * @return The configuration properties
	 */
	protected Properties getProperties() {
		return this.properties;
	}

	/**
	 * Checks the {@link MethodParameter method parameters} of the test method. Returns a {@link List list} of
	 * {@link ErrorCollector error collectors} containing any errors found.
	 * 
	 * @param parameters The test method parameters
	 * @return The list of error collectors
	 */
	protected abstract List<ErrorCollector> checkParameters(List<MethodParameter> parameters);

	/**
	 * Initializes this mapper instance. This usually means reading the header line of the CSV file from the given
	 * csv reader and checking {@link MethodParameter method parameters} and {@link Properties configuration properties}.
	 * 
	 * @param csvReader The reader for the CSV file
	 * @throws Exception if anything goes wrong during the initialization
	 */
	public abstract void init(CSVReader csvReader) throws Exception;

	/**
	 * Binds the values contained in the given {@link String string array} representing a line from the CSV file to Java
	 * objects to be used as test method parameters and returns those objects as an {@link Object array of objects}.
	 * 
	 * @param line A line from the CSV file
	 * @return The array of objects to be used as test method parameters
	 */
	public abstract Object[] createBeans(String[] line);
}
