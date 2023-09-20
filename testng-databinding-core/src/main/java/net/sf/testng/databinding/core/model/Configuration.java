package net.sf.testng.databinding.core.model;

public class Configuration {
	private final String configName;
	private final String[] basePackages;
	private final ClassLoader classLoader;
	
	public Configuration(final String configName, final String[] basePackages, ClassLoader classLoader) {
		this.configName = configName;
		this.basePackages = basePackages;
		this.classLoader = classLoader;
	}

	/**
	 * @return the configName
	 */
	public String getConfigName() {
		return configName;
	}

	/**
	 * @return the basePackages
	 */
	public String[] getBasePackages() {
		return basePackages;
	}

	/**
	 * @return the classLoader
	 */
	public ClassLoader getClassLoader() {
		return classLoader;
	}
}
