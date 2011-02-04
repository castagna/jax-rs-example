SPARQL 1.1 Uniform HTTP Protocol for Managing RDF Graphs
--------------------------------------------------------

This is an implementation of the SPARQL 1.1 Uniform HTTP Protocol for Managing 
RDF Graphs [1,2] and I am using it to learn more about JAX-RS and Jersey. 
  

Maven
-----

Once you have installed Maven, you can have fun with the following commands:

  mvn -Declipse.workspace=/opt/workspace eclipse:add-maven-repo
  mvn eclipse:clean eclipse:eclipse -DdownloadSources=true
  mvn dependency:resolve
  mvn compile
  mvn test
  mvn package
  mvn site
  mvn install
  mvn deploy
  mvn jetty:run


                                                             -- Paolo Castagna


  [1] http://www.w3.org/2009/sparql/docs/http-rdf-update/
  [2] http://www.w3.org/TR/sparql11-http-rdf-update/
