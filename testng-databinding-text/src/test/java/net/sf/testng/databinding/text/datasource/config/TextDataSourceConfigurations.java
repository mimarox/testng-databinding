package net.sf.testng.databinding.text.datasource.config;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.sf.testng.databinding.text.TextDataSourceConfiguration;

public class TextDataSourceConfigurations {
	public static final Map<String, URL> URLS = new HashMap<>();
	
	static {
		URLS.put("input1", TextDataSourceConfiguration.class.getResource("/multiValue/input1.txt"));
		URLS.put("input2", TextDataSourceConfiguration.class.getResource("/multiValue/input2.txt"));
		URLS.put("output1", TextDataSourceConfiguration.class.getResource("/multiValue/output1.txt"));
		URLS.put("output2", TextDataSourceConfiguration.class.getResource("/multiValue/output2.txt"));
	}
	
	private TextDataSourceConfigurations() {}
	
	public static TextDataSourceConfiguration withBoundaryConfig() {
		return new TextDataSourceConfiguration() {
			
			@Override
			public Map<String, URL> getURLs() {
				return URLS;
			}
			
			@Override
			public String getBoundary() {
				return "---123---";
			}
		};
	}
	
	public static TextDataSourceConfiguration withoutBoundaryConfig() {
		return new TextDataSourceConfiguration() {
			
			@Override
			public Map<String, URL> getURLs() {
				return URLS;
			}
		};
	}
}
