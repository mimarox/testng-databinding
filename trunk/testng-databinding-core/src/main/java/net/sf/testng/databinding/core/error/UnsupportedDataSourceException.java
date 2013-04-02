package net.sf.testng.databinding.core.error;

/**
 * This exception is thrown if a requested data source is not supported within the current runtime
 * environment.
 * <p>
 * The data sources supported within any given runtime environment depend on the TestNG
 * Data Binding plugins installed within that environment.
 * 
 * @author Matthias Rothe
 */
public class UnsupportedDataSourceException extends RuntimeException {
	private static final long serialVersionUID = -3055730182829644244L;
	private final String dataSource;
	private final boolean forResultDataSource;

	/**
	 * Creates a new {@link UnsupportedDataSourceException} taking the name of the
	 * unsupported data source as its argument.
	 * 
	 * @param dataSource
	 *            The name of the unsupported data source
	 */
	public UnsupportedDataSourceException(final String dataSource) {
		this(dataSource, false);
	}

	/**
	 * Creates a new {@link UnsupportedDataSourceException} taking the name of the
	 * unsupported data source and a flag indicating whether the data source had been
	 * requested as a <code>result.dataSource</code> as its argument.
	 * 
	 * @param dataSource
	 *            The name of the unsupported data source
	 * @param forResultDataSource
	 *            A flag indicating whether the unsupported data source had been requested as a
	 *            <code>result.dataSource</code>
	 */
	public UnsupportedDataSourceException(final String dataSource, final boolean forResultDataSource) {
		this.dataSource = dataSource;
		this.forResultDataSource = forResultDataSource;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getMessage() {
		return "Unsupported " + (this.forResultDataSource ? "result." : "") + "dataSource: " + this.dataSource;
	}
}