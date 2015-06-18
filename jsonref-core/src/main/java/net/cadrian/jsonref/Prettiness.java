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

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

/**
 * JSON/R "prettiness" level used for
 * {@linkplain JsonSerializer#toJson(Object, Prettiness) serialization}
 */
public enum Prettiness {
	/**
	 * Produce compact JSON/R: no spaces at all
	 */
	COMPACT {
		@Override
		<T> void toJson(final Writer out, final Collection<T> values,
				final Serializer<T> serializer, final Context context)
				throws IOException {
			String sep = "";
			for (final T value : values) {
				out.append(sep);
				serializer.toJson(out, value, this);
				sep = ",";
			}
		}

		@Override
		public Context newContext() {
			return new PrettinessContext(this);
		}
	},

	/**
	 * Produce legible JSON/R: just a few spaces where needed
	 */
	LEGIBLE {
		@Override
		<T> void toJson(final Writer out, final Collection<T> values,
				final Serializer<T> serializer, final Context context)
				throws IOException {
			String sep = "";
			for (final T value : values) {
				out.append(sep);
				serializer.toJson(out, value, this);
				sep = ", ";
			}
		}

		@Override
		public Context newContext() {
			return new PrettinessContext(this);
		}
	},

	/**
	 * Produce "pretty" JSON/R: with newlines and indentations all over the
	 * place
	 */
	INDENTED {
		@Override
		<T> void toJson(final Writer out, final Collection<T> values,
				final Serializer<T> serializer, final Context context)
						throws IOException {
			final IndentationContext iContext = (IndentationContext) context;
			iContext.more();
			String sep = "";
			for (final T value : values) {
				out.append(sep);
				iContext.indent(out);
				serializer.toJson(out, value, this);
				sep = ",";
			}
			iContext.less();
			iContext.indent(out);
		}

		@Override
		public Context newContext() {
			return new IndentationContext(this);
		}
	};

	/**
	 * Prettiness serialization interface
	 *
	 * @param <T>
	 *            the type of the serialized objects
	 */
	public static interface Serializer<T> {
		void toJson(Writer out, T value, Prettiness level) throws IOException;
	}

	/**
	 * Prettiness context; used e.g. to keep around the indentation level for
	 * indented code
	 */
	public static interface Context {
		Prettiness getPrettiness();

		/**
		 * Serialize a collection to JSON/R
		 *
		 * @param out
		 *            the JSON/R stream to append to
		 * @param values
		 *            the values to serialize
		 * @param serializer
		 *            used to serialize each value
		 * @param <T>
		 *            the type of the values in the collection
		 * @param IOException
		 *            on I/O exception
		 */
		public <T> void toJson(final Writer out, final Collection<T> values,
				Serializer<T> serializer) throws IOException;
	}

	private static class PrettinessContext implements Context {
		private final Prettiness prettiness;

		PrettinessContext(final Prettiness prettiness) {
			this.prettiness = prettiness;
		}

		@Override
		public Prettiness getPrettiness() {
			return prettiness;
		}

		@Override
		public <T> void toJson(final Writer out, final Collection<T> values,
				final Serializer<T> serializer) throws IOException {
			prettiness.toJson(out, values, serializer, this);
		}
	}

	private static class IndentationContext extends PrettinessContext {
		private int indent;

		IndentationContext(final Prettiness prettiness) {
			super(prettiness);
		}

		void indent(final Writer out) throws IOException {
			out.append('\n');
			for (int i = 0; i < indent; i++) {
				out.append("    ");
			}
		}

		void more() {
			indent++;
		}

		void less() {
			assert indent > 0;
			indent--;
		}
	}

	/**
	 * @return a new context for the given prettiness level
	 */
	public abstract Context newContext();

	abstract <T> void toJson(final Writer out, final Collection<T> values,
			Serializer<T> serializer, Context context) throws IOException;
}
