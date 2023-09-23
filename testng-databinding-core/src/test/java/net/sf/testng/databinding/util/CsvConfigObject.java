package net.sf.testng.databinding.util;

import java.net.URL;

public class CsvConfigObject {

	public static CsvDataSourceConfiguration defaultConfig() {
		return new CsvDataSourceConfiguration() {
			
			@Override
			public URL getURL() {
				return null;
			}
		};
	}
}
