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
package net.cadrian.jsonref.atomic;

import net.cadrian.jsonref.JsonAtomicValues;

public class DefaultJsonAtomicValues implements JsonAtomicValues {

	/*
	 * (non-Javadoc)
	 *
	 * @see net.cadrian.jsonref.JsonConverter#toJson(java.lang.Object)
	 */
	@Override
	public String toJson(final Object value) {
		if (value == null) {
			return "null";
		}
		assert isAtomicValue(value.getClass());
		return AtomicValue.get(value.getClass()).toJson(value);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.cadrian.jsonref.JsonConverter#fromJson(java.lang.String,
	 * java.lang.Class)
	 */
	@Override
	public <T> T fromJson(final String value, final Class<? extends T> type) {
		assert isAtomicValue(type);
		return AtomicValue.get(type).fromJson(value, type);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.cadrian.jsonref.JsonConverter#isValue(java.lang.Class)
	 */
	@Override
	public boolean isAtomicValue(final Class<?> type) {
		return AtomicValue.get(type) != null;
	}

}
