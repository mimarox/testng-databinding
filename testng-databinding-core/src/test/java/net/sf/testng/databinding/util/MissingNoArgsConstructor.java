package net.sf.testng.databinding.util;

import java.net.URL;

import net.sf.testng.databinding.DataSourceConfiguration;

@DataSourceConfiguration(name = "no-args-constructor-missing")
public class MissingNoArgsConstructor implements CsvDataSourceConfiguration {

	public MissingNoArgsConstructor(final URL url) {}
	
	@Override
	public URL getURL() {
		return null;
	}
}
