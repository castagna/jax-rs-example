/*
 * Copyright © 2011 Talis Systems Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.talis.labs.api.sparql11.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.RDFS;

public class Sparql11HttpRdfUpdateTest {

	private static int PORT = 8080;
	private static String HOST = "127.0.0.1";
	private static String SCHEME = "http";
	private static String PATH = "http-rdf-update";
	private static String URL = SCHEME + "://" + HOST + ":" + PORT + "/" + PATH;
	private static String EXISTING_GRAPH_URI = "http://www.example.com/mygraph";
	private static String NOT_EXISTING_GRAPH_URI = "http://www.example.com/mygraph3";
	
	private static HttpClient httpclient = null;
	private static Model model = null;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		httpclient = new DefaultHttpClient();
	}

	@Before
	public void setUp() throws Exception {
		httpclient = new DefaultHttpClient();
		
		model = ModelFactory.createDefaultModel();
		model.add(model.createResource("foo:bar"), RDFS.label, model.createLiteral("Bar"));
		
		delete(NOT_EXISTING_GRAPH_URI, false);
		delete(EXISTING_GRAPH_URI, false);
		post(EXISTING_GRAPH_URI, "application/rdf+xml", "RDF/XML");
	}

	@After
	public void tearDown() throws Exception {
		if ( httpclient != null ) {
			httpclient.getConnectionManager().shutdown();
			httpclient = null;
		}
		
		if (model != null) {
			model.close();
			model = null;
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {

	}
	
	@Test
	public void testPortIsOpen() {
		try {
			new URL(URL).openConnection().connect();
		} catch (MalformedURLException e) {
			fail(String.format("%s is not a valid URL.", URL));
		} catch (IOException e) {
			fail();
		}
	}
	
	private static URI uri (final String graph) throws URISyntaxException {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("graph", graph));
		URI uri = URIUtils.createURI(SCHEME, HOST, PORT, PATH, URLEncodedUtils.format(params, "UTF-8"), null);
		
		return uri;
	}
	
	@Test
	public void testGetXML() throws ClientProtocolException, IOException, URISyntaxException {
		getExisting(EXISTING_GRAPH_URI, "application/rdf+xml", "RDF/XML");
		getNotExisting(NOT_EXISTING_GRAPH_URI, "application/rdf+xml", "RDF/XML");
	}
	
	@Test
	public void testGetTurtle() throws ClientProtocolException, IOException, URISyntaxException {
		getExisting(EXISTING_GRAPH_URI, "application/x-turtle", "TURTLE");
		getNotExisting(NOT_EXISTING_GRAPH_URI, "application/x-turtle", "TURTLE");
	}
	
	@Test
	public void testGetNTriples() throws ClientProtocolException, IOException, URISyntaxException {
		getExisting(EXISTING_GRAPH_URI, "application/n-triples", "N-TRIPLE");
		getNotExisting(NOT_EXISTING_GRAPH_URI, "application/n-triples", "N-TRIPLE");
	}
	
	private static void getExisting(String graph, String mediaType, String lang) throws IllegalStateException, IOException, URISyntaxException {
		URI uri = uri(graph);

		HttpGet httpget = new HttpGet(uri);
		httpget.setHeader("Accept", mediaType);
		
		HttpResponse response = httpclient.execute(httpget);
		HttpEntity entity = response.getEntity();
		
		assertEquals(200, response.getStatusLine().getStatusCode());

		Model m = ModelFactory.createDefaultModel();
		m.read(entity.getContent(), "", lang); // TODO: fix base

		assertNotNull(m);
		assertTrue(model.isIsomorphicWith(m));
	}

	private void getNotExisting(String graph, String mediaType, String lang) throws IllegalStateException, IOException, URISyntaxException {
		URI uri = uri(graph);

		HttpGet httpget = new HttpGet(uri);
		httpget.setHeader("Accept", mediaType);
		
		HttpResponse response = httpclient.execute(httpget);
		
		assertEquals(404, response.getStatusLine().getStatusCode());
	}
	
	@Test
	public void testPostXML() throws URISyntaxException, ClientProtocolException, IOException {
		post(EXISTING_GRAPH_URI, "application/rdf+xml", "RDF/XML");
	}
	
	@Test
	public void testPostTurtle() throws URISyntaxException, ClientProtocolException, IOException {
		post(EXISTING_GRAPH_URI, "application/x-turtle", "TURTLE");
	}
	
	@Test
	public void testPostNTriples() throws URISyntaxException, ClientProtocolException, IOException {
		post(EXISTING_GRAPH_URI, "application/n-triples", "N-TRIPLE");
	}
	
	private static void post(String graph, String mediaType, String lang) throws URISyntaxException, ClientProtocolException, IOException {
		StringWriter content = new StringWriter();
		model.write(content, lang);
		
		StringEntity entity = new StringEntity(content.toString(), "UTF-8");
		
		URI uri = uri(graph);

		HttpPost httppost = new HttpPost(uri);
		httppost.setHeader("Content-Type", mediaType);
		httppost.setHeader("Accept", "text/plain");
		httppost.setEntity(entity);
		
		HttpResponse response = httpclient.execute(httppost);

		assertEquals(201, response.getStatusLine().getStatusCode());
		
		assertEquals(1, response.getHeaders("Location").length);
		assertEquals(EXISTING_GRAPH_URI, response.getFirstHeader("Location").getValue());
		
		response.getEntity().consumeContent();
	}

	@Test
	public void testPutXML() throws URISyntaxException, ClientProtocolException, IOException {
		put(EXISTING_GRAPH_URI, "application/rdf+xml", "RDF/XML");
	}
	
	@Test
	public void testPutTurtle() throws URISyntaxException, ClientProtocolException, IOException {
		put(EXISTING_GRAPH_URI, "application/x-turtle", "TURTLE");
	}
	
	@Test
	public void testPutNTriples() throws URISyntaxException, ClientProtocolException, IOException {
		put(EXISTING_GRAPH_URI, "application/n-triples", "N-TRIPLE");
	}
	
	private static void put(String graph, String mediaType, String lang) throws ClientProtocolException, URISyntaxException, IOException {
		put(model, graph, mediaType, lang);

		Model m = ModelFactory.createDefaultModel();
		m.add(m.createResource("foo:bar2"), RDFS.label, m.createLiteral("Bar2"));

		put(m, graph, mediaType, lang);

		model.add(m);

		getExisting(graph, mediaType, lang);
	}
	
	private static void put(Model model, String graph, String mediaType, String lang) throws URISyntaxException, ClientProtocolException, IOException {
		StringWriter content = new StringWriter();
		model.write(content, lang);
		
		StringEntity entity = new StringEntity(content.toString(), "UTF-8");
		
		URI uri = uri(graph);

		HttpPut httpput = new HttpPut(uri);
		httpput.setHeader("Content-Type", mediaType);
		httpput.setHeader("Accept", "text/plain");
		httpput.setEntity(entity);
		
		HttpResponse response = httpclient.execute(httpput);

		assertEquals(201, response.getStatusLine().getStatusCode());
		
		assertEquals(0, response.getHeaders("Location").length);
		
		response.getEntity().consumeContent();
	}


	
	@Test
	public void testDelete() throws URISyntaxException, ClientProtocolException, IOException {
		delete(EXISTING_GRAPH_URI, true);
	}
	
	private static void delete (final String graph, boolean check) throws URISyntaxException, ClientProtocolException, IOException {
		URI uri = uri(graph);

		HttpDelete httpdelete = new HttpDelete(uri);
		httpdelete.setHeader("Accept", "text/plain");

		HttpResponse response = httpclient.execute(httpdelete);

		if ( check ) {
			assertEquals(204, response.getStatusLine().getStatusCode());
		}
		
		httpdelete.abort();
	}
	
}
