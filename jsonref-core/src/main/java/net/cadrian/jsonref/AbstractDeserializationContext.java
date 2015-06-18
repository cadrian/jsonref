package net.cadrian.jsonref;

import java.io.IOException;

abstract class AbstractDeserializationContext implements DeserializationContext {
	private int index;
	private int ref;

	AbstractDeserializationContext() {
		index = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.cadrian.jsonref.DeserializationContext#next()
	 */
	@Override
	public void next() throws IOException {
		index++;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.cadrian.jsonref.DeserializationContext#skipSpaces()
	 */
	@Override
	public void skipSpaces() throws IOException {
		while (isValid() && Character.isWhitespace(get())) {
			next();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.cadrian.jsonref.DeserializationContext#getIndex()
	 */
	@Override
	public int getIndex() {
		return index;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.cadrian.jsonref.DeserializationContext#getRef()
	 */
	@Override
	public int getRef() {
		return ref;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.cadrian.jsonref.DeserializationContext#setRef(int)
	 */
	@Override
	public void setRef(final int ref) {
		this.ref = ref;
	}

}