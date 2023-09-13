# Jlox-plus
Jlox-plus is an superset of the Lox language created by **Robert Nystrom**. It was built by following the first two parts of _Crafting Interpreters_ and then adding my own spin onto the language.

# Building

Building is a little finnicky due to the JRE, but i'll do my best to explain it

Compiling to .object
---
```powershell
javac -d build com/nix/lox/*.java
```
This will turn each .java file int .object file and place it into the build folder

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
| equals | x = y | assigns `y` to `x` |
| equals check      | x == y | returns `true` if `x` equals `y` |
| not equals check      | x != y | returns `true` if `x` does not equal `y` |
| not      | !x | returns the opposite of `x` |
| get      | x.y | finds a property `y` on an object `x` |

| Jlox+         | code |  function (where x = the left hand side, y = the right hand side)|
|--------------|:-----:|-----------:|
| scope resolution      | x::y | finds a shared property `y` on an object `x` |
| inheritance      | x <- y | inherits `x` from `y` |
| interface implmentation      | x -> y | object `x` extends interface `y` |
| method extension      | x:y | function `y` is extension method for object `x` |
| null-safe get      | x?.y | finds a property on an object, if the object is `nil`, it returns nil without trying to find the property |
| null-safe assign      | x ?= y | assigns `y` to x only if `x` != `nil` and `y` != `nil`, otherwise just return nil |
| null-safe check      | x ?? y | returns `x` unless its `nil` otherwise it returns `y`|

| Primitive Types         | example |  meaning|
|--------------|:-----:|-----------:|
| num | num x = 10; | a number variable |
| string | string x = "Hello world!"; | a string variable |
| bool | bool x = true; | a boolean variable |
| void | void x = nil; | assigns a  `nil` variable, mostly only useful for function return types |

| Object Types (obj)        | example |  meaning|
|--------------|:-----:|-----------:|
| obj [Type] | obj Test testObj = new Test(); | an object variable of type [Type] variable |
| obj func | obj func ptr = myFunc; | a pointer to a function |

variable
---
variables in lox must match a built in or object type, and can be fixed and shared

```js
object Test {}

func testFunc() -> void {}

num x = 10;
string y = "hello";
bool z = true;
obj Test testObj = new Test();
obj func funcPointer = testFunc;
```
Assigning mutables works as expected, with added support for postfix increments and decrements
```js
num x = 10;
x += 5;
x -= 5;
x *= 5;
x /= 5;
x++;
x--;
```
jlox+ also supports the power operator
```js
num x = 5;
x ** 2;
//Result: 25
```
fixeds work the same, except that they can't be reassigned
```js
fixed num x = 5;
x = 6;
//Will throw error
```
Control Flow
---
Control flow in lox is similar to languages like C or Java
```js
num x = 5;
if(x == 5){
  System::println(x);
}
else{
  System::println("not five");
}
```
jlox+ also supports while and for loops.
```js
num x = 5;
while(x != 10){
 System::println("not ten");
}
```
```js
for(num i = 0; i < 10; i++){
  System::println("I: " + i);
}
```
functions
---
functions are defined with the `func` keyword and can have local fixeds and variables, the return type is specified with the `interface implementation` operator
```kotlin
func helloWorld(string message) -> void{
  System::println("Hello, " + message);
}
```
functions can be marked fixed, which makes them unable to be reassigned
```kotlin
fixed func helloWorld(string message) -> null{
  System::println("Hello, " + message);
}
```
Objects
---
jlox+ is an object oriented language that supports objects and single-inheritance.
fixedructers are defined by making an `constructor()` method.
instances are created by usin the `new` or `spawn` keywords and passing the parameters for its `constructor()` method.
```js
object Program{
  num x = 0;
  fixed num y = 0;
  
  method constructor() -> void{
    //do something
  }
}

object App <- Program{
  method constructor() -> void{
    super.contructor();
    this.x = 10;
  }

  method sayHi() -> void {
    System::println("hi");
  }
}

obj Program y = new Program();
obj App x = spawn App();
```
mutables in objects must be initialized immediatly. Fields can be added on the fly with the `.` operator 
# Jlox+ features
mutables
---
fixed objects, postfix increments and decrements. mutables declared in a object, can be fixed and shared.

Calling `shared` methods and mutables
---
To call a shared method or mutable, use the `scope resolution operator`
```js
object TestObj {
  shared num x = 10;
  shared method testMethod () -> void {}
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
num x = 3;
when(x > 5){
  //runs until x > 5
  x++;
} do{
  //executed once the when condition is false
  System::println("x > 5!");
}
```
Switch statements are in jlox+, with default case supported
```c#
num x = 10;
switch (x) {
  case (5) {
    System::println("x is 5");
  }

  case (10) {
    System::println("x is 10");
  }

  case (15) {
    System::println("x is 15");
  }

  default {
    System::println("x is something else");
  }
}
```

Interfaces
---
In jlox+, interfaces are built with the `interface` keyword. Any fields or methods defined in an interface must be fully implemented in any extending class including modifiers and parameters. Objects can extend interfaces with the `->` operator. This works along with inheritance, i.e `object ObjTwo <- ObjOne -> MyInterface`
```c#
interface ITest {
  num x;
  num y;

  method imethod(num x) ;
  method method2(num y, num z);
}
```
Interface functions can be defined with a body, to be used for default implementation. If you end a function with a semicolon, either in a class or an interface, it marks it as abstract. If a method is abstract in a class, it looks in its interfaces to see if there is a matching non-abstract function to fufill its default implementation.
```c#
interface ITest {
  method imethod(num x); //abstract
  method method2(num y, string z); //abstract
  method def() { System::println("def test"); } //non-abstract
}

object TestObj -> ITest {
  method imethod(num x) { } //non-abstract
  method method2(num y, string z) { } //non-abstract
  method def(); //abstract, looks for non-abstract method in ITest
}

new ITest().def(); //prints 'def test'
```
Objects can implement multiple interfaces
```c#
interface IMyInterface {}
interface IMySecondInterface {}

object MyObj -> IMyInterface, IMySecondInterface {}
```



Enums
---
Jlox+ enums are relatively simple, and are basically just int wrappers
``` c#
enum MyEnum {
  ITEM, //0
  ANOTHERITEM, //1
  AFINALITEM //1
}
```
Enums can be accessed with the `scope resolution` operator
```js
mut x = MyEnum::ITEM; //x equals 0
mut y = MyEnum::AFINALITEM; //y equals 2
```

Functions
---
fixed functions
```js
fixed func test() {}
func two() {}
test = two; //not allowed
```
When defined on a class, functions are made with the `method` keyword
```js
method testMethod {}
```

Extension Methods
---
Extension methods can be created by placing the name of the object you want to extend followed by a extension operator and then the method name and declaration.
```js
object MyObj {}

func MyObj:extMethod() {}
shared func MyObj:sharedExtMethod() {}

new MyObj().extMethod();
MyObj::sharedExtMethod();
```

Objects
---
functions and mutables in objects can be marked `shared` which works like `static` in languages such as java or c#
```js
object Test{
  shared mut x = 10;

  shared method sayHi(){
    System::println("hi");
  }
}

Test::x;
Test::sayHi();
```

Operator Overloading
---
If an object is encountered in an operator, it will look for the overload method for that operator. Operator methods must be defined with the `operator` keyword, and must have two parameters. Calling an operator function on it's own will result in a runtime error.
```c#
object MyObj {
  mut x = 0;

  method constructor(x) {
    this.x = x;
  }

  operator add(x, y) {
    return x.x + y.x;
  }
}

mut x = MyObj(10) + MyObj(20); //looks for a method defined with the `operator` keyword called add. returns 30.
```

Built-in types
---
Jlox+ defines a number of built in types to use

**System**
```js
System::random(0, 100, false); //random number between lower and upper true/false for inclusive or not
System::println(x); //print line
System::print(x); //print without newline
System::cls(); //clear screen
System::errln(x); //print line to error output
System::err(x); //print to error output without newline
System::writeToFile(path, contents, format); //writes contents to path with format 'a = "append", w = "write"'
```

**Math**

the math object contains a few `shared` functions for doing math
```js
Math::round(a);
Math::floor(a);
Math::min(a, b);
Math::max(a, b);
Math::abs(a);
Math::sqrt(a);
//pow function defined by ** operator
```

**Object**

by default in jlox+, all objects derive from `Object`.

*Methods*

- `toString()` which by default prints `Lox.Object$<object-type>`.
```js
method toString(){
  return "Lox.Object$"+type;
}
```
- `fields()` returns a LoxMap containing the fields of said object
```js
mut x = System();
mut fields = x.fields();
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
mut x = List();
x.add("item");
x.get(0);
mut index = x.indexOf("item");
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
mut map = Map();
map.put("item", 0);
map.put("item2", Object());

mut obj = map.get("item2");
mut keyList = map.keys();
mut valueList = map.values();
```

**Color**

simple color object, rgba
```js
//fields
mut r = 0;
mut g = 0;
mut b = 0;
mut a = 0;

Color::red();
Color::green();
Color::blue();
Color::black();
Color::white();

new Color().set(r, g, b);
new Color().setAlpha(a);
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
