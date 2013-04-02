package net.sf.testng.databinding;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This interface serves as the base interface for all data sources. It defines no methods, but binds the type
 * parameter of the {@link Iterator} interface to {@link Object Object[]}. It must be implemented by all classes giving
 * access to a certain kind of test data source, like a properties, CSV or XML file.
 * <p>
 * For information on how to use a particular data source, please see the classes implementing this interface. For
 * general information see the {@link GenericDataProvider} class.
 * 
 * @author Matthias Rothe
 * @see GenericDataProvider
 * @see DataBinding
 * @see TestInput
 * @see TestOutput
 */
public interface IDataSource extends Iterator<Object[]> {

	/**
	 * Checks whether this data source has another set of test data.
	 * 
	 * @return <code>true</code>, if and only if this data source has another set of test data, <code>false</code>
	 * otherwise
	 */
	@Override
	boolean hasNext();

	/**
	 * Returns the next set of test data as an array of {@link Object Objects}. If no next set of test data is
	 * available this method throws a {@link NoSuchElementException}.
	 * 
	 * @return The next set of test data.
	 * @throws NoSuchElementException If no next set of test data exists
	 */
	@Override
	Object[] next();
}