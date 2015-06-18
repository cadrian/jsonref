package net.cadrian.jsonref;

import java.io.IOException;

class StringDeserializationContext extends AbstractDeserializationContext {

	private final char[] chars;
	private int charsIndex;

	StringDeserializationContext(final String jsonR) {
		super();
		chars = jsonR.toCharArray();
	}

	@Override
	public void next() throws IOException {
		super.next();
		charsIndex++;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.cadrian.jsonref.DeserializationContext#isValid()
	 */
	@Override
	public boolean isValid() {
		return charsIndex < chars.length;
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