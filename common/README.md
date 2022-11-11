# Common support project

In order to build third party libraries using the konan cross compilers they need to be 
downloaded first.

I don't know how to do directly so this trivial project can be used to ensure they are downloaded.
[plugin/src/main/kotlin/org/danbrough/kotlinxtras/misc.kt#konanDepsTaskName](plugin/src/main/kotlin/org/danbrough/kotlinxtras/misc.kt) 
can be used as a dependency on any task that requires the konan cross compilation tools



