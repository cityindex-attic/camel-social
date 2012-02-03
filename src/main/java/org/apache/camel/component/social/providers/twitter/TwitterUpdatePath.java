package org.apache.camel.component.social.providers.twitter;

import java.util.Collections;

import org.apache.camel.component.social.SocialData;
import org.apache.camel.component.social.path.RateLimitExceededException;
import org.apache.camel.component.social.path.SocialDataFetchError;

public class TwitterUpdatePath extends AbstractTwitterPath {

	private static final String URL = "statuses/update";

	public TwitterUpdatePath(TwitterProvider twitterProvider) throws Exception {
		super(twitterProvider, "update");
	}

	@Override
	protected String getStreamPath() {
		return URL;
	}

	@Override
	public boolean isAuthRequired() {
		return true;
	}

	@Override
	public Iterable<SocialData> readData(String lastId)
			throws SocialDataFetchError, RateLimitExceededException {
		return Collections.emptyList();
	}

}
