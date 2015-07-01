# DistributedCacheExample
Hazelcast MapStore Example / Playground

I created this small project check out hazelcasts MapStore / NearCache feature. Go ahead, give it a try!

There's a H2 based "database server" and an arbitrary number of nodes that need to access the server. Why did start playing with Hazelcast MapStore? We have an application running on 70-100 nodes and it's doing around 16.000 database requests a second for data that is user related and does only change during batches at night. So it's straight forward to use to whole cluster as a cache (since there is a lot to be cached) and also activate near cache because our users are sticky ...

Based on Hazelcast 3.5

How to become up and running:
* build a shaded jar with 'mvn clean package' (that's easier to transport to different machines)
* start the database server with 'java -cp target/DistributedCache-1.0-SNAPSHOT.jar winzinger.samples.distributedcache.DummyDatabaseServer'
* now start nodes with 'java -jar target/DistributedCache-1.0-SNAPSHOT.jar' (check App.java and AppConfig.java for commandline options and defaults)
* have fun
