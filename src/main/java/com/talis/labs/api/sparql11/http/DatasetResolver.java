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

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.base.file.Location;

@Provider
public class DatasetResolver implements ContextResolver<Dataset> {

	private static final Logger logger = LoggerFactory.getLogger(DatasetResolver.class);

	private static Dataset dataset = null;
	
	@Override
	public Dataset getContext(Class<?> type) {
		if ( dataset == null ) {
			dataset = TDBFactory.createDataset(new Location("TDB"));
			logger.info("Dataset {} created", dataset);
		} 

		return dataset;
	}
	
}
