package net.sf.testng.databinding.properties.datasource.config;

import java.net.URL;

import net.sf.testng.databinding.DataSourceConfiguration;
import net.sf.testng.databinding.properties.PropertiesDataSourceConfiguration;

@DataSourceConfiguration(name = "bean-some-values-set")
public class BeanSomeValuesSetConfiguration implements PropertiesDataSourceConfiguration {

	@Override
	public URL getURL() {
		return getClass().getResource("/beanSomeValuesSet.properties");
	}

}
