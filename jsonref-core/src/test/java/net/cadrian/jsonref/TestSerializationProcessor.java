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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TestSerializationProcessor {

	private SerializationProcessor that;

	@Mock
	private JsonConverter converter;

	@Before
	public void setup() {
		that = new SerializationProcessor();
	}

	@Test
	public void testInteger() {
		final Prettiness.Context context = Prettiness.COMPACT.newContext();
		when(converter.isAtomicValue(Integer.class)).thenReturn(true);
		when(converter.toJson(42)).thenReturn("foo");
		final String json = that.serialize(42, converter, context);
		assertEquals("foo", json);
	}

	@Test
	public void testString() {
		final Prettiness.Context context = Prettiness.COMPACT.newContext();
		when(converter.isAtomicValue(String.class)).thenReturn(true);
		when(converter.toJson("foo")).thenReturn("bar");
		final String json = that.serialize("foo", converter, context);
		assertEquals("bar", json);
	}

	@Test
	public void testDate() {
		final Prettiness.Context context = Prettiness.COMPACT.newContext();
		final Date now = new Date();
		when(converter.isAtomicValue(Date.class)).thenReturn(true);
		when(converter.toJson(now)).thenReturn("now");
		final String json = that.serialize(now, converter, context);
		assertEquals("now", json);
	}

	@Test
	public void testArray() {
		final Date now = new Date();
		final String string = "string";
		when(converter.isAtomicValue(Date.class)).thenReturn(true);
		when(converter.isAtomicValue(String.class)).thenReturn(true);
		when(converter.toJson(now)).thenReturn("now");
		when(converter.toJson(string)).thenReturn("string");
		final String jsonCompact = that.serialize(new Object[] { now, string },
				converter, Prettiness.COMPACT.newContext());
		assertEquals("[now,string]", jsonCompact);
		final String jsonLegible = that.serialize(new Object[] { now, string },
				converter, Prettiness.LEGIBLE.newContext());
		assertEquals("[now, string]", jsonLegible);
		final String jsonIndented = that.serialize(
				new Object[] { now, string }, converter,
				Prettiness.INDENTED.newContext());
		assertEquals("[\n    now,\n    string\n]", jsonIndented);
	}

	@Test
	public void testCollection() {
		final Date now = new Date();
		final String string = "string";
		when(converter.isAtomicValue(Date.class)).thenReturn(true);
		when(converter.isAtomicValue(String.class)).thenReturn(true);
		when(converter.toJson(now)).thenReturn("now");
		when(converter.toJson(string)).thenReturn("string");
		final String jsonCompact = that.serialize(
				Arrays.asList(new Object[] { now, string }), converter,
				Prettiness.COMPACT.newContext());
		assertEquals("[now,string]", jsonCompact);
		final String jsonLegible = that.serialize(
				Arrays.asList(new Object[] { now, string }), converter,
				Prettiness.LEGIBLE.newContext());
		assertEquals("[now, string]", jsonLegible);
		final String jsonIndented = that.serialize(
				Arrays.asList(new Object[] { now, string }), converter,
				Prettiness.INDENTED.newContext());
		assertEquals("[\n    now,\n    string\n]", jsonIndented);
	}

	@Test
	public void testMapOfStrings() {
		final Date now = new Date();
		final String string = "string";
		final String k1 = "foo";
		final String k2 = "bar";
		when(converter.isAtomicValue(Date.class)).thenReturn(true);
		when(converter.isAtomicValue(String.class)).thenReturn(true);
		when(converter.toJson(now)).thenReturn("now");
		when(converter.toJson(string)).thenReturn("string");
		when(converter.toJson(k1)).thenReturn("1");
		when(converter.toJson(k2)).thenReturn("2");
		final Map<Object, Object> map = new HashMap<>();
		map.put(k1, now);
		map.put(k2, string);
		final String jsonCompact = that.serialize(map, converter,
				Prettiness.COMPACT.newContext());
		assertTrue(jsonCompact.equals("{1:now,2:string}")
				|| jsonCompact.equals("{2:string,1:now}"));
		final String jsonLegible = that.serialize(map, converter,
				Prettiness.LEGIBLE.newContext());
		assertTrue(jsonLegible.equals("{1: now, 2: string}")
				|| jsonLegible.equals("{2: string, 1: now}"));
		final String jsonIntented = that.serialize(map, converter,
				Prettiness.INDENTED.newContext());
		assertTrue(jsonIntented.equals("{\n    1: now,\n    2: string\n}")
				|| jsonIntented.equals("{\n    2: string,\n    1: now\n}"));
	}

	@Test
	public void testMapOfObjects() {
		final Date now = new Date();
		final String string = "string";
		final int k1 = 1;
		final float k2 = 2;
		when(converter.isAtomicValue(Date.class)).thenReturn(true);
		when(converter.isAtomicValue(String.class)).thenReturn(true);
		when(converter.isAtomicValue(Integer.class)).thenReturn(true);
		when(converter.isAtomicValue(Float.class)).thenReturn(true);
		when(converter.toJson(now)).thenReturn("now");
		when(converter.toJson(string)).thenReturn("string");
		when(converter.toJson(k1)).thenReturn("1");
		when(converter.toJson(k2)).thenReturn("2");
		final Map<Object, Object> map = new HashMap<>();
		map.put(k1, now);
		map.put(k2, string);
		final String jsonCompact = that.serialize(map, converter,
				Prettiness.COMPACT.newContext());
		assertTrue(jsonCompact.equals("[[1,now],[2,string]]")
				|| jsonCompact.equals("[[2,string],[1,now]]"));
		final String jsonLegible = that.serialize(map, converter,
				Prettiness.LEGIBLE.newContext());
		assertTrue(jsonLegible.equals("[[1, now], [2, string]]")
				|| jsonLegible.equals("[[2, string], [1, now]]"));
		final String jsonIndented = that.serialize(map, converter,
				Prettiness.INDENTED.newContext());
		assertTrue(jsonIndented
				.equals("[\n    [\n        1,\n        now\n    ],\n    [\n        2,\n        string\n    ]\n]")
				|| jsonIndented
				.equals("[\n    [\n        2,\n        string\n    ],\n    [\n        1,\n        now\n    ]\n]"));
	}

}
