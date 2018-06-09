This is the README file for the source distribution of At4J version ###VERSION###.

The source distribution contains a complete Eclipse workspace for At4J under the
src directory.

To build an At4J distribution from source, use the Schmant build script
At4J_resources/build/build.js. This can be done from within
Eclipse using the Schmant Eclipse plugin, or from the command line.

The header of the build.js script contains information on dependencies and
configurable parameters for the script.

Example:
To build an At4J distribution from a Windows command prompt, run
> set JAVA_HOME=[path to a JDK installation]
> set FINDBUGS_HOME=[path to a Findbugs installation]
> cd [At4J installation directory]\src\AT4J_resources\build
> schmant.bat \
  -p "docbookRoot=[path to Docbook XSL stylesheet distribution]" \
  build.js

Note: Older versions of Sun's JDK 6 contains a compiler with a bug causing it to
sometimes fail on autoboxing in the code when running several compile threads in
parallel. The most straightforward workaround for that bug is to upgrade to a
newer JDK. Another workaround is to use the argument -p "buildThreads=1" when
running the build script to limit the number of build threads to just one. 