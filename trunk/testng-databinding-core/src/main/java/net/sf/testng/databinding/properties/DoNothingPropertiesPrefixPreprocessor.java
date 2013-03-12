package net.sf.testng.databinding.properties;

public class DoNothingPropertiesPrefixPreprocessor implements
		PropertiesPrefixPreprocessor {

	@Override
	public String process(String prefix) {
		return prefix;
	}
}