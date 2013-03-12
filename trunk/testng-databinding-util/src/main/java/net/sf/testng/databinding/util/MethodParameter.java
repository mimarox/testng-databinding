package net.sf.testng.databinding.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class MethodParameter {
	private List<? extends Annotation> annotations;
	private Type type;
	private String name;

	public MethodParameter(List<? extends Annotation> annotations, Type type, String name) {
		this.annotations = annotations;
		this.type = type;
		this.name = name;
	}

	@SuppressWarnings("unchecked")
	public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
		for (Annotation annotation : this.annotations) {
			if (annotation.annotationType() == annotationType) {
				return (T) annotation;
			}
		}

		return null;
	}

	public List<? extends Annotation> getAnnotations() {
		return Collections.unmodifiableList(this.annotations);
	}

	public Type getType() {
		return this.type;
	}

	public String getName() {
		return this.name;
	}
}
