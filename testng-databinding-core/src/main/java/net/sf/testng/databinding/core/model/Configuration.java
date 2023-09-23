package net.sf.testng.databinding.core.model;

public class Configuration {
	private final Class<?> configClass;
	private final String configMethod;
	
	public Configuration(final Class<?> configClass, final String configMethod) {
		this.configClass = configClass;
		this.configMethod = configMethod;
	}

	/**
	 * @return the configClass
	 */
	public Class<?> getConfigClass() {
		return configClass;
	}

	/**
	 * @return the configMethod
	 */
	public String getConfigMethod() {
		return configMethod;
	}
}
