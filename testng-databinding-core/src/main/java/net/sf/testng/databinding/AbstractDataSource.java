package net.sf.testng.databinding;

/**
 * This class is the base implementation of the {@link IDataSource} interface. It must be
 * implemented by all classes giving access to a certain kind of test data source, like a
 * properties, CSV or XML file.
 * 
 * @author Matthias Rothe
 */
public abstract class AbstractDataSource implements IDataSource {

	/**
	 * @throws UnsupportedOperationException
	 *             Test data cannot be removed therefore trying to do so results in this exception
	 *             being thrown.
	 */
	@Override
	public final void remove() {
		throw new UnsupportedOperationException();
	}
}