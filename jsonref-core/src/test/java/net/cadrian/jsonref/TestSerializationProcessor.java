package net.cadrian.jsonref;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

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

}
