package net.sf.testng.databinding.error;

import java.util.List;

/**
 * This exception is thrown if there are any problems with the parameters of the test method.
 * 
 * @author Matthias Rothe
 */
public class MultipleConfigurationErrorsException extends RuntimeException {
	private static final long serialVersionUID = -7478279927061910984L;
	private final List<ErrorCollector> errorCollectors;

	/**
	 * Creates a new {@link MultipleConfigurationErrorsException} taking a {@link List} of
	 * {@link ErrorCollector}s as its argument. This list should contain at least one
	 * {@link ErrorCollector} object.
	 * 
	 * @param errorCollectors
	 *            The error collectors containing all detected configuration errors
	 */
	public MultipleConfigurationErrorsException(final List<ErrorCollector> errorCollectors) {
		this.errorCollectors = errorCollectors;
	}

	@Override
	public String getMessage() {
		return "Configuration Errors: " + this.errorCollectors;
	}
}
