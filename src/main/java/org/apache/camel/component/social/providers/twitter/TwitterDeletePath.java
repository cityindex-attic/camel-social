package org.apache.camel.component.social.providers.twitter;

import java.util.Collections;
import java.util.Map;

import org.apache.camel.component.social.SocialData;
import org.apache.camel.component.social.SocialHeaders;
import org.apache.camel.component.social.path.RateLimitExceededException;
import org.apache.camel.component.social.path.SocialDataFetchError;

public class TwitterDeletePath extends AbstractTwitterPath {

	private static final String STATUSES_DESTROY = "statuses/destroy";
	// private static Log log = LogFactory.getLog(TwitterDeletePath.class);
	private volatile String statusId;

	public TwitterDeletePath(TwitterProvider twitterProvider) throws Exception {
		super(twitterProvider, "delete");
	}

	@Override
	protected String getStreamPath() {
		return STATUSES_DESTROY;
	}

	@Override
	protected String getCommandPath() {
		return STATUSES_DESTROY;
	}

	@Override
	public Iterable<SocialData> readData(String lastId)
			throws SocialDataFetchError, RateLimitExceededException {
		return Collections.emptyList();
	}

	@Override
	protected String normalizeURL(String stream) {
		String url = super.normalizeURL(stream);
		int indexOfDot = url.lastIndexOf('.');
		StringBuilder bUrl = new StringBuilder(url).insert(indexOfDot, "/"
				+ statusId);
		return bUrl.toString();
	}

	@Override
	public SocialData updateData(Object data, Map<String, Object> headers)
			throws SocialDataFetchError {

		statusId = headers.get(SocialHeaders.SOCIAL_DATA_ID).toString();

		Map<String, Object> empty = Collections.emptyMap();
		return super.updateData(data, empty);
	}

}
