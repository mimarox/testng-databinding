package net.sf.testng.databinding.util;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

import org.testng.annotations.Test;

import net.sf.testng.databinding.core.model.Configuration;
import net.sf.testng.databinding.core.util.DataSourceConfigurationLoader;

public class DataSourceConfigurationLoaderTest {

	@Test
	public void loadDataSourceConfiguration() {
		assertNotNull(DataSourceConfigurationLoader.loadDataSourceConfiguration(
				new Configuration(CsvConfigObject.class, "defaultConfig"),
				CsvDataSourceConfiguration.class));
	}
	
	@Test
	public void shouldntFindConfigObjectWrongConfigName() {
		try {
			DataSourceConfigurationLoader.loadDataSourceConfiguration(
					new Configuration(CsvConfigObject.class, "default-1"),
					CsvDataSourceConfiguration.class);
			fail("Shouldn't have found method default-1");
		} catch (RuntimeException e) {
			assertEquals(e.getCause().getClass(), NoSuchMethodException.class);
		}
	}
}
