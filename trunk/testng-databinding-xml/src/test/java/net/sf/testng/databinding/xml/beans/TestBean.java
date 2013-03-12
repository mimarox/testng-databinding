package net.sf.testng.databinding.xml.beans;

import java.util.List;

public class TestBean {
	private TestEnum testEnum;

	private String testString;
	private double testDouble;

	private List<Boolean> testBooleans;

	private InnerTestBean innerTestBean;

	private List<InnerTestBean> testBeans;

	public TestEnum getTestEnum() {
		return testEnum;
	}

	public void setTestEnum(final TestEnum testEnum) {
		this.testEnum = testEnum;
	}

	public String getTestString() {
		return testString;
	}

	public void setTestString(final String testString) {
		this.testString = testString;
	}

	public double getTestDouble() {
		return testDouble;
	}

	public void setTestDouble(final double testDouble) {
		this.testDouble = testDouble;
	}

	public List<Boolean> getTestBooleans() {
		return testBooleans;
	}

	public void setTestBooleans(final List<Boolean> testBooleans) {
		this.testBooleans = testBooleans;
	}

	public InnerTestBean getInnerTestBean() {
		return innerTestBean;
	}

	public void setInnerTestBean(final InnerTestBean innerTestBean) {
		this.innerTestBean = innerTestBean;
	}

	public List<InnerTestBean> getTestBeans() {
		return testBeans;
	}

	public void setTestBeans(final List<InnerTestBean> testBeans) {
		this.testBeans = testBeans;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((innerTestBean == null) ? 0 : innerTestBean.hashCode());
		result = prime * result + ((testBeans == null) ? 0 : testBeans.hashCode());
		result = prime * result + ((testBooleans == null) ? 0 : testBooleans.hashCode());
		long temp;
		temp = Double.doubleToLongBits(testDouble);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((testEnum == null) ? 0 : testEnum.hashCode());
		result = prime * result + ((testString == null) ? 0 : testString.hashCode());
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
		final TestBean other = (TestBean) obj;
		if (innerTestBean == null) {
			if (other.innerTestBean != null) {
				return false;
			}
		} else if (!innerTestBean.equals(other.innerTestBean)) {
			return false;
		}
		if (testBeans == null) {
			if (other.testBeans != null) {
				return false;
			}
		} else if (!testBeans.equals(other.testBeans)) {
			return false;
		}
		if (testBooleans == null) {
			if (other.testBooleans != null) {
				return false;
			}
		} else if (!testBooleans.equals(other.testBooleans)) {
			return false;
		}
		if (Double.doubleToLongBits(testDouble) != Double.doubleToLongBits(other.testDouble)) {
			return false;
		}
		if (testEnum != other.testEnum) {
			return false;
		}
		if (testString == null) {
			if (other.testString != null) {
				return false;
			}
		} else if (!testString.equals(other.testString)) {
			return false;
		}
		return true;
	}
}
