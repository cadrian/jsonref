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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.lang.reflect.Field;
import java.sql.Timestamp;

import net.cadrian.jsonref.atomic.DefaultJsonConverter;

import org.junit.Test;

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
		final Timestamp ts = new Timestamp(2015, 6, 10, 12, 0, 0, 0);
		a.setTimestamp(ts);

		final String json = ser.toJson(a);
		assertEquals(
				"<{\"class\":\"net.cadrian.jsonref.Pojo\",\"reference\":$1,\"timestamp\":\"3915-07-10T12:00:00.000\",\"value\":\"a\"},{\"class\":\"net.cadrian.jsonref.Pojo\",\"reference\":$0,\"timestamp\":null,\"value\":\"b\"}>",
				json);
	}

	@Test
	public void testNotCycle() {
		final JsonSerializer ser = new JsonSerializer(
				new DefaultJsonConverter() {
					@Override
					public boolean isTransient(final Field field) {
						return super.isTransient(field)
								|| (field.getDeclaringClass() == Pojo.class && field
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
		final Timestamp ts = new Timestamp(2015, 6, 10, 12, 0, 0, 0);
		a.setTimestamp(ts);

		final String json = ser.toJson(a);
		assertEquals(
				"{\"class\":\"net.cadrian.jsonref.Pojo\",\"timestamp\":\"3915-07-10T12:00:00.000\",\"value\":\"a\"}",
				json);

	}

	@Test
	public void testFromJson() {
		final String json = "<{\"class\":\"net.cadrian.jsonref.Pojo\",\"reference\":$1,\"timestamp\":\"3915-07-10T12:00:00.000\",\"value\":\"a\"},{\"class\":\"net.cadrian.jsonref.Pojo\",\"reference\":$0,\"timestamp\":null,\"value\":null}>";
		final JsonSerializer ser = new JsonSerializer();
		final Pojo a = ser.fromJson(json, Pojo.class);
		final Pojo b = a.getReference();

		@SuppressWarnings("deprecation")
		final Timestamp ts = new Timestamp(2015, 6, 10, 12, 0, 0, 0);
		assertEquals(ts, a.getTimestamp());
		assertEquals("a", a.getValue());
		assertNull(b.getTimestamp());
		assertSame(a, b.getReference());
		assertNull(b.getValue());
	}
}
