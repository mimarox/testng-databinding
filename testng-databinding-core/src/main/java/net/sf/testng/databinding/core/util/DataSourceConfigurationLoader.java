package net.sf.testng.databinding.core.util;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

import net.sf.extcos.ComponentQuery;
import net.sf.extcos.ComponentScanner;
import net.sf.testng.databinding.DataSourceConfiguration;
import net.sf.testng.databinding.core.model.Configuration;

public class DataSourceConfigurationLoader {
	private DataSourceConfigurationLoader() {
	}

	public static <T> T loadDataSourceConfiguration(final Configuration configuration,
			Class<? extends T> configurationInterfaceClass) {
		Thread currentThread = Thread.currentThread();
		
		ClassLoader classLoader = currentThread.getContextClassLoader();
		currentThread.setContextClassLoader(configuration.getClassLoader());
		
		ComponentScanner scanner = new ComponentScanner();

		Set<Class<?>> candidateClasses = scanner.getClasses(new ComponentQuery() {

			@Override
			protected void query() {
				select().from(configuration.getBasePackages()).returning(
						allBeing(and(implementorOf(configurationInterfaceClass),
								annotatedWith(DataSourceConfiguration.class)
						))
				);
			}
		});

		currentThread.setContextClassLoader(classLoader);
		
		Class<?> configObjectClass = null;
		
		for (Class<?> candidateClass : candidateClasses) {
			DataSourceConfiguration dataSourceConfiguration =
					candidateClass.getAnnotation(DataSourceConfiguration.class);
			
			if (Objects.equals(dataSourceConfiguration.name(), configuration.getConfigName())) {
				configObjectClass = candidateClass;
				break;
			}
		}
		
		if (configObjectClass != null) {
			try {
				Object configObject = configObjectClass.getConstructor().newInstance();
				return configurationInterfaceClass.cast(configObject);
			} catch (InvocationTargetException | InstantiationException | IllegalAccessException
					| IllegalArgumentException | NoSuchMethodException | SecurityException |
					NullPointerException e) {
				throw new NoSuchElementException("Couldn't find or invoke default no-args "
						+ "constructor on class " + configObjectClass.getCanonicalName(), e);
			}
		} else {
			throw new NoSuchElementException("A class implementing "
					+ configurationInterfaceClass.getCanonicalName() + " annotated with "
					+ "@DataSourceConfiguration(name = \"" + configuration.getConfigName()
					+ "\") could not be found in base packages "
					+ Arrays.toString(configuration.getBasePackages())
					+ " or any subpackages.");
		}
	}
}
