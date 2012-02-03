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
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;

public class AbstractSocialTestSupport extends CamelTestSupport {

	private Properties properties;

	public AbstractSocialTestSupport() {
		URL url = getClass().getResource("/test-options.properties");

		InputStream inStream;
		try {
			inStream = url.openStream();
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalAccessError("test-options.properties could not be found");
		}

		properties = new Properties();
		try {
			properties.load(inStream);
		} catch (IOException e) {
			e.printStackTrace();
			throw new IllegalAccessError("test-options.properties could not be found");
		}
	}

	@Override
	public void setUp() throws Exception {
		log.info("********************************************************************************");
		log.info("Testing: " + getTestMethodName() + "(" + getClass().getName() + ")");
		log.info("********************************************************************************");

		log.debug("setUp test");
		if (!useJmx()) {
			disableJMX();
		} else {
			enableJMX();
		}

		context = createCamelContext();
		assertValidContext(context);

		// reduce default shutdown timeout to avoid waiting for 300 seconds
		int timeout = 100;
		context.getShutdownStrategy().setTimeout(timeout);
		log.info("Timeout of " + timeout);

		template = context.createProducerTemplate();
		template.start();
		consumer = context.createConsumerTemplate();
		consumer.start();

		postProcessTest();

		if (isUseRouteBuilder()) {
			RouteBuilder[] builders = createRouteBuilders();
			for (RouteBuilder builder : builders) {
				log.debug("Using created route builder: " + builder);
				context.addRoutes(builder);
			}
			startCamelContext();
			log.debug("Routing Rules are: " + context.getRoutes());
		} else {
			log.debug("Using route builder from the created context: " + context);
		}
		log.debug("Routing Rules are: " + context.getRoutes());
	}

	public Properties getProperties() {
		return properties;
	}
}
