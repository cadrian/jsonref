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

import java.util.Date;

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

}
