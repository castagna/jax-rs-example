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

import javax.ws.rs.core.MediaType;

public class RDFMediaType {

	public final static String TEXT_N3 = "text/rdf+n3";
    public final static MediaType TEXT_N3_TYPE = new MediaType("text","rdf+n3");

	public final static String APPLICATION_N3 = "application/n3";
    public final static MediaType APPLICATION_N3_TYPE = new MediaType("application","n3");

    public final static String APPLICATION_TURTLE_CURRENT = "application/x-turtle";
    public final static MediaType APPLICATION_TURTLE_TYPE_CURRENT = new MediaType("application","x-turtle");
    
    public final static String APPLICATION_TURTLE_IDEAL = "application/turtle";
    public final static MediaType APPLICATION_TURTLE_TYPE_IDEAL = new MediaType("application","turtle");

    public final static String APPLICATION_RDFXML = "application/rdf+xml";
    public final static MediaType APPLICATION_RDFXML_TYPE = new MediaType("application","rdf+xml");
    
    public final static String TEXT_NTRIPLES = "text/plain";
    public final static MediaType TEXT_NTRIPLES_TYPE = new MediaType("text","plain");

    public final static String APPLICATION_NTRIPLES = "application/n-triples";
    public final static MediaType APPLICATION_NTRIPLES_TYPE = new MediaType("application","n-triples");

    public final static String APPLICATION_SPARQL_RESULTS_XML = "application/sparql-result+xml";
    public final static MediaType APPLICATION_SPARQL_RESULTS_XML_TYPE = new MediaType("application","sparql-result+xml");

    public final static String APPLICATION_SPARQL_RESULTS_JSON = "application/sparql-result+json";
    public final static MediaType APPLICATION_SPARQL_RESULTS_JSON_TYPE = new MediaType("application","sparql-result+json");

    public final static String APPLICATION_SPARQL_QUERY_X = "application/x-sparql-query";
    public final static MediaType APPLICATION_SPARQL_QUERY_X_TYPE = new MediaType("application","x-sparql-query");

    public final static String APPLICATION_SPARQL_QUERY = "application/sparql-query";
    public final static MediaType APPLICATION_SPARQL_QUERY_TYPE = new MediaType("application","sparql-query");

    public final static String APPLICATION_SPARQL_UPDATE_X = "application/x-sparql-update";
    public final static MediaType APPLICATION_SPARQL_UPDATE_X_TYPE = new MediaType("application","x-sparql-update");

    public final static String APPLICATION_SPARQL_UPDATE = "application/sparql-update";
    public final static MediaType APPLICATION_SPARQL_UPDATE_TYPE = new MediaType("application","sparql-update");

}
