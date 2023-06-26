# Jlox-plus
Jlox-plus is an extendsion of the Lox language created by **Robert Nystrom**. It was built by following the first two parts of _Crafting Interpreters_ and then adding my own spin onto the language.
#Building
Building is a little finnicky due to the JRE, but i'll do my best to explain it

Compiling to .class
---
```
javac -d build com/nix/lox/*.java
```
This will turn each .java file int .class file and place it into the build folder

Building to jar
---
```
jar -cvf <name-of-jar-to-make>.jar Manifest.txt -C build .
```
This will make a jar file that you can use for the next command

Running the jar
---
```
java -cp <path-to-jar> com.nix.lox.Lox
```
