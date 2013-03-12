package net.sf.testng.databinding.properties;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import net.sf.testng.databinding.AbstractDataProviderStrategy;
import net.sf.testng.databinding.DataProviderStrategyNames;
import net.sf.testng.databinding.TestInput;
import net.sf.testng.databinding.TestOutput;
import net.sf.testng.databinding.error.ErrorCollector;
import net.sf.testng.databinding.error.MissingPropertiesException;
import net.sf.testng.databinding.error.MultipleConfigurationErrorsException;
import net.sf.testng.databinding.error.MultipleSourceErrorsException;
import net.sf.testng.databinding.util.Exceptions;
import net.sf.testng.databinding.util.MethodParameter;
import net.sf.testng.databinding.util.Types;


@DataProviderStrategyNames("properties")
public class PropertiesDataProviderStrategy extends AbstractDataProviderStrategy {
	private final Properties data;
	private final String inputValuePrefix;
	private final String outputValuePrefix;
	private final List<MethodParameter> parameters;
	private final List<MethodParameter> inputParameters = new ArrayList<MethodParameter>();
	private final List<MethodParameter> outputParameters = new ArrayList<MethodParameter>();
	private boolean returnedData = false;

	public PropertiesDataProviderStrategy(final List<MethodParameter> parameters, final Properties properties) throws Exception {
		checkProperties(properties);
		inputValuePrefix = properties.getProperty("inputValuePrefix", "in_");
		outputValuePrefix = properties.getProperty("outputValuePrefix", "out_");

		checkParameters(parameters);
		this.parameters = parameters;

		for (final MethodParameter parameter : parameters) {
			if (parameter.getAnnotation(TestInput.class) != null) {
				this.inputParameters.add(parameter);
			} else if (parameter.getAnnotation(TestOutput.class) != null) {
				this.outputParameters.add(parameter);
			}
		}

		data = readData(properties);
		// checkKeys(data.keySet());
	}

	private void checkProperties(final Properties properties) {
		final List<String> missingKeys = new ArrayList<String>();

		if (!properties.containsKey("url")) {
			missingKeys.add("url");
		}

		if (missingKeys.size() > 0) {
			throw new MissingPropertiesException(missingKeys);
		}
	}

	private void checkParameters(final List<MethodParameter> parameters) {
		final List<ErrorCollector> errorCollectors = new ArrayList<ErrorCollector>();

		for (final MethodParameter parameter : parameters) {
			final Type type = parameter.getType();
			final ErrorCollector errorCollector = new ErrorCollector(type);

			if (Types.isListOfObjectsType(type)) {
				errorCollector.addError("Type " + type + " is not supported by this data provider strategy: " + this.getClass());
			}

			if (errorCollector.hasErrors()) {
				errorCollectors.add(errorCollector);
			}
		}

		if (errorCollectors.size() > 0) {
			throw new MultipleConfigurationErrorsException(errorCollectors);
		}
	}

	private Properties readData(final Properties properties) throws Exception {
		final Properties data = new Properties();

		final URL url = resolveURL(properties.getProperty("url"));
		data.load(url.openStream());

		return normalizeKeys(data);
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

	private Properties normalizeKeys(final Properties data) {
		final Properties nkData = new Properties();

		for (final Entry<Object, Object> entry : data.entrySet()) {
			nkData.setProperty(entry.getKey().toString().toLowerCase(), entry.getValue().toString());
		}

		return nkData;
	}

	@Override
	public boolean hasNext() {
		if (returnedData) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public Object[] next() {
		returnedData = true;

		final List<Object> objects = new ArrayList<Object>();
		final List<ErrorCollector> errorCollectors = new ArrayList<ErrorCollector>();

		for (final MethodParameter parameter : parameters) {
			Object object = null;

			if (inputParameters.contains(parameter)) {
				object = createObject(parameter, inputValuePrefix);
			} else if (outputParameters.contains(parameter)) {
				object = createObject(parameter, outputValuePrefix);
			}

			if (object == null) {
				final ErrorCollector errorCollector = new ErrorCollector(parameter.getType(), parameter.getName());
				errorCollector.addError("no data found for this parameter");
				errorCollectors.add(errorCollector);
			} else {
				objects.add(object);
			}
		}

		if (errorCollectors.size() == 0) {
			return objects.toArray();
		} else {
			throw new MultipleSourceErrorsException(errorCollectors);
		}
	}

	private Object createObject(final MethodParameter parameter, final String prefix) {
		final Type type = parameter.getType();

		if (Types.isPrimitiveType(type)) {
			return createPrimitive(parameter, prefix);
		} else if (Types.isEnumType(type)) {
			return createEnum(parameter, prefix);
		} else if (Types.isSingleBeanType(type)) {
			return createSingleBean(parameter, prefix);
		} else {
			return null;
		}
	}

	private Object createPrimitive(final MethodParameter parameter, final String prefix) {
		final String value = data.getProperty(createKey(prefix, parameter.getName()));
		final Type type = parameter.getType();

		try {
			if (value == null) {
				return defaultValue(type);
			} else if (type.equals(String.class)) {
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
		} catch (final NumberFormatException e) {
			final ErrorCollector errorCollector = new ErrorCollector(parameter.getType(), parameter.getName());
			errorCollector.addError("the value [" + value + "] found in the source is invalid for this type");
			throw new MultipleSourceErrorsException(Arrays.asList(errorCollector));
		}

		// can't happen
		throw new RuntimeException();
	}

	private Object defaultValue(final Type type) {
		if (type == int.class) {
			return 0;
		} else if (type == long.class) {
			return 0l;
		} else if (type == float.class) {
			return 0f;
		} else if (type == double.class) {
			return 0d;
		} else if (type == boolean.class) {
			return false;
		} else {
			return null;
		}
	}

	private Object createEnum(final MethodParameter parameter, final String prefix) {
		final String enumName = data.getProperty(createKey(prefix, parameter.getName()));

		if (enumName == null) {
			return null;
		}

		final Class<?> enumClass = (Class<?>) parameter.getType();
		final Field[] fields = enumClass.getFields();

		for (final Field field : fields) {
			try {
				if (field.getName().equals(enumName)) {
					return field.get(null);
				}
			} catch (final Exception ignored) {
				// shouldn't happen
			}
		}

		final ErrorCollector errorCollector = new ErrorCollector(parameter.getType(), parameter.getName());
		errorCollector.addError("the value [" + enumName + "] found in the source isn't a member of this enum type");
		throw new MultipleSourceErrorsException(Arrays.asList(errorCollector));
	}

	private String createKey(final String prefix, final String name) {
		return (prefix + name).toLowerCase();
	}

	private Object createSingleBean(final MethodParameter parameter, final String prefix) {
		try {
			final Class<?> clazz = (Class<?>) parameter.getType();
			final Object object = clazz.newInstance();
			final BeanInfo info = Introspector.getBeanInfo(clazz);

			for (final PropertyDescriptor descriptor : info.getPropertyDescriptors()) {
				if (this.isWriteable(descriptor)) {
					descriptor.getWriteMethod()
							.invoke(object, createObject(createMethodParameterForProperty(descriptor), prefix));
				}
			}

			return object;
		} catch (final Exception e) {
			throw Exceptions.softenIfNecessary(e);
		}
	}

	private boolean isWriteable(final PropertyDescriptor descriptor) {
		return descriptor.getWriteMethod() != null;
	}

	private MethodParameter createMethodParameterForProperty(final PropertyDescriptor descriptor) {
		final Method writeMethod = descriptor.getWriteMethod();
		final Annotation[][] annotations = writeMethod.getParameterAnnotations();
		final Type[] parameterTypes = writeMethod.getGenericParameterTypes();
		return new MethodParameter(Arrays.asList(annotations[0]), parameterTypes[0], descriptor.getName());
	}
}