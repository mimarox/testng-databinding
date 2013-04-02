package net.sf.testng.databinding.csv;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
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


public class HeaderNameMapper extends Mapper {
	private String inputColumnPrefix;
	private String outputColumnPrefix;
	private List<String> headers = new ArrayList<String>();
	private List<MethodParameter> inputParameters = new ArrayList<MethodParameter>();
	private List<MethodParameter> outputParameters = new ArrayList<MethodParameter>();

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

	protected final String getInputColumnPrefix() {
		return this.inputColumnPrefix;
	}

	protected final String getOutputColumnPrefix() {
		return this.outputColumnPrefix;
	}

	protected final List<MethodParameter> getInputParameters() {
		return this.inputParameters;
	}

	protected final List<MethodParameter> getOutputParameters() {
		return this.outputParameters;
	}

	@Override
	protected List<ErrorCollector> checkParameters(List<MethodParameter> parameters) {
		List<ErrorCollector> errorCollectors = new ArrayList<ErrorCollector>();

		for (MethodParameter parameter : parameters) {
			Type type = parameter.getType();

			if (Types.isEnumType(type) || Types.isListOfObjectsType(type)) {
				ErrorCollector errorCollector = new ErrorCollector(type);
				errorCollector.addError("Type " + type + " is not supported " + "by this mapper: " + this.getClass());
				errorCollectors.add(errorCollector);
			}
		}

		return errorCollectors;
	}

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

	protected List<ErrorCollector> checkHeaders() throws Exception {
		List<ErrorCollector> errorCollectors = new ArrayList<ErrorCollector>();

		errorCollectors.addAll(this.checkHeaders(this.inputParameters, this.inputColumnPrefix));
		errorCollectors.addAll(this.checkHeaders(this.outputParameters, this.outputColumnPrefix));

		return errorCollectors;
	}

	protected List<ErrorCollector> checkHeaders(List<MethodParameter> parameters, String prefix) throws IntrospectionException {
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

	protected ErrorCollector checkPrimitiveTypeHeaders(Type type, String name, String prefix) {
		ErrorCollector errorCollector = new ErrorCollector(type, name);

		if (!this.headersContain(prefix, name)) {
			errorCollector.addError("header not found: " + prefix + name);
		}

		return errorCollector;
	}

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

	protected boolean headersContain(String prefix, String name) {
		return this.headers.contains(this.createHeader(prefix, name));
	}

	private String createHeader(String prefix, String name) {
		return (prefix + name).toLowerCase();
	}

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

	protected Object createPrimitive(MethodParameter parameter, String[] line, String prefix) {
		return line[this.getHeaderIndexFor(prefix, parameter.getName())];
	}

	protected Object createSingleBean(MethodParameter parameter, String[] line, String prefix) {
		try {
			Class<?> clazz = (Class<?>) parameter.getType();
			Object object = clazz.newInstance();
			BeanInfo info = Introspector.getBeanInfo(clazz);

			for (PropertyDescriptor descriptor : info.getPropertyDescriptors()) {
				if (this.isWriteable(descriptor)) {
					String value = line[this.getHeaderIndexFor(prefix, descriptor.getName())];
					Method writeMethod = descriptor.getWriteMethod();
					writeMethod.invoke(object, value);
				}
			}

			return object;
		} catch (Exception e) {
			throw Exceptions.softenIfNecessary(e);
		}
	}

	protected int getHeaderIndexFor(String prefix, String name) {
		return this.headers.indexOf((prefix + name).toLowerCase());
	}
}