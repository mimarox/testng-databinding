package net.sf.testng.databinding.text;

import java.net.URL;
import java.util.Map;

public interface TextDataSourceConfiguration {

	Map<String, URL> getURLs();

	default String getEncoding() {
		return "UTF-8";
	}

	default String getBoundary() {
		return null;
	}
}
