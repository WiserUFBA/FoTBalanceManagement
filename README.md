Pré-requisitos:

+ Executar no servicemix:

feature:repo-add mvn:org.ops4j.pax.jdbc/pax-jdbc-features/0.5.0/xml/features
feature:install transaction jndi pax-jdbc-h2 pax-jdbc-pool-dbcp2 pax-jdbc-config

+ Criar um arquivo dentro do servicemix de nome org.ops4j.datasource-balance.cfg na pasta etc com conteúdo:

osgi.jdbc.driver.name=H2-pool-xa
url=jdbc:h2:${karaf.data}/fot-balance
dataSourceName=fot-balance


