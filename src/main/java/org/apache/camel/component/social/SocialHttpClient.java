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

import java.io.IOException;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

public class SocialHttpClient implements HttpClient {

	private DefaultHttpClient client;
	private OAuthConsumer consumer;

	public SocialHttpClient(String token, String secret, String consumerToken, String consumerSecret) {
		this.client = new DefaultHttpClient();
		this.consumer = new CommonsHttpOAuthConsumer(consumerToken, consumerSecret);
		consumer.setTokenWithSecret(token, secret);
	}

	public void shutdown() {
		client.getConnectionManager().shutdown();
	}

	public SocialHttpClient(CommonsHttpOAuthConsumer oauthConsumer) {
		this.client = new DefaultHttpClient();
		this.consumer = oauthConsumer;
	}

	public SocialHttpClient() {
		this.client = new DefaultHttpClient();
	}

	public HttpParams getParams() {
		return client.getParams();
	}

	public ClientConnectionManager getConnectionManager() {
		return client.getConnectionManager();
	}

	public HttpResponse execute(HttpUriRequest request) throws IOException, ClientProtocolException {
		signOAuth(request);
		return client.execute(request);
	}

	private void signOAuth(HttpUriRequest request) throws ClientProtocolException {
		if (consumer != null) {
			try {
				consumer.sign(request);
			} catch (Exception e) {
				throw new ClientProtocolException(e);
			}
		}
	}

	public HttpResponse execute(HttpUriRequest request, HttpContext context) throws IOException,
			ClientProtocolException {
		signOAuth(request);
		return client.execute(request, context);
	}

	public HttpResponse execute(HttpHost target, HttpRequest request) throws IOException, ClientProtocolException {
		if (request instanceof HttpUriRequest) {
			signOAuth((HttpUriRequest) request);
		}
		return client.execute(target, request);
	}

	public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) throws IOException,
			ClientProtocolException {
		if (request instanceof HttpUriRequest) {
			signOAuth((HttpUriRequest) request);
		}
		return client.execute(target, request, context);
	}

	public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler) throws IOException,
			ClientProtocolException {
		signOAuth((HttpUriRequest) request);
		return client.execute(request, responseHandler);
	}

	public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context)
			throws IOException, ClientProtocolException {
		return client.execute(request, responseHandler, context);
	}

	public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler)
			throws IOException, ClientProtocolException {
		return client.execute(target, request, responseHandler);
	}

	public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler,
			HttpContext context) throws IOException, ClientProtocolException {
		return client.execute(target, request, responseHandler);
	}

}
