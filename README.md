osk-cdi
=======

Fork of the rocket simulation kit OpenSimKit to try Java Context and Dependecy Injection (CDI) technology. This fork deals just with the numerical simulation.

OpenSimKit [OpenSimKit.org] is a simulation kit written in Java. The user has to create numerical models of a system and the models can be linked with the kit. The simulation is run, having the user the possibility to see telemetry and send telecommands. The example in OSK is a simulation of a rocket stage. 

In OSK-CDI, the rocket simulation is modelled using CDI events. The events follow a chain which is embeded in the solver project. The solver project concerns with the connections of the different parts of the rocket engine simulation. The choosen physical models are in a separate project. The choosen models get injected into the solver classes. 

The build system is maven. To install the dependencies, the user should download version 3.7 of the original OSK software and add some of their dependencies (like vecmath and jat) to his local maven repository with a maven install command.

	mvn install:install-file -Dfile=vecmath.jar  -DgroupId=jat.vecmath -DartifactId=vecmath -Dversion=1.0 -Dpackaging=jar
	mvn install:install-file -Dfile=osk-j-jat-minimal.jar  -DgroupId=jat.osk -DartifactId=jat-osk -Dversion=1.0 -Dpackaging=jar 

The root maven pom is in the parent directory. Configuration files for eclipse or netbeans can be generated from maven command line.

	mvn eclipse:eclipse

The program can be executed from command line with 

	cd rpr
	mvn -Drun exec:java

he StartMain from the CDI implementation Weld is used as Main. It delivers an event which is used to start the actual application.
The simulation values get also initialised via CDI. For the configuration of the physical models, Apache Delta Spike is used. 

Design Comments
===============
The design uses Java CDI to support decoupling the application and injecting the right implementations where requested. That way, the physical models have no dependencies on other parts of the simulation, which adds flexiblity for exchanging and testing them. 

Other concerns like logging have been imnplemented in a generic way like as a CDI interceptor. For commanding and monitoring of the simulation, I would use Java JMX. The infrastructure is there but not yet totally implemented (registration of the beans in JMX is missing currently). An interesting idea, once the simulation objects are registered in JMX is to evaluate how to use a generic monitoring software like Nagios with Jolokia for commanding and monitoring. 

For communicating the possition data to external software like Celestia, the modular solution would be a CDI interceptor or observer of position related events.
 
Even if the OSK-CDI simulation has been more modularized with respect the original OSK version 3.7 code, I prefer not to develop it further. One limitation I see is that the way the structure model is built is far too verbose. There must be more elegant alternatives. I also see lack of infrastructure for having paralellism. 

An interesting possibility would be to use [Orekit](http://www.orekit.org) as main kit/framework and consider the rocket simulation as something to be integrated in a similar way as done for the atmospheric force. 

Other approach, the technology offered by [Akka IO / Pipelines](http://doc.akka.io/docs/akka/2.2.0/scala/io-codec.html) seems interesting to me. The connection of different stages is similar to the different steps to do the calculations for the simulation.


