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

import net.cadrian.jsonref.JsonAtomicValues;

public class SerializationValue extends AbstractSerializationData {
	private final Class<?> type;
	private final Object value;

	public SerializationValue(final Class<?> type, final Object value) {
		this.type = type;
		this.value = value;
	}

	/**
	 * Getter type
	 *
	 * @return the type
	 */
	public Class<?> getType() {
		return type;
	}

	/**
	 * Getter value
	 *
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	@Override
	public void toJson(final StringBuilder result, final JsonAtomicValues converter) {
		result.append(converter.toJson(value));
	}

	@Override
	public Object fromJson(final JsonAtomicValues converter) {
		return value;
	}

	@Override
	<T> T fromJson(final SerializationHeap heap, final JsonAtomicValues converter,
			final Class<? extends T> propertyType) {
		return converter.fromJson((String) value, propertyType);
	}

}