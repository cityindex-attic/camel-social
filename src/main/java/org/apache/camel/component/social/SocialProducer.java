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

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Producer;
import org.apache.camel.component.social.path.SocialPathConsumer;
import org.apache.camel.component.social.path.SocialPathSessionAware;
import org.apache.camel.component.social.path.SocialPathSessionAwareWrapper;
import org.apache.camel.impl.DefaultProducer;

public class SocialProducer extends DefaultProducer implements Producer {

	private SocialEndpoint endpoint;
	private SocialPathConsumer socialPath;

	public SocialProducer(Endpoint endpoint) {
		super(endpoint);
		this.endpoint = (SocialEndpoint) endpoint;
	}

	public void process(Exchange exchange) throws Exception {
		boolean consumingEndpoint = endpoint.notifyConsumer(exchange);

		SocialOAuth consumerOAuth = endpoint.getOAuthConsumer(exchange);
		SocialOAuth userOAuth = endpoint.getOAuthUser(exchange);

		if (!consumingEndpoint) {
			SocialPathConsumer socialPath = initSocialPath();
			SocialPathSessionAware wrapper = SocialPathSessionAwareWrapper
					.wrapper(socialPath);
			wrapper.initSession(consumerOAuth, userOAuth);
			socialPath.updateData(exchange.getIn().getBody(), exchange.getIn()
					.getHeaders());
			wrapper.endSession();
		}
	}

	private SocialPathConsumer initSocialPath() throws Exception {
		if (socialPath == null) {
			socialPath = endpoint.createSocialPath();
		}

		return socialPath;
	}

}
