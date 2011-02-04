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
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.Lock;

public class PutPostModelStreamingOutput implements StreamingOutput {

	private String uri = null;
	private String mediaType = null;
	private Dataset dataset = null;
	private InputStream in = null;
	private boolean clean = false;
	
	public PutPostModelStreamingOutput(Dataset dataset, String uri, String mediaType, InputStream in, boolean clean) {
		this.dataset = dataset;
		this.uri = uri;
		this.mediaType = mediaType;
		this.in = in;
		this.clean = clean;
	}
	
	@Override
	public void write(OutputStream output) throws IOException, WebApplicationException {
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
		Model model = ModelFactory.createDefaultModel();
		model.read(in, "", lang);

		Lock lock = dataset.getLock();
		try {
			lock.enterCriticalSection(true);
			if ( clean ) {
				if (dataset.containsNamedModel(uri)) {
					dataset.getNamedModel(uri).removeAll();
				}
			}
			dataset.getNamedModel(uri).add(model);
		} catch (Exception e) {
			throw new WebApplicationException(e, Response.Status.INTERNAL_SERVER_ERROR);
		} finally {
			lock.leaveCriticalSection();
		}
	}

}
