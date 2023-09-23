package net.sf.testng.databinding;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.testng.annotations.DataProvider;

import net.sf.extcos.ComponentQuery;
import net.sf.extcos.ComponentScanner;
import net.sf.testng.databinding.core.error.ErrorCollector;
import net.sf.testng.databinding.core.error.MissingPropertiesException;
import net.sf.testng.databinding.core.error.MultipleConfigurationErrorsException;
import net.sf.testng.databinding.core.model.Configuration;
import net.sf.testng.databinding.core.util.Annotations;
import net.sf.testng.databinding.core.util.MethodParametersAndConfigurationConstructorMatcher;
import net.sf.testng.databinding.core.util.Types;
import net.sf.testng.databinding.util.BasePackageLoader;
import net.sf.testng.databinding.util.ConstructorMatcher;
import net.sf.testng.databinding.util.Constructors;
import net.sf.testng.databinding.util.MethodParameter;

/**
 * The GenericDataProvider class is the main entry point for the TestNG Data Binding framework. It
 * provides a static @{@link DataProvider} annotated method ({@link #getDataProvider(Method)}) which
 * returns an {@link Iterator} over the data for the test method invocations.
 * <p>
 * To use the GenericDataProvider annotate your test method with the @{@link DataBinding}
 * annotation and add the {@link TestAnnotationTransformer} class as a listener to your testng.xml
 * file like so:
 * 
 * <pre>
 * &lt;suite&gt;
 *     &lt;listeners&gt;
 *         &lt;listener class-name="net.sf.testng.databinding.TestAnnotationTransformer"/&gt;
 *     &lt;/listeners&gt;
 *     ...
 * &lt;/suite&gt;
 * </pre>
 * 
 * Furthermore the parameters of your test method must be annotated with either @{@link TestInput}
 * or @{@link TestOutput}, so the binding framework can actually decide whether it can provide the
 * data as required. Which types of data are permissible for test input and test output parameters
 * depends on the data source used.
 * <p>
 * Since the various data sources are organized as plug-ins for the TestNG Data Binding
 * framework, make sure you've got the ones you need on your build path or dependencies list
 * alongside the core component.
 * 
 * @author Matthias Rothe
 */
public class GenericDataProvider {
	private static class DataSourcesMap extends HashMap<String, Class<? extends IDataSource>> {
		private static final long serialVersionUID = -1357728940375321662L;
	}

	private static DataSourcesMap dataSources;

	private GenericDataProvider() {
	}

	private synchronized static DataSourcesMap getDataSources() {
		if (dataSources == null) {
			dataSources = new DataSourcesMap();

			final Set<Class<? extends IDataSource>> classes = new HashSet<Class<? extends IDataSource>>();

			final ComponentScanner scanner = new ComponentScanner();
			scanner.getClasses(new ComponentQuery() {
				@Override
				protected void query() {
					select()
						.from(
							BasePackageLoader.loadBasePackages("testng-databinding.base-packages").toArray(
								new String[] {})).andStore(thoseImplementing(IDataSource.class).into(classes))
						.returning(none());
				}
			});

			for (final Class<? extends IDataSource> clazz : classes) {
				if (clazz.isAnnotationPresent(DataSource.class)) {
					final DataSource dataSource = clazz.getAnnotation(DataSource.class);
					dataSources.put(dataSource.name().toLowerCase(), clazz);
				} else {
					dataSources.put(clazz.getSimpleName(), clazz);
				}
			}
		}

		return dataSources;
	}

	/**
	 * This method returns the {@link Iterator} over the test data. It takes the method for which to
	 * provide the data as its argument.
	 * <p>
	 * <strong>Note:</strong> Do not call this method directly. It's only meant to be called by
	 * TestNG. See the {@link GenericDataProvider class description} for how to use the TestNG Data
	 * Binding framework.
	 * 
	 * @param method
	 *            The method for which to provide data
	 * @return An iterator over the test data for the given test method
	 * @throws Exception
	 *             If anything goes wrong during test data iterator retrieval. The most common
	 *             reasons are invalid test method parameter annotations or types, problems with the
	 *             .data.properties file or the actual data source.
	 */
	@DataProvider(name = "DataProvider")
	public static Iterator<Object[]> getDataProvider(final Method method) throws Exception {
		final List<MethodParameter> parameters = createMethodParameters(method);
		checkConfiguration(parameters);

		Class<?> declaringClass = method.getDeclaringClass();
		String configMethod = method.getName() + "Config";
		String dataSource = null;
		Class<?> configClass = null;
		
		if (declaringClass.isAnnotationPresent(DataBinding.class)) {
			DataBinding dataBinding = declaringClass.getAnnotation(DataBinding.class);
			
			if (!"".equals(dataBinding.dataSource())) {
				dataSource = dataBinding.dataSource();
			}
			
			if (!Object.class.equals(dataBinding.configClass())) {
				configClass = dataBinding.configClass();
			}
		}
		
		if (method.isAnnotationPresent(DataBinding.class)) {
			DataBinding dataBinding = method.getAnnotation(DataBinding.class);
			
			if (!"".equals(dataBinding.dataSource())) {
				dataSource = dataBinding.dataSource();
			}
			
			if (!Object.class.equals(dataBinding.configClass())) {
				configClass = dataBinding.configClass();
			}
			
			if (!"".equals(dataBinding.configMethod())) {
				configMethod = dataBinding.configMethod();
			}
		}
		
		List<String> missingKeys = new ArrayList<String>();
		
		if (dataSource == null) {
			missingKeys.add("dataSource");
		}
		
		if (configClass == null) {
			missingKeys.add("configClass");
		}
		
		if (missingKeys.size() > 0) {
			throw new MissingPropertiesException(missingKeys);
		}
		
		return getDataSource(parameters, dataSource, configClass, configMethod);
	}

	private static void checkConfiguration(final List<MethodParameter> parameters) {
		final List<ErrorCollector> errorCollectors = new ArrayList<ErrorCollector>();

		if (parameters.size() == 0) {
			final ErrorCollector errorCollector = new ErrorCollector("no parameters");
			errorCollector.addError("The method has no parameters to feed data into.");
			errorCollectors.add(errorCollector);
		}

		for (final MethodParameter parameter : parameters) {
			final Type type = parameter.getType();
			final ErrorCollector errorCollector = new ErrorCollector(type);
			boolean foundAnnotation = false;

			for (final Annotation annotation : parameter.getAnnotations()) {
				if (annotation instanceof TestInput) {
					foundAnnotation = true;

					if (!Types.isSupportedType(type)) {
						errorCollector.addError("Unsupported type.");
					} else if (Types.requiresName(type) && Annotations.nameNotSet(annotation)) {
						errorCollector.addError("No name set. You need to set the name attribute "
								+ "of the @TestInput annotation of this parameter.");
					}
				} else if (annotation instanceof TestOutput) {
					foundAnnotation = true;

					if (!Types.isSupportedType(type)) {
						errorCollector.addError("Unsupported type.");
					} else if (Types.requiresName(type) && Annotations.nameNotSet(annotation)) {
						errorCollector.addError("No name set. You need to set the name attribute "
								+ "of the @TestOutput annotation of this parameter.");
					}
				}
			}

			if (!foundAnnotation) {
				errorCollector.addError("No data type annotation given. This parameter must "
						+ "either be annotated with @TestInput or @TestOutput.");
			}

			if (errorCollector.hasErrors()) {
				errorCollectors.add(errorCollector);
			}
		}

		if (errorCollectors.size() > 0) {
			throw new MultipleConfigurationErrorsException(errorCollectors);
		}
	}

	private static IDataSource getDataSource(final List<MethodParameter> parameters,
			final String dataSource, final Class<?> configClass, final String configMethod)
			throws Exception {
		final Class<? extends IDataSource> dataSourceClass = getDataSources().get(
			dataSource.toLowerCase());
		
		if (dataSourceClass == null) {
			throw new NoSuchElementException("The data source [" + dataSource
					+ "] couldn't be found.");
		}
		
		final ConstructorMatcher matcher = new MethodParametersAndConfigurationConstructorMatcher();
		final Constructor<IDataSource> constructor = Constructors.getMatchingConstructor(dataSourceClass, matcher);
		return constructor.newInstance(parameters, new Configuration(configClass, configMethod));
	}

	private static List<MethodParameter> createMethodParameters(final Method method) {
		final List<MethodParameter> parameters = new ArrayList<MethodParameter>();
		final Annotation[][] annotationsArray = method.getParameterAnnotations();
		final Type[] parameterTypes = method.getGenericParameterTypes();

		for (int i = 0; i < parameterTypes.length; i++) {
			final List<Annotation> annotations = Arrays.asList(annotationsArray[i]);
			final Type type = parameterTypes[i];
			final String name = resolveName(annotations, type);

			final MethodParameter parameter = new MethodParameter(annotations, type, name);
			parameters.add(parameter);
		}

		return parameters;
	}

	private static String resolveName(final List<Annotation> annotations, final Type type) {
		if (Types.isSingleBeanType(type) || Types.isEnumType(type)) {
			return ((Class<?>) type).getSimpleName();
		} else if (Types.isListOfBeansType(type)) {
			return ((Class<?>) ((ParameterizedType) type).getActualTypeArguments()[0]).getSimpleName();
		} else {
			String name = null;

			for (final Annotation annotation : annotations) {
				if ((name = Annotations.getName(annotation)) != null) {
					break;
				}
			}

			return name;
		}
	}
}
