package net.sf.testng.databinding.util;

import static org.testng.Assert.assertNotNull;

import java.util.NoSuchElementException;

import org.testng.annotations.Test;

import net.sf.testng.databinding.core.model.Configuration;
import net.sf.testng.databinding.core.util.DataSourceConfigurationLoader;

public class DataSourceConfigurationLoaderTest {

	@Test
	public void loadDataSourceConfiguration() {
		assertNotNull(DataSourceConfigurationLoader.loadDataSourceConfiguration(
				new Configuration("default", new String[] {
						"net.sf.testng.databinding.util"
				}, getClass().getClassLoader()), CsvDataSourceConfiguration.class));
	}
	
	@Test(expectedExceptions = NoSuchElementException.class)
	public void shouldntFindConfigObjectWrongBasePackage() {
		DataSourceConfigurationLoader.loadDataSourceConfiguration(
				new Configuration("default", new String[] {
						"net.sf.testng.databinding.util.foo.bar"
				}, getClass().getClassLoader()), CsvDataSourceConfiguration.class);		
	}
	
	@Test(expectedExceptions = NoSuchElementException.class)
	public void shouldntFindConfigObjectWrongConfigName() {
		DataSourceConfigurationLoader.loadDataSourceConfiguration(
				new Configuration("default-1", new String[] {
						"net.sf.testng.databinding.util"
				}, getClass().getClassLoader()), CsvDataSourceConfiguration.class);		
	}
	
	@Test(expectedExceptions = NoSuchElementException.class)
	public void shouldntLoadConfigObjectNoArgsConstructorMissing() {
		DataSourceConfigurationLoader.loadDataSourceConfiguration(
				new Configuration("no-args-constructor-missing", new String[] {
						"net.sf.testng.databinding.util"
				}, getClass().getClassLoader()), CsvDataSourceConfiguration.class);		
	}
}
