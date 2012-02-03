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
package org.apache.camel.component.social.providers.twitter;

import java.util.Map;

import org.apache.camel.component.social.SocialData;
import org.apache.camel.component.social.path.SocialPathConsumer;
import org.apache.camel.component.social.providers.AbstractSocialProvider;
import org.apache.camel.component.social.providers.SocialProvider;

public class TwitterProvider extends AbstractSocialProvider implements SocialProvider {

	public TwitterProvider() {
		super("twitter");
	}

	private static final String OAUTH_ACCESS_TOKEN = "http://twitter.com/oauth/access_token";
	private static final String OAUTH_AUTHORIZE = "http://twitter.com/oauth/authorize";
	private static final String OAUTH_REQUEST_TOKEN = "http://twitter.com/oauth/request_token";

	public static final String IN_REPLY_TO = "in_reply_to_status_id";
	public static final String LATITUDE = "lat";
	public static final String LONGITUDE = "long";
	public static final String PLACE_ID = "place_id";
	public static final String TRIM_USER = "trim_user";
	public static final String STATUS = "status";

	public SocialPathConsumer createPath(String path) throws Exception {
		if (path.equals("public")) {
			return new TwitterPublicPath(this);
		}

		if (path.equals("home")) {
			return new TwitterHomePath(this);
		}

		if (path.equals("delete")) {
			return new TwitterDeletePath(this);
		}

		if (path.equals("update")) {
			return new TwitterUpdatePath(this);
		}

		if (path.equals("search")) {
			return new TwitterSearchPath(this);
		}

		return null;
	}

	public void configureHeaders(Map<String, Object> headers, SocialData data) throws Exception {
	}

	@Override
	protected String getOAuthRequestTokenURL() {
		return OAUTH_REQUEST_TOKEN;
	}

	@Override
	protected String getOAuthAccessTokenURL() {
		return OAUTH_ACCESS_TOKEN;
	}

	@Override
	protected String getOAuthAuthorizeURL() {
		return OAUTH_AUTHORIZE;
	}

}
