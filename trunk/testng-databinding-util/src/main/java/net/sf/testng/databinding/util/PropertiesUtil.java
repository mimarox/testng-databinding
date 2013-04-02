package net.sf.testng.databinding.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * This class contains utility methods pertaining to {@link Properties properties}.
 * 
 * @author Matthias Rothe
 */
public class PropertiesUtil {

	/**
	 * This class provides functionality to extract keys from {@link Properties properties}.
	 * <p>
	 * This class is part of the implementation of the fluent API definition for
	 * <code>PropertiesUtil.getAllKeysFrom(Properties p).startingWith(String prefix)</code> defining the
	 * {@link #startingWith(String)} method.
	 * <p>
	 * Instances of this class are immutable, making them also thread-safe.
	 * 
	 * @author Matthias Rothe
	 */
	public static class KeyExtractor {
		private Properties source;

		private KeyExtractor(Properties source) {
			this.source = source;
		}

		/**
		 * Retrieves all property keys starting with the given prefix.
		 * 
		 * @param prefix The prefix keys to be returned must start with
		 * @return All property keys starting with the given prefix
		 */
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

	/**
	 * Walks through all entries of the given source {@link Properties properties}. If the key of any such entry starts
	 * with the given prefix, the prefix is removed from the key and the entry is saved in the target properties. When
	 * all entries of the source properties have been processed, the resulting target properties are returned.
	 * <p>
	 * All entries of the source properties are left untouched.
	 * 
	 * @param source The source properties
	 * @param prefix The prefix to be removed
	 * @return The target properties with prefix removed
	 */
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

	/**
	 * Copies all entries of the source {@link Properties properties} into the target {@link Properties properties}
	 * exactly as they are.
	 * 
	 * @param source The source properties
	 * @param target The target properties
	 */
	public static void copyProperties(Properties source, Properties target) {
		for (Entry<Object, Object> entry : source.entrySet()) {
			target.put(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Returns a {@link KeyExtractor} instance for the given source {@link Properties properties} to get the keys
	 * of the property entries from.
	 * <p>
	 * This method is the first part of the fluent API definition of
	 * <code>PropertiesUtil.getAllKeysFrom(Properties p).startingWith(String prefix)</code>.
	 * 
	 * @param source The source properties
	 * @return A KeyExtractor instance for the given source properties
	 */
	public static KeyExtractor getAllKeysFrom(Properties source) {
		return new KeyExtractor(source);
	}
}