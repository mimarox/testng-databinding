package net.sf.testng.databinding.xml.datasource.config;

import java.net.URL;

import net.sf.testng.databinding.DataSourceConfiguration;
import net.sf.testng.databinding.xml.XMLDataSourceConfiguration;

@DataSourceConfiguration(name = "multiple-values")
public class MultipleValuesConfiguration implements XMLDataSourceConfiguration {

	@Override
	public URL getURL() {
		return getClass().getResource("/multipleInputValuesTestData.xml");
	}
}
