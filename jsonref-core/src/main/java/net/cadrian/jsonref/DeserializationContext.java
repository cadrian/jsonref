package net.cadrian.jsonref;

import java.io.IOException;

interface DeserializationContext {
	public void next() throws IOException;

	public void skipSpaces() throws IOException;

	public boolean isValid();

	public char get();

	public int getIndex();

	public int getRef();

	public void setRef(int ref);

}