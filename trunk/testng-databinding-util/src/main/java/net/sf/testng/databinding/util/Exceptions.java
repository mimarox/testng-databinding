package net.sf.testng.databinding.util;

/**
 * This class contains utility methods pertaining to {@link Exception exceptions}.
 * 
 * @author Matthias Rothe
 */
public class Exceptions {
	private Exceptions() {
	}

	/**
	 * Wraps the given {@link Exception exception} in a {@link RuntimeException} if it isn't one already and returns
	 * the wrapping RuntimeException instance. In case the given exception is already a RuntimeException it is returned
	 * without any changes. This wrapping is referred to as softening, as the resulting RuntimeException can then be
	 * thrown without the need to declare it being thrown.
	 * 
	 * @param e The exception to soften if necessary
	 * @return The RuntimeException instance wrapping the given exception if softened, or the given exception as it was,
	 * when softening was not necessary
	 */
	public static RuntimeException softenIfNecessary(Exception e) {
		if (RuntimeException.class.isAssignableFrom(e.getClass())) {
			return (RuntimeException) e;
		} else {
			return new RuntimeException(e);
		}
	}
}