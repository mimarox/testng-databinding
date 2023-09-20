package net.sf.testng.databinding.text.datasource.config;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.sf.testng.databinding.DataSourceConfiguration;
import net.sf.testng.databinding.text.TextDataSourceConfiguration;

@DataSourceConfiguration(name = "without-boundary")
public class WithoutBoundaryConfiguration implements TextDataSourceConfiguration {

	@Override
	public Map<String, URL> getURLs() {
		Map<String, URL> urls = new HashMap<>();
		
		urls.put("input1", getClass().getResource("/multiValue/input1.txt"));
		urls.put("input2", getClass().getResource("/multiValue/input2.txt"));
		urls.put("output1", getClass().getResource("/multiValue/output1.txt"));
		urls.put("output2", getClass().getResource("/multiValue/output2.txt"));

		return urls;
	}
}
