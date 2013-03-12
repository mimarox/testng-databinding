package net.sf.testng.databinding.error;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class allows the collection of errors during the process of retrieving the test data for the
 * test method parameters. These errors are then reported together with either a
 * {@link MultipleConfigurationErrorsException} or a {@link MultipleSourceErrorsException} being
 * thrown.
 * 
 * @author Matthias Rothe
 */
public class ErrorCollector {
	private final Type targetType;
	private final String targetName;
	private final List<String> errors = new ArrayList<String>();

	/**
	 * Creates a new {@link ErrorCollector} with only a target name. The target is the test method
	 * parameter for which the errors are to be collected.
	 * 
	 * @param targetName
	 *            The name of the test method parameter
	 */
	public ErrorCollector(final String targetName) {
		this(null, targetName);
	}

	/**
	 * Creates a new {@link ErrorCollector} with only a target type. The target is the test method
	 * parameter for which the errors are to be collected.
	 * 
	 * @param targetType
	 *            The type of the test method parameter
	 */
	public ErrorCollector(final Type targetType) {
		this(targetType, null);
	}

	/**
	 * Creates a new {@link ErrorCollector} with a target type and target name. The target is the
	 * test method parameter for which the errors are to be collected.
	 * 
	 * @param targetType
	 *            The type of the test method parameter
	 * @param targetName
	 *            The name of the test method parameter
	 */
	public ErrorCollector(final Type targetType, final String targetName) {
		this.targetType = targetType;
		this.targetName = targetName;
	}

	/**
	 * Adds an error message.
	 * 
	 * @param error
	 *            The error message to add
	 */
	public void addError(final String error) {
		this.errors.add(error);
	}

	/**
	 * @return The target type as given to the constructor or null if none was set.
	 */
	public Type getTargetType() {
		return this.targetType;
	}

	/**
	 * @return The target name as given to the constructor or null if none was set.
	 */
	public String getTargetName() {
		return this.targetName;
	}

	/**
	 * @return The list of all errors added to this collector instance
	 */
	public List<String> getErrors() {
		return Collections.unmodifiableList(this.errors);
	}

	/**
	 * Checks and returns whether any errors have been added.
	 * 
	 * @return true, if and only if at least one error has been added, otherwise false
	 */
	public boolean hasErrors() {
		return this.errors.size() > 0;
	}

	/**
	 * @return The textual representation of this error collector
	 */
	@Override
	public String toString() {
		String string = "Errors";

		if (this.targetType == null && this.targetName != null) {
			string += " for " + this.targetName;
		} else if (this.targetType != null) {
			string += " for type: " + this.targetType;

			if (this.targetName != null) {
				string += "(named '" + this.targetName + "')";
			}
		}

		return string + ": " + this.errors;
	}
}
