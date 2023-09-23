package net.sf.testng.databinding;

import java.util.List;

import net.sf.testng.databinding.core.model.Configuration;
import net.sf.testng.databinding.util.MethodParameter;

@DataSource(name = "csv")
public class CsvDataSourceStub extends AbstractDataSource {
	public CsvDataSourceStub(List<MethodParameter> parameters, Configuration configuration) {
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