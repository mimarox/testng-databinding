package net.sf.testng.databinding.properties.datasource.config;

import java.net.URL;

import net.sf.testng.databinding.DataSourceConfiguration;
import net.sf.testng.databinding.properties.PropertiesDataSourceConfiguration;

@DataSourceConfiguration(name = "primitive-values")
public class PrimitiveValuesConfiguration implements PropertiesDataSourceConfiguration {

	@Override
	public URL getURL() {
		return getClass().getResource("/primitive.properties");
	}
}
