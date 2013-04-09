package net.sf.testng.databinding.properties.beans;

public class NestingTestBean {
	private String stringValue;
	private TestBean testBean;

	public NestingTestBean() {
	}

	public NestingTestBean(String stringValue, TestBean testBean) {
		this.stringValue = stringValue;
		this.testBean = testBean;
	}

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	public TestBean getTestBean() {
		return testBean;
	}

	public void setTestBean(TestBean testBean) {
		this.testBean = testBean;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((stringValue == null) ? 0 : stringValue.hashCode());
		result = prime * result + ((testBean == null) ? 0 : testBean.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		NestingTestBean other = (NestingTestBean) obj;
		if (stringValue == null) {
			if (other.stringValue != null) {
				return false;
			}
		} else if (!stringValue.equals(other.stringValue)) {
			return false;
		}
		if (testBean == null) {
			if (other.testBean != null) {
				return false;
			}
		} else if (!testBean.equals(other.testBean)) {
			return false;
		}
		return true;
	}
}