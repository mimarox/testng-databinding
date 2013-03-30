package net.sf.testng.databinding.text;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.StringReader;

import org.testng.annotations.Test;

public class TextFileReaderTest {

	@Test
	public void shouldReadAllData() throws Exception {
		String boundary = "---";
		String content = "123\n123\n---\n456\n456\n---\n789\n789";

		StringReader contentReader = new StringReader(content);
		BufferedReader bufferedReader = new BufferedReader(contentReader);

		TextFileReader reader = new TextFileReader(bufferedReader, boundary);
		assertTrue(reader.hasMoreData(), "Expected more data");

		assertEquals(reader.readNextChunk(), "123\n123");
		assertTrue(reader.hasMoreData(), "Expected more data");

		assertEquals(reader.readNextChunk(), "456\n456");
		assertTrue(reader.hasMoreData(), "Expected more data");

		assertEquals(reader.readNextChunk(), "789\n789");
		assertFalse(reader.hasMoreData(), "Expected no more data");

		assertEquals(reader.readNextChunk(), null);
	}

	@Test
	public void shouldHaveNoData() throws Exception {
		String boundary = "---";
		String content = "";

		StringReader contentReader = new StringReader(content);
		BufferedReader bufferedReader = new BufferedReader(contentReader);

		TextFileReader reader = new TextFileReader(bufferedReader, boundary);
		assertFalse(reader.hasMoreData(), "Expected no data");

		assertEquals(reader.readNextChunk(), null);
	}

	@Test
	public void shouldHaveEmptyValues() throws Exception {
		String boundary = "---";
		String content = "\n---\n\n---\n\n---\n\n";

		StringReader contentReader = new StringReader(content);
		BufferedReader bufferedReader = new BufferedReader(contentReader);

		TextFileReader reader = new TextFileReader(bufferedReader, boundary);

		for (int i = 0; i < 4; i++) {
			assertTrue(reader.hasMoreData(), "Expected more data");
			assertEquals(reader.readNextChunk(), "");
		}

		assertFalse(reader.hasMoreData(), "Expected no more data");
		assertEquals(reader.readNextChunk(), null);
	}

	@Test
	public void boundaryOnBoundaryShouldCauseNullValue() throws Exception {
		String boundary = "---";
		String content = "---\n---\n---";

		StringReader contentReader = new StringReader(content);
		BufferedReader bufferedReader = new BufferedReader(contentReader);

		TextFileReader reader = new TextFileReader(bufferedReader, boundary);

		for (int i = 0; i < 3; i++) {
			assertTrue(reader.hasMoreData(), "Expected more data");
			assertEquals(reader.readNextChunk(), null);
		}

		assertFalse(reader.hasMoreData(), "Expected no more data");
		assertEquals(reader.readNextChunk(), null);
	}
}