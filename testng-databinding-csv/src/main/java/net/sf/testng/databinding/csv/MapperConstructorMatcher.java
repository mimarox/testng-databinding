package net.sf.testng.databinding.csv;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import net.sf.testng.databinding.util.ConstructorMatcher;
import net.sf.testng.databinding.util.MethodParameter;

public class MapperConstructorMatcher implements ConstructorMatcher {

	@Override
	public boolean matches(Type[] parameterTypes) {
		// expected parameter types: List<MethodParameter>, CsvDataSourceConfiguration
		if (parameterTypes.length == 2 && parameterTypes[0] instanceof ParameterizedType
				&& parameterTypes[1] instanceof Class<?>) {

			final ParameterizedType firstParam = (ParameterizedType) parameterTypes[0];
			final Type[] typeArguments = firstParam.getActualTypeArguments();

			if (firstParam.getRawType() == List.class && typeArguments.length == 1
					&& typeArguments[0] == MethodParameter.class
					&& parameterTypes[1] == CsvDataSourceConfiguration.class) {
				return true;
			}
		}
		return false;
	}
}
