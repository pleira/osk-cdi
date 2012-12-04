osk-cdi
=======

Fork of OpenSimKit to try Java CDI. It deals just with the numerical simulation.

OpenSimKit [OpenSimKit.org] is a simulation kit written in Java. The user has to create numerical models of a system and the models can be linked with the kit. The simulation is run, having the user the possibility to see telemetry and send telecommands. The example in OSK is a simulation of a rocket stage. 

I changed the build system to use maven. The user should download the original OSK software and add some of their dependencies (like vecmath and jat) to his local maven repository with a maven install command.

	mvn install:install-file -Dfile=vecmath.jar  -DgroupId=jat.vecmath -DartifactId=vecmath -Dversion=1.0 -Dpackaging=jar
	mvn install:install-file -Dfile=osk-j-jat-minimal.jar  -DgroupId=jat.osk -DartifactId=jat-osk -Dversion=1.0 -Dpackaging=jar 

The root maven pom is in the parent directory. Configuration files for eclipse or netbeans can be generated from maven command line.

	mvn eclipse:eclipse

The program can be executed from command line with 

	cd rpr
	mvn -Drun exec:java

I am checking how CDI (Context Dependency and Injection) works. The simulation gets initialised via CDI using Apache Delta Spike. CDI events are used to integrate the different objects that simulte the Astris Rocket. It is planned to try interceptors for logging. 

The StartMain from the CDI implementation Weld is used as Main. It delivers an event which is used to start the actual application.

