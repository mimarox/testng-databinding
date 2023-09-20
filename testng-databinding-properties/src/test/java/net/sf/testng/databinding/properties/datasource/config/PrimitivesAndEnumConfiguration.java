package net.sf.testng.databinding.properties.datasource.config;

import java.net.URL;

import net.sf.testng.databinding.DataSourceConfiguration;
import net.sf.testng.databinding.properties.PropertiesDataSourceConfiguration;

@DataSourceConfiguration(name = "primitives-and-enum")
public class PrimitivesAndEnumConfiguration implements PropertiesDataSourceConfiguration {

	@Override
	public URL getURL() {
		return getClass().getResource("/primitivesAndEnum.properties");
	}

}
