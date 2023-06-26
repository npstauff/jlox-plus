# Jlox-plus
Jlox-plus is an superset of the Lox language created by **Robert Nystrom**. It was built by following the first two parts of _Crafting Interpreters_ and then adding my own spin onto the language.
#Building
Building is a little finnicky due to the JRE, but i'll do my best to explain it

Compiling to .class
---
```shell
javac -d build com/nix/lox/*.java
```
This will turn each .java file int .class file and place it into the build folder

Building to jar
---
```shell
jar -cvf <name-of-jar-to-make>.jar Manifest.txt -C build .
```
This will make a jar file that you can use for the next command

Running the jar
---
```shell
java -cp <path-to-jar> com.nix.lox.Lox
```
# Lox syntax and examples
Variables
---
Variables in lox are dynamic, and can be assigned as either constant or variable

```c#
var x = 5;
const x = 5;
```
Assigning variables works as expected, with added support for postfix increments and decrements
```c#
var x = 10;
x += 5;
x -= 5;
x *= 5;
x /= 5;
x++;
x--;
```
jlox+ also supports the power operator
```c#
var x = 5;
x ** 2;
//Result: 25
```
Constants work the same, except that they can't be reassigned
```c#
const x = 5;
x = 6;
//Will throw error
```
Control Flow
---
Control flow in lox is similar to languages like C or Java
```c#
var x = 5;
if(x == 5){
  println(x);
}
else{
  println("not five");
}
```
jlox+ also supports while and for loops.
```c#
var x = 5;
while(x != 10){
  println("not ten");
}
```
```c#
for(var i = 0; i < 10; i++){
  println("I: " + i);
}
```
Functions
---
Functions are defined with the `fun` keyword and can have local constants and variables
```kotlin
fun helloWorld(message){
  println("Hello, " + message);
}
```
Functions can be marked constant, which makes them unable to be reassigned
```kotlin
const fun helloWorld(message){
  println("Hello, " + message);
}
```
Classes
---
jlox+ is an object oriented language that supports classes and single-inheritance.
constructers are defined by making an `init()` method.
instances are created by calling the class like a function and passing the parameters for its `init()` method.
```java
class Program{
  var x = 0;
  const y = 0;
  
  init(){
    //do something
  }
}

class App < Program{
  init(){
    super.init();
    this.x = 10;
    super.x = 15;
  }
}

var y = Program();
var x = App();
```
Variables in classes must be initialized immediatly. Fields can be added on the fly with the `.` operator 
# Jlox+ features
Variables
---
Constants, postfix increments and decrements. Variables declared in a class.

Functions
---
Constant functions
```kotlin
const fun test() {}
fun two() {}
test = two; //not allowed
```

Classes
---
functions in classes can be marked `common` which functions like `static` in languages such as java or C
```java
class Test{
  common sayHi(){
    println("hi");
  }
}

Test.sayHi();
```

Built-in types
---
Jlox+ defines a number of built in types to use

**System**

for now, the system class only defines a `common` method `random(lower, upper)` but more is planned for the future
```java
System.random(0, 100);
```

**Math**

the math class contains a few `common` functions for doing math
```java
Math.round(a);
Math.floor(a);
Math.min(a, b);
Math.max(a, b);
Math.abs(a);
Math.sqrt(a);
//pow function defined by ** operator
```

**Object**

by default in jlox+, all classes derive from `Object`.

*Methods*

- `toString()` which by default prints `Lox.Object$<object-type>`.
```java
toString(){
  return "Lox.Object$"+type;
}
```
- `typeof(other)` returns true if the objects are the same type
```java
var x = Object();
var y = System();
x.typeof(y); //false
x.typeof(Object); //true
```
- `fields()` returns a LoxMap containing the fields of said object
```java
var x = System();
var fields = x.fields();
//loop through map
```

**List**

Lists in lox are ambiguous, meaning they dont have a specific type. You can add or remove any object.
Lists contain some basic methods.
```java
//Methods
add(a);
get(i);
indexOf(a);
remove(i);

//Example
var x = List();
x.add("item");
x.get(0);
var index = x.indexOf("item");
x.remove(0);
```

**Map**

Maps in lox are similar to lists, in which they are basically just a list of keys and a list of values
```Java
//Methods
put(key, value);
get(key);
keys();
values();

//Example
var map = Map();
map.put("item", 0);
map.put("item2", Object());

var obj = map.get("item2");
var keyList = map.keys();
var valueList = map.values();
```

**Color**

simple color class, rgba
```java
//fields
var r = 0;
var g = 0;
var b = 0;
var a = 0;

Color.red();
Color.green();
Color.blue();
Color.black();
Color.white();

Color().set(r, g, b);
Color().setAlpha(a);
```

# Native Function
jlox+ has many built-in functions for stuff like reading and writing

**The 'println' and 'print' functions take the place of the Lox 'print' statement**


```java
//Returns the next string typed by the user
readLine();

//Returns the next number entered by the user
readNum();

//Returns the next boolean entered by the user
readBool();

//Returns the text contents of the given file
scanFile(filePath);

//Prints the given expression without a linebreak at the end
print(expression);

//Prints the given expression with a linebreak at the end
println(expression);

//Error logs the given expression with a linebreak at the end
error(expression);
```

# MORE FEATURES TO COME :)
