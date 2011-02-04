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
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.header.MediaTypes;

public class Sparql11HttpRdfUpdateResourceTest {

	private static int PORT = 8080;
	private static String HOST = "127.0.0.1";
	private static String SCHEME = "http";
	private static String URL = SCHEME + "://" + HOST + ":" + PORT;
	private static String PATH = "http-rdf-update";
	private static String GRAPH_URI = "http://www.example.com/mygraph";

	private static Client client = null;
	private WebResource resource = null; 
	private Model model = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		client = Client.create();
	}

	@Before
	public void setUp() throws Exception {
		resource = client.resource(URL).path(PATH);
		
		model = ModelFactory.createDefaultModel();
		model.add(model.createResource("foo:bar"), RDFS.label, model.createLiteral("Bar"));
	}

	@After
	public void tearDown() throws Exception {
		if (resource != null) {
			resource = null;
		}
		
		if (model != null) {
			model.close();
			model = null;
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		if (client != null) {
			client.destroy();
			client = null;
		}
	}

	@Test
	public void testSetUp() {
		assertNotNull(client);
		assertNotNull(resource);
		assertNotNull(model);
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

	@Test
	public void testGetXML() {
		testPostXML();
		
		InputStream in = resource.queryParam("graph", GRAPH_URI).accept(RDFMediaType.APPLICATION_TURTLE_CURRENT).get(InputStream.class);
		Model m = ModelFactory.createDefaultModel();
		m.read(in, "", "TURTLE");
		assertTrue(model.isIsomorphicWith(m));
	}

	@Test
	public void testGetTurtle() {
		testPostTurtle();
		
		InputStream in = resource.queryParam("graph", GRAPH_URI).accept(RDFMediaType.APPLICATION_TURTLE_CURRENT).get(InputStream.class);
		Model m = ModelFactory.createDefaultModel();
		m.read(in, "", "TURTLE");
		assertTrue(model.isIsomorphicWith(m));
	}

	@Test
	public void testPut() {
		assertEquals("Put!", resource.queryParam("graph", GRAPH_URI).accept(MediaType.TEXT_PLAIN).put(String.class));
	}

	@Test
	public void testPostXML() {
		StringWriter content = new StringWriter();
		model.write(content, "RDF/XML");
		/* InputStream in = */ resource.queryParam("graph", GRAPH_URI).accept(MediaType.TEXT_PLAIN).entity(content.toString(), RDFMediaType.APPLICATION_RDFXML).post(InputStream.class);
//		assertEquals(Response.Status.CREATED, response.getStatus());
//		System.out.println (response);
	}

	@Test
	public void testPostTurtle() {
		StringWriter content = new StringWriter();
		model.write(content, "TURTLE");
		/* InputStream in = */ resource.queryParam("graph", GRAPH_URI).accept(MediaType.TEXT_PLAIN).entity(content.toString(), RDFMediaType.APPLICATION_TURTLE_CURRENT).post(InputStream.class);
//		assertEquals(Response.Status.CREATED, response.getStatus());
//		System.out.println (response);
	}

	// TODO: there must be a better way
	@Test
	public void testDelete() {
		try { 
			Response response = resource.queryParam("graph", GRAPH_URI).accept(MediaType.WILDCARD_TYPE).delete(Response.class);
			assertEquals(Response.Status.NOT_FOUND, response.getStatus());
		} catch (UniformInterfaceException e) {
			// expected
		}
		
		testPostTurtle();

		try { 
			Response response = resource.queryParam("graph", GRAPH_URI).accept(MediaType.WILDCARD_TYPE).delete(Response.class);
			assertEquals(Response.Status.NO_CONTENT, response.getStatus());
		} catch (UniformInterfaceException e) {
			// expected
		}
		
	}

    @Test
    public void testApplicationWadl() {
        resource = client.resource(URL);
        String wadl = resource.path("application.wadl").accept(MediaTypes.WADL).get(String.class);

        assertTrue(wadl.length() > 0);
        assertTrue(wadl.contains("method name=\"GET\""));
        assertTrue(wadl.contains("method name=\"PUT\""));
        assertTrue(wadl.contains("method name=\"POST\""));
        assertTrue(wadl.contains("method name=\"DELETE\""));
    }

}
