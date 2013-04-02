package net.sf.testng.databinding.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * This class represents a method parameter. It stores the parameter's {@link Annotation annotations}, {@link Type type}
 * and name.
 * <p>
 * Since the actual names of Java method parameters cannot be retrieved at runtime it's the responsibility of the code
 * using this class to determine the parameter's name by any appropriate means. This class does not care how a parameter's
 * name is actually determined or whether it fits with the name actually given to the parameter a particular instance
 * of this class is thought to represent.
 * <p>
 * Instances of this class are immutable, as method parameters cannot be changed at runtime. This also has the effect of
 * making instances of this class thread-safe.
 * 
 * @author Matthias Rothe
 */
public class MethodParameter {
	private List<? extends Annotation> annotations;
	private Type type;
	private String name;

	/**
	 * Constructs a new instance of this class setting the {@link Annotation annotations}, {@link Type type} and name of
	 * the method parameter this instance represents.
	 * 
	 * @param annotations The annotations of the method parameter
	 * @param type The type of the method parameter
	 * @param name The name of the method parameter
	 */
	public MethodParameter(List<? extends Annotation> annotations, Type type, String name) {
		this.annotations = annotations;
		this.type = type;
		this.name = name;
	}

	/**
	 * Retrieves the instance of the given {@link Annotation annotation} type defined for this method parameter if the
	 * annotation was defined for it. Returns <code>null</code> if no such annotation was defined for this method
	 * parameter.
	 * 
	 * @param annotationType The type of the annotation of which the instance defined for this method parameter is to be
	 * returned
	 * @return The instance of the given annotation type defined for this method parameter, or <code>null</code> if no
	 * annotation of the given type was defined for this method parameter
	 */
	@SuppressWarnings("unchecked")
	public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
		for (Annotation annotation : this.annotations) {
			if (annotation.annotationType() == annotationType) {
				return (T) annotation;
			}
		}

		return null;
	}

	/**
	 * Retrieves all {@link Annotation annotations} defined for this method parameter as an unmodifiable {@link List list}.
	 * 
	 * @return All annotations defined for this method parameter
	 */
	public List<? extends Annotation> getAnnotations() {
		return Collections.unmodifiableList(this.annotations);
	}

	/**
	 * Retrieves the {@link Type type} of this method parameter.
	 * 
	 * @return The type of this method parameter
	 */
	public Type getType() {
		return this.type;
	}

	/**
	 * Retrieves the name of this method parameter.
	 * 
	 * @return The name of this method parameter
	 */
	public String getName() {
		return this.name;
	}
}