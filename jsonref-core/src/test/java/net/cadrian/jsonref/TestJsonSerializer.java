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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import net.cadrian.jsonref.JsonConverter.Context;
import net.cadrian.jsonref.atomic.DefaultJsonConverter;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class TestJsonSerializer {

	@Test
	public void testCycleToJson() {
		final JsonSerializer ser = new JsonSerializer();
		final Pojo a = new Pojo();
		a.setValue("a");
		final Pojo b = new Pojo();
		b.setValue("b");
		a.setReference(b);
		b.setReference(a);

		@SuppressWarnings("deprecation")
		final Timestamp ts = new Timestamp(115, 5, 10, 12, 0, 0, 0);
		a.setTimestamp(ts);

		final String json = ser.toJson(a);
		assertEquals(
				"<{\"class\":\"net.cadrian.jsonref.Pojo\",\"reference\":$1,\"timestamp\":\"2015-06-10T12:00:00.000\",\"value\":\"a\"},{\"class\":\"net.cadrian.jsonref.Pojo\",\"reference\":$0,\"timestamp\":null,\"value\":\"b\"}>",
				json);
	}

	@Test
	public void testNotCycle() {
		final JsonSerializer ser = new JsonSerializer(
				new DefaultJsonConverter() {
					@Override
					public boolean isTransient(final Context context) {
						if (super.isTransient(context)) {
							return true;
						}
						final Field propertyField = context.getPropertyField();
						return (propertyField.getDeclaringClass() == Pojo.class && propertyField
								.getType() == Pojo.class);
					}
				});
		final Pojo a = new Pojo();
		a.setValue("a");
		final Pojo b = new Pojo();
		b.setValue("b");

		a.setReference(b);
		b.setReference(a);

		@SuppressWarnings("deprecation")
		final Timestamp ts = new Timestamp(115, 5, 10, 12, 0, 0, 0);
		a.setTimestamp(ts);

		final String json = ser.toJson(a);
		assertEquals(
				"{\"class\":\"net.cadrian.jsonref.Pojo\",\"timestamp\":\"2015-06-10T12:00:00.000\",\"value\":\"a\"}",
				json);

	}

	@Test
	public void testComplexCycleBreak() {
		final DefaultJsonConverter defaultConverter = new DefaultJsonConverter();
		final JsonConverter converter = mock(JsonConverter.class);
		final JsonConverter.Context context = mock(JsonConverter.Context.class);

		final Answer<JsonConverter.Context> contextWithProperty = new Answer<JsonConverter.Context>() {
			@Override
			public Context answer(final InvocationOnMock invocation)
					throws Throwable {
				final PropertyDescriptor pd = invocation.getArgumentAt(0,
						PropertyDescriptor.class);
				final Field pf = invocation.getArgumentAt(1, Field.class);
				final JsonConverter.Context result = mock(JsonConverter.Context.class);
				when(result.getPropertyDescriptor()).thenReturn(pd);
				when(result.getPropertyField()).thenReturn(pf);
				when(
						result.withProperty(any(PropertyDescriptor.class),
								any(Field.class))).thenAnswer(this);
				return result;
			}
		};
		when(
				context.withProperty(any(PropertyDescriptor.class),
						any(Field.class))).thenAnswer(contextWithProperty);

		final JsonSerializer ser = new JsonSerializer(converter);
		when(converter.getNewContext()).thenReturn(context,
				(JsonConverter.Context) null);

		final Pojo a = new Pojo();
		a.setValue("a");
		final Pojo b = new Pojo();
		b.setValue("b");

		a.setReference(b);
		b.setReference(a);

		final List<Object[]> startList = new ArrayList<>();
		final List<Object[]> endList = new ArrayList<>();

		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(final InvocationOnMock invocation)
					throws Throwable {
				final JsonConverter.Context context = invocation.getArgumentAt(
						0, JsonConverter.Context.class);
				if ("reference".equals(context.getPropertyDescriptor()
						.getName())) {
					final Object o0 = invocation.getArgumentAt(1, Object.class);
					final Object o1 = invocation.getArgumentAt(2, Object.class);
					startList.add(new Object[] { o0, o1 });
				}
				return null;
			}
		}).when(converter).nestIn(any(JsonConverter.Context.class),
				any(Object.class), any(Object.class));

		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(final InvocationOnMock invocation)
					throws Throwable {
				final JsonConverter.Context context = invocation.getArgumentAt(
						0, JsonConverter.Context.class);
				if ("reference".equals(context.getPropertyDescriptor()
						.getName())) {
					final Object o0 = invocation.getArgumentAt(1, Object.class);
					final Object o1 = invocation.getArgumentAt(2, Object.class);
					endList.add(new Object[] { o0, o1 });
				}
				return null;
			}
		}).when(converter).nestOut(any(JsonConverter.Context.class),
				any(Object.class), any(Object.class));

		when(converter.isTransient(any(JsonConverter.Context.class)))
				.thenAnswer(new Answer<Boolean>() {
					@Override
					public Boolean answer(final InvocationOnMock invocation)
							throws Throwable {
						assertTrue(endList.size() <= startList.size());
						final boolean result;
						final JsonConverter.Context context = invocation
								.getArgumentAt(0, JsonConverter.Context.class);
						final PropertyDescriptor pd = context
								.getPropertyDescriptor();
						if (pd.getPropertyType() == Pojo.class) {
							result = startList.size() >= 2;
							if (result) {
								assertArrayEquals(new Object[] { a, b },
										startList.get(0));
								assertArrayEquals(new Object[] { b, a },
										startList.get(1));
							}
						} else {
							result = defaultConverter.isTransient(context);
						}
						return result;
					}
				});

		when(converter.getPropertyType(any(JsonConverter.Context.class))).then(
				new Answer<Class<?>>() {
					@Override
					public Class<?> answer(final InvocationOnMock invocation)
							throws Throwable {
						final JsonConverter.Context context = invocation
								.getArgumentAt(0, JsonConverter.Context.class);
						return context.getPropertyDescriptor()
								.getPropertyType();
					}
				});
		when(
				converter.getPropertyValue(any(JsonConverter.Context.class),
						any(Object.class))).thenAnswer(new Answer<Object>() {
			@Override
			public Object answer(final InvocationOnMock invocation)
					throws Throwable {
				final JsonConverter.Context context = invocation.getArgumentAt(
						0, JsonConverter.Context.class);
				final PropertyDescriptor pd = context.getPropertyDescriptor();
				if (pd.getPropertyType() == Pojo.class) {
					final Pojo p = (Pojo) pd.getReadMethod().invoke(
							invocation.getArgumentAt(1, Object.class));
					if (p == null) {
						return p;
					}
					final Pojo result = new Pojo();
					result.setValue(p.getValue());
					result.setTimestamp(p.getTimestamp());
					return result;
				}
				return pd.getReadMethod().invoke(
						invocation.getArgumentAt(1, Object.class));
			}
		});
		when(converter.isAtomicValue(any(Class.class))).thenAnswer(
				new Answer<Boolean>() {
					@Override
					public Boolean answer(final InvocationOnMock invocation)
							throws Throwable {
						final Class<?> type = invocation.getArgumentAt(0,
								Class.class);
						return defaultConverter.isAtomicValue(type);
					}
				});
		when(converter.toJson(any(Object.class))).thenAnswer(
				new Answer<Object>() {
					@Override
					public Object answer(final InvocationOnMock invocation)
							throws Throwable {
						final Object value = invocation.getArgumentAt(0,
								Object.class);
						return defaultConverter.toJson(value);
					}
				});

		@SuppressWarnings("deprecation")
		final Timestamp tsa = new Timestamp(115, 5, 10, 12, 0, 0, 0);
		a.setTimestamp(tsa);
		@SuppressWarnings("deprecation")
		final Timestamp tsb = new Timestamp(115, 5, 22, 12, 0, 0, 0);
		b.setTimestamp(tsb);

		final String json = ser.toJson(a);
		assertEquals(
				"<{\"class\":\"net.cadrian.jsonref.Pojo\",\"reference\":$1,\"timestamp\":\"2015-06-10T12:00:00.000\",\"value\":\"a\"},{\"class\":\"net.cadrian.jsonref.Pojo\",\"reference\":null,\"timestamp\":\"2015-06-22T12:00:00.000\",\"value\":\"b\"}>",
				json);
	}

	@Test
	public void testFromJson() {
		final String json = "<{\"class\":\"net.cadrian.jsonref.Pojo\",\"reference\":$1,\"timestamp\":\"2015-06-10T12:00:00.000\",\"value\":\"a\"},{\"class\":\"net.cadrian.jsonref.Pojo\",\"reference\":$0,\"timestamp\":null,\"value\":null}>";
		final JsonSerializer ser = new JsonSerializer();
		final Pojo a = ser.fromJson(json, Pojo.class);
		final Pojo b = a.getReference();

		@SuppressWarnings("deprecation")
		final Timestamp ts = new Timestamp(115, 5, 10, 12, 0, 0, 0);
		assertEquals(ts, a.getTimestamp());
		assertEquals("a", a.getValue());
		assertNull(b.getTimestamp());
		assertSame(a, b.getReference());
		assertNull(b.getValue());
	}
}
