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
```

Copiar e colar no terminal do Faraf, nas demais máquinas:

```sh
feature:repo-add cellar 4.0.4
feature:install webconsole cellar cellar-obr cellar-log
```

### Support

Desenvolvido por Jurandir Barbosa <jurandirbarbosa@ifba.edu.br>.

