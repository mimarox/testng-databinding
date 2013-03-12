package net.sf.testng.databinding.xml.beans;

public class InnerTestBean {
	private String testValue;

	public InnerTestBean() {
	}

	public InnerTestBean(final String testValue) {
		setTestValue(testValue);
	}

	public String getTestValue() {
		return testValue;
	}

	public void setTestValue(final String testValue) {
		this.testValue = testValue;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((testValue == null) ? 0 : testValue.hashCode());
		return result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final InnerTestBean other = (InnerTestBean) obj;
		if (testValue == null) {
			if (other.testValue != null) {
				return false;
			}
		} else if (!testValue.equals(other.testValue)) {
			return false;
		}
		return true;
	}
}
