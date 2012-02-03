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

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;

public class DeleteTweetTest extends AbstractSocialTestSupport {

	@EndpointInject(uri = "mock:result")
	protected MockEndpoint resultEndpoint;

	// @Produce(uri = "social://twitter/home")
	// protected ProducerTemplate template;

	// String socialEndpoint =
	// "social://twitter/public?skipRead=true&rateLimit=150&rateLimitPeriod=1&rateLimitUnit=HOURS";

	@Test
	public void testRoute1() throws Exception {
		resultEndpoint.expectedMinimumMessageCount(1);
		resultEndpoint.await();
	}

	@Override
	protected RouteBuilder createRouteBuilder() {
		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				String baseUrl = "social://twitter/home";

				final SocialOAuth consumerOAuth = new SocialOAuth(
						"jm5YtmDbffkWEQ3PqPWqg",
						"kQGG4JihzfZ94x1ywoRRIk43f0EPu2OKVYzsCww");

				String params = "?skipRead=true&poll=false";
				String homeUrl = new StringBuilder(baseUrl).append(params)
						.toString();

				setErrorHandlerBuilder(loggingErrorHandler());

				from("file:/home/bruno/oauth/?noop=true").process(
						new Processor() {
							public void process(Exchange e) throws Exception {
								System.out.println("lendo arquivo ...");
								File f = e.getIn().getBody(File.class);
								Properties p = new Properties();
								FileInputStream fileInputStream = new FileInputStream(
										f);
								p.load(fileInputStream);
								fileInputStream.close();

								String secret = p.getProperty("secret");
								String token = p.getProperty("token");
								// String status = p.getProperty("status");

								e.getOut().setHeader(
										SocialHeaders.SOCIAL_USER_OAUTH,
										new SocialOAuth(token, secret));
								e.getOut().setHeader(
										SocialHeaders.SOCIAL_CONSUMER_OAUTH,
										consumerOAuth);
								e.getOut().setHeader(
										SocialHeaders.SOCIAL_POLL_PATH,
										Boolean.TRUE);
								// e.getOut().setBody(status);
							}
						}).to(homeUrl);

				from(homeUrl)
						.transform(xpath("//status/text/text()").stringResult())
						.process(new Processor() {
							public void process(Exchange arg0) throws Exception {
								System.out.print(arg0.getIn().getHeader(
										SocialHeaders.SOCIAL_DATA_ID)
										+ ": ");
								System.out.println(arg0.getIn().getBody());
							}
						})
						.filter(body().contains("pedofilia"))
						.process(new Processor() {
							public void process(Exchange arg0) throws Exception {
								System.out.print("vai deletar tweet: ");
								System.out.print(arg0.getIn().getHeader(
										SocialHeaders.SOCIAL_DATA_ID));
							}
						}).wireTap("direct:tweetAboutDeletedStatus")
						.to("social://twitter/delete");

				from("direct:tweetAboutDeletedStatus")
						.setBody(
								header(SocialHeaders.SOCIAL_DATA_ID)
										.prepend(
												simple("I'm #Apache #Camel camel-social and I've just deleted a filtered status with id")))
						.to("social://twitter/update");
			}
		};
	}

}
