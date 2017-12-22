# Fot Balance Management

### Introdução

Serviço de balanceamento de bundles através do conceito de grupos utilizando do framework para resolução
de problemas de planejamento OptaPlanner

### Instalação

Para utilizar o bundle você primeiramente deve instalar as seguintes dependências, e após isso
compilar e instalar o projeto na versão **Servicemix 7.0.1** através do [webconsole](http://localhost:8181/system/console).

Copiar e colar no terminal do Karaf, na máquina responsável pelo controller:

```sh
config:edit org.apache.karaf.features.repos
config:property-set kie mvn:org.kie/kie-karaf-features/7.0.0.Final/xml/features
config:update
feature:repo-add cellar 4.0.4
feature:repo-add kie 7.0.0.Final
feature:install webconsole cellar cellar-obr cellar-log optaplanner-engine
config:edit org.ops4j.pax.url.mvn 
config:property-append org.ops4j.pax.url.mvn.repositories ", https://github.com/WiserUFBA/wiser-mvn-repo/raw/master/releases@id=wiser"
config:update
bundle:install -s mvn:br.ufba.dcc.wiser.fot/httpInstallMvnBundle/1.0.10
config:edit org.apache.karaf.cellar.groups
config:property-set default.bundle.sync disabled
config:property-set default.config.sync disabled
config:property-set default.feature.sync disabled
config:property-set default.obr.urls.sync disabled
config:update
```

Copiar e colar no terminal do karaf, nas demais máquinas:

```sh
feature:repo-add cellar 4.0.4
feature:install webconsole cellar cellar-obr cellar-log
config:edit org.ops4j.pax.url.mvn 
config:property-append org.ops4j.pax.url.mvn.repositories ", https://github.com/WiserUFBA/wiser-mvn-repo/raw/master/releases@id=wiser"
config:update
bundle:install -s mvn:br.ufba.dcc.wiser.fot/httpInstallMvnBundle/1.0.10
config:edit org.apache.karaf.cellar.groups
config:property-set default.bundle.sync disabled
config:property-set default.config.sync disabled
config:property-set default.feature.sync disabled
config:property-set default.obr.urls.sync disabled
config:update
```

## Support and development

<p align="center">
	Desenvolvido por Jurandir Barbosa <jurandirbarbosa@ifba.edu.br> em </br>
  <img src="https://wiki.dcc.ufba.br/pub/SmartUFBA/ProjectLogo/wiserufbalogo.jpg"/>
</p>
