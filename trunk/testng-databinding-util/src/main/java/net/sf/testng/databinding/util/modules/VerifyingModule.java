package net.sf.testng.databinding.util.modules;

public interface VerifyingModule<T> extends Module {
	void setActualSource(T source);

	void setExpectedValue(Object value);
}
