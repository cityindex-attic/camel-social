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

import java.util.concurrent.TimeUnit;

import org.apache.camel.component.social.providers.SocialProvider;

public class SocialConfiguration {

	private String query;
	
	private SocialProvider provider;

	private String oauthToken;

	private String oauthSecret;

	private String oauthConsumerToken;

	private String oauthConsumerSecret;

	private String path;

	public Boolean skipRead = false;

	private Integer maxKeepId = 200;

	private Integer rateLimit = 150;

	private TimeUnit rateLimitWindow = TimeUnit.HOURS;

	private Integer rateLimitPeriod = 1;

	private boolean poll;

	protected String getOauthConsumerToken() {
		return oauthConsumerToken;
	}

	public void setOauthConsumerToken(String oauthConsumerToken) {
		this.oauthConsumerToken = oauthConsumerToken;
	}

	protected String getOauthConsumerSecret() {
		return oauthConsumerSecret;
	}

	public void setOauthConsumerSecret(String oauthConsumerSecret) {
		this.oauthConsumerSecret = oauthConsumerSecret;
	}

	protected SocialProvider getProvider() {
		return provider;
	}

	protected String getOauthToken() {
		return oauthToken;
	}

	protected String getOauthSecret() {
		return oauthSecret;
	}

	protected String getPath() {
		return path;
	}

	public void setProvider(SocialProvider socialProvider) {
		this.provider = socialProvider;
	}

	public void setOauthToken(String user) {
		this.oauthToken = user;
	}

	public void setOauthSecret(String pass) {
		this.oauthSecret = pass;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Boolean hasConsumerAuth() {
		return oauthConsumerToken != null & oauthConsumerSecret != null;
	}

	public Boolean hasAuth() {
		return oauthToken != null & oauthSecret != null;
	}

	public Boolean isSkipRead() {
		return skipRead;
	}

	public void setSkipRead(Boolean skipRead) {
		this.skipRead = skipRead;
	}

	public Integer getMaxKeepId() {
		return maxKeepId;
	}

	public void setMaxKeepId(Integer value) {
		this.maxKeepId = value;
	}

	public Integer getRateLimit() {
		return rateLimit;
	}

	public void setRateLimit(Integer rateLimit) {
		this.rateLimit = rateLimit;
	}

	public TimeUnit getRateLimitUnit() {
		return rateLimitWindow;
	}

	public void setRateLimitWindow(TimeUnit rateLimitWindow) {
		this.rateLimitWindow = rateLimitWindow;
	}

	public Integer getRateLimitPeriod() {
		return rateLimitPeriod;
	}

	public void setRateLimitPeriod(Integer rateLimitPeriod) {
		this.rateLimitPeriod = rateLimitPeriod;
	}

	public boolean isPoll() {
	    return poll;
    }

	public void setPoll(boolean poll) {
    	this.poll = poll;
    }

	public String getQuery() {
		return query;
	}

	public void setQuery(String q) {
		this.query = q;
	}

}
