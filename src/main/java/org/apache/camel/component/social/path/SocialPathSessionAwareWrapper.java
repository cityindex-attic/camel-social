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
package org.apache.camel.component.social.path;

import org.apache.camel.component.social.SocialOAuth;

public class SocialPathSessionAwareWrapper implements SocialPathSessionAware {

	private static class Mock implements SocialPathSessionAware {
		public void endSession() {
		}

		public void initSession(SocialOAuth consumer, SocialOAuth user) {
		}

		public boolean isSessionActive() {
			return false;
		}

		public boolean isAuthRequired() {
			return false;
		}
	}

	private SocialPathSessionAware target;
	private static final SocialPathSessionAware mock = new Mock();

	public static SocialPathSessionAware wrapper(Object o) {
		return new SocialPathSessionAwareWrapper(o);
	}

	public SocialPathSessionAwareWrapper(Object o) {
		if (o instanceof SocialPathSessionAware) {
			this.target = (SocialPathSessionAware) o;
		} else {
			target = mock;
		}
	}

	public void endSession() {
		if (target.isSessionActive()) {
			target.endSession();
		}
	}

	public void initSession(SocialOAuth consumer, SocialOAuth user) throws Exception {
		target.initSession(consumer, user);
	}

	public boolean isSessionActive() {
		return target.isSessionActive();
	}

	public boolean isAuthRequired() {
		return target.isAuthRequired();
	}

}
