package net.sf.testng.databinding.error;

/**
 * This exception is thrown if a requested data provider strategy is not supported within the
 * current runtime environment.
 * <p>
 * The data provider strategies supported within any given runtime environment depend on the TestNG
 * DataBinding plugins installed within that environment.
 * 
 * @author Matthias Rothe
 */
public class UnsupportedDataProviderStrategyException extends RuntimeException {
	private static final long serialVersionUID = -3055730182829644244L;
	private final String strategy;
	private final boolean forResultStrategy;

	/**
	 * Creates a new {@link UnsupportedDataProviderStrategyException} taking the name of the
	 * unsupported data provider strategy as its argument.
	 * 
	 * @param strategy
	 *            The name of the unsupported data provider strategy
	 */
	public UnsupportedDataProviderStrategyException(final String strategy) {
		this(strategy, false);
	}

	/**
	 * Creates a new {@link UnsupportedDataProviderStrategyException} taking the name of the
	 * unsupported data provider strategy and a flag indicating whether the strategy had been
	 * requested as a <code>result.strategy</code> as its argument.
	 * 
	 * @param strategy
	 *            The name of the unsupported data provider strategy
	 * @param forResultStrategy
	 *            A flag indicating whether the unsupported strategy had been requested as a
	 *            <code>result.strategy</code>
	 */
	public UnsupportedDataProviderStrategyException(final String strategy, final boolean forResultStrategy) {
		this.strategy = strategy;
		this.forResultStrategy = forResultStrategy;
	}

	@Override
	public String getMessage() {
		return "Unsupported " + (this.forResultStrategy ? "result." : "") + "strategy: " + this.strategy;
	}
}