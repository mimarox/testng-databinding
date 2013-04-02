package net.sf.testng.databinding.csv;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Properties;

import net.sf.testng.databinding.core.error.ErrorCollector;
import net.sf.testng.databinding.core.error.MultipleConfigurationErrorsException;
import net.sf.testng.databinding.core.util.Types;
import net.sf.testng.databinding.util.MethodParameter;

import au.com.bytecode.opencsv.CSVReader;


public abstract class Mapper {
	private List<MethodParameter> parameters;
	private Properties properties;

	public Mapper(List<MethodParameter> parameters, Properties properties) {
		List<ErrorCollector> errorCollectors = this.checkParameters(parameters);
		if (errorCollectors.size() > 0) {
			throw new MultipleConfigurationErrorsException(errorCollectors);
		}

		this.parameters = parameters;
		this.properties = properties;
	}

	protected List<MethodParameter> getParameters() {
		return this.parameters;
	}

	protected void checkIsNotEnumType(Type type, ErrorCollector errorCollector) {
		if (Types.isEnumType(type)) {
			errorCollector.addError("Type " + type + " is not supported " + "by this mapper: " + this.getClass());
		}
	}

	protected Properties getProperties() {
		return this.properties;
	}

	protected abstract List<ErrorCollector> checkParameters(List<MethodParameter> parameters);

	public abstract void init(CSVReader csvReader) throws Exception;

	public abstract Object[] createBeans(String[] line);
}
