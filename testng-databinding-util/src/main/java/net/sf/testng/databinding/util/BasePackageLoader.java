package net.sf.testng.databinding.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads base packages to be used for component scanning.
 * <p>
 * Expects base packages to be defined in files having the following format:
 * <ul>
 * <li>Must be UTF-8 encoded</li>
 * <li>Must have one base package per line
 * <ul>
 * <li>Base packages must be valid Java package names, but can also include wildcards. Use one <code>'*'</code>
 * (<code>ASTERISK</code>) to replace a single subpackage and two <code>'**'</code> to replace several subpackages</li>
 * </ul>
 * </li>
 * <li>Can have single line comments, the comment character is <code>'#'</code> (<code>'\u0023'</code>, <code>NUMBER SIGN</code>)
 * <ul>
 * <li>The first comment character on a line and all following characters on the same line are ignored</li>
 * </ul>
 * </li>
 * <li>Can have blank lines. Any blank lines and any whitespace preceding or following base packages will be ignored</li>
 * </ul>
 * 
 * @author Matthias Rothe
 */
public class BasePackageLoader {
	private static class CacheKey {
		private final String source;
		private final ClassLoader classLoader;

		CacheKey(final String source, final ClassLoader classLoader) {
			this.source = source;
			this.classLoader = classLoader;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((classLoader == null) ? 0 : classLoader.hashCode());
			result = prime * result + ((source == null) ? 0 : source.hashCode());
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
			CacheKey other = (CacheKey) obj;
			if (classLoader == null) {
				if (other.classLoader != null) {
					return false;
				}
			} else if (!classLoader.equals(other.classLoader)) {
				return false;
			}
			if (source == null) {
				if (other.source != null) {
					return false;
				}
			} else if (!source.equals(other.source)) {
				return false;
			}
			return true;
		}
	}

	private static final Map<CacheKey, Set<String>> CACHE = new HashMap<CacheKey, Set<String>>();
	private static final Logger LOG = LoggerFactory.getLogger(BasePackageLoader.class);

	private BasePackageLoader() {
	}

	/**
	 * Loads base packages from the given source using the current thread's context class loader.
	 * 
	 * @param source The source from where to load the base packages
	 * @return The set of base packages found in the given source
	 */
	public static Set<String> loadBasePackages(final String source) {
		return loadBasePackages(source, Thread.currentThread().getContextClassLoader());
	}

	/**
	 * Loads base packages from the given source using the given class loader.
	 * 
	 * @param source The source from where to load the base packages
	 * @param classLoader The class loader to use for resolving the source
	 * @return The set of base packages found in the given source
	 */
	public static Set<String> loadBasePackages(final String source, final ClassLoader classLoader) {
		CacheKey cacheKey = new CacheKey(source, classLoader);
		Set<String> basePackages;

		if (CACHE.containsKey(cacheKey)) {
			basePackages = CACHE.get(cacheKey);
		} else {
			basePackages = new LinkedHashSet<String>();
			fillBasePackages(basePackages, source, classLoader);

			CACHE.put(cacheKey, basePackages);
		}

		return basePackages;
	}

	static void fillBasePackages(Set<String> basePackages, String source, ClassLoader classLoader) {
		try {
			Enumeration<URL> resources = classLoader.getResources(source);

			while (resources.hasMoreElements()) {
				URL resourceUrl = resources.nextElement();
				BufferedReader reader = new BufferedReader(new InputStreamReader(resourceUrl.openStream(), "UTF-8"));
				try {
					fillBasePackages(basePackages, reader);
				} finally {
					reader.close();
				}
			}

			//			URL resourceUrl = BasePackageLoader.class.getResource(source);
			//			BufferedReader reader = new BufferedReader(new InputStreamReader(resourceUrl.openStream(), "UTF-8"));
			//			try {
			//				fillBasePackages(basePackages, reader);
			//			} finally {
			//				reader.close();
			//			}
		} catch (IOException e) {
			LOG.warn("Exception occurred when trying to load base packages from " + source, e);
		}
	}

	static void fillBasePackages(Set<String> basePackages, BufferedReader reader) throws IOException {
		String line;

		while ((line = reader.readLine()) != null) {
			line = line.split("#")[0].trim();

			if (!line.equals("")) {
				basePackages.add(line);
			}
		}
	}
}