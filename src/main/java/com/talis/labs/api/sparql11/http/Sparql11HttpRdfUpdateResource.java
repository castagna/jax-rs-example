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

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Providers;

import org.apache.xerces.util.URI;
import org.apache.xerces.util.URI.MalformedURIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.shared.Lock;
import com.hp.hpl.jena.update.UpdateAction;

@Path("/http-rdf-update")
public class Sparql11HttpRdfUpdateResource {

	private static final Logger logger = LoggerFactory.getLogger(Sparql11HttpRdfUpdateResource.class);
	
	@Context 
	private Providers providers;

	@GET 
	@Produces(MediaType.TEXT_PLAIN)
	public Response doGetPlain() {
		logger.info("GET");
		return Response.status(Response.Status.OK).entity("Hello World!").build();
	}
	
	@GET 
	@Produces("application/rdf+xml")
	public StreamingOutput doGetXML(@QueryParam("graph") final String uri) {
		logger.info("GET, uri = {}", uri);
		validateUri(uri);
		return new GetModelStreamingOutput(getDataset(), uri, RDFMediaType.APPLICATION_RDFXML); 
	}

	@GET
	@Produces("application/x-turtle")
	public StreamingOutput doGetTurtle(@QueryParam("graph") final String uri) {
		logger.info("GET, uri = {}", uri);
		validateUri(uri);
		return new GetModelStreamingOutput(getDataset(), uri, RDFMediaType.APPLICATION_TURTLE_CURRENT); 
	}

	@GET
	@Produces("application/n-triples")
	public StreamingOutput doGetNTriples(@QueryParam("graph") final String uri) {
		logger.info("GET, uri = {}", uri);
		validateUri(uri);
		return new GetModelStreamingOutput(getDataset(), uri, RDFMediaType.APPLICATION_NTRIPLES); 
	}

	@POST 
	@Consumes(RDFMediaType.APPLICATION_RDFXML)
	@Produces(MediaType.TEXT_PLAIN)
	public Response doPostXML(@QueryParam("graph") final String uri, final InputStream in) {
		logger.info("POST to {}", uri);
		validateUri(uri);
		Dataset dataset = getDataset();
		StreamingOutput so = new PutPostModelStreamingOutput(dataset, uri, RDFMediaType.APPLICATION_RDFXML, in, true);
		return Response.status(Response.Status.CREATED).header(HttpHeaders.LOCATION, uri).entity(so).build();
	}

	@POST 
	@Consumes(RDFMediaType.APPLICATION_TURTLE_CURRENT)
	@Produces(MediaType.TEXT_PLAIN)
	public Response doPostTurtle(@QueryParam("graph") final String uri, final InputStream in) {
		logger.info("POST to {}", uri);
		validateUri(uri);
		Dataset dataset = getDataset();
		StreamingOutput so = new PutPostModelStreamingOutput(dataset, uri, RDFMediaType.APPLICATION_TURTLE_CURRENT, in, true);
		return Response.status(Response.Status.CREATED).header(HttpHeaders.LOCATION, uri).entity(so).build();
	}
	
	@POST 
	@Consumes(RDFMediaType.APPLICATION_NTRIPLES)
	@Produces(MediaType.TEXT_PLAIN)
	public Response doPostNTriples(@QueryParam("graph") final String uri, final InputStream in) {
		logger.info("POST to {}", uri);
		validateUri(uri);
		Dataset dataset = getDataset();
		StreamingOutput so = new PutPostModelStreamingOutput(dataset, uri, RDFMediaType.APPLICATION_NTRIPLES, in, true);
		return Response.status(Response.Status.CREATED).header(HttpHeaders.LOCATION, uri).entity(so).build();
	}
	
	@PUT
	@Consumes(RDFMediaType.APPLICATION_RDFXML)
	@Produces(MediaType.TEXT_PLAIN)
	public Response doPutXML(@QueryParam("graph") final String uri, final InputStream in) {
		validateUri(uri);
		Dataset dataset = getDataset();
		StreamingOutput so = new PutPostModelStreamingOutput(dataset, uri, RDFMediaType.APPLICATION_RDFXML, in, false);
		return Response.status(Response.Status.CREATED).entity(so).build();
	}

	@PUT
	@Consumes(RDFMediaType.APPLICATION_TURTLE_CURRENT)
	@Produces(MediaType.TEXT_PLAIN)
	public Response doPutTurtle(@QueryParam("graph") final String uri, final InputStream in) {
		validateUri(uri);
		Dataset dataset = getDataset();
		StreamingOutput so = new PutPostModelStreamingOutput(dataset, uri, RDFMediaType.APPLICATION_TURTLE_CURRENT, in, false);
		return Response.status(Response.Status.CREATED).entity(so).build();
	}

	@PUT
	@Consumes(RDFMediaType.APPLICATION_NTRIPLES)
	@Produces(MediaType.TEXT_PLAIN)
	public Response doPutNTriples(@QueryParam("graph") final String uri, final InputStream in) {
		validateUri(uri);
		Dataset dataset = getDataset();
		StreamingOutput so = new PutPostModelStreamingOutput(dataset, uri, RDFMediaType.APPLICATION_NTRIPLES, in, false);
		return Response.status(Response.Status.CREATED).entity(so).build();
	}

	@DELETE 
	@Produces(MediaType.TEXT_PLAIN)
	public Response doDelete(@QueryParam("graph") final String uri) {
		validateUri(uri);

		Dataset dataset = getDataset();
		Lock lock = dataset.getLock();
		try {
			lock.enterCriticalSection(true);
			if (dataset.containsNamedModel(uri)) {
				UpdateAction.parseExecute("DROP GRAPH <" + uri + ">", dataset);
			} else {
				throw new WebApplicationException(Response.Status.NOT_FOUND);
			}
		} catch (WebApplicationException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		} finally {
			lock.leaveCriticalSection();
		}
		
		return Response.status(Response.Status.NO_CONTENT).build();
	}

	private Dataset getDataset() {
		logger.info ("PROVIDERS >>>>>>>>>>>>>" + providers);
		ContextResolver<Dataset> ds = providers.getContextResolver(Dataset.class, MediaType.WILDCARD_TYPE);
		logger.info ("DATASET >>>>>>>>>>>>>" + ds);
		Dataset dataset = ds.getContext(Dataset.class);
		
		return dataset;
	}
	
	private void validateUri(String uri) throws WebApplicationException {
		if ((uri == null) || (uri.length() == 0)) { 
			throw new WebApplicationException(Response.Status.BAD_REQUEST); 
		}

		try {
			new URI(uri);
		} catch (MalformedURIException e) {
			throw new WebApplicationException(Response.Status.BAD_REQUEST); 
		} 
	}
	
}
