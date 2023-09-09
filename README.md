# Jlox-plus
Jlox-plus is an superset of the Lox language created by **Robert Nystrom**. It was built by following the first two parts of _Crafting Interpreters_ and then adding my own spin onto the language.

# Building

Building is a little finnicky due to the JRE, but i'll do my best to explain it

Compiling to .class
---
```powershell
javac -d build com/nix/lox/*.java
```
This will turn each .java file int .class file and place it into the build folder

Building to jar
---
```powershell
jar -cvf <name-of-jar-to-make>.jar Manifest.txt -C build .
```
This will make a jar file that you can use for the next command

Running the jar
---
```powershell
java -cp <path-to-jar>.jar com.nix.lox.Lox <file-to-run>.lox
```
# Lox syntax and examples

| Operator         | code |  function (where x = the left hand side, y = the right hand side)|
|--------------|:-----:|-----------:|
| equals | = | assigns `y` to `x` |
| equals check      |  == | returns `true` if `x` equals `y` |
| not equals check      |  != | returns `true` if `x` does not equal `y` |
| not      | ! | returns the opposite of `x` |
| get      | . | finds a property on an object |
| null-safe get      | ?. | finds a property on an object, if the object is `nil`, it returns nil without trying to find the property |
| null-safe assign      | ?= | assigns `y` to x only if `x` != `nil` and `y` != `nil`, otherwise just return nil |
| null-safe check      | ?? | returns `x` unless its `nil` otherwise it returns `y`|

Variables
---
Variables in lox are dynamic, and can be assigned as either fixed, variable, or shared

```js
var x = 5;
fixed x = 5;
```
Assigning variables works as expected, with added support for postfix increments and decrements
```js
var x = 10;
x += 5;
x -= 5;
x *= 5;
x /= 5;
x++;
x--;
```
jlox+ also supports the power operator
```js
var x = 5;
x ** 2;
//Result: 25
```
fixeds work the same, except that they can't be reassigned
```js
fixed x = 5;
x = 6;
//Will throw error
```
Control Flow
---
Control flow in lox is similar to languages like C or Java
```js
var x = 5;
if(x == 5){
  println(x);
}
else{
  println("not five");
}
```
jlox+ also supports while and for loops.
```js
var x = 5;
while(x != 10){
  println("not ten");
}
```
```js
for(var i = 0; i < 10; i++){
  println("I: " + i);
}
```
Functions
---
Functions are defined with the `fun` keyword and can have local fixeds and variables
```kotlin
fun helloWorld(message){
  println("Hello, " + message);
}
```
Functions can be marked fixed, which makes them unable to be reassigned
```kotlin
fixed fun helloWorld(message){
  println("Hello, " + message);
}
```
Classes
---
jlox+ is an object oriented language that supports classes and single-inheritance.
fixedructers are defined by making an `init()` method.
instances are created by calling the class like a function and passing the parameters for its `init()` method.
```js
class Program{
  var x = 0;
  fixed y = 0;
  
  init(){
    //do something
  }
}

class App < Program{
  init(){
    super.init();
    this.x = 10;
  }
}

var y = Program();
var x = App();
```
Variables in classes must be initialized immediatly. Fields can be added on the fly with the `.` operator 
# Jlox+ features
Variables
---
fixed objects, postfix increments and decrements. Variables declared in a class, can be fixed and shared.

Calling `shared` methods/variables
---
To call a shared method or variable, use the `scope resolution operator`
```c++
object TestObj {
  shared mut x = 10;
  shared method testMethod () {}
}

TestObj::x; //10
TestObject::testMethod();

Math::floor(TestObject::x);
System::println(TestObject::x);
```

Control Flow
---
I added a new type of loop, although I dont know how useful it really is. When statements execute their block until the condition evaluates to FALSE, and then they execute their do block.
```c#
when(x > 5){
  //runs until x > 5
  x++;
} do{
  //executed once the when condition is false
  println("x > 5!");
}
```

Functions
---
fixed functions
```kotlin
fixed fun test() {}
fun two() {}
test = two; //not allowed
```

Classes
---
functions in classes can be marked `shared` which works like `static` in languages such as java or C
```js
class Test{
  shared method sayHi(){
    println("hi");
  }
}

Test::sayHi();
```

Built-in types
---
Jlox+ defines a number of built in types to use

**System**

for now, the system class only defines a `shared` method `random(lower, upper)` but more is planned for the future
```java
System.random(0, 100);
```

**Math**

the math class contains a few `shared` functions for doing math
```js
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
```js
toString(){
  return "Lox.Object$"+type;
}
```
- `typeof(other)` returns true if the objects are the same type
```js
var x = Object();
var y = System();
x.typeof(y); //false
x.typeof(Object); //true
```
- `fields()` returns a LoxMap containing the fields of said object
```js
var x = System();
var fields = x.fields();
//loop through map
```

**List**

Lists in lox are ambiguous, meaning they dont have a specific type. You can add or remove any object.
Lists contain some basic methods.
```js
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
```js
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
```js
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

# Native Functions
jlox+ has many built-in functions for stuff like reading and writing

**The 'println' and 'print' functions take the place of the Lox 'print' statement**


```js
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

# TODO
- [ ] Ternary operators
- [ ] String functions
- [ ] operator overloading
- [x] extension methods
- [x] prefix increment/decrement
- [x] break from loops
- [ ] continue in loops
- [x] packages
- [x] multi-file import

**Not all of these will neccessary be implemented they are just things I want to do**
