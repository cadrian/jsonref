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
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TestDeserializationProcessor {

	private DeserializationProcessor that;

	@Mock
	private JsonConverter converter;

	@Before
	public void setup() {
		that = new DeserializationProcessor();
	}

	@Test
	public void testInteger() {
		when(converter.fromJson("42", Integer.class)).thenReturn(42);
		when(converter.fromJson("42", int.class)).thenReturn(13);
		when(converter.fromJson("42", null)).thenReturn(421);

		Integer i = that.deserialize("42", converter, Integer.class);
		assertEquals(Integer.valueOf(42), i);

		i = that.deserialize("42", converter, int.class);
		assertEquals(13, i.intValue());

		i = that.deserialize("42", converter, null);
		assertEquals(421, i.intValue());
	}

	@Test
	public void testString() {
		when(converter.fromJson("\"foo\"", String.class)).thenReturn("bar");

		final String s = that.deserialize("\"foo\"", converter, String.class);
		assertEquals("bar", s);
	}

	@Test
	public void testDate() {
		final Date now = new Date();
		when(converter.fromJson("\"foo\"", Date.class)).thenReturn(now);

		final Date d = that.deserialize("\"foo\"", converter, Date.class);
		assertEquals(now, d);
	}

	@Test
	public void testArrayOfObjects() {
		final Date now = new Date();
		when(converter.fromJson("\"now\"", Object.class)).thenReturn(now);
		final String string = "foobar";
		when(converter.fromJson("\"string\"", Object.class)).thenReturn(string);

		final Object[] objects = that.deserialize("[\"now\",\"string\"]",
				converter, Object[].class);
		assertEquals(now, objects[0]);
		assertEquals(string, objects[1]);
	}

	@Test
	public void testArrayOfStrings() {
		final String string1 = "foo";
		when(converter.fromJson("\"string1\"", String.class)).thenReturn(
				string1);
		final String string2 = "bar";
		when(converter.fromJson("\"string2\"", String.class)).thenReturn(
				string2);

		final String[] strings = that.deserialize("[\"string1\",\"string2\"]",
				converter, String[].class);
		assertEquals(string1, strings[0]);
		assertEquals(string2, strings[1]);
	}

	@Test
	public void testCollection() {
		final String string1 = "foo";
		when(converter.fromJson("\"string1\"", null)).thenReturn(string1);
		final String string2 = "bar";
		when(converter.fromJson("\"string2\"", null)).thenReturn(string2);
		@SuppressWarnings("unchecked")
		final Class<? extends Collection<Object>> collectionOfObjectsType = (Class<? extends Collection<Object>>) Collection.class;
		when(converter.newCollection(collectionOfObjectsType)).thenReturn(
				new ArrayList<Object>());

		final Collection<?> objects = that.deserialize(
				"[\"string1\",\"string2\"]", converter, Collection.class);
		assertEquals(2, objects.size());
		final Iterator<?> iterator = objects.iterator();
		assertEquals(string1, iterator.next());
		assertEquals(string2, iterator.next());
	}

	@Test
	public void testMapOfStrings() {
		// TODO
	}

	@Test
	public void testMapOfObjects() {
		// TODO
	}

}
