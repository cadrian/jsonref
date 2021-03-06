/*
   Copyright 2015 Cyril Adrian <cyril.adrian@gmail.com>

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package net.cadrian.jsonref;

/**
 * Serialization exception
 */
public class SerializationException extends RuntimeException {

	private static final long serialVersionUID = -4035186625389166979L;

	/**
	 *
	 */
	public SerializationException() {
		super();
	}

	/**
	 * @param msg
	 *            the message
	 */
	public SerializationException(final String msg) {
		super(msg);
	}

	/**
	 * @param t
	 *            the cause
	 */
	public SerializationException(final Throwable t) {
		super(t);
	}

	/**
	 * @param msg
	 *            the message
	 * @param t
	 *            the cause
	 */
	public SerializationException(final String msg, final Throwable t) {
		super(msg, t);
	}

}
