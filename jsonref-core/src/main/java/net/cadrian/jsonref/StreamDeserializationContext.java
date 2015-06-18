package net.cadrian.jsonref;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;

class StreamDeserializationContext extends AbstractDeserializationContext {

	private final Reader in;
	private boolean eof = false;
	private final char[] chars = new char[4096];
	private int charsIndex;
	private int charsCount;

	StreamDeserializationContext(final Reader in) {
		super();
		this.in = in;
		this.charsIndex = 4096;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.cadrian.jsonref.AbstractDeserializationContext#next()
	 */
	@Override
	public void next() throws IOException {
		super.next();
		charsIndex++;
		if (charsIndex >= charsCount && !eof) {
			try {
				charsCount = in.read(chars);
			} catch (final EOFException e) {
				eof = true;
			}
			charsIndex = 0;
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.cadrian.jsonref.DeserializationContext#isValid()
	 */
	@Override
	public boolean isValid() {
		return !eof || charsIndex < charsCount;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.cadrian.jsonref.DeserializationContext#get()
	 */
	@Override
	public char get() {
		return chars[charsIndex];
	}

}