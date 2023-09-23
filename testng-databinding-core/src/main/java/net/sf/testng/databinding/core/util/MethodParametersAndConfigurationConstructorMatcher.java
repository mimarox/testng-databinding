package net.sf.testng.databinding.core.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import net.sf.testng.databinding.core.model.Configuration;
import net.sf.testng.databinding.util.ConstructorMatcher;
import net.sf.testng.databinding.util.MethodParameter;

/**
 * This class implements a {@link ConstructorMatcher} matching constructors taking 2 parameters:
 * <code>{@link List}&lt;{@link MethodParameter}&gt;</code> and <code>{@link Configuration}</code>, in
 * that order.
 * 
 * @author Matthias Rothe
 */
public class MethodParametersAndConfigurationConstructorMatcher implements ConstructorMatcher {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean matches(final Type[] parameterTypes) {
		// expected parameter types: List<MethodParameter>, Properties
		if (parameterTypes.length == 2 && parameterTypes[0] instanceof ParameterizedType
				&& parameterTypes[1] instanceof Class<?>) {

			final ParameterizedType firstParam = (ParameterizedType) parameterTypes[0];
			final Type[] typeArguments = firstParam.getActualTypeArguments();

			if (firstParam.getRawType() == List.class && typeArguments.length == 1
					&& typeArguments[0] == MethodParameter.class
					&& parameterTypes[1] == Configuration.class) {
				return true;
			}
		}
		return false;
	}
}