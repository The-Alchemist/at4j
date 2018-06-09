/**
 * Schmant Javascript script for running all unit tests. This can only be run
 * from the source distribution.
 * + The working directory must be the directory of this script.
 *
 * Optional properties:
 *  projectPattern - Only test projects with names matching this pattern will be
 *                 run. If omitted, all projects will be run.
 *  javaCmd      - Java executable to use for running the tests.
 *  testSuite    - Run this test suite instead of all test suites from
 *                 run_unit_tests.properties
 *  testClass    - Run only this test or test suite class. Requires that
 *                 testSuite is set.
 *  runEmma      - Run Emma coverage analysis.
 */

function getJUnit4TFForSuiteClass(/* String */ testClass, /* String */ testSuite) {
	var tf = new JUnit4TF();

	// Must add this before the other classpath entries
	if (useEmma) {
		// By adding the Emma Jar to the classpath here,
		// it ends up before the classes to analyze.
		tf.addDecorator(
				new EmmaExternalJavaTaskDecorator().
					setRtControl(false).
					setCoverageOutFile(new FutureFile(emmaOutDir, testNo + ".ec")).
					setCoverageMerge(false).
					setVerbosityLevel(EmmaVerbosityLevel.QUIET)).
			addClasspathEntry(emmaInstrumentedClasses);
		
		testNo++;
	}
	
	tf.addClasspathEntries(
			Directories.getAllFilesMatching(
				Directories.getDirectory(root, "lib"), "*.jar")).
		addClasspathEntries(
			Directories.getAllFilesMatching(testDir, "*.jar")).
		addJvmOption("-Xmx512m").
		addJvmOption("-Dat4j.test.testDataPath=" +
			ECFileResolvableUtil.getFileObject(
				Directories.getDirectory(root, new RelativeLocation("src/AT4J_resources/testdata")))).
		addJvmOption("-Dat4j.test.docPath=" +
			ECFileResolvableUtil.getFileObject(
				Directories.getDirectory(root, new RelativeLocation("src/AT4J_doc")))).
		setAssertions(true).
		addTestClass(testClass).
		addReporter(reporter);
		
	if (props.containsKey("javaCmd")) {
		var javaCmd = props.getStringValue("javaCmd");
		info("Using java command " + javaCmd);
		tf.setJavaExecutable(javaCmd);
	}

	return tf;
}

function getJUnit4TF(/* String */ testSuite) {
	return getJUnit4TFForSuiteClass(unitTestSettings.getStringValue(testSuite + ".suiteClass"), testSuite);
}

/*
 * START HERE
 */
 
info(""); 
info("Running At4J unit tests. This may take 30 minutes or more.");
info("Be patient...");
info(""); 

enableTaskPackage("org.junit.junit4");

// Create a file system on the source distribution
root = SchmantFileSystems.getEntityForDirectory(SchmantUtil.getScriptFile().getAbsoluteFile().getParentFile().getParentFile(), true);
testDir = Directories.getDirectory(root, "test");

unitTestSettings = PropertiesUtil.loadFromFile(
	Directories.getFile(root, new RelativeLocation("build/run_unit_tests.properties")));

testSuites = unitTestSettings.getStringValue("testSuites").split("\\p{Space}*,\\p{Space}*");

reporter = new JUnit4TestSuiteReporter();

if (props.containsKey("runEmma")) {
	enableTaskPackage("com.vladium.emma");
	
	tmpDir = TempFileUtil.createTempDirectory(true);
	emmaInstrumentedClasses = Directories.newDirectory(tmpDir, "bin");
	
	// Instrument the HeliDB Jar files.
	new EmmaInstrumentationTF().
		addSource(Directories.getFileMatching(Directories.getDirectory(root, "lib"), "at4j-full-*.jar")).
		addSource(Directories.getFileMatching(Directories.getDirectory(root, "test"), "at4j-junit4-*.jar")).
		setTarget(emmaInstrumentedClasses).
		setMetadataFile(new FutureFile(tmpDir, "coverage.em")).run();
		
	useEmma = true;
	testNo = 0;
	
	emmaOutDir = Directories.newDirectory(tmpDir, "emmaOut");
} else {
	useEmma = false;
}
	

// Don't use a TaskExecutor since tests running in parallel can interfere with 
// each other.

if (props.containsKey("testSuite")) {
	var testSuite = props.getStringValue("testSuite");
	
	if (props.containsKey("testClass")) {
		testClass = props.getStringValue("testClass");
		
		info("Running test class " + testClass + " in suite " + testSuite + " with isolated class loaders");
		getJUnit4TFForSuiteClass(testClass, testSuite).
			addJvmOption("-Dschmant.test.isolatedClassLoaders").run();
		
		info("Running test class " + testClass + " in suite " + testSuite + " with shared class loader");
		getJUnit4TFForSuiteClass(testClass, testSuite).run();
		
	} else {
		info("Running tests in test suite " + testSuite + " with isolated class loaders");
		getJUnit4TF(testSuite).
			addJvmOption("-Dschmant.test.isolatedClassLoaders").run();
		
		info("Running tests in test suite " + testSuite + " with shared class loader");
		getJUnit4TF(testSuite).run();
	}
} else {
	for (var i = 0; i < testSuites.length; i++) {
		testSuite = testSuites[i];
		if (props.containsKey("projectPattern")) {
			projectPattern = props.getStringValue("projectPattern");
			if (!GlobSupport.getPattern(projectPattern).matcher(testSuite).matches()) {
				info("Test suite " + testSuite + " does not match pattern " + projectPattern);
				continue;
			}
		}

		requiredJavaVersion = new JdkVersion(unitTestSettings.getStringValue(testSuite + ".requiredJava"));
		if (props.containsKey("javaCmd") && 
				JdkUtil.getJdkVersionFromJavaExecutable(new File(props.getStringValue("javaCmd"))).isOlderThan(requiredJavaVersion)) {
			info("Skipping test suite " + testSuite + " since it requires Java " + requiredJavaVersion);
		} else {
			info("Running tests in test suite " + testSuite);
			getJUnit4TF(testSuite).run();
		}
	}
}

reporter.printReport();

if (props.containsKey("runEmma")) {
	coverageFile = new FutureFile(tmpDir, "coverage.es");
	
	// Merge the coverage files and create a report
	new EmmaMergeTF().
		addMetadataFile(Directories.getFile(tmpDir, "coverage.em")).
		addCoverageDataFiles(Directories.getAllFilesMatching(emmaOutDir, "*.ec")).
		setTarget(coverageFile).run();
		
	reportFile = File.createTempFile("emma", ".html");
	reportTask = new EmmaReportTF().
		setSource(coverageFile).
		addOutputFile(EmmaReportFormat.HTML, reportFile);
	
	// Add all source directories

	ewosSettings = new EclipseWorkspaceSettings().
		// Add dummy values for these
		addClasspathVariable("ENTITYFS_CORE", new File(".")).
		addClasspathVariable("ENTITYFS_UTIL", new File(".")).
		addClasspathVariable("JUNIT4", new File("."));
		
	ewos = new EclipseWorkspace(Directories.getDirectory(root, "src"), ewosSettings);
	projItr = ewos.getProjects(JavaProjectFilter.INSTANCE).iterator();
	while(projItr.hasNext()) {
		reportTask.addSourceDirectories(projItr.next().getSourceDirectories());
	}
	
	reportTask.run();
	info("Emma report in " + reportFile);
}
