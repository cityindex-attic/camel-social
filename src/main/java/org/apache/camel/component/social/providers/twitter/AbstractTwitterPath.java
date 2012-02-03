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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;

import org.apache.camel.component.social.DefaultSocialData;
import org.apache.camel.component.social.SocialData;
import org.apache.camel.component.social.SocialHttpClient;
import org.apache.camel.component.social.SocialOAuth;
import org.apache.camel.component.social.path.RateLimitExceededException;
import org.apache.camel.component.social.path.SocialDataFetchError;
import org.apache.camel.component.social.path.SocialPathConsumer;
import org.apache.camel.component.social.path.SocialPathSessionAware;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public abstract class AbstractTwitterPath implements SocialPathConsumer,
		SocialPathSessionAware {

	private static final String STATUS_UPDATE = "statuses/update";

	private static Log log = LogFactory.getLog(AbstractTwitterPath.class);

	protected static final String FORMAT = ".xml";

	protected static final String TWITTER_API_URL = "http://api.twitter.com/1/";

	private static final String SINCE_ID_PARAM = "since_id";

	private volatile boolean sessionActive;
	private String path;
	private TwitterProvider provider;
	private SocialHttpClient httpClient;

	private DocumentBuilderFactory domFac;

	public DocumentBuilderFactory getDomFac() {
		return domFac;
	}

	protected String getFormat() {
		return FORMAT;
	}

	private XPathFactory factory;
	private XPath xpath;

	public XPath getXpath() {
		return xpath;
	}

	private Transformer transformer;

	public Transformer getTransformer() {
		return transformer;
	}

	private long started = 0;

	AbstractTwitterPath(TwitterProvider twitterProvider, String path)
			throws Exception {
		this.path = path;
		this.provider = twitterProvider;

		domFac = DocumentBuilderFactory.newInstance();
		factory = XPathFactory.newInstance();
		xpath = factory.newXPath();
		transformer = TransformerFactory.newInstance().newTransformer();
	}

	public String getPath() {
		return path;
	}

	public void endSession() {
		httpClient.shutdown();
		sessionActive = false;
	}

	public void initSession(SocialOAuth consumerCredentials,
			SocialOAuth userCredentials) throws Exception {
		OAuthConsumer consumer = provider
				.createOAuthConsumer(consumerCredentials);
		consumer.setTokenWithSecret(userCredentials.token,
				userCredentials.secret);
		httpClient = new SocialHttpClient((CommonsHttpOAuthConsumer) consumer);
		sessionActive = true;
	}

	public boolean isSessionActive() {
		return sessionActive;
	}

	protected String getCommandPath() {
		return STATUS_UPDATE;
	}

	public SocialData updateData(Object data, Map<String, Object> headers)
			throws SocialDataFetchError {
		String status = data.toString();

		String url = normalizeURL(getCommandPath());

		Map<String, Object> params = new HashMap<String, Object>();

		Object inReplyTo = headers.get(TwitterProvider.IN_REPLY_TO);
		if (inReplyTo != null) {
			params.put(TwitterProvider.IN_REPLY_TO, inReplyTo);
		}

		Object lat = headers.get(TwitterProvider.LATITUDE);
		if (lat != null) {
			params.put(TwitterProvider.LATITUDE, lat);
		}

		Object _long = headers.get(TwitterProvider.LONGITUDE);
		if (_long != null) {
			params.put(TwitterProvider.LONGITUDE, _long);
		}

		Object place_id = headers.get(TwitterProvider.PLACE_ID);
		if (place_id != null) {
			params.put(TwitterProvider.PLACE_ID, place_id);
		}

		Object trim_user = headers.get(TwitterProvider.TRIM_USER);
		params.put(TwitterProvider.TRIM_USER, trim_user == null ? "1"
				: trim_user);
		params.put(TwitterProvider.STATUS, status);

		HttpPost post = new HttpPost(url);
		HttpResponse response;
		response = callHttpMethod(params, post);

		if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
			try {
				log.warn("Could not update Twitter status: "
						+ response.getStatusLine() + "\n"
						+ EntityUtils.toString(response.getEntity()));
			} catch (ParseException e) {
			} catch (IOException e) {
			}
			return null;
		}

		String body;
		try {
			body = EntityUtils.toString(response.getEntity());

			DocumentBuilder db = domFac.newDocumentBuilder();

			InputSource source = new InputSource();
			source.setCharacterStream(new StringReader(body));
			Document doc = db.parse(source);

			DefaultSocialData socialData = parseStatus(doc.getFirstChild());
			return socialData;
		} catch (Exception e) {
			throw new SocialDataFetchError(e);
		}
	}

	public Iterable<SocialData> readData(String lastId)
		throws SocialDataFetchError, RateLimitExceededException {
		return readData(lastId, null);
	}

	public Iterable<SocialData> readData(String lastId, Map<String, Object> providerParams)
			throws SocialDataFetchError, RateLimitExceededException {
		if (httpClient == null) {
			initHttpClientNoAuth();
		}

		Map<String, Object> params = new HashMap<String, Object>();
		if (lastId != null) {
			params.put(SINCE_ID_PARAM, lastId);
		}

		if (providerParams != null) {
			params.putAll(providerParams);
		}

		if (started == 0) {
			started = System.currentTimeMillis();
		}

		String body = listStatuses(getStreamPath(), params);

		try {
			return convertToSocialDataList(body);
		} catch (Exception e) {
			throw new SocialDataFetchError(e);
		}
	}

	private void initHttpClientNoAuth() {
		httpClient = new SocialHttpClient();
	}

	protected final String listStatuses(String stream,
			Map<String, Object> params) throws SocialDataFetchError,
			RateLimitExceededException {
		String url = normalizeURL(stream);

		// TODO fix
		if (params != null && params.get("q") != null) {
			if (url.indexOf('?') == -1) {
				url = url.concat("?");
			}
			
			url= url.concat("q=").concat(params.get("q").toString());
		}
		HttpGet get = new HttpGet(url);
		HttpResponse response;
		try {
			response = callHttpMethod(params, get);
		} catch (Exception e1) {
			throw new SocialDataFetchError(e1);
		}

		String body;
		try {
			body = EntityUtils.toString(response.getEntity());
		} catch (Exception e) {
			throw new SocialDataFetchError(e);
		}

		if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
			log.warn("Twitter provider could not fetch social data: "
					+ response.getStatusLine());

			if (log.isDebugEnabled()) {
				log.debug("Body Request: " + body);

				log.debug("Headers: ");
				for (Header h : response.getAllHeaders()) {
					log.warn(h.getName() + ": " + h.getValue());
				}
			}

			Header[] hRate = response.getHeaders("X-RateLimit-Remaining");
			if (hRate != null && hRate.length > 0
					&& Long.parseLong(hRate[0].getValue()) == 0) {
				log.warn("Twitter provider found rate limit exceeded");

				long timeNow = System.currentTimeMillis();
				long howLong = timeNow - started;
				long haveToWait = TimeUnit.MILLISECONDS.convert(1,
						TimeUnit.HOURS) - howLong;

				Header[] hRateReset = response.getHeaders("X-RateLimit-Reset");

				if (hRateReset != null && hRateReset.length > 0) {
					try {
						haveToWait = Long.parseLong(hRateReset[0].getValue());
					} catch (Exception e) {
					}
				}
				log.warn("throwing RateLimitExceededException(" + haveToWait
						+ ")");

				throw new RateLimitExceededException(haveToWait);
			}
		}

		get.abort();

		return body;
	}

	protected HttpResponse callHttpMethod(Map<String, Object> params,
			HttpUriRequest method) throws SocialDataFetchError {
		method.getParams().setBooleanParameter(
				CoreProtocolPNames.USE_EXPECT_CONTINUE, false);

		List<NameValuePair> postMethod = new ArrayList<NameValuePair>();

		for (Map.Entry<String, Object> e : params.entrySet()) {
			if (method instanceof HttpEntityEnclosingRequestBase) {
				postMethod.add(new BasicNameValuePair(e.getKey(), e.getValue()
						.toString()));
			} else {
				method.getParams().setParameter(e.getKey(), e.getValue());
			}
		}

		if (method instanceof HttpEntityEnclosingRequestBase) {
			try {
				((HttpEntityEnclosingRequestBase) method)
						.setEntity(new UrlEncodedFormEntity(postMethod,
								HTTP.UTF_8));
			} catch (UnsupportedEncodingException e1) {
			}
		}

		HttpResponse response;
		try {
			response = httpClient.execute(method);
		} catch (Exception e) {
			throw new SocialDataFetchError(e);
		}
		return response;
	}

	protected String normalizeURL(String stream) {
		if (stream.charAt(0) == '/') {
			stream = stream.substring(1);
		}

		StringBuilder url = new StringBuilder();
		url.append(getTwitterApiUrl()).append(stream).append(getFormat());
		return url.toString();
	}

	protected Iterable<SocialData> convertToSocialDataList(String body)
			throws Exception {
		DocumentBuilder db = getDomFac().newDocumentBuilder();

		InputSource source = new InputSource();
		source.setCharacterStream(new StringReader(body));
		Document doc = db.parse(source);

		XPathExpression expr = getXpath().compile("/statuses/status");

		Object result = expr.evaluate(doc, XPathConstants.NODESET);
		NodeList nodes = (NodeList) result;

		List<SocialData> socialDataList = new ArrayList<SocialData>(
				nodes.getLength());
		for (int index = 0; index < nodes.getLength(); index++) {
			Node aNode = nodes.item(index);

			DefaultSocialData socialData = parseStatus(aNode);
			socialDataList.add(socialData);
		}

		return socialDataList;
	}

	protected DefaultSocialData parseStatus(Node aNode)
			throws TransformerException {
		StreamResult streamResult = new StreamResult(new StringWriter());
		DOMSource domSource = new DOMSource(aNode);
		getTransformer().transform(domSource, streamResult);

		String xmlString = streamResult.getWriter().toString();
		String id = null;
		NodeList childs = aNode.getChildNodes();
		for (int j = 0; j < childs.getLength(); j++) {
			Node child = childs.item(j);
			if (child.getNodeName().equals("id")) {
				id = child.getTextContent();
				break;
			}
		}

		DefaultSocialData socialData = new DefaultSocialData(id, xmlString);
		return socialData;
	}

	protected abstract String getStreamPath();

	public boolean isAuthRequired() {
		return false;
	}

	protected String getTwitterApiUrl() {
		return TWITTER_API_URL;
	}

}
