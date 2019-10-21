package net.anatolich.parameterobject.example;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.anatolich.parameterobject.ParameterObject;

public class Example {

    @ParameterObject
    public UUID createUser(String username, char[] password, Set<String> roles) {
        return UUID.randomUUID();
    }
}
