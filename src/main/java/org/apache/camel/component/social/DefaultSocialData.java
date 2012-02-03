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
package org.apache.camel.component.social;

import java.util.Map;

public class DefaultSocialData implements SocialData {

	private String id;
	private Object data;
	private Map<String, Object> headers;

	public DefaultSocialData(String id, Object data) {
		this.id = id;
		this.data = data;
	}

	public DefaultSocialData(String id, Object data, Map<String, Object> headers) {
		this(id, data);
		this.headers = headers;
	}

	public String getId() {
		return id;
	}

	public Object getData() {
		return data;
	}

	public Map<String, Object> getHeaders() {
		return headers;
	}

}
