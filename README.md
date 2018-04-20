# JSF Web Application Test Case Generator

This is an automated test case generation tool for JSF web applications.

It uses Apache BCEL to parse Java Bytecode files, and an ANTLR parser to parse XML files (e.g. .xhtml web pages).

## Setup

Clone from GitHub repository:

```bash
$ git clone https://github.com/wwu-pi/webapp
$ cd webapp
```

We have an example JSF web application in the [/artifacts/issue-tracker](https://github.com/wwu-pi/webapp/tree/master/artifacts/issue-tracker) directory.

First, navigate to it:

```bash
$ cd artifacts/issue-tracker
```

You can now compile the sources:

```bash
$ mvn clean install
```

Alternatively, you can build a deployable WAR file with:

```bash
$ mvn clean package
```

The tool is grouped in a maven module located at [/artifacts/tap18-webapp-module](https://github.com/wwu-pi/webapp/tree/master/artifacts/tap18-webapp-module).

First, navigate to that maven project:

```bash
$ cd ..
$ cd tap18-webapp-module
```

Then, install the required maven projects into your local repository:

```bash
$ mvn clean install
```

Once successfully installed, you can start the test case generation tool.

First, navigate to its main project located at [/artifacts/webapp](https://github.com/wwu-pi/webapp/tree/master/artifacts/webapp):

```bash
$ cd ..
$ cd webapp
```

The Main class requires some arguments:

```bash
-w location/of/webapp/folder
-s name-of-start-pages
-wcp class/path/of/webapp/files
-lcp additional/class/path/for/javaee/classes
-o output/directory/of/generated/testcases
```

Optionally, you can set the action-sequence length:

```bash
-steps 5
```

For example:

```bash
-w C:\webapp\artifacts\issue-tracker\src\main\webapp
-s 1-view
-wcp C:\webapp\artifacts\issue-tracker\target\classes
-lcp C:\Users\yourusername\.m2\repository\javax\javaee-api\7.0\javaee-api-7.0.jar
-o C:\webapp\issue-tracker-tests
-steps 5
```

Then, start its Main class (you need to pass arguments at the end):

```bash
$ mvn exec:java -Dexec.mainClass="w.Main" -Dexec.args="<ARGUMENTS>"
```

For example:

```bash
$ mvn exec:java -Dexec.mainClass="w.Main" -Dexec.args="-w C:\webapp\artifacts\issue-tracker\src\main\webapp -s 1-view -wcp C:\webapp\artifacts\issue-tracker\target\classes -lcp C:\Users\yourusername\.m2\repository\javax\javaee-api\7.0\javaee-api-7.0.jar -o C:\webapp\issue-tracker-tests -steps 5"
```

Thank you!
