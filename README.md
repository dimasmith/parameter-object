Parameter Object Generator
==========================

[![Build Status](https://travis-ci.org/dimasmith/parameter-object.svg?branch=develop)](https://travis-ci.org/dimasmith/parameter-object)

---
Annotation and annotation processor to extract parameter objects from methods.

## Example

Let's imagine we have a service class and we want to do something with methods of this service. 
For example write an audit trail.
The `@Audit` annotation tells auditing system to use certain processor class to create audit message.

```java
class UserService {
    
    @Audit(processor = CreateUserAuditor.class)
    User createUser(String firstName, String lastName, List<Order> orders) {
        // do some work
    }
}
```

Audit processors have an interface like this

```java
interface AuditProcessor {
    
    AuditMessage beforeMethod(Audit auditAnnotation, Map<String, Object> parameters);
}
```

Method parameters passed to audit processors as a map of named objects.
This solution is brittle - once we rename parameter in an audited method an error will occur somewhere in runtime.
Besides we need to explicitly cast types when processing parameters. E.g.

```java
class CreateUserAuditor implements AuditProcessor {
    AuditMessage beforeMethod(Audit auditAnnotation, Map<String, Object> parameters) {
        List<Order> orders = (List<Order>) parameters.get("orders");
        return AuditMessage.builder()
            .message("Creating user")
            .addAttribute("orders", orders.size())
            .build();
    }
}
```

Imagine we cannot change the signature of `AuditProcessor` and get rid of parameters map or pass annotated method down.
One possibility to mitigate it is to create a Parameter Object from all method parameters.
Add factory method to create it from the map of named objects and use wrapped map.

```java
class CreateUserParameters {
    private final String firstName;
    private final String lastName;
    private final List<Order> orders;
    
    CreateUserParameters(Map<String, Object> parameters) {
        firstName = (String) parameters.get("firstName");
        lastName = (String) parameters.get("lastName");
        orders = (Collections).unmodifiableList((List<Order>)parameters.get("orders"));
    }    
    
    // getters
}
```  

Using this instead of operating on map will make code in audit processor cleaner
 
 ```java
class CreateUserAuditor implements AuditProcessor {
   AuditMessage beforeMethod(Audit auditAnnotation, Map<String, Object> parameters) {
       CreateUserParameters args = new CreateUserParameters(parameters);
       return AuditMessage.builder()
           .message("Creating user")
           .addAttribute("orders", args.getOrders().size())
           .addAttribute("name", args.getLastName() + " " + args.getFirstName())
           .build();
   }
}
 ```
 
 But the issue with renaming parameters is still there. 
 It may go out of sync in parameter object as well.
 But what if instead writing such parameter object we can generate it on compilation?
 No writing boilerplate code is necessary.
 Also reading from map does not go out of sync neither in names, nor in types.
 
 Parameter object adds an annotation `@ParameterObject` and compiler processor to generate classes with parameters.
 
 What we'll have is:
 
 ```java
 class UserService {
     
     @Audit(processor = CreateUserAuditor.class)
     @ParameterObject // processor generates parameter object from signature
     User createUser(String firstName, String lastName, List<Order> orders) {
         // do some work
     }
 }
 ```
 
 Parameter objects will compile before the rest of the code, so it is possible to use it right away.
 
 ## Project structure
 
 The project contains two subprojects
 
 * `annotation` - containing an `@ParameterObject` annotation;
 * `annotation-processor` - an annotation processor for the compiler;
 * `example` - project with usage examples and tests;
 
 ## How to use it
 
 ### With maven
 
 Add annotation processor as optional dependency to the project dependency list:
 
 ```xml
<dependency>
  <groupId>net.anatolich.parameterobject</groupId>
  <artifactId>annotation-processor</artifactId>
  <version>0.1.0-SNAPSHOT</version>
  <optional>true</optional>
</dependency>
 ``` 
 
 Add an annotation module to the project dependency list:
  ```xml
 <dependency>
   <groupId>net.anatolich.parameterobject</groupId>
   <artifactId>annotation</artifactId>
   <version>0.1.0-SNAPSHOT</version>
 </dependency>
  ``` 

Annotate methods of your classes with `@ParameterObject` annotation.

## Features

### Customize parameter object class names

By default the parameter object will be named by the pattern: `<ClassName><MethodName>Parameters`.
It will reside in the same package as the class with an annotated method.

To change name and/or package use:

```java
@ParameterObject(packageName="my.parameters", className="AwesomeParameters")
void awesomeMethod(String message) {}
```

When any of the attributes missing the default strategy is applied to missing component.
