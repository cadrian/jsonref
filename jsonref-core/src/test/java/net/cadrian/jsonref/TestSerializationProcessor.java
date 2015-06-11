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
	private JsonAtomicValues converter;

	@Before
	public void setup() {
		that = new SerializationProcessor();
	}

	@Test
	public void testInteger() {
		when(converter.isAtomicValue(Integer.class)).thenReturn(true);
		when(converter.toJson(42)).thenReturn("foo");
		final String json = that.serialize(42, converter);
		assertEquals("foo", json);
	}

	@Test
	public void testString() {
		when(converter.isAtomicValue(String.class)).thenReturn(true);
		when(converter.toJson("foo")).thenReturn("bar");
		final String json = that.serialize("foo", converter);
		assertEquals("bar", json);
	}

	@Test
	public void testDate() {
		final Date now = new Date();
		when(converter.isAtomicValue(Date.class)).thenReturn(true);
		when(converter.toJson(now)).thenReturn("now");
		final String json = that.serialize(now, converter);
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
		final String json = that.serialize(new Object[] { now, string },
				converter);
		assertEquals("[now,string]", json);
	}

	@Test
	public void testCollection() {
		final Date now = new Date();
		final String string = "string";
		when(converter.isAtomicValue(Date.class)).thenReturn(true);
		when(converter.isAtomicValue(String.class)).thenReturn(true);
		when(converter.toJson(now)).thenReturn("now");
		when(converter.toJson(string)).thenReturn("string");
		final String json = that.serialize(
				Arrays.asList(new Object[] { now, string }), converter);
		assertEquals("[now,string]", json);
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
		final String json = that.serialize(map, converter);
		assertTrue(json.equals("{1:now,2:string}")
				|| json.equals("{2:string,1:now}"));
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
		final String json = that.serialize(map, converter);
		assertTrue(json.equals("[[1,now],[2,string]]")
				|| json.equals("[[2,string],[1,now]]"));
	}

}
