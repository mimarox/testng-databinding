package net.sf.testng.databinding.xml;

import java.net.URL;

public interface XMLDataSourceConfiguration {
	URL getURL();
	
	default String getEncoding() {
		return "UTF-8";
	}
}
