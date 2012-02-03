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

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Consumer;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.component.social.path.SocialPathConsumer;
import org.apache.camel.component.social.util.SocialUtils;
import org.apache.camel.impl.ScheduledPollEndpoint;
import org.apache.camel.util.ObjectHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Represents a direct socialEndpoint that synchronously invokes the consumers
 * of the socialEndpoint when a producer sends a message to it.
 * 
 * @version
 */
public class SocialEndpoint extends ScheduledPollEndpoint {

	private static final transient Log LOG = LogFactory
			.getLog(SocialEndpoint.class);

	private SocialConfiguration configuration;
	private SocialConsumer consumer = null;

	private SocialOAuth consumerOAuth;
	private SocialOAuth userOAuth;

	public SocialEndpoint(String uri, SocialComponent component,
			SocialConfiguration config) {
		super(uri, component);

		setExchangePattern(ExchangePattern.InOnly);

		this.configuration = config;

		if (configuration.hasConsumerAuth()) {
			consumerOAuth = new SocialOAuth(config.getOauthConsumerToken(),
					config.getOauthConsumerSecret());
		}

		if (configuration.hasAuth()) {
			userOAuth = new SocialOAuth(config.getOauthToken(),
					config.getOauthSecret());
		}
	}

	public Consumer createConsumer(Processor processor) throws Exception {
		SocialConsumer tc = new SocialConsumer(this, processor);
		if ((!configuration.hasAuth() && tc.isSocialPathAuthRequired())
				|| !configuration.isPoll()) {
			tc.suspend(); // no auth given but path requires auth
							// so it is event-driven consumer based
							// on messages comming from producer
			LOG.info("Suspending this consumer endpoint because it has no required auth credentials to poll data.");
		}

		consumer = tc;
		configureConsumer(tc);

		if (configuration.getRateLimit() != null
				&& configuration.getRateLimit() > -1) {
			ObjectHelper.notNull(configuration.getRateLimitUnit(),
					"rateLimitUnit", configuration);
			ObjectHelper.notNull(configuration.getRateLimitPeriod(),
					"rateLimitPeriod", configuration);

			long calculatedDelay = SocialUtils.calculateDelatBasedOnRate(
					configuration.getRateLimit(),
					configuration.getRateLimitUnit(),
					configuration.getRateLimitPeriod(), tc.getTimeUnit());
			tc.setDelay(calculatedDelay);

			LOG.info("Using calculated delay based on rate limits: "
					+ calculatedDelay + " " + tc.getTimeUnit());
		}

		return tc;
	}

	public Producer createProducer() throws Exception {
		return new SocialProducer(this);
	}

	public boolean isSingleton() {
		return true;
	}

	public SocialConfiguration getConfiguration() {
		return configuration;
	}

	public void configureMessage(Message in, SocialData data) throws Exception {
		in.setHeader(SocialHeaders.SOCIAL_DATA_ID, data.getId());
		in.setBody(data.getData());

		Map<String, Object> providerHeaders = new HashMap<String, Object>();
		configuration.getProvider().configureHeaders(providerHeaders, data);
		in.getHeaders().putAll(providerHeaders);
	}

	public boolean notifyConsumer(Exchange exchange) throws Exception {
		if (consumer == null) {
			return false;
		}

		consumer.process(exchange);
		return true;
	}

	protected SocialPathConsumer createSocialPath() throws Exception {
		String path = configuration.getPath();
		return configuration.getProvider().createPath(path);
	}

	public SocialOAuth getOAuthConsumer(Exchange exchange) {
		return exchange != null ? exchange.getIn().getHeader(
				SocialHeaders.SOCIAL_CONSUMER_OAUTH, consumerOAuth,
				SocialOAuth.class) : consumerOAuth;
	}

	public SocialOAuth getOAuthUser(Exchange exchange) {
		return exchange != null ? exchange.getIn().getHeader(
				SocialHeaders.SOCIAL_USER_OAUTH, userOAuth, SocialOAuth.class)
				: userOAuth;
	}

}
