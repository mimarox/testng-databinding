package net.sf.testng.databinding.properties.datasource.config;

import java.net.URL;

import net.sf.testng.databinding.properties.PropertiesDataSourceConfiguration;

public class PropertiesDataSourceConfigurations {
	
	private PropertiesDataSourceConfigurations() {}
	
	public static PropertiesDataSourceConfiguration beanAllValuesSetConfig() {
		return new PropertiesDataSourceConfiguration() {
			@Override
			public URL getURL() {
				return getClass().getResource("/beanAllValuesSet.properties");
			}
		};
	}
	
	public static PropertiesDataSourceConfiguration beansSomeValuesSetConfig() {
		return new PropertiesDataSourceConfiguration() {

			@Override
			public URL getURL() {
				return getClass().getResource("/beanSomeValuesSet.properties");
			}
		};
	}
	
	public static PropertiesDataSourceConfiguration primitivesAndEnumConfig() {
		return new PropertiesDataSourceConfiguration() {
			
			@Override
			public URL getURL() {
				return getClass().getResource("/primitivesAndEnum.properties");
			}
		};
	}
	
	public static PropertiesDataSourceConfiguration primitiveValuesConfig() {
		return new PropertiesDataSourceConfiguration() {
			
			@Override
			public URL getURL() {
				return getClass().getResource("/primitive.properties");
			}
		};
	}
}
