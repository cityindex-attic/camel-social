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
package org.apache.camel.component.social.providers;

import java.util.Map;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.apache.camel.component.social.SocialData;
import org.apache.camel.component.social.SocialOAuth;

public abstract class AbstractSocialProvider implements SocialProvider {

	private final OAuthProvider oauthProvider;
	private final String providerName;

	public AbstractSocialProvider(String name) {
		this.oauthProvider = new DefaultOAuthProvider(getOAuthRequestTokenURL(), getOAuthAccessTokenURL(),
		        getOAuthAuthorizeURL());
		this.providerName = name;
	}

	public OAuthProvider getOauthProvider() {
		return oauthProvider;
	}

	protected abstract String getOAuthRequestTokenURL();

	protected abstract String getOAuthAccessTokenURL();

	protected abstract String getOAuthAuthorizeURL();

	public OAuthConsumer createOAuthConsumer(SocialOAuth consumerAuth) {
		return new CommonsHttpOAuthConsumer(consumerAuth.token, consumerAuth.secret);
	}

	public void configureHeaders(Map<String, Object> headers, SocialData data) throws Exception {
	}

	public final String getProviderName() {
		return providerName;
	}

}
