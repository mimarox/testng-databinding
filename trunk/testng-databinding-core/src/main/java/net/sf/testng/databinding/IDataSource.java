package net.sf.testng.databinding;

import java.util.Iterator;

/**
 * This interface defines no methods, but binds the type parameter of the {@link Iterator} interface
 * to {@link Object}[]. It must be implemented by all classes giving access to a certain kind of
 * test data source, like a properties, CSV or XML file.
 * 
 * @author Matthias Rothe
 */
public interface IDataSource extends Iterator<Object[]> {
}