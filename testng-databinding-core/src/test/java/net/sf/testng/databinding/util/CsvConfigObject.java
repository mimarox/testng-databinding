package net.sf.testng.databinding.util;

import java.net.URL;

import net.sf.testng.databinding.DataSourceConfiguration;

@DataSourceConfiguration(name = "default")
public class CsvConfigObject implements CsvDataSourceConfiguration {

	@Override
	public URL getURL() {
		return null;
	}
}
