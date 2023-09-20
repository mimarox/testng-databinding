package net.sf.testng.databinding.xml.datasource.config;

import java.net.URL;

import net.sf.testng.databinding.DataSourceConfiguration;
import net.sf.testng.databinding.xml.XMLDataSourceConfiguration;

@DataSourceConfiguration(name = "multi-row-single-string")
public class MultiRowSingleStringConfiguration implements XMLDataSourceConfiguration {

	@Override
	public URL getURL() {
		return getClass().getResource("/multiRowSingleStringInputValueTestData.xml");
	}
}
