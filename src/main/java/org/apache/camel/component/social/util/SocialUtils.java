/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.social.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.camel.component.social.DefaultSocialData;
import org.apache.camel.component.social.SocialData;
import org.apache.camel.util.IntrospectionSupport;

public class SocialUtils {

	public static List<SocialData> socialDataList(List<?> unknowSocialDataList, String idProperty) {
		if (unknowSocialDataList == null || unknowSocialDataList.isEmpty()) {
			return Collections.emptyList();
		}

		List<SocialData> list = new ArrayList<SocialData>(unknowSocialDataList.size());
		for (Object data : unknowSocialDataList) {
			String idValue;
			try {
				idValue = IntrospectionSupport.getProperty(data, idProperty).toString();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			list.add(new DefaultSocialData(idValue, data));
		}

		return list;
	}

	public static long calculateDelatBasedOnRate(int rateLimit, TimeUnit periodUnit, int period, TimeUnit resultingTimeUnit) {
		return resultingTimeUnit.convert(period, periodUnit) / rateLimit;
	}
}
