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

import java.io.FileReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;

import org.apache.camel.CamelException;
import org.apache.camel.component.social.DefaultSocialData;
import org.apache.camel.component.social.SocialData;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class TwitterSearchPath extends AbstractTwitterPath {

	TwitterSearchPath(TwitterProvider twitterProvider) throws Exception {
		super(twitterProvider, "search");
	}

	@Override
	protected String getStreamPath() {
		return "search";
	}

	protected String getTwitterApiUrl() {
		return "http://search.twitter.com/";
	}

	@Override
	protected String getFormat() {
		return ".json";
	}

	protected Iterable<SocialData> convertToSocialDataList(String body) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode rootNode = mapper.readTree(body);
		rootNode = rootNode.get("results");

		if (rootNode == null) {
			throw new CamelException("Nao deu pra pegar os dados JSON");
		}
		List<SocialData> socialDataList = new ArrayList<SocialData>(rootNode.size());
		Iterator<JsonNode> ijn = rootNode.getElements();
		while (ijn.hasNext()) {
			JsonNode aNode = ijn.next();

			String id = aNode.get("id_str").getTextValue();
			DefaultSocialData socialData = new DefaultSocialData(id, aNode.toString());
			socialDataList.add(socialData);
		}

		return socialDataList;
	}

}
