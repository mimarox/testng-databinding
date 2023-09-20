package net.sf.testng.databinding.csv;

import java.net.URL;
import java.nio.charset.Charset;

import net.sf.testng.databinding.TestInput;
import net.sf.testng.databinding.TestOutput;

public interface CsvDataSourceConfiguration {

	/**
	 * The locator of the actual data source file.
	 * 
	 * @return the url
	 */
	URL getURL();

	/**
	 * The character set of the CSV source file.
	 * <p>
	 * May be any charset name or alias deemed to be legal by
	 * {@link Charset#forName(String)}
	 * <p>
	 * Defaults to UTF-8
	 * 
	 * @return the charset
	 */
	default String getCharset() {
		return "UTF-8";
	}

	/**
	 * The character used as the value separator within the CSV source file.
	 * <p>
	 * Defaults to ',' (Comma)
	 * 
	 * @return the separator char
	 */
	default char getSeparator() {
		return ',';
	}

	/**
	 * The character used for quotes within the CSV source file.
	 * <p>
	 * Defaults to '"' (Quotation mark)
	 * 
	 * @return the quote char
	 */
	default char getQuoteChar() {
		return '"';
	}

	/**
	 * The character used for escapes within the CSV source file.
	 * <p>
	 * Defaults to '\' (Backslash)
	 * 
	 * @return the escape char
	 */
	default char getEscapeChar() {
		return '\\';
	}

	/**
	 * The number of lines to skip at the beginning of the CSV source file.
	 * <p>
	 * May be any integer &gt;= 0
	 * <p>
	 * Defaults to 0
	 * 
	 * @return the lines to skip
	 */
	default int getLinesToSkip() {
		return 0;
	}

	/**
	 * Whether to use strict quotes or not.
	 * <p>
	 * Defaults to <code>false</code>
	 * 
	 * @return <code>true</code>, or <code>false</code> 
	 */
	default boolean useStrictQuotes() {
		return false;
	}

	/**
	 * Whether to ignore leading whitespace or not.
	 * <p>
	 * Defaults to <code>false</code>
	 * 
	 * @return <code>true</code>, or <code>false</code> 
	 */
	default boolean ignoreLeadingWhitespace() {
		return false;
	}

	/**
	 * The mapper implementation class defining how the data within the
	 * CSV source file will be mapped to the test method parameters.
	 * <p>
	 * Defaults to {@link HeaderNameMapper HeaderNameMapper.class}
	 *
	 * @return
	 */
	default Class<? extends Mapper> getMapperClass() {
		return HeaderNameMapper.class;
	}

	/**
	 * The prefix to signify test data input columns, i.e. columns
	 * containing data for test method parameters annotated with
	 * {@link TestInput}.
	 * <p>
	 * May be any arbitrary {@link String} different from the value
	 * of {@link #getOutputColumnPrefix()} and {@link #getLinkingColumnPrefix()}
	 * <p>
	 * Defaults to in_
	 * 
	 * @return the input column prefix
	 */
	default String getInputColumnPrefix() {
		return "in_";
	}

	/**
	 * The prefix to signify test data output columns, i.e. columns
	 * containing data for test method parameters annotated with
	 * {@link TestOutput}.
	 * <p>
	 * May be any arbitrary {@link String} different from the value
	 * of {@link #getInputColumnPrefix()} and {@link #getLinkingColumnPrefix()}
	 * <p>
	 * Defaults to out_
	 * 
	 * @return the output column prefix
	 */
	default String getOutputColumnPrefix() {
		return "out_";
	}

	/**
	 * The prefix to signify the linking column. Only applicable if the
	 * {@link HeaderNameFileLinkingMapper} is used as the
	 * {@link #getMapperClass() mapper}.
	 * <p>
	 * May be any arbitrary {@link String} different from the value
	 * of {@link #getInputColumnPrefix()} and {@link #getOutputColumnPrefix()}
	 * <p>
	 * Defaults to link_
	 * 
	 * @return the linking column prefix
	 */
	default String getLinkingColumnPrefix() {
		return "link_";
	}
}
