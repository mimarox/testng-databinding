package net.sf.testng.databinding.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

public class PropertiesUtil {
	public static class KeyExtractor {
		private Properties source;

		private KeyExtractor(Properties source) {
			this.source = source;
		}

		public String[] startingWith(String prefix) {
			List<String> keys = new ArrayList<String>();

			for (Object key : this.source.keySet()) {
				if (key.toString().startsWith(prefix)) {
					keys.add(key.toString());
				}
			}

			return keys.toArray(new String[0]);
		}
	}

	private PropertiesUtil() {
	}

	public static Properties removeKeyPrefix(Properties source, String prefix) {
		Properties target = new Properties();

		for (Entry<Object, Object> entry : source.entrySet()) {
			String key = entry.getKey().toString();
			if (key.startsWith(prefix)) {
				key = key.substring(prefix.length());
				target.setProperty(key, entry.getValue().toString());
			}
		}

		return target;
	}

	public static void copyProperties(Properties source, Properties target) {
		for (Entry<Object, Object> entry : source.entrySet()) {
			target.put(entry.getKey(), entry.getValue());
		}
	}

	public static KeyExtractor getAllKeysFrom(Properties source) {
		return new KeyExtractor(source);
	}
}
