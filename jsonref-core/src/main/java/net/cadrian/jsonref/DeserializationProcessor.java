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

import java.util.ArrayList;
import java.util.List;

import net.cadrian.jsonref.data.AbstractSerializationObject;
import net.cadrian.jsonref.data.SerializationArray;
import net.cadrian.jsonref.data.SerializationHeap;
import net.cadrian.jsonref.data.SerializationObject;
import net.cadrian.jsonref.data.SerializationRef;
import net.cadrian.jsonref.data.SerializationValue;

class DeserializationProcessor {

	private static final char[] CONST_NULL = new char[] { 'n', 'u', 'l', 'l' };
	private static final char[] CONST_FALSE = new char[] { 'f', 'a', 'l', 's',
	'e' };
	private static final char[] CONST_TRUE = new char[] { 't', 'r', 'u', 'e' };

	private static class DeserializationContext {
		private final char[] chars;
		private int index;
		private int ref;

		DeserializationContext(final String jsonR) {
			chars = jsonR.toCharArray();
			index = 0;
		}

		void next() {
			index++;
		}

		boolean isValid() {
			return index < chars.length;
		}

		char get() {
			return chars[index];
		}

		int getIndex() {
			return index;
		}

		public int getRef() {
			return ref;
		}

		public void setRef(final int ref) {
			this.ref = ref;
		}

	}

	private static class ParseException extends RuntimeException {
		private static final long serialVersionUID = 6155459663107862353L;

		ParseException(final String msg) {
			super(msg);
		}
	}

	/**
	 * Deserialize a JSON/R object graph back to Java objects
	 *
	 * @param jsonR
	 *            the JSON/R object graph
	 * @param converter
	 *            the Javers converter
	 * @return the Java object
	 */
	public Object deserialize(final String jsonR,
			final JsonAtomicValues converter) {
		final DeserializationContext context = new DeserializationContext(jsonR);
		final SerializationData data = parse(context, converter);
		return data.fromJson(converter);
	}

	private SerializationData parse(final DeserializationContext context,
			final JsonAtomicValues converter) {
		SerializationData result = null;
		if (context.isValid()) {
			switch (context.get()) {
			case '<':
				result = parseHeap(context, converter);
				break;
			case '{':
				result = parseObject(context, converter);
				break;
			case '[':
				result = parseArray(context, converter);
				break;
			case '$':
				result = parseReference(context);
				break;
			case '"':
				result = parseString(context);
				break;
			case 't':
				result = parseConst(context, CONST_TRUE, "true");
				break;
			case 'f':
				result = parseConst(context, CONST_FALSE, "false");
				break;
			case 'n':
				result = parseConst(context, CONST_NULL, "null");
				break;
			default:
				if (Character.isDigit(context.get())) {
					result = parseNumber(context);
				} else {
					throw new ParseException("unexpected character '"
							+ context.get() + "' at " + context.getIndex());
				}
			}
		}
		return result;
	}

	private SerializationHeap parseHeap(final DeserializationContext context,
			final JsonAtomicValues converter) {
		final List<SerializationData> dataList = parseDataList(context, "heap",
				'<', '>', converter);
		final SerializationHeap result = new SerializationHeap();
		for (final SerializationData data : dataList) {
			result.add((AbstractSerializationObject) data);
		}
		return result;
	}

	private SerializationObject parseObject(
			final DeserializationContext context,
			final JsonAtomicValues converter) {
		assert context.isValid() && context.get() == '{' : "unexpected character";

		final SerializationObject result = new SerializationObject(null,
				context.getRef());
		int state = 1;
		String property = null;
		context.next(); // skip '{'
		while (state > 0) {
			if (!context.isValid()) {
				throw new ParseException("invalid object: not terminated at "
						+ context.getIndex());
			}
			switch (state) {
			case 1:
				if (context.get() != '"') {
					throw new ParseException(
							"invalid object: unexpected character '"
									+ context.get() + "' instead of '\"' at "
									+ context.getIndex());
				}
				property = converter.fromJson(parseString0(context),
						String.class);
				if (result.contains(property)) {
					throw new ParseException(
							"invalid object: duplicated property \"" + property
							+ "\" at " + context.getIndex());
				}
				state = 2;
				break;
			case 2:
				if (context.get() == ':') {
					state = 3;
					context.next();
				} else {
					throw new ParseException(
							"invalid object: unexpected character '"
									+ context.get() + "' instead of ':' at "
									+ context.getIndex());
				}
				break;
			case 3:
				result.add(property, parse(context, converter));
				state = 4;
				break;
			case 4:
				switch (context.get()) {
				case ',':
					state = 1;
					break;
				case '}':
					state = 0;
					break;
				default:
					throw new ParseException(
							"invalid object: unexpected character '"
									+ context.get()
									+ "' instead of ',' or '}' at "
									+ context.getIndex());
				}
				context.next();
				break;
			}
		}
		return result;
	}

	private SerializationArray parseArray(final DeserializationContext context,
			final JsonAtomicValues converter) {
		final List<SerializationData> dataList = parseDataList(context,
				"array", '[', ']', converter);
		final SerializationArray result = new SerializationArray(
				dataList.size(), null, context.getRef());
		for (final SerializationData data : dataList) {
			result.add(data);
		}
		return result;
	}

	private List<SerializationData> parseDataList(
			final DeserializationContext context, final String type,
			final char open, final char close, final JsonAtomicValues converter) {
		assert context.isValid() && context.get() == open : "unexpected character";
		assert close == ']' || close == '>' : "bad close character";

		final List<SerializationData> dataList = new ArrayList<>();
		int state = 1;
		context.next(); // skip open
		while (state > 0) {
			if (!context.isValid()) {
				throw new ParseException("invalid " + type
						+ ": not terminated at " + context.getIndex());
			}
			switch (state) {
			case 1:
				if (open == '<') { // it's a heap
					context.setRef(dataList.size());
				}
				dataList.add(parse(context, converter));
				state = 2;
				break;
			case 2:
				final char c = context.get();
				switch (c) {
				case ',':
					state = 1;
					break;
				case ']':
				case '>':
					if (c != close) {
						throw new ParseException("invalid " + type
								+ ": unexpected character '" + context.get()
								+ "' instead of ',' or '" + close + "' at "
								+ context.getIndex());
					}
					state = 0;
					break;
				default:
					throw new ParseException("invalid " + type
							+ ": unexpected character '" + context.get()
							+ "' instead of ',' or '" + close + "' at "
							+ context.getIndex());
				}
				context.next();
				break;
			}
		}
		return dataList;
	}

	private SerializationRef parseReference(final DeserializationContext context) {
		assert context.isValid() && context.get() == '$' : "unexpected character";

		final StringBuilder value = new StringBuilder();
		context.next(); // skip '$'
		if (!context.isValid() || !Character.isDigit(context.get())) {
			throw new ParseException("invalid reference at "
					+ context.getIndex());
		}
		do {
			value.append(context.get());
			context.next();
		} while (context.isValid() && Character.isDigit(context.get()));
		return new SerializationRef(Integer.parseInt(value.toString()));
	}

	private SerializationValue parseString(final DeserializationContext context) {
		return new SerializationValue(null, parseString0(context));
	}

	private String parseString0(final DeserializationContext context) {
		assert context.isValid() && context.get() == '"' : "unexpected character";

		int state = 1;
		final StringBuilder value = new StringBuilder("\"");
		context.next(); // skip '"'
		while (state > 0) {
			if (!context.isValid()) {
				throw new ParseException("invalid string at "
						+ context.getIndex());
			}
			final char c = context.get();
			value.append(c);
			switch (state) {
			case 1: // normal characters
				switch (c) {
				case '"':
					state = 0;
					break;
				case '\\':
					state = 2;
					break;
				}
				break;
			case 2: // escaped character
				state = 0;
				break;
			}
			context.next();
		}
		return value.toString();
	}

	private SerializationValue parseConst(final DeserializationContext context,
			final char[] string, final String object) {
		assert string.length > 0 && context.isValid()
		&& context.get() == string[0] : "unexpected character";

		for (int i = 0; i < string.length; i++) {
			if (!context.isValid() || context.get() != string[i]) {
				throw new ParseException(
						"invalid const, unexpected character '" + context.get()
						+ "' instead of '" + string[i] + "' at "
						+ context.getIndex());
			}
			context.next();
		}
		return new SerializationValue(null, object);
	}

	private SerializationValue parseNumber(final DeserializationContext context) {
		assert context.isValid() && Character.isDigit(context.get()) : "unexpected character";

		final StringBuilder value = new StringBuilder();
		do {
			value.append(context.get());
			context.next();
		} while (context.isValid() && Character.isDigit(context.get()));
		if (context.isValid() && context.get() == '.') {
			value.append('.');
			context.next();
			if (!context.isValid()) {
				throw new ParseException(
						"invalid number, unterminated after '.' at "
								+ context.getIndex());
			}
			if (!Character.isDigit(context.get())) {
				throw new ParseException(
						"invalid number, unexpected character '"
								+ context.get() + "' after '.' at "
								+ context.getIndex());
			}
			do {
				value.append(context.get());
				context.next();
			} while (context.isValid() && Character.isDigit(context.get()));
		}
		return new SerializationValue(null, value.toString());
	}

}
