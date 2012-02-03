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

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.junit.Test;

public class TestTwitterSearchRoute extends AbstractSocialTestSupport {

	@EndpointInject(uri = "mock:result")
	protected MockEndpoint resultEndpoint;

	// @Produce(uri = "social://twitter/home")
	// protected ProducerTemplate template;

	// String socialEndpoint =
	// "social://twitter/public?skipRead=true&rateLimit=150&rateLimitPeriod=1&rateLimitUnit=HOURS";

	@Test
	public void testRoute1() throws Exception {
		resultEndpoint.expectedMinimumMessageCount(5000);
		resultEndpoint.await();
	}

	@Override
	protected RouteBuilder createRouteBuilder() {
		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				String socialSearch = "social://twitter/search?poll=true&query=TDC2010&skipRead=true";

				setErrorHandlerBuilder(loggingErrorHandler());

				from(socialSearch)
					.unmarshal().json(JsonLibrary.Jackson)
					.process(new Processor() {
					public void process(Exchange exchange) throws Exception {
						Message in = exchange.getIn();
						Map body = (Map) in.getBody();
						String text = body.get("text").toString();
						in.setBody(text);
						System.out.println(text);
					}
				})
					.to("mock:result");
			}
		};
	}

}
