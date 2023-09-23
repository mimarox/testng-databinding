package net.sf.testng.databinding.xml;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;

import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.sf.testng.databinding.AbstractDataSource;
import net.sf.testng.databinding.DataSource;
import net.sf.testng.databinding.IDataSource;
import net.sf.testng.databinding.TestInput;
import net.sf.testng.databinding.TestOutput;
import net.sf.testng.databinding.core.error.ErrorCollector;
import net.sf.testng.databinding.core.error.MultipleConfigurationErrorsException;
import net.sf.testng.databinding.core.error.MultipleSourceErrorsException;
import net.sf.testng.databinding.core.model.Configuration;
import net.sf.testng.databinding.core.util.DataSourceConfigurationLoader;
import net.sf.testng.databinding.core.util.Types;
import net.sf.testng.databinding.util.Exceptions;
import net.sf.testng.databinding.util.MethodParameter;

/**
 * <p>
 * This {@link IDataSource data source} binds test data contained in XML files with a special format to
 * test method parameters. It supports an arbitrary number of {@link TestInput test input} and
 * {@link TestOutput test output} parameters and supports all generally supported parameter types for
 * input and output parameters. These types are all primitive types defined in the Java Language Specification
 * and their wrappers, {@link String Strings}, {@link Enum Enums}, Java Beans and {@link List Lists} of all of those
 * except {@link Enum Enums}.
 * </p>
 * <h3>Specifications</h3>
 * <h4>XML Data Files</h4>
 * <p>
 * The format of the XML files to be bound needs to adhere to these rules:
 * <ul>
 * <li>The file's encoding must be equal to the encoding given in the data properties or UTF-8 if no encoding is given
 * <li>The document definition must be <code>&lt;?xml version="1.0" encoding="&lt;encoding of the file&gt;"?&gt;</code></li>
 * <li>The root tag must be <code>&lt;testData&gt;</code></li>
 * <li>If data for more than one method invocation is defined, all data belonging to one method invocation must
 * be wrapped inside a <code>&lt;dataSet&gt;</code> tag, which is a child of the root tag</li>
 * <li>There must be as many <code>&lt;dataSet&gt;</code> tags, as desired test method invocations</li>
 * <li>If there is only test data for one method invocation, the <code>&lt;dataSet&gt;</code> tag can be omitted</li>
 * <li>All test input data must be wrapped in a <code>&lt;testInputData&gt;</code> tag</li>
 * <li>All test output data must be wrapped in a <code>&lt;testOutputData&gt;</code> tag</li>
 * <li>If the <code>&lt;dataSet&gt;</code> tag is omitted, the <code>&lt;testInputData&gt;</code> and
 * <code>&lt;testOutputData&gt;</code> tags must be direct child tags of the root tag. Otherwise they must be direct
 * child tags of each <code>&lt;dataSet&gt;</code> tag and must not be direct child tags of the root tag.</li>
 * <li>There can be at most one <code>&lt;testInputData&gt;</code> and one <code>&lt;testOutputData&gt;</code> tag,
 * either as direct children of the root tag or each <code>&lt;dataSet&gt;</code> tag</li>
 * <li>Either the <code>&lt;testInputData&gt;</code> or <code>&lt;testOutputData&gt;</code> tag must be given as a
 * direct child of the root tag or each <code>&lt;dataSet&gt;</code> tag</li>
 * <li>There must be exactly one tag for each test method parameter which is not a {@link List}, as a direct child tag of
 * either <code>&lt;testInputData&gt;</code> or <code>&lt;testOutputData&gt;</code>. The name of the tag is determined as
 * follows:
 * <ul>
 * <li>If the parameter is of primitive, primitive wrapper or {@link String} type, the tag name must case insensitively
 * equal the name given as {@link TestInput#name() &#64;TestInput(name = "")} or {@link TestOutput#name() &#64;TestOutput(name = "")}</li>
 * <li>If the parameter is of Java Bean or {@link Enum} type, the tag name must case insensitively equal the simple name of
 * the type as returned from {@link Class#getSimpleName() ((Class&lt;?&gt;) type).getSimpleName()}</li>
 * </ul>
 * </li>
 * <li>There can be one or several similarly named tags for each test method parameter which is a {@link List}, as direct
 * child tags of either <code>&lt;testInputData&gt;</code> or <code>&lt;testOutputData&gt;</code>. If there are several
 * similarly named tags, they must appear as one consecutive group of tags. The contents of all such tags will be placed into
 * the list. Any similarly named tags appearing after tags named otherwise are not included in the list, but simply ignored.
 * The name of the tags is determined as follows:
 * <ul>
 * <li>Generally the same rules that apply to non-list type parameters also apply to list type parameters, taking the name
 * either from the <code>&#64;TestInput</code>/<code>&#64;TestOutput</code> annotation <code>name</code> property or the
 * simple name of the type. However plurals in such names are handled: names can either have a single <code>'s'</code> for
 * general names or an <code>'ies'</code> suffix for singulars ending in <code>'y'</code>. The tag name will always be
 * the singular.</li>
 * </ul>
 * </li>
 * <li>There must be tags following the same rules just given for test method parameters for all the properties in a Java
 * Bean type and any nested Java Bean type.</li>
 * </ul>
 * </p>
 * <h4>Java Beans</h4>
 * <p>
 * Java Beans that data is to be bound to need to have a standard constructor taking no arguments. The same types that are
 * supported as test method parameter types are also supported as Java Bean properties types. Particularly any parameterized
 * types other than {@link List Lists}, whether they are parameterized Java Beans, {@link Collection Collections} other than
 * Lists, or {@link Map Maps}, are not supported. Nested Java Beans are however supported up to any nesting level (nesting
 * is only limited by the max allowable method stack size of the used JVM which generally allows thousands of levels). Any
 * Java Bean properties for which no tags can be found or that have an unsupported type are just not set, leaving them
 * unchanged. Any tags for which no Java Bean properties of matching name and supported type can be found are skipped
 * ignoring them.
 * </p>
 * <h3>Example</h3>
 * <p>
 * To make issues clearer, here is an example of this data source in use. It's test method does a kind of
 * functional test on the advanced Google Search facility using a limited set of input data and checking the search results
 * returned. The actual test code is pseudocode that does not work, and is not part of the TestNG Data Binding framework,
 * but clearly conveys the intent. Getters and setters are omitted in Java Beans for brevity in this example. They are
 * however crucial in actual Java Beans, so you have to include them in any Java Bean you actually want to bind data to.
 * </p>
 * <h4>Test Method</h4>
 * <pre>
 * &#64;DataBinding(propertiesPrefix = "advancedSearch")
 * public void testAdvancedGoogleSearch(&#64;TestInput(name = "searchTerm") final String searchTerm,
 *         &#64;TestInput final FileType fileType, &#64;TestOutput final List&lt;SearchResult&gt; searchResults) {
 *     // Run the search
 *     open("http://www.google.com/advanced_search"); // hard coded as it doesn't change
 *     insert(searchTerm).into("allTermsInputField");
 *     select(fileType.getLabel()).from("fileTypeSelection");
 *     click("advancedSearchButton").andWaitForPageToLoad();
 *     
 *     // Check search results
 *     for (SearchResult searchResult : searchResults) {
 *         SearchResultDefinition definition = searchResult.getSearchResultDefinition();
 *         ResultingPage page = searchResult.getResultingPage();
 *         
 *         assertPageContainsLinkLabeled(definition.getLinkLabel());
 *         assertPageContainsResultDescription(definition.getDescription());
 *         
 *         String pageUrl = getLinkUrlForLabel(definition.getLinkLabel());
 *         page.setPageUrl(pageUrl);
 *     }
 *     
 *     // Check resulting pages
 *     for (SearchResult searchResult : searchResults) {
 *         ResultingPage page = searchResult.getResultingPage();
 *         
 *         open(page.getPageUrl());
 *         assertTitle(page.getTitle());
 *         assertContainsText(page.getText());
 *     }
 * }
 * </pre>
 * <h4>Java Classes</h4>
 * <h5>Enum: FileType</h5>
 * <pre>
 * public enum FileType {
 *     ALL("any format"),
 *     PDF("Adobe Acrobat PDF (.pdf)"),
 *     PS("Adobe Postscript (.ps)"),
 *     /* Remaining types omitted for brevity &#42;/;
 *     
 *     private String label;
 *     
 *     private FileType(final String label) {
 *         this.label = label;
 *     }
 *     
 *     public String getLabel() {
 *         return label;
 *     }
 * }
 * </pre>
 * <h5>Java Bean: SearchResult</h5>
 * <pre>
 * public class SearchResult {
 *     private SearchResultDefinition searchResultDefinition;
 *     private ResultingPage resultingPage;
 *     
 *     /* Getters and setters omitted for brevity &#42;/
 * }
 * </pre>
 * <h5>Java Bean: SearchResultDefinition</h5>
 * <pre>
 * public class SearchResultDefinition {
 *     private String linkLabel;
 *     private String description;
 *     
 *     /* Getters and setters omitted for brevity &#42;/
 * }
 * </pre>
 * <h5>Java Bean: ResultingPage</h5>
 * <pre>
 * public class ResultingPage {
 *     private String pageUrl;
 *     private String title;
 *     private String text;
 *     
 *     /* Getters and setters omitted for brevity &#42;/
 * }
 * </pre>
 * <h4>Data Properties File</h4>
 * <pre>
 * advancedSearch.dataSource=xml
 * advancedSearch.url=/data/advancedSearch.xml
 * </pre>
 * <h4>XML Data Sources</h4>
 * <h5>Data source containing data for one invocation of the test method</h5>
 * <p>
 * The following two XML files are equivalent. For just one invocation of the test method, the
 * <code>&lt;dataSet&gt;</code> tag can be given or omitted.
 * </p>
 * <p>With <code>&lt;dataSet&gt;</code> tag:</p>
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * &lt;testData&gt;
 *     &lt;dataSet&gt;
 *         &lt;testInputData&gt;
 *             &lt;searchTerm&gt;Java Component Scanner&lt;/searchTerm&gt;
 *             &lt;fileType&gt;ALL&lt;/fileType&gt;
 *         &lt;/testInputData&gt;
 *         &lt;testOutputData&gt;
 *             &lt;searchResult&gt;
 *                 &lt;searchResultDefinition&gt;
 *                     &lt;linkLabel&gt;Download Extensible Component Scanner 0.4b Free - A Java ...&lt;/linkLabel&gt;
 *                     &lt;description&gt;Nov 19, 2012 – Download Extensible Component Scanner - A Java component to help you with your work.&lt;/description&gt;
 *                 &lt;/searchResultDefinition&gt;
 *                 &lt;resultingPage&gt;
 *                     &lt;title&gt;Download Extensible Component Scanner 0.4b Free - A Java component to help you with your work. - Softpedia&lt;/title&gt;
 *                     &lt;text&gt;Extensible Component Scanner 0.4b&lt;/text&gt;
 *                 &lt;/resultingPage&gt;
 *             &lt;/searchResult&gt;
 *             &lt;searchResult&gt;
 *                 &lt;searchResultDefinition&gt;
 *                     &lt;linkLabel&gt;Extensible Component Scanner | Free Development software ...&lt;/linkLabel&gt;
 *                     &lt;description&gt;Nov 24, 2012 – You'll love this: component scanning as easy as select(javaClasses()).from("your.package").returning(allAnnotatedWith(YourAnnotation.class)).&lt;/description&gt;
 *                 &lt;/searchResultDefinition&gt;
 *                 &lt;resultingPage&gt;
 *                     &lt;title&gt;Extensible Component Scanner | Free Development software downloads at SourceForge.net&lt;/title&gt;
 *                     &lt;text&gt;eXtcos is now also available from Maven Central. To include it into your Maven project just add this dependency:&lt;/text&gt;
 *                 &lt;/resultingPage&gt;
 *             &lt;/searchResult&gt;
 *         &lt;/testOutputData&gt;
 *     &lt;/dataSet&gt;
 * &lt;/testData&gt;
 * </pre>
 * <p>Without <code>&lt;dataSet&gt;</code> tag:</p>
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * &lt;testData&gt;
 *     &lt;testInputData&gt;
 *         &lt;searchTerm&gt;Java Component Scanner&lt;/searchTerm&gt;
 *         &lt;fileType&gt;ALL&lt;/fileType&gt;
 *     &lt;/testInputData&gt;
 *     &lt;testOutputData&gt;
 *         &lt;searchResult&gt;
 *             &lt;searchResultDefinition&gt;
 *                 &lt;linkLabel&gt;Download Extensible Component Scanner 0.4b Free - A Java ...&lt;/linkLabel&gt;
 *                 &lt;description&gt;Nov 19, 2012 – Download Extensible Component Scanner - A Java component to help you with your work.&lt;/description&gt;
 *             &lt;/searchResultDefinition&gt;
 *             &lt;resultingPage&gt;
 *                 &lt;title&gt;Download Extensible Component Scanner 0.4b Free - A Java component to help you with your work. - Softpedia&lt;/title&gt;
 *                 &lt;text&gt;Extensible Component Scanner 0.4b&lt;/text&gt;
 *             &lt;/resultingPage&gt;
 *         &lt;/searchResult&gt;
 *         &lt;searchResult&gt;
 *             &lt;searchResultDefinition&gt;
 *                 &lt;linkLabel&gt;Extensible Component Scanner | Free Development software ...&lt;/linkLabel&gt;
 *                 &lt;description&gt;Nov 24, 2012 – You'll love this: component scanning as easy as select(javaClasses()).from("your.package").returning(allAnnotatedWith(YourAnnotation.class)).&lt;/description&gt;
 *             &lt;/searchResultDefinition&gt;
 *             &lt;resultingPage&gt;
 *                 &lt;title&gt;Extensible Component Scanner | Free Development software downloads at SourceForge.net&lt;/title&gt;
 *                 &lt;text&gt;eXtcos is now also available from Maven Central. To include it into your Maven project just add this dependency:&lt;/text&gt;
 *             &lt;/resultingPage&gt;
 *         &lt;/searchResult&gt;
 *     &lt;/testOutputData&gt;
 * &lt;/testData&gt;
 * </pre>
 * <h5>Data source containing data for several invocations of the test method</h5>
 * <p>
 * For several invocations of the test method, the <code>&lt;dataSet&gt;</code> tag must be given. The number of
 * <code>&lt;dataSet&gt;</code> tags equals the number of test method invocations. For brevity's sake this example will
 * just include two data sets resulting in two test method invocations.
 * </p>
 * <pre>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * &lt;testData&gt;
 *     &lt;dataSet&gt;
 *         &lt;testInputData&gt;
 *             &lt;searchTerm&gt;Java Component Scanner&lt;/searchTerm&gt;
 *             &lt;fileType&gt;ALL&lt;/fileType&gt;
 *         &lt;/testInputData&gt;
 *         &lt;testOutputData&gt;
 *             &lt;searchResult&gt;
 *                 &lt;searchResultDefinition&gt;
 *                     &lt;linkLabel&gt;Download Extensible Component Scanner 0.4b Free - A Java ...&lt;/linkLabel&gt;
 *                     &lt;description&gt;Nov 19, 2012 – Download Extensible Component Scanner - A Java component to help you with your work.&lt;/description&gt;
 *                 &lt;/searchResultDefinition&gt;
 *                 &lt;resultingPage&gt;
 *                     &lt;title&gt;Download Extensible Component Scanner 0.4b Free - A Java component to help you with your work. - Softpedia&lt;/title&gt;
 *                     &lt;text&gt;Extensible Component Scanner 0.4b&lt;/text&gt;
 *                 &lt;/resultingPage&gt;
 *             &lt;/searchResult&gt;
 *             &lt;searchResult&gt;
 *                 &lt;searchResultDefinition&gt;
 *                     &lt;linkLabel&gt;Extensible Component Scanner | Free Development software ...&lt;/linkLabel&gt;
 *                     &lt;description&gt;Nov 24, 2012 – You'll love this: component scanning as easy as select(javaClasses()).from("your.package").returning(allAnnotatedWith(YourAnnotation.class)).&lt;/description&gt;
 *                 &lt;/searchResultDefinition&gt;
 *                 &lt;resultingPage&gt;
 *                     &lt;title&gt;Extensible Component Scanner | Free Development software downloads at SourceForge.net&lt;/title&gt;
 *                     &lt;text&gt;eXtcos is now also available from Maven Central. To include it into your Maven project just add this dependency:&lt;/text&gt;
 *                 &lt;/resultingPage&gt;
 *             &lt;/searchResult&gt;
 *         &lt;/testOutputData&gt;
 *     &lt;/dataSet&gt;
 *     &lt;dataSet&gt;
 *         &lt;testInputData&gt;
 *             &lt;searchTerm&gt;json transformation engine&lt;/searchTerm&gt;
 *             &lt;fileType&gt;ALL&lt;/fileType&gt;
 *         &lt;/testInputData&gt;
 *         &lt;testOutputData&gt;
 *             &lt;searchResult&gt;
 *                 &lt;searchResultDefinition&gt;
 *                     &lt;linkLabel&gt;Jetro | Free software downloads at SourceForge.net&lt;/linkLabel&gt;
 *                     &lt;description&gt;Jetro provides a JSON transformation engine and a comprehensive JSON tree API. It allows transforming any JSON source representation into&lt;/description&gt;
 *                 &lt;/searchResultDefinition&gt;
 *                 &lt;resultingPage&gt;
 *                     &lt;title&gt;Jetro | Free  software downloads at SourceForge.net&lt;/title&gt;
 *                     &lt;text&gt;JSON transformations - powerful, yet quick and easy&lt;/text&gt;
 *                 &lt;/resultingPage&gt;
 *             &lt;/searchResult&gt;
 *         &lt;/testOutputData&gt;
 *     &lt;/dataSet&gt;
 * &lt;/testData&gt;
 * </pre>
 * 
 * @author Matthias Rothe
 */
@DataSource(name = "xml")
public class XMLDataSource extends AbstractDataSource {
	private static final String TEST_INPUT_DATA_TAG = "testInputData";
	private static final String TEST_OUTPUT_DATA_TAG = "testOutputData";
	private static final String DATA_SET_TAG = "dataSet";
	private static final String ROOT_TAG = "testData";

	private final List<MethodParameter> inputParameters = new ArrayList<MethodParameter>();
	private final List<MethodParameter> outputParameters = new ArrayList<MethodParameter>();
	private final XMLStreamReader xmlReader;
	private final XMLDataSourceConfiguration configuration;
	private InputStream urlStream;
	private List<MethodParameter> parameters;
	private boolean usesDataSetTag;
	private boolean hasNext;

	/**
	 * Constructs a new instance of this class, setting the {@link MethodParameter test method parameters} to load the
	 * data for and the {@link Properties properties} describing where to load the data from.
	 * 
	 * @param parameters The test method parameters for which data is to be loaded
	 * @param properties The properties describing where to load the data from
	 * @throws Exception If anything goes wrong during the creation of this instance
	 */
	public XMLDataSource(final List<MethodParameter> parameters,
			final Configuration configuration) throws Exception {
		boolean cleanUpNecessary = true;
		try {
			XMLDataSourceConfiguration dataSourceConfiguration =
					DataSourceConfigurationLoader.loadDataSourceConfiguration(configuration,
							XMLDataSourceConfiguration.class);
			
			this.configuration = dataSourceConfiguration;

			setParameters(parameters);

			xmlReader = createXmlReader();
			checkDataSource();
			cleanUpNecessary = false;
		} finally {
			if (cleanUpNecessary)
				cleanUp();
		}
	}

	private XMLStreamReader createXmlReader() throws Exception {
		final URL url = configuration.getURL();
		urlStream = url.openStream();

		final XMLInputFactory factory = XMLInputFactory.newInstance();
		final XMLStreamReader xmlReader = factory.createXMLStreamReader(urlStream,
			configuration.getEncoding());

		return xmlReader;
	}

	private void checkDataSource() {
		boolean validDataSource;

		try {
			if (xmlReader.next() == XMLStreamConstants.START_ELEMENT && xmlReader.getLocalName().equals(ROOT_TAG)
					&& xmlReader.getAttributeCount() == 0 && xmlReader.nextTag() == XMLStreamConstants.START_ELEMENT
					&& isExpectedBeginOfDataSetTag(xmlReader.getLocalName(), true)) {
				validDataSource = true;
				hasNext = true;
			} else {
				validDataSource = false;
			}
		} catch (final Throwable t) {
			validDataSource = false;
		}

		if (!validDataSource) {
			throw genericSourceErrorsException(null);
		}
	}

	private boolean isExpectedBeginOfDataSetTag(final String tagName, final boolean dataSetAllowed)
			throws XMLStreamException {
		if (dataSetAllowed && tagName.equals(DATA_SET_TAG) && xmlReader.nextTag() == XMLStreamConstants.START_ELEMENT) {
			usesDataSetTag = true;
			return isExpectedBeginOfDataSetTag(xmlReader.getLocalName(), false);
		} else if ((inputParameters.size() > 0 && tagName.equals(TEST_INPUT_DATA_TAG))
				|| (inputParameters.size() == 0 && outputParameters.size() > 0 && tagName.equals(TEST_OUTPUT_DATA_TAG))) {
			return true;
		} else {
			return false;
		}
	}

	private void setParameters(final List<MethodParameter> parameters) {
		this.parameters = parameters;

		for (final MethodParameter parameter : parameters) {
			if (parameter.getAnnotation(TestInput.class) != null) {
				inputParameters.add(parameter);
			} else if (parameter.getAnnotation(TestOutput.class) != null) {
				outputParameters.add(parameter);
			}
		}

		if (inputParameters.isEmpty() && outputParameters.isEmpty()) {
			final ErrorCollector errorCollector = new ErrorCollector("method parameters");
			errorCollector.addError("no parameters with @TestInput or @TestOutput annotation given");
			throw new MultipleConfigurationErrorsException(Arrays.asList(errorCollector));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasNext() {
		return hasNext;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] next() {
		if (hasNext) {
			boolean cleanUpNecessary = true;
			try {
				final Object[] nextDataSet = createNextDataSet(xmlReader);
				cleanUpNecessary = false;
				return nextDataSet;
			} catch (final MultipleConfigurationErrorsException e) {
				throw e;
			} catch (final MultipleSourceErrorsException e) {
				throw e;
			} catch (final Exception e) {
				throw genericSourceErrorsException(e.getClass().getName() + ": " + e.getMessage());
			} finally {
				if (cleanUpNecessary)
					cleanUp();
			}
		} else {
			throw new NoSuchElementException();
		}
	}

	private Object[] createNextDataSet(final XMLStreamReader xmlReader) throws XMLStreamException {
		final Map<MethodParameter, Object> objects = new HashMap<MethodParameter, Object>();

		if (inputParameters.size() > 0) {
			findOpeningTag(TEST_INPUT_DATA_TAG, getSectionTagName(), xmlReader);
			objects.putAll(createNextInputData(xmlReader));
		}

		if (outputParameters.size() > 0) {
			findOpeningTag(TEST_OUTPUT_DATA_TAG, getSectionTagName(), xmlReader);
			objects.putAll(createNextOutputData(xmlReader));
		}

		hasNext = determineHasNext(xmlReader);

		return orderAndConvert(objects);
	}

	private Map<MethodParameter, Object> createNextInputData(final XMLStreamReader xmlReader) throws XMLStreamException {
		// assumes that xmlReader is always at a testInputData tag on entering this method
		return createNextData(xmlReader, inputParameters, TEST_INPUT_DATA_TAG);
	}

	private Map<MethodParameter, Object> createNextOutputData(final XMLStreamReader xmlReader)
			throws XMLStreamException {
		// assumes that xmlReader is always at a testOutputData tag on entering this method
		return createNextData(xmlReader, outputParameters, TEST_OUTPUT_DATA_TAG);
	}

	private Map<MethodParameter, Object> createNextData(final XMLStreamReader xmlReader,
			final List<MethodParameter> parameters, final String sectionTagName) throws XMLStreamException {
		final List<MethodParameter> remainingParameters = new ArrayList<MethodParameter>(parameters);
		final Map<MethodParameter, Object> objects = new HashMap<MethodParameter, Object>();

		for (xmlReader.next(); !reachedEndOfDataSection(sectionTagName, xmlReader.getEventType(), xmlReader); parseNextInDataSectionIfNecessary(
			sectionTagName, xmlReader)) {
			if (xmlReader.getEventType() == XMLStreamConstants.START_ELEMENT) {
				final String tagName = xmlReader.getLocalName();
				final MethodParameter parameter = getMethodParameterByName(remainingParameters, tagName);

				if (parameter != null) {
					objects.put(parameter, processMethodParameter(parameter, xmlReader));
					remainingParameters.remove(parameter);
				} else {
					skipToEndTag(tagName, xmlReader);
				}
			}
		}

		if (remainingParameters.isEmpty()) {
			return objects;
		} else {
			throw remainingParametersSourceErrorsException(remainingParameters);
		}
	}

	private boolean reachedEndOfDataSection(final String sectionTagName, final int event,
			final XMLStreamReader xmlReader) {
		return event == XMLStreamConstants.END_ELEMENT && xmlReader.getLocalName().equals(sectionTagName);
	}

	private void parseNextInDataSectionIfNecessary(final String sectionTagName, final XMLStreamReader xmlReader)
			throws XMLStreamException {
		final int event = xmlReader.getEventType();
		if ((event != XMLStreamConstants.START_ELEMENT && !reachedEndOfDataSection(sectionTagName, event, xmlReader))
				|| (event == XMLStreamConstants.START_ELEMENT && xmlReader.getLocalName().equals(sectionTagName))) {
			xmlReader.next();
		}
	}

	private Object processMethodParameter(final MethodParameter parameter, final XMLStreamReader xmlReader)
			throws XMLStreamException {
		final Type type = parameter.getType();
		if (Types.isEnumType(type)) {
			return processEnumParameter(parameter, xmlReader);
		} else if (Types.isPrimitiveType(type)) {
			return processPrimitiveParameter(parameter, xmlReader);
		} else if (Types.isSingleBeanType(type)) {
			return processSingleBeanParameter(parameter, xmlReader);
		} else if (Types.isListOfPrimitivesType(type)) {
			return processListOfPrimitivesParameter(parameter, xmlReader);
		} else if (Types.isListOfBeansType(type)) {
			return processListOfBeansParameter(parameter, xmlReader);
		} else {
			final ErrorCollector errorCollector = new ErrorCollector(type, parameter.getName());
			errorCollector.addError("unsupported type for data source " + getClass().getSimpleName());
			throw new MultipleConfigurationErrorsException(Arrays.asList(errorCollector));
		}
	}

	private Object processEnumParameter(final MethodParameter parameter, final XMLStreamReader xmlReader)
			throws XMLStreamException {
		final String enumName = xmlReader.getElementText();

		final Class<?> enumClass = (Class<?>) parameter.getType();
		final Field[] fields = enumClass.getFields();

		for (final Field field : fields) {
			try {
				if (field.getName().equals(enumName)) {
					return field.get(null);
				}
			} catch (final Exception ignored) {
				// shouldn't happen
			}
		}

		final ErrorCollector errorCollector = new ErrorCollector(parameter.getType(), parameter.getName());
		errorCollector.addError("the value [" + enumName + "] found in the source isn't a member of this enum type");
		throw new MultipleSourceErrorsException(Arrays.asList(errorCollector));
	}

	private Object processPrimitiveParameter(final MethodParameter parameter, final XMLStreamReader xmlReader)
			throws XMLStreamException {
		final String value = xmlReader.getElementText();
		final Type type = parameter.getType();

		if (type.equals(String.class)) {
			return value;
		} else if (type == Integer.class || type == int.class) {
			return Integer.parseInt(value);
		} else if (type == Long.class || type == long.class) {
			return Long.parseLong(value);
		} else if (type == Float.class || type == float.class) {
			return Float.parseFloat(value);
		} else if (type == Double.class || type == double.class) {
			return Double.parseDouble(value);
		} else if (type == Boolean.class || type == boolean.class) {
			return Boolean.parseBoolean(value);
		}

		// can't happen
		throw new RuntimeException();
	}

	private Object processListOfPrimitivesParameter(final MethodParameter listParameter, final XMLStreamReader xmlReader)
			throws XMLStreamException {
		final MethodParameter valueParameter = deriveValueFromListParameter(listParameter);
		final List<Object> primitives = new ArrayList<Object>();

		do {
			primitives.add(processPrimitiveParameter(valueParameter, xmlReader));
			xmlReader.nextTag();
		} while (xmlReader.getEventType() == XMLStreamConstants.START_ELEMENT
				&& nameMatches(listParameter.getType(), listParameter.getName(), xmlReader.getLocalName()));

		return primitives;
	}

	private Object processSingleBeanParameter(final MethodParameter parameter, final XMLStreamReader xmlReader)
			throws XMLStreamException {
		try {
			final String beanName = parameter.getName();
			final Class<?> clazz = (Class<?>) parameter.getType();
			final BeanInfo info = Introspector.getBeanInfo(clazz);
			final List<PropertyDescriptor> remainingCandidates = filterCandidateProperties(info
				.getPropertyDescriptors());
			final Object bean = clazz.getConstructor().newInstance();

			for (xmlReader.next(); !reachedEndOfBeanSection(clazz, beanName, xmlReader); parseNextInBeanIfNecessary(
				clazz, beanName, xmlReader)) {
				if (xmlReader.getEventType() == XMLStreamConstants.START_ELEMENT) {
					final String tagName = xmlReader.getLocalName();
					final PropertyDescriptor descriptor = getPropertyDescriptorByName(remainingCandidates, tagName);

					if (descriptor != null) {
						descriptor.getWriteMethod().invoke(bean,
							processMethodParameter(createMethodParameterForProperty(descriptor), xmlReader));
						remainingCandidates.remove(descriptor);
					} else {
						skipToEndTag(tagName, xmlReader);
					}
				}
			}

			return bean;
		} catch (final XMLStreamException e) {
			throw e;
		} catch (final NumberFormatException e) {
			throw e;
		} catch (final Exception e) {
			final ErrorCollector errorCollector = new ErrorCollector(parameter.getType());
			errorCollector.addError("unable to create type: " + e.getMessage());
			throw new MultipleConfigurationErrorsException(Arrays.asList(errorCollector));
		}
	}

	private void skipToEndTag(final String tagName, final XMLStreamReader xmlReader) throws XMLStreamException {
		while (!(xmlReader.getEventType() == XMLStreamConstants.END_ELEMENT && xmlReader.getLocalName().equals(tagName))) {
			if (xmlReader.hasNext()) {
				xmlReader.next();
			} else {
				throw genericSourceErrorsException("unexpected end of document encountered while "
						+ "skipping to closing tag </" + tagName + ">");
			}
		}
	}

	private boolean reachedEndOfBeanSection(final Class<?> beanType, final String beanName,
			final XMLStreamReader xmlReader) {
		return xmlReader.getEventType() == XMLStreamConstants.END_ELEMENT
				&& nameMatches(beanType, beanName, xmlReader.getLocalName());
	}

	private void parseNextInBeanIfNecessary(final Class<?> beanType, final String beanName,
			final XMLStreamReader xmlReader) throws XMLStreamException {
		if (xmlReader.getEventType() != XMLStreamConstants.START_ELEMENT
				&& !reachedEndOfBeanSection(beanType, beanName, xmlReader)) {
			xmlReader.next();
		}
	}

	private List<PropertyDescriptor> filterCandidateProperties(final PropertyDescriptor[] descriptors) {
		final List<PropertyDescriptor> candidates = new ArrayList<PropertyDescriptor>();

		for (final PropertyDescriptor descriptor : descriptors) {
			if (descriptor.getWriteMethod() != null) {
				candidates.add(descriptor);
			}
		}

		return candidates;
	}

	private PropertyDescriptor getPropertyDescriptorByName(final List<PropertyDescriptor> descriptors,
			final String tagName) {
		for (final PropertyDescriptor descriptor : descriptors) {
			final Type propertyType = descriptor.getWriteMethod().getGenericParameterTypes()[0];
			if (nameMatches(propertyType, descriptor.getName(), tagName)) {
				return descriptor;
			}
		}
		return null;
	}

	private MethodParameter createMethodParameterForProperty(final PropertyDescriptor descriptor) {
		final Method writeMethod = descriptor.getWriteMethod();
		final Annotation[][] annotations = writeMethod.getParameterAnnotations();
		final Type[] parameterTypes = writeMethod.getGenericParameterTypes();
		return new MethodParameter(Arrays.asList(annotations[0]), parameterTypes[0], descriptor.getName());
	}

	private Object processListOfBeansParameter(final MethodParameter listParameter, final XMLStreamReader xmlReader)
			throws XMLStreamException {
		final MethodParameter valueParameter = deriveValueFromListParameter(listParameter);
		final List<Object> beans = new ArrayList<Object>();

		do {
			beans.add(processSingleBeanParameter(valueParameter, xmlReader));
			xmlReader.nextTag();
		} while (xmlReader.getEventType() == XMLStreamConstants.START_ELEMENT
				&& nameMatches(listParameter.getType(), listParameter.getName(), xmlReader.getLocalName()));

		return beans;
	}

	private MethodParameter deriveValueFromListParameter(final MethodParameter listParameter) {
		final MethodParameter valueParameter = Types.unwrapIfPossible(listParameter);

		final String listParameterName = listParameter.getName();
		String name = null;
		if (listParameterName.endsWith("ies")) {
			name = listParameterName.substring(0, listParameterName.length() - "ies".length()) + "y";
		} else {
			name = listParameterName.substring(0, listParameterName.length() - 1);
		}

		return new MethodParameter(valueParameter.getAnnotations(), valueParameter.getType(), name);
	}

	private boolean determineHasNext(final XMLStreamReader xmlReader) throws XMLStreamException {
		Boolean hasNext = null;

		if (usesDataSetTag) {
			while (xmlReader.getEventType() != XMLStreamConstants.START_ELEMENT) {
				if (xmlReader.hasNext()) {
					xmlReader.next();
				} else {
					hasNext = false;
					break;
				}
			}

			if (hasNext == null) {
				final String tagName = xmlReader.getLocalName();
				if (tagName.equals(DATA_SET_TAG)) {
					hasNext = true;
				} else {
					throw genericSourceErrorsException("expected opening tag <" + DATA_SET_TAG + "> "
							+ "or end of file, but found opening tag <" + tagName + ">");
				}
			}
		} else {
			hasNext = false;
		}

		if (!hasNext) {
			cleanUp();
		}

		return hasNext;
	}

	private void cleanUp() {
		try {
			if (xmlReader != null) {
				xmlReader.close();
			}
			if (urlStream != null) {
				urlStream.close();
			}
		} catch (final Exception e) {
			throw Exceptions.softenIfNecessary(e);
		}
	}

	private Object[] orderAndConvert(final Map<MethodParameter, Object> objectsMap) {
		final List<Object> objects = new ArrayList<Object>();

		for (final MethodParameter parameter : parameters) {
			objects.add(objectsMap.get(parameter));
		}

		return objects.toArray();
	}

	private MethodParameter getMethodParameterByName(final List<MethodParameter> parameters, final String name) {
		for (final MethodParameter parameter : parameters) {
			if (nameMatches(parameter.getType(), parameter.getName(), name)) {
				return parameter;
			}
		}

		return null;
	}

	private boolean nameMatches(final Type namedType, final String typeName, final String tagName) {
		final String normalizedTypeName = typeName.toLowerCase();
		final String normalizedTagName = tagName.toLowerCase();

		if (Types.isSingleObjectType(namedType) && normalizedTypeName.equals(normalizedTagName)) {
			return true;
		} else if (Types.isListOfObjectsType(namedType)) {
			if (normalizedTypeName.equals(normalizedTagName + "s")) {
				return true;
			} else if (normalizedTypeName.endsWith("ies") && normalizedTagName.endsWith("y")) {
				final String typeNameBody = normalizedTypeName.substring(0,
					normalizedTypeName.length() - "ies".length());
				final String tagNameBody = normalizedTagName.substring(0, normalizedTagName.length() - "y".length());
				return typeNameBody.equals(tagNameBody);
			}
		}

		return false;
	}

	private String getSectionTagName() {
		return usesDataSetTag ? DATA_SET_TAG : ROOT_TAG;
	}

	private void findOpeningTag(final String tagName, final String sectionTagName, final XMLStreamReader xmlReader)
			throws XMLStreamException {
		if (xmlReader.getEventType() == XMLStreamConstants.START_ELEMENT && xmlReader.getLocalName().equals(tagName)) {
			return;
		}

		for (int event = xmlReader.next(); xmlReader.hasNext(); event = xmlReader.next()) {
			if (event == XMLStreamConstants.START_ELEMENT && xmlReader.getLocalName().equals(tagName)) {
				return;
			} else if (event == XMLStreamConstants.END_ELEMENT && xmlReader.getLocalName().equals(sectionTagName)) {
				throw genericSourceErrorsException("couldn't find tag <" + tagName + "> within section <"
						+ sectionTagName + ">");
			}
		}

		throw genericSourceErrorsException("couldn't find end tag </" + sectionTagName + "> - source is malformed!");
	}

	private MultipleSourceErrorsException genericSourceErrorsException(final String detailMessage) {
		final Location location = xmlReader.getLocation();
		final ErrorCollector errorCollector = new ErrorCollector(configuration.getURL().toExternalForm());
		errorCollector.addError("invalid source for data source " + getClass().getName() + " at ["
				+ location.getLineNumber() + ":" + location.getColumnNumber() + "]" + ", current parse event: "
				+ eventCodeToText(xmlReader.getEventType())
				+ (detailMessage != null ? ", detail message: " + detailMessage : ""));
		return new MultipleSourceErrorsException(Arrays.asList(errorCollector));
	}

	private MultipleSourceErrorsException remainingParametersSourceErrorsException(
			final List<MethodParameter> remainingParameters) {
		final List<ErrorCollector> errorCollectors = new ArrayList<ErrorCollector>();

		for (final MethodParameter parameter : remainingParameters) {
			final ErrorCollector errorCollector = new ErrorCollector(parameter.getType(), parameter.getName());
			errorCollector.addError("no data found for this parameter");
			errorCollectors.add(errorCollector);
		}

		return new MultipleSourceErrorsException(errorCollectors);
	}

	private String eventCodeToText(final int event) {
		switch (event) {
		case XMLStreamConstants.ATTRIBUTE:
			return "attribute";
		case XMLStreamConstants.CDATA:
			return "CDATA";
		case XMLStreamConstants.CHARACTERS:
			return "characters";
		case XMLStreamConstants.COMMENT:
			return "comment";
		case XMLStreamConstants.DTD:
			return "dtd definition";
		case XMLStreamConstants.END_DOCUMENT:
			return "end of document";
		case XMLStreamConstants.END_ELEMENT:
			return "closing tag";
		case XMLStreamConstants.ENTITY_DECLARATION:
			return "dtd entity declaration";
		case XMLStreamConstants.ENTITY_REFERENCE:
			return "dtd entity reference";
		case XMLStreamConstants.NAMESPACE:
			return "namespace";
		case XMLStreamConstants.NOTATION_DECLARATION:
			return "notation declaration";
		case XMLStreamConstants.PROCESSING_INSTRUCTION:
			return "processing instruction";
		case XMLStreamConstants.SPACE:
			return "whitespace";
		case XMLStreamConstants.START_DOCUMENT:
			return "begin of document";
		case XMLStreamConstants.START_ELEMENT:
			return "opening tag";
		default:
			return "unknown xml parse event";
		}
	}
}