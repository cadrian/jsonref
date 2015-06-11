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
package net.cadrian.jsonref.data;

import java.util.HashMap;
import java.util.Map;

import net.cadrian.jsonref.JsonAtomicValues;
import net.cadrian.jsonref.SerializationData;
import net.cadrian.jsonref.SerializationException;

public class SerializationMap extends AbstractSerializationObject {

	private final Map<SerializationData, SerializationData> map;
	private boolean isMapOfStrings = true;

	public SerializationMap(final int capacity, final Class<?> type,
			final int ref) {
		super(type, ref);
		this.map = new HashMap<SerializationData, SerializationData>(capacity);
	}

	@Override
	public void toJson(final StringBuilder result,
			final JsonAtomicValues converter) {
		if (isMapOfStrings) {
			toJsonMap(result, converter);
		} else {
			toJsonArray(result, converter);
		}
	}

	private void toJsonMap(final StringBuilder result,
			final JsonAtomicValues converter) {
		result.append('{');
		String sep = "";
		for (final Map.Entry<SerializationData, SerializationData> data : map
				.entrySet()) {
			result.append(sep);
			data.getKey().toJson(result, converter);
			result.append(':');
			data.getValue().toJson(result, converter);
			sep = ",";
		}
		result.append('}');
	}

	private void toJsonArray(final StringBuilder result,
			final JsonAtomicValues converter) {
		result.append('[');
		String sep = "";
		for (final Map.Entry<SerializationData, SerializationData> data : map
				.entrySet()) {
			result.append(sep).append('[');
			data.getKey().toJson(result, converter);
			result.append(',');
			data.getValue().toJson(result, converter);
			result.append(']');
			sep = ",";
		}
		result.append(']');
	}

	@Override
	<T> T fromJson(final SerializationHeap heap,
			final JsonAtomicValues converter,
			final Class<? extends T> propertyType) {
		throw new SerializationException(
				"SerializationMap not used for deserialization");
	}

	/**
	 * @return the type
	 */
	@Override
	public Class<?> getType() {
		return type;
	}

	public void add(final SerializationData key, final SerializationData value) {
		map.put(key, value);
		if (key instanceof SerializationValue) {
			final Object keyval = ((SerializationValue) key).getValue();
			if (keyval != null && keyval.getClass() != String.class) {
				isMapOfStrings = false;
			}
		} else {
			isMapOfStrings = false;
		}
	}

}
