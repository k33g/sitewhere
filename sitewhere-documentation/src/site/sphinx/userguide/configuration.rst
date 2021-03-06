====================
System Configuration
====================
SiteWhere uses a hierarchy of `Spring <http://projects.spring.io/spring-framework/>`_ XML files as
its configuration mechanism. When the SiteWhere server starts, one of the first steps is to bootstrap
the core system components by loading the **conf/sitewhere/sitewhere-server.xml** file. This file
acts as the global server configuration, specifying aspects of the system shared by all tenants such
as the user datastore implementation and Hazelcast configuration. In addition to the global configuration,
there is a per-tenant configuration in **conf/sitewhere/xxx-tenant.xml** (where **xxx** is the tenant id)
which specifies details about how the tenant engine is to be configured. Most other SiteWhere features 
such as device management and communication engine are configured in these tenant configuration files.

.. contents:: Contents
   :local:

---------------------------------
Global Configuration Fundamentals
---------------------------------
A valid SiteWhere configuration is based on a standard Spring beans XML file with an embedded section
that uses a schema specific to SiteWhere. The XML below is a partial configuration file illustrating some
of the key features. 

Notice the schema declarations and enclosing *<beans>* element at the top of the file. These are standard for a 
`Spring beans <http://docs.spring.io/spring-framework/docs/current/spring-framework-reference/html/beans.html>`_ 
configuration file. There is an *http://www.sitewhere.com/schema/sitewhere/ce* namespace declared and 
pointed to the schema for the targeted release. Often a new SiteWhere release will add features to the 
schema, so it is important to point to the schema for the version of SiteWhere being run on the server.

The *<sw:configuraton>* section contains all of the schema-based SiteWhere configuration elements. If a
schema-aware editor such as Eclipse is being used, the editor will provide syntax completion based on the 
SiteWhere schema. An example of a SiteWhere global configuration file is included below:

.. code-block:: xml

   <?xml version="1.0" encoding="UTF-8"?>
   <beans xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xmlns:context="http://www.springframework.org/schema/context" xmlns:sw="http://www.sitewhere.com/schema/sitewhere/ce"
      xsi:schemaLocation="
              http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.1.xsd
              http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-3.1.xsd
              http://www.springframework.org/schema/security http://www.springframework.org/schema/security/spring-security-3.0.xsd
              http://www.sitewhere.com/schema/sitewhere/ce http://www.sitewhere.org/schema/sitewhere/ce/1.2.0/sitewhere.xsd">
              
      <!-- Load property values for substitution -->
      <context:property-placeholder location="file:${catalina.home}/conf/sitewhere/sitewhere.properties"
         ignore-resource-not-found="true"/>
      
      <!-- ########################### -->
      <!-- # SITEWHERE CONFIGURATION # -->
      <!-- ########################### -->
      <sw:configuration>
   
         <!-- ################################# -->
         <!-- # GLOBAL SERVICES CONFIGURATION # -->
         <!-- ################################# -->
         <sw:globals>
            <sw:hazelcast-configuration configFileLocation="${catalina.home}/conf/sitewhere/hazelcast.xml"/>
            <sw:solr-configuration solrServerUrl="http://localhost:8983/solr/SiteWhere"/>
            <sw:groovy-configuration debug="true" verbose="true"/>
         </sw:globals>
         
         <!-- ################################## -->
         <!-- # GLOBAL DATASTORE CONFIGURATION # -->
         <!-- ################################## -->
         <sw:datastore>
         
            <!-- MongoDB datastore used for global data model -->
            <sw:mongo-datastore hostname="localhost" port="27017" databaseName="sitewhere"/>
            
            <!-- Initializes users and tenant data if datastore is empty -->
            <sw:default-user-model-initializer/>
   
         </sw:datastore>
   
      </sw:configuration>
   
   </beans>

   
Moving Sensitive Data Outside of the Configuration
--------------------------------------------------
SiteWhere configuration files often contain login credentials or other information that should not
be shared with other users. Also, there are situations where settings for a system are 
environment-specific (production vs. staging vs. development) and maintaining a separate configuration 
for each creates extra work. Using Spring
`property placeholders <http://docs.spring.io/spring-framework/docs/current/spring-framework-reference/html/xsd-config.html#xsd-config-body-schemas-context-pphc>`_
allows sensitive data to be moved into an external properties file and injected at runtime.
In the following example, the hostname and port for the MongoDB datastore would be loaded from
the **sitewhere.properties** file in the same directory as the main configuration file.

.. code-block:: xml
   :emphasize-lines: 1, 14
   
   <context:property-placeholder location="file:${catalina.home}/conf/sitewhere/sitewhere.properties" ignore-resource-not-found="true"/>

   <!-- ########################### -->
   <!-- # SITEWHERE CONFIGURATION # -->
   <!-- ########################### -->
   <sw:configuration>
      
      <!-- ########################### -->
      <!-- # DATASTORE CONFIGURATION # -->
      <!-- ########################### -->
      <sw:datastore>
      
         <!-- Default MongoDB Datastore -->
         <sw:mongo-datastore hostname="${mongo.host}" port="${mongo.port}" databaseName="sitewhere"/>
 
The properties file would contain values for the placeholders as shown below:

.. code-block:: properties

   # SiteWhere configuration properties.
   mongo.host=localhost
   mongo.port=1234

------------------------------
Global Datastore Configuration
------------------------------
SiteWhere can use either `MongoDB <http://www.mongodb.org/>`_ or `Apache HBase <https://hbase.apache.org/>`_ for 
underlying data storage. Tenant datastores must use the same database type as is specified in the 
global configuration.

Configuring a MongoDB Datastore
-------------------------------
To use MongoDB as the global datastore, edit the SiteWhere configuration *<sw:datastore>* section
and use the *<sw:mongo-datastore>* element as shown below:

.. code-block:: xml
   :emphasize-lines: 7

   <!-- ################################## -->
   <!-- # GLOBAL DATASTORE CONFIGURATION # -->
   <!-- ################################## -->
   <sw:datastore>
   
      <!-- MongoDB datastore used for global data model -->
      <sw:mongo-datastore hostname="localhost" port="27017" databaseName="sitewhere"/>
      
      <!-- Initializes users and tenant data if datastore is empty -->
      <sw:default-user-model-initializer/>

   </sw:datastore>

Note that the default settings assume a local MongoDB instance running on the default port and using a database
named **sitewhere**.

The following attributes may be specified for the *<sw:mongo-datastore>* element.
      
+------------------------+----------+--------------------------------------------------+
| Attribute              | Required | Description                                      |
+========================+==========+==================================================+
| hostname               | optional | Server hostname for MongoDB instance.            |
|                        |          | Defaults to *localhost*.                         |
+------------------------+----------+--------------------------------------------------+
| port                   | optional | Server port for MongoDB instance.                |
|                        |          | Defaults to *27017*.                             |
+------------------------+----------+--------------------------------------------------+
| databaseName           | optional | MongoDB database name for SiteWhere storage.     |
|                        |          | Defaults to *sitewhere*.                         |
+------------------------+----------+--------------------------------------------------+

Configuring an HBase Datastore
------------------------------
To use Apache HBase as the global datastore, edit the SiteWhere configuration  *<sw:datastore>* section 
and use the *<sw:hbase-datastore>* element as shown below:

.. code-block:: xml
   :emphasize-lines: 7-8

   <!-- ################################## -->
   <!-- # GLOBAL DATASTORE CONFIGURATION # -->
   <!-- ################################## -->
   <sw:datastore>

      <!-- Default HBase Datastore -->
      <sw:hbase-datastore quorum="sandbox.hortonworks.com"
         zookeeperZnodeParent="/hbase-unsecure"/>

      <!-- Initializes users and tenant data if datastore is empty -->
      <sw:default-user-model-initializer/>

   </sw:datastore>

The above configuration may be used to connect to a Hortonworks HDP instance.

The following attributes may be specified for the *<sw:hbase-datastore>* element.
      
+--------------------------+----------+--------------------------------------------------+
| Attribute                | Required | Description                                      |
+==========================+==========+==================================================+
| quorum                   | required | Server hostname for HBase ZooKeeper quorum.      |
+--------------------------+----------+--------------------------------------------------+
| zookeeperClientPort      | optional | ZooKeeper client port. Defaults to 2181.         |
+--------------------------+----------+--------------------------------------------------+
| zookeeperZnodeParent     | optional | ZooKeeper znode parent. Defaults to '/hbase'.    |
+--------------------------+----------+--------------------------------------------------+
| zookeeperZnodeRootServer | optional | ZooKeeper znode root server. Defaults to         |
|                          |          | 'root-region-server'.                            |
+--------------------------+----------+--------------------------------------------------+

Populating Sample User and Tenant Data
--------------------------------------
In both MongoDB and HBase installations, SiteWhere will automatically create the underlying database if it does 
not already exist. After that, each time that SiteWhere server starts up, it will check whether there is data 
in the database and, if data initializers are configured, will prompt to populate 
the database with sample data (for non-console startup, there are properties on the 
model initializers in the configuration file that allow you to specify whether 
to populate the sample data automatically). SiteWhere has an initializer that will
create sample data for user and tenant data models. It can be configured by adding 
*<sw:default-user-model-initializer/>* to the *<sw:datastore>* section as shown in
the datastore examples above.

---------------------------------
Tenant Configuration Fundamentals
---------------------------------
In addition to the global configuration file, there is a per-tenant configuration file
located at **conf/sitewhere/xxx-tenant.xml** (where **xxx** is the tenant id). Each tenant
has its own device data and configurable processing pipeline. The tenant configuration file
uses Spring beans and a custom schema like the global configuration, but with a different
schema targeted at tenant-specific features:

.. code-block:: xml

   <?xml version="1.0" encoding="UTF-8"?>
   <beans xmlns="http://www.springframework.org/schema/beans" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xmlns:context="http://www.springframework.org/schema/context" xmlns:sw="http://www.sitewhere.com/schema/sitewhere/ce/tenant"
      xmlns:global="http://www.sitewhere.com/schema/sitewhere/ce"
      xsi:schemaLocation="
              http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.1.xsd
              http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-3.1.xsd
              http://www.springframework.org/schema/security http://www.springframework.org/schema/security/spring-security-3.0.xsd
              http://www.sitewhere.com/schema/sitewhere/ce http://www.sitewhere.org/schema/sitewhere/ce/1.2.0/sitewhere.xsd
              http://www.sitewhere.com/schema/sitewhere/ce/tenant http://www.sitewhere.org/schema/sitewhere/ce/1.2.0/sitewhere-tenant.xsd">
              
      <!-- Load property values for substitution -->
      <context:property-placeholder
         location="file:${catalina.home}/conf/sitewhere/${tenant.id}-tenant.properties"
         ignore-resource-not-found="true"/>
      
      <!-- ######################## -->
      <!-- # TENANT CONFIGURATION # -->
      <!-- ######################## -->
      <sw:tenant-configuration>

------------------------------
Tenant Datastore Configuration
------------------------------
Tenant datastores extend the features of the global datastore with
implementations of the device and asset data models.

Configuring a MongoDB Tenant Datastore
--------------------------------------
To use MongoDB as the tenant datastore, edit the SiteWhere configuration *<sw:tenant-datastore>* section
and use the *<sw:mongo-tenant-datastore>* element as shown below:

.. code-block:: xml
   :emphasize-lines: 7-8

   <!-- ########################### -->
   <!-- # DATASTORE CONFIGURATION # -->
   <!-- ########################### -->
   <sw:tenant-datastore>
   
      <!-- Default MongoDB Datastore -->
      <sw:mongo-tenant-datastore useBulkEventInserts="true"
         bulkInsertMaxChunkSize="1000"/>

      <!-- Improves performance by using Hazelcast for distributed caching -->
      <sw:hazelcast-cache/>
      
      <!-- Initializes data model with sample data if datastore is empty -->
      <sw:default-device-model-initializer/>
      <sw:default-asset-model-initializer/>

   </sw:tenant-datastore>

The following attributes may be specified for the *<sw:mongo-tenant-datastore>* element.
      
+------------------------+----------+--------------------------------------------------+
| Attribute              | Required | Description                                      |
+========================+==========+==================================================+
| useBulkEventInserts    | optional | Indicates whether the bulk loading APIs should   |
|                        |          | be used to increase event write performance.     |
|                        |          | Defaults to *false*.                             |
+------------------------+----------+--------------------------------------------------+
| bulkInsertMaxChunkSize | optional | Indicates the max number of events to queue      |
|                        |          | before sending a batch via the bulk APIs.        |
|                        |          | Defaults to *1000*.                              |
+------------------------+----------+--------------------------------------------------+

Configuring an HBase Tenant Datastore
-------------------------------------
To use HBase as the tenant datastore, edit the SiteWhere configuration  *<sw:tenant-datastore>* section 
and use the *<sw:hbase-tenant-datastore/>* element as shown below:

.. code-block:: xml
   :emphasize-lines: 7

   <!-- ########################### -->
   <!-- # DATASTORE CONFIGURATION # -->
   <!-- ########################### -->
   <sw:tenant-datastore>

      <!-- Default HBase Datastore -->
      <sw:hbase-tenant-datastore/>

      <!-- Improves performance by using Hazelcast for distributed caching -->
      <sw:hazelcast-cache/>
      
      <!-- Initializes data model with sample data if datastore is empty -->
      <sw:default-device-model-initializer/>
      <sw:default-asset-model-initializer/>

   </sw:tenant-datastore>


Device Management Cache Providers
---------------------------------
Many elements of the device data model do not change often and can benefit from a caching implementation.
SiteWhere offers a service provider interface 
`IDeviceManagementCacheProvider <../apidocs/com/sitewhere/spi/device/IDeviceManagementCacheProvider.html>`_
which may be implemented to provide caching capabilities that use an external cache provider.
Note that removing the cache will result in noticeably slower performance since the underlying
service provider implementations will load all data from the datastore.

Hazelcast Cache Provider
************************
SiteWhere offers a default device management cache implementation based on `Hazelcast <http://hazelcast.com//>`_
which can be configured as shown below:

.. code-block:: xml
   :emphasize-lines: 8

   <sw:tenant-datastore>
   
      <!-- Default MongoDB Datastore -->
      <sw:mongo-tenant-datastore useBulkEventInserts="true"
         bulkInsertMaxChunkSize="1000"/>

      <!-- Improves performance by using Hazelcast for distributed caching -->
      <sw:hazelcast-cache/>

The Hazelcast cache works well in standalone mode as well as in clustered environments since the cache
contents are synchronized across the Hazelcast cluster.

Ehcache Cache Provider
**********************
SiteWhere offers a device management cache implementation based on `Ehcache <http://ehcache.org/>`_
which can be configured as shown below:

.. code-block:: xml
   :emphasize-lines: 8

   <sw:tenant-datastore>
   
      <!-- Default MongoDB Datastore -->
      <sw:mongo-tenant-datastore useBulkEventInserts="true"
         bulkInsertMaxChunkSize="1000"/>

		<!-- Improves performance by using EHCache to store device management entities -->
		<sw:ehcache-device-management-cache/>

The following attributes may be specified for the *<sw:ehcache-device-management-cache>* element.
      
+------------------------------------+----------+--------------------------------------------------+
| Attribute                          | Required | Description                                      |
+====================================+==========+==================================================+
| siteCacheMaxEntries                | optional | Max number of site entries in cache.             |
+------------------------------------+----------+--------------------------------------------------+
| deviceSpecificationCacheMaxEntries | optional | Max number of specification entries in cache.    |
+------------------------------------+----------+--------------------------------------------------+
| deviceCacheMaxEntries              | optional | Max number of device entries in cache.           |
+------------------------------------+----------+--------------------------------------------------+
| deviceAssignmentCacheMaxEntries    | optional | Max number of assignment entries in cache.       |
+------------------------------------+----------+--------------------------------------------------+
| siteCacheTtl                       | optional | Time to live for site entries.                   |
+------------------------------------+----------+--------------------------------------------------+
| deviceSpecificationCacheTtl        | optional | Time to live for specification entries.          |
+------------------------------------+----------+--------------------------------------------------+
| deviceCacheTtl                     | optional | Time to live for device entries.                 |
+------------------------------------+----------+--------------------------------------------------+
| deviceAssignmentCacheTtl           | optional | Time to live for assignment entries.             |
+------------------------------------+----------+--------------------------------------------------+

Note that the Ehcache implementation should **not** be used in clustered environments. There will
be an instance of the cache on each SiteWhere instance and the caches will not be synchronized.

--------------------
Device Communication
--------------------
The communication subsystem configures how SiteWhere communicates with devices.
On the inbound side, device data is brought in to the system via **event sources**. The inbound 
data is converted into SiteWhere events and passed in to the **inbound processing chain** by 
the **inbound processing strategy**. On the outbound side (as part of the **outbound processing chain**)
commands are sent to external devices via **command destinations**. An **outbound command router** 
makes the choice of which command destination will be used to deliver the command payload.

Event Sources
-------------
Event sources are responsible for bringing data into SiteWhere. All event sources implement the
`IInboundEventSource <../apidocs/com/sitewhere/spi/device/communication/IInboundEventSource.html>`_
interface and are composed of one or more **event receivers** (implementing 
`IInboundEventReceiver <../apidocs/com/sitewhere/spi/device/communication/IInboundEventReceiver.html>`_) 
and a single **event decoder** (implementing 
`IDeviceEventDecoder <../apidocs/com/sitewhere/spi/device/communication/IDeviceEventDecoder.html>`_).
Event receivers take care of dealing with protocols for gathering data. The data is then processed
by the event decoder in order to create SiteWhere events which provide a common representation of
the device data so it can be processed by the inbound processing chain.

MQTT Event Source
*****************
Since consuming MQTT data is common in IoT applications, SiteWhere includes a component that 
streamlines the process. In the example below, an event source is configured to listen for messages
on the given topic, then use the *<sw:protobuf-event-decoder/>* to decode the message payload 
using the standard SiteWhere Google Protocol Buffers message format.

.. code-block:: xml
   :emphasize-lines: 7-10

   <sw:device-communication>
	
      <!-- Inbound event sources -->
      <sw:event-sources>

         <!-- Event source for protobuf messages over MQTT -->
         <sw:mqtt-event-source sourceId="protobuf" hostname="localhost"
            port="1883" topic="SiteWhere/input/protobuf">
            <sw:protobuf-event-decoder/>
        </sw:mqtt-event-source>

The following attributes may be specified for the *<sw:mqtt-event-source>* element.
      
+----------------------+----------+--------------------------------------------------+
| Attribute            | Required | Description                                      |
+======================+==========+==================================================+
| sourceId             | required | Unique event source id.                          |
+----------------------+----------+--------------------------------------------------+
| hostname             | required | MQTT broker server hostname or IP address.       |
+----------------------+----------+--------------------------------------------------+
| port                 | required | MQTT broker server port.                         |
+----------------------+----------+--------------------------------------------------+
| topic                | required | MQTT topic where devices will post events.       |
+----------------------+----------+--------------------------------------------------+

ActiveMQ Event Source
*********************
`Apache ActiveMQ <http://activemq.apache.org/>`_ is an open source messaging platform
that supports many wire formats such as AMQP, OpenWire, XMPP, and MQTT. It also supports
the standard Java JMS APIs for message processing. SiteWhere includes an event source
that creates an embedded ActiveMQ broker that listens on a configured transport. A
multithreaded pool of consumers listen on a configured topic and hand off the binary
payload to the configured decoder.

.. code-block:: xml
   :emphasize-lines: 7-10

   <sw:device-communication>
   
      <!-- Inbound event sources -->
      <sw:event-sources>

         <!-- Event source for protobuf messages over ActiveMQ queue -->
         <sw:activemq-event-source sourceId="activemq" transportUri="tcp://localhost:1234"
            queueName="SITEWHERE.IN" numConsumers="150">
            <sw:protobuf-event-decoder/>
         </sw:activemq-event-source>
         
The example above listens for JMS connections over TCP/IP with 150 consumer threads that 
read data from the configured queue, decode the data using SiteWhere Google Protocol Buffers
format, then send the decoded events to be processed.

The following attributes may be specified for the *<sw:activemq-event-source>* element.
      
+----------------------+----------+--------------------------------------------------+
| Attribute            | Required | Description                                      |
+======================+==========+==================================================+
| sourceId             | required | Unique event source id.                          |
+----------------------+----------+--------------------------------------------------+
| transportUri         | required | Configures the ActiveMQ transport that will be   |
|                      |          | made available for clients to connect to.        |
+----------------------+----------+--------------------------------------------------+
| queueName            | required | Queue that external clients post events to.      |
+----------------------+----------+--------------------------------------------------+
| numConsumers         | optional | Number of threaded consumers used to process     |
|                      |          | data from the queue. Defaults to *3*.            |
+----------------------+----------+--------------------------------------------------+

Socket Event Source
*******************
Many devices connect over direct socket connections to report events. For instance, many
GPS trackers have cellular connectivity and report location or other events over GPRS.
The *<sw:socket-event-source/>* can be used to create a server socket which listens
on a given port, receiving client connections and processing them using a multithreaded
approach. Socket interactions are often complex and stateful, so the processing is
delegated to an implementation of 
`ISocketInteractionHandler <../apidocs/com/sitewhere/spi/device/communication/socket/ISocketInteractionHandler.html>`_
which handles the conversation between device and server. The socket interaction handler
returns a payload which is passed to the configured decoder to build SiteWhere events.

.. code-block:: xml
   :emphasize-lines: 7-10

   <sw:device-communication>
   
      <!-- Inbound event sources -->
      <sw:event-sources>

         <!-- Event source for protobuf messages from socket connections -->
         <sw:socket-event-source port="8585" numThreads="10" sourceId="socket">
            <sw:read-all-interaction-handler-factory/>
            <sw:protobuf-event-decoder/>
         </sw:socket-event-source>

Configuring the *<sw:read-all-interaction-handler-factory/>* reads all of the input from
the client socket and passes the binary information to the configured decoder. In some cases
(such as sending payloads in the standard SiteWhere Google Protocol Buffers format) this
is sufficient. However, in most cases, the user will need to create an interaction handler that
understands the conversational logic between the device and server. A custom implementation
can be referenced by using the *<sw:interaction-handler-factory/>* element
which references a Spring bean that contains the socket interaction handler factory. The factory implements the
`ISocketInteractionHandlerFactory <../apidocs/com/sitewhere/spi/device/communication/socket/ISocketInteractionHandlerFactory.html>`_
interface and creates instances of the socket interaction handler that manages device 
conversation.

The following attributes may be specified for the *<sw:socket-event-source>* element.
      
+----------------------+----------+--------------------------------------------------+
| Attribute            | Required | Description                                      |
+======================+==========+==================================================+
| sourceId             | required | Unique event source id.                          |
+----------------------+----------+--------------------------------------------------+
| port                 | optional | Server port to listen on. Defaults to *8484*.    |
+----------------------+----------+--------------------------------------------------+
| numThreads           | required | Number of threads used to process client         |
|                      |          | requests. Defaults to *5*.                       |
+----------------------+----------+--------------------------------------------------+

WebSocket Event Source
**********************
A common connectivity option for IoT applications is interaction with a remote 
`WebSocket <http://en.wikipedia.org/wiki/WebSocket>`_. 
The *<sw:web-socket-event-source/>* can be used to connect to a WebSocket and
stream data into the system. The data payload can be either binary or text
and the event decoder should be configured based on the expected type of data.

.. code-block:: xml
   :emphasize-lines: 7-10

   <sw:device-communication>
   
      <!-- Inbound event sources -->
      <sw:event-sources>

         <!-- Event source for WebSocket connectivity -->
         <sw:web-socket-event-source sourceId="websocket"
            webSocketUrl="ws://localhost:6543/sitewhere/stringsender" payloadType="string">
            <sw:groovy-string-event-decoder scriptPath="customDecoder.groovy"/>
         </sw:web-socket-event-source>
         
Note that the payload type is 'string' and that the *<sw:groovy-string-event-decoder/>* decoder
expects a String input. If a binary decoder is configured for a String payload type or vice versa,
the system will generate an error on startup.

The following attributes may be specified for the *<sw:web-socket-event-source/>* element.
      
+----------------------+----------+--------------------------------------------------+
| Attribute            | Required | Description                                      |
+======================+==========+==================================================+
| sourceId             | required | Unique event source id.                          |
+----------------------+----------+--------------------------------------------------+
| webSocketUrl         | required | URL of the WebSocket to connect to.              |
+----------------------+----------+--------------------------------------------------+
| payloadType          | required | Either 'string' or 'binary' depending on which   |
|                      |          | type of message is sent from the server socket.  |
+----------------------+----------+--------------------------------------------------+

Hazelcast Queue Event Source
****************************
This event source is used to pull decoded device events from a Hazelcast queue. 
The usual usage scenario is that one SiteWhere instance uses the
*<sw:hazelcast-queue-processor>* on the inbound processing chain to send all decoded events
to the queue and the subordinate instances use the *<sw:hazelcast-queue-event-source>*
element to process the events. Multiple subordinate instances can attach to the
same queue, allowing parallel processing of the events. Note that all subordinate
instances must be in the same Hazelcast group in order to process the queue.

.. code-block:: xml
   :emphasize-lines: 7-10

   <sw:device-communication>
   
      <!-- Inbound event sources -->
      <sw:event-sources>

         <!-- Event source for pulling events from Hazelcast queue -->
         <sw:hazelcast-queue-event-source sourceId="hzQueue"/>

The following attributes may be specified for the *<sw:hazelcast-queue-event-source>* element.
      
+----------------------+----------+--------------------------------------------------+
| Attribute            | Required | Description                                      |
+======================+==========+==================================================+
| sourceId             | required | Unique event source id.                          |
+----------------------+----------+--------------------------------------------------+

Custom Event Source
*******************
In cases where a custom protocol is needed to support inbound events for devices, SiteWhere makes
it easy to plug in a custom event source. The custom event source class must implement the
`IInboundEventSource <../apidocs/com/sitewhere/spi/device/communication/IInboundEventSource.html>`_
interface. SiteWhere provides base classes that provide much of the common event source 
functionality. For instance the com.sitewhere.device.communication.BinaryInboundEventSource found
in sitewhere-core provides an event source that deals with binary data. By creating an instance
of BinaryInboundEventSource and plugging in a custom 
`IInboundEventReceiver <../apidocs/com/sitewhere/spi/device/communication/IInboundEventReceiver.html>`_
and `IDeviceEventDecoder <../apidocs/com/sitewhere/spi/device/communication/IDeviceEventDecoder.html>`_
implementation, the behavior can be completely customized. The event receiver takes care of receiving
binary data from the device and the decoder converts the data into SiteWhere events that can be 
processed.

.. code-block:: xml
   :emphasize-lines: 7

   <sw:device-communication>
   
      <!-- Inbound event sources -->
      <sw:event-sources>

         <!-- Custom event source referencing a Spring bean -->
         <sw:event-source ref="customEventSourceBean"/>

The following attributes may be specified for the *<sw:event-source>* element.
      
+----------------------+----------+--------------------------------------------------+
| Attribute            | Required | Description                                      |
+======================+==========+==================================================+
| ref                  | required | Reference to externally defined Spring bean      |
+----------------------+----------+--------------------------------------------------+

Inbound Processing Strategy
---------------------------
The inbound processing strategy is responsible for moving events from event sources into the
inbound processing chain. It is responsible for handling threading and reliably delivering
events for processing. An inbound processing strategy must implement the 
`IInboundProcessingStrategy <../apidocs/com/sitewhere/spi/device/communication/IInboundProcessingStrategy.html>`_
interface.

Default Inbound Processing Strategy
***********************************
The default inbound processing strategy for SiteWhere CE uses a bounded queue to hold events
being delivered from event sources. It creates a thread pool that consumes the queue to 
deliver events to the inbound processing chain. If events are delivered faster than the thread
pool can process them, the queue will eventually start blocking the event receiver threads.
Increasing the number of threads for event processing takes load from the queue but increases
processing load on the core system. SiteWhere CE does not persist the inbound queue, so shutting 
down the server may result in data loss. SiteWhere EE offers a more advanced inbound processing
strategy implementation with persistent queues and transactional semantics.

.. code-block:: xml
   :emphasize-lines: 5-6

   <sw:device-communication>
   
         <!-- Inbound Processing Strategy -->
         <sw:inbound-processing-strategy>
            <sw:default-inbound-processing-strategy
               numEventProcessorThreads="150" enableMonitoring="true" monitoringIntervalSec="1"/>
         </sw:inbound-processing-strategy>

The following attributes may be specified for the *<sw:default-inbound-processing-strategy>* element.
      
+--------------------------+----------+----------------------------------------------------+
| Attribute                | Required | Description                                        |
+==========================+==========+====================================================+
| numEventProcessorThreads | optional | Number of threads used to process incoming events. |
|                          |          | Defaults to *100*.                                 |
+--------------------------+----------+----------------------------------------------------+
| enableMonitoring         | optional | Enables monitoring of event processing in the log. |
|                          |          | Defaults to *false*.                               |
+--------------------------+----------+----------------------------------------------------+
| monitoringIntervalSec    | optional | Interval (in seconds) at which monitoring messages |
|                          |          | are posted. Defaults to *5*.                       |
+--------------------------+----------+----------------------------------------------------+

Batch Operation Manager
-----------------------
The batch operation manager is responsible for asynchronously processing operations that 
are applied to many devices. Batch operations can be submitted via the administrative
console or via the REST services. The batch operation manager cycles through the list 
of batch operation elements, executing each and keeping state regarding progress of
execution. The default batch operation manager can be configured by using the
*<sw:default-batch-operation-manager>* element as shown below.

.. code-block:: xml
   :emphasize-lines: 5

   <sw:device-communication>
               
      <!-- Batch operation management -->
      <sw:batch-operations>
         <sw:default-batch-operation-manager throttleDelayMs="10000"/>
      </sw:batch-operations>

The throttle delay value can be used to slow down the rate that elements are processed
so that the system is not overloaded by large operations.
      
A custom batch operation manager can be added by creating a class that implements
`IBatchOperationManager <../apidocs/com/sitewhere/spi/device/batch/IBatchOperationManager.html>`_
and adding a reference to it using the *<sw:batch-operation-manager>* element.

The following attributes may be specified for the *<sw:default-batch-operation-manager>* element.
      
+--------------------------+----------+----------------------------------------------------+
| Attribute                | Required | Description                                        |
+==========================+==========+====================================================+
| throttleDelayMs          | optional | Number of milliseconds to wait between processing  |
|                          |          | batch operation elements. Defaults to *0*.         |
+--------------------------+----------+----------------------------------------------------+

Command Destinations
--------------------
Command destinations are responsible for delivering commands to devices. All command destinations implement the
`ICommandDestination <../apidocs/com/sitewhere/spi/device/communication/ICommandDestination.html>`_
interface and are composed of a **command encoder** (implementing 
`ICommandExecutionEncoder <../apidocs/com/sitewhere/spi/device/communication/ICommandExecutionEncoder.html>`_),
a **parameter extractor** (implementing
`ICommandDeliveryParameterExtractor <../apidocs/com/sitewhere/spi/device/communication/ICommandDeliveryParameterExtractor.html>`_),
and a **delivery provider** (implementing 
`ICommandDeliveryProvider <../apidocs/com/sitewhere/spi/device/communication/ICommandDeliveryProvider.html>`_).
The command encoder is used to convert the command payload into a format understood by the device. The parameter
extractor pulls information needed for delivering the message to the delivery provider (e.g. for an SMS provider,
the extractor may pull the SMS phone number for the device from device metadata). The delivery provider takes 
the encoded payload and extracted parameters, then delivers the message to the device.

MQTT Command Destination
************************
For devices that listen on an MQTT topic for commands, the *<sw:mqtt-command-destination>* element can 
be used to easily configure a destination. An encoder and parameter extractor should be configured
based on the expected command format and location of MQTT routing information. The 
*<sw:hardware-id-topic-extractor>* element configures the MQTT topics for delivery based
on an expression that includes the hardware id of the device to be addressed. In cases where this
is not appropriate, a custom parameter extractor can be injected instead.

.. code-block:: xml
   :emphasize-lines: 7-12

   <sw:device-communication>
					
      <!-- Outbound command destinations -->
      <sw:command-destinations>

         <!-- Delivers commands via MQTT -->
         <sw:mqtt-command-destination destinationId="default"
            hostname="localhost" port="1883">
            <sw:protobuf-command-encoder/>
            <sw:hardware-id-topic-extractor commandTopicExpr="SiteWhere/commands/%s"
               systemTopicExpr="SiteWhere/system/%s"/>
         </sw:mqtt-command-destination>

The following attributes may be specified for the *<sw:mqtt-command-destination>* element.
      
+----------------------+----------+--------------------------------------------------+
| Attribute            | Required | Description                                      |
+======================+==========+==================================================+
| destinationId        | required | Unique id for destination.                       |
+----------------------+----------+--------------------------------------------------+
| hostname             | required | MQTT broker hostname.                            |
+----------------------+----------+--------------------------------------------------+
| port                 | required | MQTT broker port.                                |
+----------------------+----------+--------------------------------------------------+

Twilio Command Destination
**************************
For devices that receive commands via SMS messages, the *<sw:twilio-command-destination>* may be used to
deliver the command via the `Twilio <https://www.twilio.com/>`_ online service. To use the service you will
need to create a Twilio account and pay for the outbound SMS service (including a phone number that
messages will be sent from).

.. code-block:: xml
   :emphasize-lines: 7-12

   <sw:device-communication>
					
      <!-- Outbound command destinations -->
      <sw:command-destinations>

         <!-- Delivers commands via Twilio SMS messages -->
         <sw:twilio-command-destination destinationId="laipac"
            accountSid="${twilio.account.sid}" authToken="${twilio.auth.token}" 
            fromPhoneNumber="${twilio.from.phone.number}">
            <sw:protobuf-command-encoder/>
            <sw:parameter-extractor ref="laipacExtractor"/>
         </sw:twilio-command-destination>
				
The account SID, auth token, and sending phone number are all pieces of data related to the Twilio account.
The parameter extractor implementation should be one that supplies parameters of type 
SmsParameters which is used by the delivery provider to determine the SMS phone number 
to deliver the command to.

The following attributes may be specified for the *<sw:twilio-command-destination>* element.
      
+----------------------+----------+--------------------------------------------------+
| Attribute            | Required | Description                                      |
+======================+==========+==================================================+
| destinationId        | required | Unique id for destination.                       |
+----------------------+----------+--------------------------------------------------+
| accountSid           | required | Twilio account SID (from Twilio website).        |
+----------------------+----------+--------------------------------------------------+
| authToken            | required | Twilio account auth token (from Twilio website). |
+----------------------+----------+--------------------------------------------------+
| fromPhoneNumber      | required | Twilio phone number used to originate SMS.       |
+----------------------+----------+--------------------------------------------------+

------------------------
Inbound Processing Chain
------------------------
After data has been decoded into SiteWhere device events by event sources, the
inbound processing strategy queues up events to be processed by the 
**inbound processing chain**. The chain is a series of **inbound event processors** (implementing 
`IInboundEventProcessor <../apidocs/com/sitewhere/spi/device/event/processor/IInboundEventProcessor.html>`_)
that each handle the inbound events in series. New inbound event processors can be added to the chain to augment
the existing functionality. For instance, a metrics processor could keep count of events processed per second. 

**Since REST calls (or other calls that directly invoke the device management APIs) do not enter the system via event sources, 
they are not processed by the inbound processing chain.**

Event Storage Processor
-----------------------
By default, an instance of *<sw:event-storage-processor/>* is configured in the inbound chain. This processor
takes care of persisting device events via the device management service provider interfaces. If this 
processor is removed, events will not be stored. The default configuration is shown below:

.. code-block:: xml
   :emphasize-lines: 6

      <sw:device-communication>
					
         <sw:inbound-processing-chain>

            <!-- Store events -->
            <sw:event-storage-processor/>

         </sw:inbound-processing-chain>

Registration Processor
----------------------
By default, an instance of *<sw:registration-processor/>* is configured in the inbound chain. This processor
handles the dynamic registration of devices which includes creating a new device and assignment for
devices requesting registration. If this processor is removed, registration requests will be ignored. 
The default configuration is shown below:

.. code-block:: xml
   :emphasize-lines: 6

      <sw:device-communication>
               
         <sw:inbound-processing-chain>
            
            <!-- Allow devices to dynamically register -->
            <sw:registration-processor/>
   
         </sw:inbound-processing-chain>

Device Stream Processor
-----------------------
By default, an instance of *<sw:device-stream-processor/>* is configured in the inbound chain. This processor
handles streaming data from devices. If this processor is removed, stream creation requests as well as requests
for adding data to a stream will be ignored. The default configuration is shown below:

.. code-block:: xml
   :emphasize-lines: 6

      <sw:device-communication>
               
         <sw:inbound-processing-chain>
            
            <!-- Allow devices to create streams and send stream data -->
            <sw:device-stream-processor/>
   
         </sw:inbound-processing-chain>

Hazelcast Queue Processor
-------------------------
An instance of *<sw:hazelcast-queue-processor/>* may be configured in the inbound processing chain
to forward all decoded events into a Hazelcast queue. This allows multiple subordinate SiteWhere 
instances to use the *<sw:hazelcast-queue-event-source/>* to pull the events in and 
process them. The events are handed to the subordinate instances in round-robin fashion 
so the processing load can be distributed. If this processor is configured, normally the
other default processors for storage, registration, and stream processing are removed, since
the processing occurs in the subordinate instances.

.. code-block:: xml
   :emphasize-lines: 6

      <sw:device-communication>
               
         <sw:inbound-processing-chain>
         
            <!-- Note that other processors have been removed -->
            
            <!-- Send all events to a Hazelcast queue -->
            <sw:hazelcast-queue-processor/>
   
         </sw:inbound-processing-chain>

-------------------------
Outbound Processing Chain
-------------------------
In the default provisioning implementation, each time an event is saved via the device management 
service provider interfaces, the outbound event processing chain is invoked. In the same way the 
inbound processing chain acts on unsaved inbound event data, the oubound processing chain acts on 
data that has been successfully persisted to the datastore. Each **outbound event processor** (implementing 
`IOutboundEventProcessor <../apidocs/com/sitewhere/spi/device/event/processor/IOutboundEventProcessor.html>`_)
is executed in series. New outbound event processors can be added to the chain to augment existing
functionality. For instance, SiteWhere has an event processor for sending all outbound events to
Hazelcast subscribers, allowing external clients to act on the events.

**REST calls (or other calls that directly invoke the device management APIs) are processed by the
outbound processing chain in the same manner as events from event sources.**

Command Delivery Event Processor
--------------------------------
By default, an instance of *<sw:command-delivery-event-processor/>* is configured in the outbound chain. This
processor hands off device command invocations to the communication subsystem for processing. If this 
processor is removed, device command invocations will be persisted, but will never be processed. The
default configuration is shown below:

.. code-block:: xml
   :emphasize-lines: 6

   <sw:device-communication>
					
      <sw:outbound-processing-chain>
      
         <!-- Routes commands for outbound processing -->
         <sw:command-delivery-event-processor/>
				
         <!-- Send outbound device events over Hazelcast -->
         <sw:outbound-event-processor ref="hazelcastDeviceEventProcessor"/>
	
      </sw:outbound-processing-chain>

This example also shows the addition of a custom outbound event processor which references a Spring bean
defined elsewhere in the configuration. Events will be passed to the custom processor after they have
been processed by the provisioning processor.

Zone Test Event Processor
-------------------------
The *<sw:zone-test-event-processor/>* outbound event processor is used to test location events against
a list of predefined zones to verify if they fall within the zone boundaries. Each location event is
tested against the conditions defined in the list of *<sw:zone-test/>* elements. The zone tests
specify the unique token of the zone to test against (defined via the admin interface or REST services)
and the test condition (inside or outside the zone). If the condition is met, a new alert event is 
created based on the alert attributes in the test. The alert event can be processed like any other
alert entering the system, allowing other outbound processing components to handle reaction to the
zone condition.

.. code-block:: xml
   :emphasize-lines: 9-12
 
   <sw:device-communication>
   
      <sw:outbound-processing-chain>
      
         <!-- Routes commands for outbound processing -->
         <sw:command-delivery-event-processor/>
         
         <!-- Performs zone checking for locations -->
         <sw:zone-test-event-processor>
            <sw:zone-test zoneToken="777fa4e5-bc2f-458b-9968-b598b2e2d2ca" condition="outside"
               alertLevel="error" alertType="off.site" alertMessage="Asset has left the worksite."/>
         </sw:zone-test-event-processor>

In the example above, each location will be checked against the zone defined by the given zone token.
If the location is outside the given zone (in this case the worksite where an asset is deployed), an
alert is fired. The alert is an error of type 'off.site' an includes an alert message. If an asset 
goes offsite, the alert event can be used for reactions such as firing an SMS message or sending 
an audible alarm to a device on the worksite.
 
The following attributes may be specified for the *<sw:zone-test>* element.
      
+----------------------+----------+--------------------------------------------------+
| Attribute            | Required | Description                                      |
+======================+==========+==================================================+
| zoneToken            | required | Unique token for zone to test.                   |
+----------------------+----------+--------------------------------------------------+
| condition            | required | Condition for test.                              |
|                      |          | Either *inside* or *outside*.                    |
+----------------------+----------+--------------------------------------------------+
| alertType            | required | Alert type for generated alert.                  |
+----------------------+----------+--------------------------------------------------+
| alertLevel           | optional | Alert level for generated alert.                 |
|                      |          | Defaults to *error*.                             |
+----------------------+----------+--------------------------------------------------+
| alertMessage         | required | Alert message for generated alert.               |
+----------------------+----------+--------------------------------------------------+

Sending Events to an Azure Event Hub
------------------------------------
The *<sw:azure-eventhub-event-processor/>* outbound event processor connects to an 
`Azure Event Hub <http://azure.microsoft.com/en-us/services/event-hubs/>`_ and forwards
device events to it. The current implementation sends all events in JSON format. Future
implementations will allow for filtering which events are sent and choosing the wire 
format of the event data. An Azure Event Hub outbound event
processor can be figured as shown below:

.. code-block:: xml
   :emphasize-lines: 3-4
   
   <sw:outbound-processing-chain>
      
      <sw:azure-eventhub-event-processor sasKey="{azure.sas.key}"
         sasName="default" serviceBusName="sitewhere.servicebus.windows.net" eventHubName="sitewhere"/>

   </sw:outbound-processing-chain>

Note that a SAS name and key are required in order to connect to the Event Hub. See
`this <https://msdn.microsoft.com/en-us/library/azure/dn170477.aspx>`_ article to find
more information about Shared Access Signatures.

The following attributes may be specified for the *<sw:azure-eventhub-event-processor>* element.
      
+----------------------+----------+--------------------------------------------------+
| Attribute            | Required | Description                                      |
+======================+==========+==================================================+
| serviceBusName       | required | Name of the service bus where the event hub      |
|                      |          | is configured.                                   |
+----------------------+----------+--------------------------------------------------+
| eventHubName         | required | Name of the event hub to connect to.             |
+----------------------+----------+--------------------------------------------------+
| sasName              | required | Name of SAS entity to connect with.              |
+----------------------+----------+--------------------------------------------------+
| sasKey               | required | Key for SAS entity to connect with.              |
+----------------------+----------+--------------------------------------------------+

Broadcasting Events via Hazelcast
---------------------------------
SiteWhere has support for broadcasting events over `Hazelcast <http://hazelcast.com/>`_ topics, making it
easy to share events with external agents. To enable Hazelcast broadcasting, first add the configuration
information to the *<sw:globals>* section as shown below:

.. code-block:: xml
   :emphasize-lines: 4
   
   <sw:configuration>

      <sw:globals>
         <sw:hazelcast-configuration configFileLocation="${catalina.home}/conf/sitewhere/hazelcast.xml"/>
      </sw:globals>

Note that the *configFileLocation* attribute specifies the full path to a Hazelcast configuration file.
The configuration above is the default which assumes SiteWhere is running inside a Tomcat container.
Once the configuration has been declared, it may be referenced as part of the outbound processing chain to
enable broadcasting of events.

.. code-block:: xml
   :emphasize-lines: 7
   
   <sw:outbound-processing-chain>
      
      <!-- Routes commands for outbound processing -->
      <sw:command-delivery-event-processor/>

      <!-- Send outbound device events over Hazelcast -->
      <sw:hazelcast-event-processor/>

   </sw:outbound-processing-chain>

To consume events from the Hazelcast topics, listen on the topic names as defined in 
`ISiteWhereHazelcast <../apidocs/com/sitewhere/spi/server/hazelcast/ISiteWhereHazelcast.html>`_.

Sending Events to Apache Solr
-----------------------------
SiteWhere supports forwarding events to `Apache Solr <http://lucene.apache.org/solr/>`_ to leverage
the sophisticated search and analytics features it provides. The Solr outbound event processor uses
the `Solrj <https://cwiki.apache.org/confluence/display/solr/Using+SolrJ>`_ library to send each
outbound event to a Solr instance. The events are stored using a custom SiteWhere document schema,
allowing event data to be indexed based on its type. For instance, location events are stored with
geospatial indexes to allow efficient location searches. To enable the Solr event processor, first add the configuration
information to the *<sw:globals>* section as shown below:

.. code-block:: xml
   :emphasize-lines: 5
   
   <sw:configuration>

      <sw:globals>
         <sw:hazelcast-configuration configFileLocation="${catalina.home}/conf/sitewhere/hazelcast.xml"/>
         <sw:solr-configuration solrServerUrl="http://localhost:8983/solr/SiteWhere"/>
      </sw:globals>

The **solrServerUrl** attribute needs to point to the Solr core being used for SiteWhere data. To
add the outbound event processor to the chain, reference it as shown below:

.. code-block:: xml
   :emphasize-lines: 7
   
   <sw:outbound-processing-chain>
		
      <!-- Routes commands for outbound processing -->
      <sw:command-delivery-event-processor/>
			
      <!-- Index events in Solr -->
      <sw:solr-event-processor/>

   </sw:outbound-processing-chain>

Note that on system startup, the event processor attempts to ping the Solr server to verify the 
settings are correct. If the ping fails, server startup will fail.

Sending Events to InitialState.com
----------------------------------
SiteWhere events can be forwarded to `IniitialState.com <https://www.initialstate.com/>`_ to
allow them to be visualized using the advanced dashboarding features offered by the platform.
To enable event forwarding, add the *<sw:initial-state-event-processor/>* element and
specify the streaming access key made available when you create an InitialState account.
Separate data streams are created for each device assignment based on the unique token
for the assignment. An example configuration is shown below:

.. code-block:: xml
   :emphasize-lines: 7
   
   <sw:outbound-processing-chain>
      
      <!-- Routes commands for outbound processing -->
      <sw:command-delivery-event-processor/>
         
      <!-- Sends events to InitialState.com -->
      <sw:initial-state-event-processor streamingAccessKey="your_access_key"/>

   </sw:outbound-processing-chain>


Using Siddhi for Complex Event Processing (CEP)
-----------------------------------------------
SiteWhere supports integration with `Siddhi <https://github.com/wso2/siddhi>`_ for complex
event processing. Adding a *<sw:siddhi-event-processor/>* to the outbound processing chain
routes all SiteWhere events into Siddhi event streams for processing. The Spring XML configuration
allows multiple queries to be registered with Siddhi while allowing callbacks to be registered
so that the resulting streams can be processed. An example configuration is shown below:

.. code-block:: xml
   :emphasize-lines: 7-24
   
   <sw:outbound-processing-chain>
      
      <!-- Routes commands for outbound processing -->
      <sw:command-delivery-event-processor/>
         
      <!-- Processes event streams using Siddhi for complex event processing -->
      <sw:siddhi-event-processor>
         
         <sw:siddhi-query
            selector="from e1 = MeasurementStream[mxname == 'engine.temp'], e2 = MeasurementStream[mxname == 'engine.temp' and e1.assignment == assignment and ((e2.mxvalue - e1.mxvalue) > 5)] select e1.assignment insert into EngineTempRose">
            <sw:stream-debugger stream="EngineTempRose"/>
         </sw:siddhi-query>
            
         <sw:siddhi-query
            selector="from e1 = LocationStream, e2 = LocationStream[(latitude != e1.latitude or longitude != e1.longitude) and e1.assignment == assignment] select e2.assignment, e2.latitude, e2.longitude insert into LocationChanged">
            <sw:stream-debugger stream="LocationChanged"/>
         </sw:siddhi-query>
            
         <sw:siddhi-query
            selector="from every e1 = AlertStream[type == 'low.bp'] -> e2 = AlertStream[type == 'g.shock' and e1.assignment == assignment] within 7 sec select e1.assignment insert into Fainted">
            <sw:groovy-stream-processor scriptPath="siddhiEventProcessor.groovy" stream="Fainted"/>
         </sw:siddhi-query>

      </sw:siddhi-event-processor>

   </sw:outbound-processing-chain>

SiteWhere currently registers three event streams with Siddhi, **MeasurementStream** for individual measurements,
**AlertStream** for alerts, and **LocationStream** for locations. The events injected into the streams contain
all of the same information provided by the core SiteWhere event APIs.

Any number of queries may be registered with Siddhi by adding *<sw:siddhi-query/>* elements within the processor.
Each query specifies a selector which indicates the logic to be performed on the event streams (for more information
on the query language see `the documentation <https://docs.wso2.com/display/CEP310/Queries>`_). To process the
stream results, any number of callbacks may be registered. The *<sw:stream-debugger/>* callback will print
all events for a given stream to the log. The *<sw:groovy-stream-processor/>* may be used to process stream events
with a Groovy script. 

----------------
Asset Management
----------------
SiteWhere includes an asset management subsystem that provides a standardized way to reference assets from
many different sources. SiteWhere assets reference items in the real world including people (person assets),
places (location assets) and things (hardware assets). There is also a class of assets called device assets
that are hardware assets which can be used in device specifications. 

Assets are used to specify information about device specifications such as the name, photo, and properties 
that make the asset unique. They are also used in device assignments to indicate a physical object that is
associated with a device such as a person associated with a badge or the car associated with a tracking device.

Starting with SiteWhere 1.1.0, all assets are stored in the datastore by default. Previous versions required
them to be stored in XML files or external asset management systems. In the administrative console, new 
asset categories and assets can be added. When the system starts, each of the asset categories is loaded
as an asset module. Other assets can still be added via the filesystem or external sources as detailed below.

Filesystem Asset Modules
------------------------
Assets can be loaded from the filesystem using files containing XML data. For an example of the data
format take a look at the files in the **conf/sitewhere/assets/** directory. The available modules 
include *<sw:filesystem-device-asset-module/>*, *<sw:filesystem-hardware-asset-module/>*,
*<sw:filesystem-person-asset-module/>*, and *<sw:filesystem-location-asset-module/>*. Each module
must have a unique id. Filesystem asset modules load all assets at server startup, but can be reloaded
by calling the **refresh** method in the asset REST services.

.. code-block:: xml
   
   <sw:asset-management>

      <sw:filesystem-device-asset-module filename="my-devices.xml"
         moduleId="my-devices" moduleName="My Devices"/>

   </sw:asset-management>
   
The following attributes may be specified for the filesystem asset module elements.
      
+----------------------+----------+--------------------------------------------------+
| Attribute            | Required | Description                                      |
+======================+==========+==================================================+
| filename             | optional | Name of XML file relative to the assets          |
|                      |          | configuration directory.                         |
+----------------------+----------+--------------------------------------------------+
| moduleId             | optional | Unique module id.                                |
+----------------------+----------+--------------------------------------------------+
| moduleName           | optional | Name shown in use interface for module.          |
+----------------------+----------+--------------------------------------------------+

WSO2 Identity Server Asset Module
---------------------------------
SiteWhere can load and reference assets from `WSO2 Identity Server <http://wso2.com/products/identity-server/>`_
allowing asset data to be stored externally. WSO2 Identity Server allows information about people to be
stored in many formats including LDAP and many databases. It also allows data to be retrieved in many
common formats. SiteWhere uses SCIM to load the list of users from the server. The current implementation
loads all users at startup, so when adding users, the **refresh** method should be called in the asset REST
services to pick up the changes. An example WSO2 asset module configuration is shown below:

.. code-block:: xml
   
   <sw:wso2-identity-asset-module moduleId="wso2"
      scimUsersUrl="https://wso2is:9443/wso2/scim/Users" username="admin" password="admin"
      ignoreBadCertificate="true"/>

The following attributes may be specified for the *<sw:wso2-identity-asset-module>* element.
      
+----------------------+----------+--------------------------------------------------+
| Attribute            | Required | Description                                      |
+======================+==========+==================================================+
| scimUsersUrl         | required | URL for accessing SCIM users list.               |
+----------------------+----------+--------------------------------------------------+
| username             | required | Admin username for server authentication.        |
+----------------------+----------+--------------------------------------------------+
| password             | required | Admin password for server authentication.        |
+----------------------+----------+--------------------------------------------------+
| ignoreBadCertificate | required | Allows connection via SSL to the server even if  |
|                      |          | the certificate is not valid. Only use this in   |
|                      |          | development envionments.                         |
+----------------------+----------+--------------------------------------------------+

-------------------
Configuring Logging
-------------------
SiteWhere uses `Apache Log4j <http://logging.apache.org/log4j/1.2/>`_ for logging information about the running system.
The logging output is configured by the **log4j.xml** file which is found in the lib folder of the default server
distributions. For users running SiteWhere on their own application server instance, the default logging configuration
file can be found on `GitHub <https://github.com/sitewhere/sitewhere/blob/master/sitewhere-core/config/log4j.xml>`_.
The file must be available on the server classpath in order to be used.

The default logging configuration file logs to the console output and also creates a separate log file named
**sitewhere.log** which contains the same content.

Enabling Server Debug Output
----------------------------
By default, most debugging output is not logged for SiteWhere. To turn debugging on for all aspects of the server,
scroll down to the following block:

.. code-block:: xml
   
   <category name="com.sitewhere">
      <priority value="INFO" />
   </category>

Change the **INFO** value to **DEBUG** and restart the server. All debug information will be now be available. This is
discouraged in production environments because logging takes system resources and will degrade performance.

Debugging Device Communication
------------------------------
Debugging can also be enabled just for certain areas of the system. A common area where users require detailed
debugging information is in the device communication subsystem. It is often helpful to see exactly what SiteWhere is
doing to handle inbound and outbound data. To turn on communication debugging, scroll down to the following block in
the **log4j.xml** file:

.. code-block:: xml
   
   <category name="com.sitewhere.device.communication">
      <priority value="INFO" />
   </category>

Update the **INFO** value to **DEBUG** and restart the server to see more detailed communication information.
