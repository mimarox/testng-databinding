package net.sf.testng.databinding;

import java.util.List;
import java.util.Properties;

import net.sf.testng.databinding.AbstractDataProviderStrategy;
import net.sf.testng.databinding.DataProviderStrategyNames;
import net.sf.testng.databinding.util.MethodParameter;


@DataProviderStrategyNames({ "CSV", "csv" })
public class CsvDataProviderStrategyStub extends AbstractDataProviderStrategy {
	public CsvDataProviderStrategyStub(List<MethodParameter> parameters, Properties properties) {
	}

	@Override
	public boolean hasNext() {
		return false;
	}

	@Override
	public Object[] next() {
		return null;
	}
}