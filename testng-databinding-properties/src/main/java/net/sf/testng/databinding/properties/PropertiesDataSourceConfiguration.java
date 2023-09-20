package net.sf.testng.databinding.properties;

import java.net.URL;

public interface PropertiesDataSourceConfiguration {

	/**
	 * The locator of the actual data source file.
	 * 
	 * @return the url
	 */
	URL getURL();

	/**
	 * The prefix of keys for test input data.
	 * <p>
	 * May be any {@link String}, can also be empty
	 * <p>
	 * Defaults to in_
	 * 
	 * @return
	 */
	default String getInputValuePrefix() {
		return "in_";
	}

	/**
	 * The prefix of keys for test output data.
	 * <p>
	 * May be any {@link String}, can also be empty
	 * <p>
	 * Defaults to out_
	 * 
	 * @return
	 */
	default String getOutputValuePrefix() {
		return "out_";
	}
}
