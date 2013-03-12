package net.sf.testng.databinding.properties.beans;

public class TestBean {
	private String stringValue;
	private long longValue;
	private float floatValue;
	private TestEnum testEnum;

	public TestBean() {
	}

	public TestBean(final String stringValue, final long longValue, final float floatValue, final TestEnum testEnum) {
		this.stringValue = stringValue;
		this.longValue = longValue;
		this.floatValue = floatValue;
		this.testEnum = testEnum;
	}

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(final String stringValue) {
		this.stringValue = stringValue;
	}

	public long getLongValue() {
		return longValue;
	}

	public void setLongValue(final long longValue) {
		this.longValue = longValue;
	}

	public float getFloatValue() {
		return floatValue;
	}

	public void setFloatValue(final float floatValue) {
		this.floatValue = floatValue;
	}

	public TestEnum getTestEnum() {
		return testEnum;
	}

	public void setTestEnum(final TestEnum testEnum) {
		this.testEnum = testEnum;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(floatValue);
		result = prime * result + (int) (longValue ^ (longValue >>> 32));
		result = prime * result + ((stringValue == null) ? 0 : stringValue.hashCode());
		result = prime * result + ((testEnum == null) ? 0 : testEnum.hashCode());
		return result;
	}

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
		if (Float.floatToIntBits(floatValue) != Float.floatToIntBits(other.floatValue)) {
			return false;
		}
		if (longValue != other.longValue) {
			return false;
		}
		if (stringValue == null) {
			if (other.stringValue != null) {
				return false;
			}
		} else if (!stringValue.equals(other.stringValue)) {
			return false;
		}
		if (testEnum != other.testEnum) {
			return false;
		}
		return true;
	}
}