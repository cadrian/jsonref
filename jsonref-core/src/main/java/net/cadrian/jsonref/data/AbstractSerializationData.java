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

import net.cadrian.jsonref.JsonConverter;
import net.cadrian.jsonref.JsonConverter.Context;
import net.cadrian.jsonref.SerializationData;

abstract class AbstractSerializationData implements SerializationData {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.cadrian.jsonref.SerializationData#fromJson(java.lang.Class,
	 * net.cadrian.jsonref.JsonConverter,
	 * net.cadrian.jsonref.JsonConverter.Context)
	 */
	@Override
	public <T> T fromJson(final Class<? extends T> wantedType,
			final JsonConverter converter, final Context converterContext) {
		return fromJson(null, wantedType, converter, converterContext);
	}

	abstract <T> T fromJson(SerializationHeap heap,
			Class<? extends T> propertyType, JsonConverter converter,
			Context converterContext);
}