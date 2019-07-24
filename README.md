# unit

[![Build Status](https://travis-ci.com/patientsknowbest/unit.svg?token=LrppdCfPNf4VxFcdJJwi&branch=develop)](https://travis-ci.com/patientsknowbest/unit)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=com.pkb%3Aunit&metric=bugs)](https://sonarcloud.io/dashboard?id=com.pkb%3Aunit)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=com.pkb%3Aunit&metric=coverage)](https://sonarcloud.io/dashboard?id=com.pkb%3Aunit)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=com.pkb%3Aunit&metric=vulnerabilities)](https://sonarcloud.io/dashboard?id=com.pkb%3Aunit)

A library for managing the lifecycle of application components, or _units_. 

A complex application may be composed of several software components, e.g. 
a web server, a database, an HTTP API client. Components which rely on external
resources may sometimes fail, and you would like them to restart. You may also 
wish to change their configuration and dynamically restart, without restarting 
the whole application. This library helps to achieve this.

See the following demonstration:  
```java
class MyDatabase extends Unit {
    ...
    @Override public HandleOutcome handleStart() {
        try {
            connection = DriverManager.getConnection(connectionUrl);
            return SUCCESS;
        } catch (Exception e) {
            return FAILED;
        }
    }
    
    @Override public HandleOutcome handleStop() {
        try {
            connection.close();
            return SUCCESS;
        } catch (Exception e) {
            return FAILED;
        }    
    }
    
    public ResultSet execSql(String sql) {
        try {
            return connection.exec(sql);            
        } catch (Exception e) {
            failed();
            throw new RuntimeException(e);
        }
        
    }
    ...
}

class MyWebServer extends Unit {
    ...
    @Override public HandleOutcome handleStart() {
        try {
            HttpServer server = HttpServer.create();
            httpServer.createContext("/", this::index);
            httpServer.start();
            return SUCCESS;
        } catch (Exception e) {
            return FAILED;
        }
    }
    
    @Override public HandleOutcome handleStop() {
        try {
            httpServer.stop(1);
            return SUCCESS;
        } catch (Exception e) {
            return FAILED;
        }    
    }
    
    public void index(HttpExchange ex) {
        ResultSet rs = myDatabase.exec("select name from person");
        ...
    }
    ...
}

class Main {
    public static void main(String[] args) {
        Bus b = new LocalBus();
        MyDatabase database = new MyDatabase(b);
        MyWebServer webServer = new MyWebServer(b, mydb);
        webServer.enable();
        ...
    }
}

```

In this example; if there is a problem with the database 
connection, then the unit `MyDatabase` moves into a failed state.
This stops the HTTP server as well, since it depends on the database.

The web server will continue to retry, until the database connection is 
restored and `MyDatabase` starts successfully. 

For other examples, including mutable configuration and reporting on the 
state of the system see [unit-plain-sample](https://github.com/patientsknowbest/unit-plain-sample)
and [unit-springboot-sample](https://github.com/patientsknowbest/unit-springboot-sample) 

maven
-----
The unit library can be added to your maven project as follows:
```xml
    <dependencies>
        <dependency>
            <groupId>com.pkb</groupId>
            <artifactId>unit</artifactId>
            <version>1.0.0</version>
        </dependency>
    </dependencies>
```

Please note this library is currently hosted on bintray, so you'll need to 
add the patientsknowbest repository into your maven settings:
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<settings xsi:schemaLocation='http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd'
          xmlns='http://maven.apache.org/SETTINGS/1.0.0' xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>
    
    <profiles>
        <profile>
            <repositories>
                <repository>
                    <snapshots>
                        <enabled>false</enabled>
                    </snapshots>
                    <id>bintray-patientsknowbest-patientsknowbest</id>
                    <name>bintray</name>
                    <url>https://dl.bintray.com/patientsknowbest/patientsknowbest</url>
                </repository>
            </repositories>
            <pluginRepositories>
                <pluginRepository>
                    <snapshots>
                        <enabled>false</enabled>
                    </snapshots>
                    <id>bintray-patientsknowbest-patientsknowbest</id>
                    <name>bintray-plugins</name>
                    <url>https://dl.bintray.com/patientsknowbest/patientsknowbest</url>
                </pluginRepository>
            </pluginRepositories>
            <id>bintray</id>
        </profile>
    </profiles>
    <activeProfiles>
        <activeProfile>bintray</activeProfile>
    </activeProfiles>
</settings>
```