package net.anatolich.parameterobject.example;

import java.util.UUID;
import net.anatolich.parameterobject.ParameterObject;

public class Example {

    @ParameterObject
    public UUID createUser(String username, char[] password) {
        return UUID.randomUUID();
    }
}
