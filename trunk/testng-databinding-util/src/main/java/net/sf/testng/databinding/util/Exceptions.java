package net.sf.testng.databinding.util;

public class Exceptions {
	private Exceptions() {
	}

	public static RuntimeException softenIfNecessary(Exception e) {
		if (RuntimeException.class.isAssignableFrom(e.getClass())) {
			return (RuntimeException) e;
		} else {
			return new RuntimeException(e);
		}
	}
}