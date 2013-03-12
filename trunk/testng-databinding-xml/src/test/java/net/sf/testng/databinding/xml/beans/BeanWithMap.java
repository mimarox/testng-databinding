package net.sf.testng.databinding.xml.beans;

import java.util.Map;

public class BeanWithMap {
	private Map<String, String> map;

	public Map<String, String> getMap() {
		return map;
	}

	public void setMap(final Map<String, String> map) {
		this.map = map;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((map == null) ? 0 : map.hashCode());
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
		final BeanWithMap other = (BeanWithMap) obj;
		if (map == null) {
			if (other.map != null) {
				return false;
			}
		} else if (!map.equals(other.map)) {
			return false;
		}
		return true;
	}
}
