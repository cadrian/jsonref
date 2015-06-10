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
package net.cadrian.jsonref.atomic;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

enum AtomicValue {
	STRING(String.class) {
		@Override
		String toJson(final Object value) {
			return "\"" + ((String) value).replace("\"", "\\\"") + "\"";
		}

		@Override
		Object fromJson0(final String json, final Class<?> type) {
			return json.substring(1, json.length() - 1).replace("\\\"", "\"");
		}
	},
	BYTE(Byte.class, byte.class) {
		@Override
		String toJson(final Object value) {
			return ((Byte) value).toString();
		}

		@Override
		Object fromJson0(final String json, final Class<?> type) {
			return Byte.valueOf(json);
		}
	},
	SHORT(Short.class, short.class) {
		@Override
		String toJson(final Object value) {
			return ((Short) value).toString();
		}

		@Override
		Object fromJson0(final String json, final Class<?> type) {
			return Short.valueOf(json);
		}
	},
	INTEGER(Integer.class, int.class) {
		@Override
		String toJson(final Object value) {
			return ((Integer) value).toString();
		}

		@Override
		Object fromJson0(final String json, final Class<?> type) {
			return Integer.valueOf(json);
		}
	},
	LONG(Long.class, long.class) {
		@Override
		String toJson(final Object value) {
			return ((Long) value).toString();
		}

		@Override
		Object fromJson0(final String json, final Class<?> type) {
			return Long.valueOf(json);
		}
	},
	BOOLEAN(Boolean.class, boolean.class) {
		@Override
		String toJson(final Object value) {
			return ((Boolean) value).toString();
		}

		@Override
		Object fromJson0(final String json, final Class<?> type) {
			return Boolean.valueOf(json);
		}
	},
	FLOAT(Float.class, float.class) {
		@Override
		String toJson(final Object value) {
			return ((Float) value).toString();
		}

		@Override
		Object fromJson0(final String json, final Class<?> type) {
			return Float.valueOf(json);
		}
	},
	DOUBLE(Double.class, double.class) {
		@Override
		String toJson(final Object value) {
			return ((Double) value).toString();
		}

		@Override
		Object fromJson0(final String json, final Class<?> type) {
			return Double.valueOf(json);
		}
	},
	BIG_INTEGER(BigInteger.class) {
		@Override
		String toJson(final Object value) {
			return ((BigInteger) value).toString();
		}

		@Override
		Object fromJson0(final String json, final Class<?> type) {
			return new BigInteger(json);
		}
	},
	BIG_DECIMAL(BigDecimal.class) {
		@Override
		String toJson(final Object value) {
			return ((BigDecimal) value).toString();
		}

		@Override
		Object fromJson0(final String json, final Class<?> type) {
			return new BigDecimal(json);
		}
	},
	DATE(Date.class) {
		private static final String DATE_FORMAT = "'\"'yyyy-MM-dd'T'HH:mm:ss.SSS'\"'";

		@Override
		String toJson(final Object value) {
			return new SimpleDateFormat(DATE_FORMAT).format((Date) value);
		}

		@Override
		Object fromJson0(final String json, final Class<?> type) {
			try {
				final Date date = new SimpleDateFormat(DATE_FORMAT).parse(json);
				if (type == Date.class) {
					return date;
				}
				return type.getDeclaredConstructor(long.class).newInstance(
						date.getTime());
			} catch (final ParseException | InstantiationException
					| IllegalAccessException | IllegalArgumentException
					| InvocationTargetException | NoSuchMethodException
					| SecurityException e) {
				throw new RuntimeException(e);
			}
		}
	};

	private static final Map<Class<?>, AtomicValue> MAP = new HashMap<>();
	private final Set<Class<?>> classes;

	static {
		for (final AtomicValue p : values()) {
			for (final Class<?> c : p.classes) {
				MAP.put(c, p);
			}
		}
	}

	private AtomicValue(final Class<?>... classes) {
		this.classes = new HashSet<Class<?>>(Arrays.asList(classes));
	}

	static AtomicValue get(final Class<?> clazz) {
		AtomicValue result = MAP.get(clazz);
		if (result == null) {
			for (final Map.Entry<Class<?>, AtomicValue> entry : MAP.entrySet()) {
				if (entry.getKey().isAssignableFrom(clazz)) {
					result = entry.getValue();
					break;
				}
			}
		}
		return result;
	}

	abstract String toJson(Object value);

	abstract Object fromJson0(String json, Class<?> type);

	@SuppressWarnings("unchecked")
	<T> T fromJson(final String json, final Class<? extends T> type) {
		assert classes.contains(type);
		return (T) fromJson0(json, type);
	}
}