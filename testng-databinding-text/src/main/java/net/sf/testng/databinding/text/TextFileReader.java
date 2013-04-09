package net.sf.testng.databinding.text;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Reader class for reading text files in chunks. Chunks are separated from each other by a boundary line.
 * The boundary line is taken to be a separate line, but must not end with a newline character <code>\n</code>.
 * 
 * @author Matthias Rothe
 */
public class TextFileReader {
	private boolean moreData;
	private BufferedReader reader;
	private String boundary;
	private String currentLine;

	/**
	 * Constructor taking a {@link BufferedReader} object to read the chunks from and a boundary line separating the
	 * chunks. 
	 * 
	 * @param reader The reader to read the chunks from
	 * @param boundary The boundary line separating the chunks
	 */
	public TextFileReader(final BufferedReader reader, final String boundary) {
		if (reader == null) {
			throw new IllegalArgumentException("reader must not be null");
		}

		this.reader = reader;
		this.boundary = boundary;
		this.moreData = true;
	}

	/**
	 * Determines whether there's at least one more chunk available.
	 * 
	 * @return <code>true</code>, if and only if there's at least one more chunk available, <code>false</code> otherwise
	 * @throws IOException If the data file cannot be read for any reason
	 */
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

	/**
	 * Reads and returns the next chunk of the text file.
	 * 
	 * @return The next chunk
	 * @throws IOException If the data file cannot be read for any reason
	 */
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