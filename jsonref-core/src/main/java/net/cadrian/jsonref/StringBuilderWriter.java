package net.cadrian.jsonref;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

class StringBuilderWriter extends Writer {

	private final StringBuilder data = new StringBuilder();
	private boolean closed;

	/*
	 * (non-Javadoc)
	 *
	 * @see java.io.Writer#write(char[], int, int)
	 */
	@Override
	public void write(final char[] cbuf, final int off, final int len)
			throws IOException {
		if (closed) {
			throw new IOException("closed");
		}
		data.append(Arrays.copyOfRange(cbuf, off, off + len));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.io.Writer#flush()
	 */
	@Override
	public void flush() throws IOException {
		if (closed) {
			throw new IOException("closed");
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.io.Writer#close()
	 */
	@Override
	public void close() throws IOException {
		closed = true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return data.toString();
	}

}
