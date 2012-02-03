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
package org.apache.camel.component.social.providers.foursquare;

import org.apache.camel.component.social.path.SocialPathConsumer;
import org.apache.camel.component.social.providers.AbstractSocialProvider;
import org.apache.camel.component.social.providers.SocialProvider;

public class FoursquareProvider extends AbstractSocialProvider implements SocialProvider {

	public FoursquareProvider() {
		super("foursquare");
	}

	private static final String OAUTH_REQUEST_TOKEN = "http://foursquare.com/oauth/request_token";
	private static final String OAUTH_ACCESS_TOKEN = "http://foursquare.com/oauth/access_token";
	private static final String OAUTH_AUTHORIZE = "http://foursquare.com/oauth/authorize";

	public SocialPathConsumer createPath(String path) throws Exception {
		return null;
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
