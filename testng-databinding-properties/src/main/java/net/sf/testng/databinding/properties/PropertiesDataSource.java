package net.sf.testng.databinding.properties;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import net.sf.testng.databinding.AbstractDataSource;
import net.sf.testng.databinding.DataSource;
import net.sf.testng.databinding.IDataSource;
import net.sf.testng.databinding.TestInput;
import net.sf.testng.databinding.TestOutput;
import net.sf.testng.databinding.core.error.ErrorCollector;
import net.sf.testng.databinding.core.error.MultipleConfigurationErrorsException;
import net.sf.testng.databinding.core.error.MultipleSourceErrorsException;
import net.sf.testng.databinding.core.model.Configuration;
import net.sf.testng.databinding.core.util.DataSourceConfigurationLoader;
import net.sf.testng.databinding.core.util.Types;
import net.sf.testng.databinding.util.Exceptions;
import net.sf.testng.databinding.util.MethodParameter;

/**
 * <p>
 * This {@link IDataSource data source} binds test data contained in {@link Properties} files to
 * test method parameters. It supports an arbitrary number of {@link TestInput test input} and
 * {@link TestOutput test output} parameters and supports all generally supported parameter types for
 * input and output parameters, except {@link List Lists}. These types are all primitive types defined
 * in the Java Language Specification and their wrappers, {@link String Strings}, {@link Enum Enums},
 * and Java Beans. Properties data sources always contain data for just one test method invocation.
 * </p>
 * <h3>Specifications</h3>
 * <h4>Properties Files</h4>
 * <p>
 * The properties files must follow the standards defined by the {@link Properties} class. The property keys
 * have no name spaces, but must have the prefixes as defined in the data properties file, or the defaults
 * if they are not defined. Following the prefix each key must be the name as defined by
 * {@link TestInput#name() &#64;TestInput(name = "")} or {@link TestOutput#name() &#64;TestOutput(name = "")}
 * for any primitive value, primitive wrapper or {@link String}. For {@link Enum enums} the name returned from
 * {@link Class#getSimpleName() EnumType.class.getSimpleName()} needs to be used. All names are case-insensitive.
 * </p>
 * <h4>Java Beans</h4>
 * <p>
 * Java Beans that data is to be bound to need to have a standard constructor taking no arguments. The same types that are
 * supported as test method parameter types are also supported as Java Bean properties types. Nested Java Beans are
 * supported up to any nesting level (nesting is only limited by the max allowable method stack size of the used JVM which
 * generally allows thousands of levels). Primitive Java Bean properties for which no values can be found are set to the
 * default value of the type. All Java Bean type properties are initialized and all properties of unsupported types are
 * set to <code>null</code>.
 * </p>
 * <h3>Example</h3>
 * <p>
 * To make issues clearer, here is an example of this data source in use. It loads login credentials from a properties
 * file. Getters and setters are omitted in Java Beans for brevity in this example. They are however crucial in actual
 * Java Beans, so you have to include them in any Java Bean you actually want to bind data to.
 * </p>
 * <h4>Test Method</h4>
 * <pre>
 * &#64;DataBinding(propertiesPrefix = "login")
 * public void loadLoginCredentials(&#64;TestInput final LoginCredentials loginCredentials) {
 *     this.loginCredentials = loginCredentials
 * }
 * </pre>
 * <h4>Java Classes</h4>
 * <h5>Java Bean: LoginCredentials</h5>
 * <pre>
 * public class LoginCredentials {
 *     private String userName;
 *     private String password;
 *     
 *     /* Getters and setters omitted for brevity &#42;/
 * }
 * </pre>
 * <h4>Data Properties File</h4>
 * <pre>
 * login.dataSource=properties
 * login.url=/data/loginCredentials.properties
 * </pre>
 * <h4>Properties Data Source</h4>
 * <pre>
 * in_userName=admin
 * in_password=admin
 * </pre>
 * 
 * @author Matthias Rothe
 */
@DataSource(name = "properties")
public class PropertiesDataSource extends AbstractDataSource {
	private final Properties data;
	private final String inputValuePrefix;
	private final String outputValuePrefix;
	private final List<MethodParameter> parameters;
	private final List<MethodParameter> inputParameters = new ArrayList<MethodParameter>();
	private final List<MethodParameter> outputParameters = new ArrayList<MethodParameter>();
	private boolean returnedData = false;

	/**
	 * Constructs a new instance of this class, setting the {@link MethodParameter test method parameters} to load the
	 * data for and the {@link Configuration}.
	 * 
	 * @param parameters The test method parameters for which data is to be loaded
	 * @param configuration The configuration of the configuration class and method
	 * @throws Exception If anything goes wrong during the creation of this instance
	 */
	public PropertiesDataSource(final List<MethodParameter> parameters,
			final Configuration configuration) throws Exception {
		PropertiesDataSourceConfiguration dataSourceConfiguration =
				DataSourceConfigurationLoader.loadDataSourceConfiguration(configuration,
						PropertiesDataSourceConfiguration.class);
		
		inputValuePrefix = dataSourceConfiguration.getInputValuePrefix();
		outputValuePrefix = dataSourceConfiguration.getOutputValuePrefix();

		checkParameters(parameters);
		this.parameters = parameters;

		for (final MethodParameter parameter : parameters) {
			if (parameter.getAnnotation(TestInput.class) != null) {
				this.inputParameters.add(parameter);
			} else if (parameter.getAnnotation(TestOutput.class) != null) {
				this.outputParameters.add(parameter);
			}
		}

		data = readData(dataSourceConfiguration);
	}

	private void checkParameters(final List<MethodParameter> parameters) {
		final List<ErrorCollector> errorCollectors = new ArrayList<ErrorCollector>();

		for (final MethodParameter parameter : parameters) {
			final Type type = parameter.getType();
			final ErrorCollector errorCollector = new ErrorCollector(type);

			if (Types.isListOfObjectsType(type)) {
				errorCollector.addError("Type " + type + " is not supported by this data source: " + this.getClass());
			}

			if (errorCollector.hasErrors()) {
				errorCollectors.add(errorCollector);
			}
		}

		if (errorCollectors.size() > 0) {
			throw new MultipleConfigurationErrorsException(errorCollectors);
		}
	}

	private Properties readData(final PropertiesDataSourceConfiguration configuration)
			throws Exception {
		final Properties data = new Properties();

		final URL url = configuration.getURL();
		data.load(url.openStream());

		return normalizeKeys(data);
	}

	private Properties normalizeKeys(final Properties data) {
		final Properties nkData = new Properties();

		for (final Entry<Object, Object> entry : data.entrySet()) {
			nkData.setProperty(entry.getKey().toString().toLowerCase(), entry.getValue().toString());
		}

		return nkData;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasNext() {
		if (returnedData) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * {@inheritDoc}
	 */
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
			final Object object = clazz.getConstructor().newInstance();
			final BeanInfo info = Introspector.getBeanInfo(clazz);

			for (final PropertyDescriptor descriptor : info.getPropertyDescriptors()) {
				if (this.isWriteable(descriptor)) {
					descriptor.getWriteMethod().invoke(object,
						createObject(createMethodParameterForProperty(descriptor), prefix));
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