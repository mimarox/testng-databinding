package net.sf.testng.databinding;

import java.util.List;
import java.util.Properties;

import net.sf.testng.databinding.util.MethodParameter;

@DataSource(name = "csv")
public class CsvDataSourceStub extends AbstractDataSource {
	public CsvDataSourceStub(List<MethodParameter> parameters, Properties properties) {
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