# PoShI

A simple Java application vulnerable to PowerShell code injection under certain conditions (e.g., when executed on Japanese Windows) - see article on our [blog page](https://blog.stmcyber.com/powershell-unicode-quotes-and-command-injection).

## Build & run

Tested with OpenJDK 11 and Windows 10.

Go to PoShI directory and execute the following commands:

```
javac -d ./release PoShI.java StreamHandler.java
cd release
java -cp . -Dfile.encoding=UTF-8 PoShI
```

## Examples

```
curl http://localhost:8000/date
curl http://localhost:8000/date?format=dd
curl http://localhost:8000/date?format=dd.MM.yyyy
```

