package net.sf.testng.databinding.core.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import net.sf.testng.databinding.core.model.Configuration;
import net.sf.testng.databinding.util.Exceptions;

public class DataSourceConfigurationLoader {
	private DataSourceConfigurationLoader() {
	}

	public static <T> T loadDataSourceConfiguration(final Configuration configuration,
			Class<? extends T> configurationInterfaceClass) {
		try {
			Class<?> configObjectClass = configuration.getConfigClass();
			Method method = configObjectClass.getDeclaredMethod(configuration.getConfigMethod(),
					(Class<?>[]) null);
			int modifiers = method.getModifiers();
			
			if ((modifiers & Modifier.PUBLIC) > 0 && (modifiers & Modifier.STATIC) > 0
					&& method.getReturnType().equals(configurationInterfaceClass)) {
				return configurationInterfaceClass.cast(method.invoke(null, (Object[]) null));
			} else {
				throw new NoSuchMethodException("The specified configuration method is not "
						+ "public static or returns the wrong type.");
			}
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			throw Exceptions.softenIfNecessary(e);
		}		
	}
}
