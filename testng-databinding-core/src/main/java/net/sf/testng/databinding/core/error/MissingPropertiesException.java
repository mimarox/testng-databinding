package net.sf.testng.databinding.core.error;

import java.util.Arrays;
import java.util.List;

import net.sf.testng.databinding.IDataSource;

/**
 * This exception is thrown if required properties are missing in the data source specification for
 * a test method given in a .data.properties file.
 * <p>
 * The only property always required is <code>dataSource</code> which specifies the kind of data
 * source to be used. Each {@link IDataSource data source} may define additional required properties.
 * 
 * @author Matthias Rothe
 */
public class MissingPropertiesException extends RuntimeException {
	private static final long serialVersionUID = 4713222350085311607L;
	private final List<String> missingKeys;

	/**
	 * Creates a new {@link MissingPropertiesException} taking an arbitrary number of missing keys
	 * as its argument.
	 * 
	 * @param missingKeys
	 *            The keys of missing properties
	 */
	public MissingPropertiesException(final String... missingKeys) {
		this.missingKeys = Arrays.asList(missingKeys);
	}

	/**
	 * Creates a new {@link MissingPropertiesException} taking missing keys as a {@link List}&lt;{@link String}&gt;
	 * as its argument.
	 * 
	 * @param missingKeys
	 *            The keys of missing properties as a list
	 */
	public MissingPropertiesException(final List<String> missingKeys) {
		this.missingKeys = missingKeys;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getMessage() {
		return this.missingKeys.toString();
	}
}