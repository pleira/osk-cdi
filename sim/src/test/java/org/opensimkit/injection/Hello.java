package org.opensimkit.injection;


import javax.inject.Inject;

public class Hello {

    @Inject
    World world;

    public String sayHelloWorld() {
        return "Hello " + world.sayWorld();
    }
}