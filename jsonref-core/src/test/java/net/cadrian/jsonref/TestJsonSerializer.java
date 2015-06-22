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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

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
					public boolean isTransient(final PropertyDescriptor pd,
							final Field propertyField, final Context context) {
						return super.isTransient(pd, propertyField, context)
								|| (propertyField.getDeclaringClass() == Pojo.class && propertyField
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
				if (invocation.getArgumentAt(0, PropertyDescriptor.class)
						.getName().equals("reference")) {
					final Object o2 = invocation.getArgumentAt(2, Object.class);
					final Object o3 = invocation.getArgumentAt(3, Object.class);
					startList.add(new Object[] { o2, o3 });
				}
				return null;
			}
		}).when(converter).nestIn(any(PropertyDescriptor.class),
				any(Field.class), any(Object.class), any(Object.class),
				eq(context));

		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(final InvocationOnMock invocation)
					throws Throwable {
				if (invocation.getArgumentAt(0, PropertyDescriptor.class)
						.getName().equals("reference")) {
					final Object o2 = invocation.getArgumentAt(2, Object.class);
					final Object o3 = invocation.getArgumentAt(3, Object.class);
					endList.add(new Object[] { o2, o3 });
				}
				return null;
			}
		}).when(converter).nestOut(any(PropertyDescriptor.class),
				any(Field.class), any(Object.class), any(Object.class),
				eq(context));

		when(
				converter.isTransient(any(PropertyDescriptor.class),
						any(Field.class), eq(context))).thenAnswer(
								new Answer<Boolean>() {
									@Override
									public Boolean answer(final InvocationOnMock invocation)
											throws Throwable {
										assertTrue(endList.size() <= startList.size());
										final boolean result;
										final PropertyDescriptor pd = invocation.getArgumentAt(
												0, PropertyDescriptor.class);
										if (pd.getPropertyType() == Pojo.class) {
											result = startList.size() >= 2;
											if (result) {
												assertArrayEquals(new Object[] { a, b },
														startList.get(0));
												assertArrayEquals(new Object[] { b, a },
														startList.get(1));
											}
										} else {
											result = defaultConverter.isTransient(pd,
													invocation.getArgumentAt(1, Field.class),
													context);
										}
										return result;
									}
								});

		when(
				converter.getPropertyType(any(PropertyDescriptor.class),
						any(Field.class), eq(context))).then(
								new Answer<Class<?>>() {
									@Override
									public Class<?> answer(final InvocationOnMock invocation)
											throws Throwable {
										final PropertyDescriptor pd = invocation.getArgumentAt(
												0, PropertyDescriptor.class);
										return pd.getPropertyType();
									}
								});
		when(
				converter.getPropertyValue(any(PropertyDescriptor.class),
						any(Object.class), eq(context))).thenAnswer(
								new Answer<Object>() {
									@Override
									public Object answer(final InvocationOnMock invocation)
											throws Throwable {
										final PropertyDescriptor pd = invocation.getArgumentAt(
												0, PropertyDescriptor.class);
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
