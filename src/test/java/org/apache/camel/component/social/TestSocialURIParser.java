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

import junit.framework.TestCase;

import org.junit.Test;

public class TestSocialURIParser extends TestCase {

	@Test
	public void testBasicURI() throws Exception {
		SocialURIParser parser = new SocialURIParser("social://twitter");
		assertEquals(parser.getProvider(), "twitter");
	}

	@Test
	public void testUserPassURI() throws Exception {
		SocialURIParser parser = new SocialURIParser(
				"social://user:p%40ss!@twitter");

		assertEquals(parser.getUser(), "user");
		assertEquals(parser.getPass(), "p@ss!");
	}

	@Test
	public void testPath() throws Exception {
		SocialURIParser parser = new SocialURIParser(
				"social://user:pass@twitter/path");
		assertEquals("path", parser.getPath());
	}

}
