package net.sf.testng.databinding.util.modules;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Properties;

import net.sf.testng.databinding.util.Exceptions;


public class ParameterResolver {
	private final Map<String, Object> parameters = new HashMap<String, Object>();

	public ParameterResolver() {
	}

	public ParameterResolver(final Map<String, Object> parameters) {
		this.parameters.putAll(parameters);
	}

	public ParameterResolver(final Properties properties) {
		for (final Entry<Object, Object> entry : properties.entrySet()) {
			this.parameters.put(entry.getKey().toString(), entry.getValue());
		}
	}

	public ParameterResolver(final String propertiesPath) {
		this(createProperties(propertiesPath));
	}

	private static Properties createProperties(final String propertiesPath) {
		try {
			final Properties properties = new Properties();
			properties.load(ParameterResolver.class.getResourceAsStream(propertiesPath));
			return properties;
		} catch (final Exception e) {
			throw Exceptions.softenIfNecessary(e);
		}
	}

	public ParameterResolver(final File propertiesFile) {
		this(createProperties(propertiesFile));
	}

	private static Properties createProperties(final File propertiesFile) {
		try {
			final Properties properties = new Properties();
			properties.load(new FileInputStream(propertiesFile));
			return properties;
		} catch (final Exception e) {
			throw Exceptions.softenIfNecessary(e);
		}
	}

	public ParameterResolver(final URL propertiesUrl) {
		this(createProperties(propertiesUrl));
	}

	private static Properties createProperties(final URL propertiesUrl) {
		try {
			final Properties properties = new Properties();
			properties.load(propertiesUrl.openStream());
			return properties;
		} catch (final Exception e) {
			throw Exceptions.softenIfNecessary(e);
		}
	}

	public ParameterResolver(final String propertiesPath, final Map<String, Object> addons) {
		this(propertiesPath);
		this.parameters.putAll(addons);
	}

	public ParameterResolver(final File propertiesFile, final Map<String, Object> addons) {
		this(propertiesFile);
		this.parameters.putAll(addons);
	}

	public ParameterResolver(final URL propertiesUrl, final Map<String, Object> addons) {
		this(propertiesUrl);
		this.parameters.putAll(addons);
	}

	public ParameterResolver append(final String key, final Object value) {
		parameters.put(key, value);
		return this;
	}

	public Object get(final String key) {
		if (this.parameters.containsKey(key)) {
			return this.parameters.get(key);
		} else {
			throw new NoSuchElementException(key);
		}
	}

	public <T> T get(final String key, final Class<T> asType) {
		return asType.cast(this.get(key));
	}

	public Object get(final String key, final Object defaultValue) {
		if (this.parameters.containsKey(key)) {
			return this.parameters.get(key);
		} else {
			return defaultValue;
		}
	}

	public <T> T get(final String key, final T defaultValue, final Class<T> asType) {
		return asType.cast(this.get(key, defaultValue));
	}
}
