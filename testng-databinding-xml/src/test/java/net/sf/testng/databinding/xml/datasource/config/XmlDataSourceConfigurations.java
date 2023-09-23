package net.sf.testng.databinding.xml.datasource.config;

import java.net.URL;

import net.sf.testng.databinding.xml.XMLDataSourceConfiguration;

public class XmlDataSourceConfigurations {

	private XmlDataSourceConfigurations() {}
	
	public static XMLDataSourceConfiguration beanWithMapConfig() {
		return new XMLDataSourceConfiguration() {
			
			@Override
			public URL getURL() {
				return getClass().getResource("/beanWithMapTestData.xml");
			}
		};
	}
	
	public static XMLDataSourceConfiguration inputOutputValuesConfig() {
		return new XMLDataSourceConfiguration() {
			
			@Override
			public URL getURL() {
				return getClass().getResource("/inputOutputValuesTestData.xml");
			}
		};
	}
	
	public static XMLDataSourceConfiguration multipleValuesConfig() {
		return new XMLDataSourceConfiguration() {
	
			@Override
			public URL getURL() {
				return getClass().getResource("/multipleInputValuesTestData.xml");
			}
		};
	}
	
	public static XMLDataSourceConfiguration multiRowComplexBeanConfig() {
		return new XMLDataSourceConfiguration() {
			
			@Override
			public URL getURL() {
				return getClass().getResource("/multiRowComplexBeanInputValueTestData.xml");
			}
		};
	}
	
	public static XMLDataSourceConfiguration multiRowSingleStringConfig() {
		return new XMLDataSourceConfiguration() {
			
			@Override
			public URL getURL() {
				return getClass().getResource("/multiRowSingleStringInputValueTestData.xml");
			}
		};
	}
	
	public static XMLDataSourceConfiguration singleRowSingleEnumConfig() {
		return new XMLDataSourceConfiguration() {
			
			@Override
			public URL getURL() {
				return getClass().getResource("/singleRowSingleEnumInputValueTestData.xml");
			}
		};
	}
	
	public static XMLDataSourceConfiguration singleRowSingleStringConfig() {
		return new XMLDataSourceConfiguration() {
			
			@Override
			public URL getURL() {
				return getClass().getResource("/singleRowSingleStringInputValueTestData.xml");
			}
		};
	}
	
	public static XMLDataSourceConfiguration singleRowSingleStringNoDataConfig() {
		return new XMLDataSourceConfiguration() {
			
			@Override
			public URL getURL() {
				return getClass().getResource("/singleRowSingleStringInputValueTestData-noData.xml");
			}
		};
	}
}
