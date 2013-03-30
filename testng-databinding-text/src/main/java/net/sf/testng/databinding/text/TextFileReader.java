package net.sf.testng.databinding.text;

import java.io.BufferedReader;
import java.io.IOException;

public class TextFileReader {
	private boolean moreData;
	private BufferedReader reader;
	private String boundary;
	private String currentLine;

	public TextFileReader(final BufferedReader reader, final String boundary) {
		if (reader == null) {
			throw new IllegalArgumentException("reader must not be null");
		}

		this.reader = reader;
		this.boundary = boundary;
		this.moreData = true;
	}

	public boolean hasMoreData() throws IOException {
		if (moreData && currentLine == null) {
			currentLine = reader.readLine();

			if (currentLine == null) {
				moreData = false;
				reader.close();
			}
		}

		return moreData;
	}

	public String readNextChunk() throws IOException {
		if (!hasMoreData() || currentLine.equals(boundary)) {
			currentLine = null;
			return null;
		}

		StringBuilder buffer = new StringBuilder(currentLine).append("\n");

		while (true) {
			currentLine = reader.readLine();

			if (currentLine == null) {
				moreData = false;
				break;
			} else if (currentLine.equals(boundary)) {
				break;
			} else {
				buffer.append(currentLine).append("\n");
			}
		}

		currentLine = null;
		return buffer.substring(0, buffer.length() - 1);
	}
}