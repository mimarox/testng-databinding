package net.sf.testng.databinding.xml.beans;

import java.util.List;

public class IESTestBean {
	private List<String> entries;

	public List<String> getEntries() {
		return entries;
	}

	public void setEntries(final List<String> entries) {
		this.entries = entries;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entries == null) ? 0 : entries.hashCode());
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
		final IESTestBean other = (IESTestBean) obj;
		if (entries == null) {
			if (other.entries != null) {
				return false;
			}
		} else if (!entries.equals(other.entries)) {
			return false;
		}
		return true;
	}
}
