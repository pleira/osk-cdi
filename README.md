osk-cdi
=======

Fork of OpenSimKit to try Java CDI. 

OpenSimKit [OpenSimKit.org] is a simulation kit written in Java. The developer has to create numerical models of a system and the models can be linked with the kit. The simulation is run, having the user the possibility to see telemetry and send telecommands. The example in the framework is a simulation of a rocket stage. 

I am checking how CDI (Context Dependency and Injection) works. I have taken some of the original OSK code and played with CDI initialisation. The MMI part has not been included.

I changed the build system to use maven. The user should download the original OSK software and add some of their dependencies (like vecmath and jat) to his local maven repository with a maven install command. Configuration files for eclipse or netbeans can be generated from maven command line.

The program can be executed from command line with 

cd sim
mvn -Drun exec:java

The StartMain from the CDI implementation Weld is used as Main. It delivers an event which is used to start the actual application.
   
