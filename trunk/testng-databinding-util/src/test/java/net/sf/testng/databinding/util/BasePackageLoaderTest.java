package net.sf.testng.databinding.util;

import static org.testng.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import org.testng.annotations.Test;

public class BasePackageLoaderTest {

	@Test
	public void shouldLoadBasePackagesFromReader() throws Exception {
		/*+*
		 * #a comment
		 * 
		 * 		net.sf 
		 *  com		# all in com.
		 */
		String source = "#a comment\n\n		net.sf \n com		# all in com.";

		BufferedReader reader = new BufferedReader(new StringReader(source));
		Set<String> basePackages = new HashSet<String>();
		BasePackageLoader.fillBasePackages(basePackages, reader);

		String[] expecteds = new String[] { "com", "net.sf" };
		assertEquals(basePackages.toArray(), expecteds);
	}

	@Test(dependsOnMethods = "shouldLoadBasePackagesFromReader")
	public void shouldLoadBasePackagesFromClasspath() {
		Set<String> basePackages = BasePackageLoader.loadBasePackages("testng-databinding.base-packages");
		String[] expecteds = new String[] { "com", "net.sf", "org.testng" };
		assertEquals(basePackages.toArray(), expecteds);
	}
}