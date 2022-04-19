# Silverchain ObjectPascal: Fluent API generator for Java and ObjectPascal

The original ist Silverchain by Tomoki Nakamaru
Link: https://github.com/tomokinakamaru/silverchain

## What is Silverchain for?

Consider creating a library for writing SQL statements in the following style:

```java
// SELECT name FROM users WHERE id = 1
Result r = new SQL().select("name").from("users").where("id = 1").execute();
```

```pascal
// SELECT name FROM users WHERE id = 1
R:=SQL.Create.select('name').from('users').where('id=1').execute();
```

The simplest way to create such a library is to define a class and put all the methods in that class:

```java
class SQL {
  SQL() { ... }
  SQL select(String columns) { ... ; return this; }
  SQL from(String table) { ... ; return this; }
  SQL where(String expression) { ... ; return this; }
  Result execute() { ... }
}

Result r = new SQL()
  .select("name")   // Returns `SQL`
  .from("users")    // Returns `SQL`
  .where("id = 1")  // Returns `SQL`
  .execute();       // Returns `Result`
```

This simple implementation certainly lets the users write SQL statements as expected. However, it also allows its users to write invalid SQL statements, for example:

```java
new SQL().select("name").where("id = 1").execute(); // Missing `from(...)`
```

```pascal
R:=SQL.Create.select('name').where('id=1').execute(); // Missing `from(...)`
```

Can we prevent the users from writing such an invalid statement? Yes! If the return type of each method is chosen appropriately based on what the users can invoke next, an invalid chaining of method invocations causes a compile error. In our case, an invalid SQL statement comes to cause an error by defining classes/methods as follows:

```java
class SQL {
  SQL() { ... }
  SQL1 select(String columns) { ... }
}
class SQL1 {
  SQL2 from(String table) { ... }
}
class SQL2 {
  SQL3 where(String expression) { ... }
  Result execute() { ... }
}
class SQL3 {
  Result execute() { ... }
}

// Invalid statement causes compile error
new SQL()
  .select("name")  // Returns `SQL1`
  .where("id = 1") // `SQL1` does not have `where(...)` → Type error!

// Valid statement causes no error
Result r = new SQL()
  .select("name")   // Returns `SQL1`
  .from("users")    // Returns `SQL2`
  .where("id = 1")  // Returns `SQL3`
  .execute();       // Returns `Result`
```

Now, the library users must be happy because they will never accidentally write invalid SQL statements. *This implementation is user-friendly, but how about from the developers' viewpoint?* It must be tedious to define many classes and carefully put methods in each class. Imagine that you create a library that also supports insert/update/delete statements. The development of such a library is too tedious and you would give up the user-friendly implementation.

**Silverchain is a tool that significantly reduces the cost of the user-friendly implementation!** Silverchain generates class/method definitions from the code that defines valid chains. For example, it generates the four classes (`SQL`, `SQL1`, `SQL2`, and `SQL3`) from the following chain definition:

```
SQL {
  Result select(String columns) from(String table) where(String expression)? execute();
}
```

To learn how to write the input, see this [reference](./doc/ag-reference.md).

## Not only preventing invalid chains!

A Silverchain-generated library (i.e. library implemented in the user-friendly way) cooperates well with method completion system and lets the library users write code faster.

![completion](https://github.com/tomokinakamaru/silverchain/raw/main/doc/completion.gif)

When a library is implemented in the user-friendly way, the completion system shows only methods that library users can chain next (see the left of the GIF animation). On the other hand, the completion system shows all the methods including the ones that cannot be chained when a library is implemented in the simplest way (see the right of the animation).

## Build jar

```sh
./gradlew shadowJar # Creates ./build/libs/silverchain-<version>-all.jar
                    # Run it with `java -jar ...`
```
```cmd
gradlew.bat shadowJar
```
## Command line options

```
  -h, --help                Show this message and exit
  -v, --version             Show version and exit
  -i, --input <path>        Input grammar file
  -o, --output <path>       Output directory
  -j, --javadoc <path>      Javadoc source directory
  -m, --max-file-count <n>  Max number of generated files
  -p  --objectpascal        Generate ObjectPascal output
```

[This page](./doc/javadoc.md) describes the use of `--javadoc`.

## Command line example

```sh
java -jar silverchain-0.3.0-SNAPSHOT-all.jar -p -i src/test/resources/alertdialog.ag -o alertdialog
```
```cmd
java.exe -jar silverchain-0.3.0-SNAPSHOT-all.jar -p -i src/test/resources/alertdialog.ag -o alertdialog
```

