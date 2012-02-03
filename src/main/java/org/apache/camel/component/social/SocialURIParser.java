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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class SocialURIParser {

	private String pass;
	private String user;
	private String provider;
	private String path;
	private Map<String, String> parameters = new HashMap<String, String>();

	public SocialURIParser(String uri) {
		URI uriObject;
		try {
			uriObject = new URI(uri);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}

		String userInfo = uriObject.getUserInfo();

		if (userInfo != null) {
			user = userInfo.substring(0, userInfo.indexOf(':'));

			if (userInfo.indexOf(':') > -1) {
				pass = userInfo.substring(userInfo.indexOf(':') + 1);
			}
		}

		provider = uriObject.getHost();

		String query = uriObject.getQuery();
		if (query != null) {
			StringTokenizer tokenizer = new StringTokenizer(query, "&");
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				String[] param = token.split("=");
				parameters.put(param[0], param[1]);
			}
		}

		if (uriObject.getPath().length() > 1) {
			path = uriObject.getPath().substring(1);
		}
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public String getPass() {
		return pass;
	}

	public String getUser() {
		return user;
	}

	public String getProvider() {
		return provider;
	}

	public String getPath() {
		return path;
	}

}
