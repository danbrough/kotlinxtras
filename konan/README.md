# Konan dependencies project

In order to build third party libraries using the konan cross compilers they need to be 
downloaded first.

I don't know how to do directly so this trivial project can be used to ensure they are downloaded.
[buildSrc/src/main/BuildEnvironment#konanDepsTask](buildSrc/src/main/BuildEnvironment.kt) 
can be used as a dependency on any task that requires the konan cross compilation tools

