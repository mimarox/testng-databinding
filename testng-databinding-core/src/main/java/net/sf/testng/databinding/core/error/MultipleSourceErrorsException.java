package net.sf.testng.databinding.core.error;

import java.util.List;

/**
 * This exception is thrown if there are any problems with the data source for the test method.
 * <p>
 * Depending on the data source these may be missing data source access properties or missing data
 * within the data source.
 * 
 * @author Matthias Rothe
 */
public class MultipleSourceErrorsException extends RuntimeException {
	private static final long serialVersionUID = -4155756999259747645L;
	private final List<ErrorCollector> errorCollectors;

	/**
	 * Creates a new {@link MultipleSourceErrorsException} taking a {@link List} of
	 * {@link ErrorCollector}s as its argument. This list should contain at least one
	 * {@link ErrorCollector} object.
	 * 
	 * @param errorCollectors
	 *            The error collectors containing all detected data source errors
	 */
	public MultipleSourceErrorsException(final List<ErrorCollector> errorCollectors) {
		this.errorCollectors = errorCollectors;
	}

	@Override
	public String getMessage() {
		return "Source Errors: " + this.errorCollectors;
	}
}