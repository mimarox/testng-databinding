package net.sf.testng.databinding.xml;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
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
import net.sf.testng.databinding.TestInput;
import net.sf.testng.databinding.TestOutput;
import net.sf.testng.databinding.error.ErrorCollector;
import net.sf.testng.databinding.error.MissingPropertiesException;
import net.sf.testng.databinding.error.MultipleConfigurationErrorsException;
import net.sf.testng.databinding.error.MultipleSourceErrorsException;
import net.sf.testng.databinding.util.Exceptions;
import net.sf.testng.databinding.util.MethodParameter;
import net.sf.testng.databinding.util.Types;


@DataSource(name = "xml")
public class XMLDataSource extends AbstractDataSource {
	private static final String TEST_INPUT_DATA_TAG = "testInputData";
	private static final String TEST_OUTPUT_DATA_TAG = "testOutputData";
	private static final String DATA_SET_TAG = "dataSet";
	private static final String ROOT_TAG = "testData";

	private final List<MethodParameter> inputParameters = new ArrayList<MethodParameter>();
	private final List<MethodParameter> outputParameters = new ArrayList<MethodParameter>();
	private final XMLStreamReader xmlReader;
	private final Properties properties;
	private InputStream urlStream;
	private List<MethodParameter> parameters;
	private boolean usesDataSetTag;
	private boolean hasNext;

	public XMLDataSource(final List<MethodParameter> parameters, final Properties properties) throws Exception {
		boolean cleanUpNecessary = true;
		try {
			checkProperties(properties);
			this.properties = properties;

			setParameters(parameters);

			xmlReader = createXmlReader();
			checkDataSource();
			cleanUpNecessary = false;
		} finally {
			if (cleanUpNecessary)
				cleanUp();
		}
	}

	private void checkProperties(final Properties properties) {
		final List<String> missingKeys = new ArrayList<String>();

		if (!properties.containsKey("url")) {
			missingKeys.add("url");
		}

		if (missingKeys.size() > 0) {
			throw new MissingPropertiesException(missingKeys);
		}
	}

	private XMLStreamReader createXmlReader() throws Exception {
		final URL url = resolveUrl(properties.getProperty("url"));
		urlStream = url.openStream();

		final XMLInputFactory factory = XMLInputFactory.newInstance();
		final XMLStreamReader xmlReader = factory.createXMLStreamReader(urlStream, properties.getProperty("encoding", "UTF-8"));

		return xmlReader;
	}

	private URL resolveUrl(final String urlString) {
		URL url;

		try {
			url = new URL(urlString);
		} catch (final MalformedURLException e) {
			url = getClass().getResource(urlString);
		}

		return url;
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

	private boolean isExpectedBeginOfDataSetTag(final String tagName, final boolean dataSetAllowed) throws XMLStreamException {
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

	@Override
	public boolean hasNext() {
		return hasNext;
	}

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

	private Map<MethodParameter, Object> createNextOutputData(final XMLStreamReader xmlReader) throws XMLStreamException {
		// assumes that xmlReader is always at a testOutputData tag on entering this method
		return createNextData(xmlReader, outputParameters, TEST_OUTPUT_DATA_TAG);
	}

	private Map<MethodParameter, Object> createNextData(final XMLStreamReader xmlReader, final List<MethodParameter> parameters,
			final String sectionTagName) throws XMLStreamException {
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

	private boolean reachedEndOfDataSection(final String sectionTagName, final int event, final XMLStreamReader xmlReader) {
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
			errorCollector.addError("unsupported type for data provider strategy " + getClass().getSimpleName());
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
			final List<PropertyDescriptor> remainingCandidates = filterCandidateProperties(info.getPropertyDescriptors());
			final Object bean = clazz.newInstance();

			for (xmlReader.next(); !reachedEndOfBeanSection(clazz, beanName, xmlReader); parseNextInBeanIfNecessary(clazz,
					beanName, xmlReader)) {
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
				throw genericSourceErrorsException("unexpected end of document encountered while " + "skipping to closing tag </"
						+ tagName + ">");
			}
		}
	}

	private boolean reachedEndOfBeanSection(final Class<?> beanType, final String beanName, final XMLStreamReader xmlReader) {
		return xmlReader.getEventType() == XMLStreamConstants.END_ELEMENT
				&& nameMatches(beanType, beanName, xmlReader.getLocalName());
	}

	private void parseNextInBeanIfNecessary(final Class<?> beanType, final String beanName, final XMLStreamReader xmlReader)
			throws XMLStreamException {
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

	private PropertyDescriptor getPropertyDescriptorByName(final List<PropertyDescriptor> descriptors, final String tagName) {
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
				final String typeNameBody = normalizedTypeName.substring(0, normalizedTypeName.length() - "ies".length());
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
				throw genericSourceErrorsException("couldn't find tag <" + tagName + "> within section <" + sectionTagName + ">");
			}
		}

		throw genericSourceErrorsException("couldn't find end tag </" + sectionTagName + "> - source is malformed!");
	}

	private MultipleSourceErrorsException genericSourceErrorsException(final String detailMessage) {
		final Location location = xmlReader.getLocation();
		final ErrorCollector errorCollector = new ErrorCollector(properties.getProperty("url"));
		errorCollector
				.addError("invalid source for data provider strategy " + getClass().getName() + " at ["
						+ location.getLineNumber() + ":" + location.getColumnNumber() + "]" + ", current parse event: "
						+ eventCodeToText(xmlReader.getEventType())
						+ (detailMessage != null ? ", detail message: " + detailMessage : ""));
		return new MultipleSourceErrorsException(Arrays.asList(errorCollector));
	}

	private MultipleSourceErrorsException remainingParametersSourceErrorsException(final List<MethodParameter> remainingParameters) {
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