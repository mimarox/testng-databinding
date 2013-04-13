package net.sf.testng.databinding.csv;

import java.beans.IntrospectionException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import net.sf.testng.databinding.TestInput;
import net.sf.testng.databinding.TestOutput;
import net.sf.testng.databinding.core.error.ErrorCollector;
import net.sf.testng.databinding.core.util.Types;
import net.sf.testng.databinding.util.Exceptions;
import net.sf.testng.databinding.util.MethodParameter;

/**
 * Maps the column names in the first line (header line) of the CSV file to the names of the
 * {@link MethodParameter method parameters}. Takes input and output column prefixes into account, mapping the
 * input and output columns to {@link TestInput test input} and {@link TestOutput test output} parameters, and
 * supports a linking column prefix. The linking column allows binding a dependent CSV file per line of the main
 * CSV file to a {@link List} of Java Beans for a {@link TestOutput test output} parameter.
 * <p>
 * As all configuration properties are reused for binding any dependent CSV file all dependent CSV files need to
 * have the same structure as the main CSV file. Dependent CSV files cannot have another linking column set again.
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
	public HeaderNameFileLinkingMapper(final List<MethodParameter> parameters, final Properties properties) {
		super(parameters, properties);
		this.linkingColumnPrefix = properties.getProperty("linkingColumnPrefix", "link_");
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
			final String link = line[this.getHeaderIndexFor(this.linkingColumnPrefix, name)];

			final Properties properties = this.copyProperties();
			this.setLinkUrl(properties, link);

			final List<MethodParameter> parameters = this.createParameters(parameter);

			final CsvDataSource provider = new CsvDataSource(parameters, properties);
			while (provider.hasNext()) {
				list.add(provider.next()[0]);
			}

			return list;
		} catch (final Exception e) {
			throw Exceptions.softenIfNecessary(e);
		}
	}

	private Properties copyProperties() {
		final Properties properties = new Properties();

		for (final Entry<Object, Object> entry : this.getProperties().entrySet()) {
			properties.put(entry.getKey(), entry.getValue());
		}

		return properties;
	}

	private void setLinkUrl(final Properties properties, final String link) {
		final String linkUrl = this.resolveUrl(properties.getProperty("url"), link);
		properties.setProperty("url", linkUrl);
	}

	private String resolveUrl(final String url, final String link) {
		final String urlBase = url.substring(0, url.lastIndexOf("/"));
		return urlBase + "/" + link;
	}

	private List<MethodParameter> createParameters(final MethodParameter parameter) {
		return Arrays.asList(Types.unwrapIfPossible(parameter));
	}
}