package some.other.base.pack.foo.bar;

import java.util.List;

import net.sf.testng.databinding.AbstractDataSource;
import net.sf.testng.databinding.DataSource;
import net.sf.testng.databinding.core.model.Configuration;
import net.sf.testng.databinding.util.MethodParameter;

@DataSource(name = "other")
public class OtherStubbedDataSource extends AbstractDataSource {
	public OtherStubbedDataSource(List<MethodParameter> parameters, Configuration configuration) {
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