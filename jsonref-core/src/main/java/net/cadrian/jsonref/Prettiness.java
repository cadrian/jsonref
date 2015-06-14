package net.cadrian.jsonref;

import java.util.Collection;

/**
 * JSON/R "prettiness" level
 */
public enum Prettiness {
	/**
	 * Produce compact JSON/R: no spaces at all
	 */
	COMPACT {
		@Override
		<T> void toJson(final StringBuilder result, final Collection<T> values,
				final Serializer<T> serializer, final Context context) {
			String sep = "";
			for (final T value : values) {
				result.append(sep);
				serializer.toJson(result, value, this);
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
		<T> void toJson(final StringBuilder result, final Collection<T> values,
				final Serializer<T> serializer, final Context context) {
			String sep = "";
			for (final T value : values) {
				result.append(sep);
				serializer.toJson(result, value, this);
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
		<T> void toJson(final StringBuilder result, final Collection<T> values,
				final Serializer<T> serializer, final Context context) {
			final IndentationContext iContext = (IndentationContext) context;
			iContext.more();
			String sep = "";
			for (final T value : values) {
				result.append(sep);
				iContext.indent(result);
				serializer.toJson(result, value, this);
				sep = ",";
			}
			iContext.less();
			iContext.indent(result);
		}

		@Override
		public Context newContext() {
			return new IndentationContext(this);
		}
	};

	public static interface Serializer<T> {
		void toJson(StringBuilder result, T value, Prettiness level);
	}

	public static interface Context {
		Prettiness getPrettiness();

		public <T> void toJson(final StringBuilder result,
				final Collection<T> values, Serializer<T> serializer);
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
		public <T> void toJson(final StringBuilder result,
				final Collection<T> values, final Serializer<T> serializer) {
			prettiness.toJson(result, values, serializer, this);
		}
	}

	private static class IndentationContext extends PrettinessContext {
		private int indent;

		IndentationContext(final Prettiness prettiness) {
			super(prettiness);
		}

		void indent(final StringBuilder result) {
			result.append('\n');
			for (int i = 0; i < indent; i++) {
				result.append("    ");
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

	public abstract Context newContext();

	abstract <T> void toJson(final StringBuilder result,
			final Collection<T> values, Serializer<T> serializer,
			Context context);
}
