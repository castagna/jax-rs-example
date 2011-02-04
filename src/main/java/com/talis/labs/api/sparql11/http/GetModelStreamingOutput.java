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

import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.shared.Lock;

public class GetModelStreamingOutput implements StreamingOutput {

	private String uri = null;
	private String mediaType = null;
	private Dataset dataset = null;
	
	public GetModelStreamingOutput(Dataset dataset, String uri, String mediaType) {
		this.dataset = dataset;
		this.uri = uri;
		this.mediaType = mediaType;
	}
	
	@Override
	public void write(OutputStream output) throws IOException, WebApplicationException {
		Lock lock = dataset.getLock();
		try {
			lock.enterCriticalSection(false);
			if (dataset.containsNamedModel(uri)) {
				String lang = null;
				if (RDFMediaType.APPLICATION_TURTLE_CURRENT.equals(mediaType)) {
					lang = "TURTLE";
				} else if (RDFMediaType.APPLICATION_RDFXML.equals(mediaType)) {
					lang = "RDF/XML";
				} else if (RDFMediaType.APPLICATION_NTRIPLES.equals(mediaType)) {
					lang = "N-TRIPLE";
				} else {
					throw new WebApplicationException(Response.Status.BAD_REQUEST);
				}
				dataset.getNamedModel(uri).write(output, lang);
			} else {
				throw new WebApplicationException(Response.Status.NOT_FOUND);
			}
		} catch (WebApplicationException e) {
			throw e;
		} catch (Exception e) {
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		} finally {
			lock.leaveCriticalSection();
		}		
	}

}
