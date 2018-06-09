# build.rb -- Build script for AT4J.
#
# Script parameters:
#   buildThreads    -- (optional) The number of build threads. Defaults to 2 *
#                      the number of available processors.
#   version         -- (optional) The version of AT4J being built.
#   noJavadocs      -- (optional) If this property is set (to anything),
#                      javadocs will not be built.
#   noFindbugs      -- (optional) Don't run Findbugs code analysis.
#   noDocbook       -- (optional) If this property is set (to anything),
#                      Docbook documentation such as the Programmer's guide
#                      will not be built.
#   docbookRoot     -- Absolute path to Docbook XSL stylesheets. This has to be
#                      set unless noDocbook is set. 
#   linkValidation  -- Validate HTML links. Remember to save stdout and stderr
#                      to a file!
#
# Environment variables:
#   JAVA_HOME     -- Should point to a version 5+ JDK.
#   FINDBUGS_HOME -- Should point to a Findbugs installation.
#
# The working directory when launching the script should be the root directory
# of the Eclipse workspace.
#
# Must have Xalan JAR:s on classpath if noDocbook is not set.
 
#
# CONSTANTS
#
XSL_TRANSFORMER = "org.apache.xalan.processor.TransformerFactoryImpl"

#
# FUNCTIONS
#
 
# Create a task that creates a XHTML file/book from a Docbook XML document
def createDocbookToXHtmlTask()
	return Schmant::XsltTF.new.
		setTransformerFactoryClassName(XSL_TRANSFORMER).
		setParameter("img.src.path", "").
		setParameter("use.role.for.mediaobject", "1").
		setParameter("chunk.section.depth", "0").
		setParameter("html.ext", ".xhtml")
end

def getApiLinks()
	res = [
		Schmant::ApiLink.new("org.at4j.", "../api/index.html"),
		Schmant::ApiLink.new("org.apache.commons.compress.compressors.", "../api/index.html"),
		Schmant::ApiLink.new("SevenZip.", "../api/index.html"),
		Schmant::ApiLink.new("org.entityfs.", "http://entityfs.sourceforge.net/releases/current/api/index.html"),
		Schmant::ApiLink.new("java.", "http://java.sun.com/j2se/1.5.0/docs/api/index.html"),
		Schmant::ApiLink.new("javax.", "http://java.sun.com/j2se/1.5.0/docs/api/index.html"),
		Schmant::ApiLink.new("org.xml.", "http://java.sun.com/j2se/1.5.0/docs/api/index.html")]

	return res
end

# Get a TextReplaceTF that is used for post processing of documentation files.
def getPostProcessReplacementTF(ramTempDir, version)
	return Schmant::ReplaceSourceFileTF.new.
		addTaskFactory(
			Schmant::ApiLinksTF.new.
			    setTempDirectory(ramTempDir).
				setLinkClass("apilink").
				addApiLinks(getApiLinks)).
		addTaskFactory(
			Schmant::TextReplaceTF.new.
				addReplace("###VERSION###", version))
end

def createAddEncodingHeaderTF()
	return Schmant::TextReplaceTF.new.
		addReplace(
			Java::JavaUtilRegex::Pattern.compile("<\\s*head(\\p{Print}|\\s)*?>", Java::JavaUtilRegex::Pattern::CASE_INSENSITIVE),
			# \\0 returns the zeroth capture group, i.e. the whole matched string
			"\\0<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>")
end

#
# START HERE
#

# Create a workspace directory with a filter that hides all .svn directories
wosDir = Schmant::FSROFileSystemBuilder.new.setRoot(Java::JavaIo::File.new(".")).create.getRootDirectory.newView(Schmant::EntityNameFilter.new(".svn").and(Schmant::DirectoryFilter::FILTER).not)
wosResourcesDir = Schmant::Directories.getDirectory(wosDir, "AT4J_resources")
wosBuildDir = Schmant::Directories.getDirectory(wosResourcesDir, "build")
wosLibDir = Schmant::Directories.getDirectory(wosResourcesDir, "lib")
wosOptlibDir = Schmant::Directories.getDirectory(wosResourcesDir, "optlib")
wosDocDir = Schmant::Directories.getDirectory(wosDir, "AT4J_doc")

# The target directory
targetDir = Schmant::TempFileUtil.createTempDirectory("at4jbuild", "", true)
# Create a temporary directory in the target directory
tmpDir = Schmant::TempFileUtil.createTempDirectory(targetDir)
# The root directory for the distribution
distDir = Schmant::Directories.newDirectory(tmpDir, "dist")
distLibDir = Schmant::Directories.newDirectory(distDir, "lib")
distDocDir = Schmant::Directories.newDirectory(distDir, "doc")

srcDistDir = Schmant::Directories.newDirectory(tmpDir, "srcDist")
srcDistTestDir = Schmant::Directories.newDirectory(srcDistDir, "test")

# A RAM directory for temporary files
ramTempDir = Schmant::SchmantFileSystems.createRamFileSystem

version = $props.getStringValue("version", "no_version_set")

eWos = Schmant::EclipseWorkspace.new(wosDir)
javaProjects = Schmant::ProjectFilterUtil.filter(eWos.getProjects, Schmant::JavaProjectFilter::INSTANCE)
nonTestProjects = Schmant::ProjectFilterUtil.filter(javaProjects,
	Schmant::ProjectNameFilter.new("AT4J_test_support").or(
		Schmant::ProjectNameGlobFilter.new("*_test")).not)

# A task executor
te = Schmant::TaskExecutor.new.
	setNumberOfThreads($props.getIntValue("buildThreads", Java::JavaLang::Math.max(Java::JavaLang::Runtime.getRuntime.availableProcessors, 2))).start

begin
	compileTargetDir = Schmant::Directories.newDirectory(tmpDir, "compile")
	
	compileWorkspace = Schmant::JavaWorkspaceBuilderTF.new.
		setLogHeader("Compiling workspace").
		setProjectSpecificTargetDirectories(true).
		setTaskExecutor(te).
		setWorkspace(eWos).
		setTarget(compileTargetDir).
		setCompileTaskFactory(
			Schmant::Jdk6JavacTF.new.
				setTargetVersion("5").
				setSourceCodeVersion("5")).create
	te.add(compileWorkspace)
	
	compileDep = compileWorkspace.getDependencyForTasksScheduledByThisTask
		
	fullJar = Schmant::FutureFile.new(distLibDir, "at4j-full-" + version + ".jar")

	testProjectsFilter = Schmant::DirectoryFilter::FILTER.and(
		Schmant::EntityNameFilter.new("AT4J_test_support").or(
			Schmant::EntityNameGlobFilter.new("*_test")))
	
	# Build jar file
	jarTask = Schmant::JarTF.new.
		setLogHeader("Building full Jar").
		# Use a closure that lists the results from all non-test projects when
		# the task is run.
		addSources(
			lambda { return Schmant::Directories.listEntities(compileTargetDir, testProjectsFilter.not) }).
		setTarget(fullJar).create
	te.add(jarTask, compileDep)
	
	# Build unit test jar file
	junitJarTask = Schmant::JarTF.new.
		setLogHeader("Building Jar for unit test classes").
		# Use a closure that lists all the test projects when the task is run
		addSources(
			lambda { return Schmant::Directories.listEntities(compileTargetDir, testProjectsFilter) }).
		setTarget(Schmant::FutureFile.new(srcDistTestDir, "at4j-junit4-" + version + ".jar")).create
	te.add(junitJarTask, compileDep)
	
	# Copy the JUnit jar to the test directory
	te.add(Schmant::CopyTF.new.
		setLogHeader("Copying JUnit Jar to the source distribution test directory").
		setSource(Schmant::Directories.getFileMatching(wosOptlibDir, "junit4-*.jar")).
		setTarget(srcDistTestDir))
	
	# Copy the AT4J_resources/build directory to the source distribution.
	te.add(Schmant::TreeCopyTF.new.
		setLogHeader("Copying the build directory to the source distribution").
		setSource(Schmant::Directories.getDirectory(wosResourcesDir, "build")).
		setTarget(Schmant::Directories.newDirectory(srcDistDir, "build"))) 

	# Run Findbugs?
	if !$props.containsKey("noFindbugs")
		enableTaskPackage("net.findbugs")
			
		findbugsDir = Schmant::Directories.newDirectory(distDocDir, "findbugs")
		
		# Schedule tasks for Findbugs-analyzing each non-test Java project
		Schmant::ProjectFilterUtil::filter(
			javaProjects,
			Schmant::ProjectNameGlobFilter.new("*_test").not).each { |proj|
			
				projName = proj.getName
				te.add(
					Schmant::ExtFindbugsTF.new.
						setLogHeader("Running Findbugs on " + projName).
						addAuxClasspathEntry(Schmant::JdkUtil.getJdkFile("jre/lib/rt.jar")).
						addAuxClasspathEntries(
							Schmant::Directories.getAllFilesMatching(wosLibDir, "*.jar")).
						addAuxClasspathEntry(fullJar).
						addSourceCodeContainers(proj.getSourceDirectories).
						# Use a closure that gets the project directory.
						addSource(
							lambda { return Schmant::Directories.getDirectory(compileTargetDir, projName) }).
						setEffortLevel(Schmant::FindbugsEffortLevel::MAX).
						setFindbugsReportLevel(Schmant::FindbugsReportLevel::LOW).
						setFindbugsReportFormat(Schmant::FindbugsReportFormat::HTML).
						setExcludeFilterFile(Schmant::Directories.getFile(wosBuildDir, "findbugs_exclude.xml")).
						setTarget(Schmant::FutureFile.new(findbugsDir, projName + ".html")),
					jarTask)
			}
	end

	# Build Javadocs?
	if !$props.containsKey("noJavadoc")
	
		nonTestSources = nonTestProjects.collect { |proj| proj.getSourceDirectories }
		puts nonTestSources
		
		buildJavadoc = Schmant::ExtJavadocTF.new.
			setLogHeader("Building binary distribution Javadocs").
			setReportLevel(Java::JavaUtilLogging::Level::WARNING).
		   	addPackageName("org.at4j").
		   	addPackageName("org.apache.commons.compress").
		   	addPackageName("SevenZip").
		   	setSubPackages(true).
			addTag(Schmant::JavadocTagDefinition.createTag("injar").
				addPlace(Schmant::JavadocTagPlace::TYPES).
				# A bug in the javadoc command prevents us from using
				# a space in this text. (Java bug #6447784)
				setTagText("In_jar:")).
			addClasspathEntry(fullJar).
			addClasspathEntries(Schmant::Directories.getAllFilesMatching(wosLibDir, "*.jar")).
			addSources(nonTestSources).
			addSource(Schmant::Directories.getDirectory(wosDocDir, "3rd_party_javadocs")).
			addDecorator(
				Schmant::ExtStandardDocletDecorator.new.
					setTarget(Schmant::Directories.newDirectory(distDocDir, "api")).
					setAuthor(true).
					addLink("http://java.sun.com/javase/6/docs/api/").
					addLink("http://entityfs.sourceforge.net/releases/current/api/").
					setKeywords(true).
					setDocTitle("AT4J version " + version + " Javadocs").
					setWindowTitle("AT4J version " + version + " Javadocs"))
			
		te.add(buildJavadoc, jarTask)
	end
	
	# Build Docbook documentation?
	if !$props.containsKey("noDocbook")
		docbookRoot = Schmant::FileSystems.getEntityForDirectory(Java::JavaIo::File.new($props.getStringValue("docbookRoot")), true)

		# A catalog resolver that will be used for resolving external entities.
		# Instruct it to use Files instead of InputStreams for creating URI
		# Source:s
		cr = Schmant::XmlCatalogResolver.new.setUseFilesInsteadOfStreamsForUris(true)
		
		# Add the Docbook DTD to the catalog
		#cr.addSystemId("http://www.oasis-open.org/docbook/xml/4.5/docbookx.dtd", Schmant::Directories.getFile(wosBuildDir, Schmant::RelativeLocation.new("docbook-4.5/docbookx.dtd")))
		
		# Need Saxon 6 TransformerFactory or the Xalan TransformerFactory as 
		# they seem to be the only TransformerFactories that can parse 
		# Docbook XSL:s.

		catalogBuildDep = Schmant::CompoundTaskDependency.new
	
		# Java cannot handle relative paths in XSL files
		# (see http://www.sagehill.net/docbookxsl/WriteCatalog.html )
		# We have to create a temporary XML catalog for the XSL entities
		t = Schmant::RecursiveActionTF.new.
			setLogHeader("Adding Docbook XSL files to XML catalog").
			setSource(Schmant::DirectoryAndFilter.new(docbookRoot, Schmant::EFileFilter::FILTER)).
			setTaskFactory(
				Schmant::AddUriToCatalogTF.new.
					setXmlCatalog(cr).
					setBaseLocation(Schmant::AbsoluteLocation.new("/xhtml"))).create
		catalogBuildDep.add(t)
		te.add(t)
		
		t = Schmant::RecursiveActionTF.new.
			setLogHeader("Adding Programmer's guide entities to XML catalog").
			addSource(
				Schmant::DirectoryAndFilter.new(
					Schmant::Directories.getDirectory(wosDocDir, "pg"),
					Schmant::EFileFilter::FILTER)).
			setTaskFactory(
				Schmant::AddSystemIdToCatalogTF.new.
					setXmlCatalog(cr).
					setLocationPrefix("http://pg/")).create
		catalogBuildDep.add(t)
		te.add(t)
		
		cs = Schmant::TemplateCompilerTF.new.
			setLogHeader("Compiling Docbook chunked HTML XSL stylesheet").
			setTransformerFactoryClassName(XSL_TRANSFORMER).
			setSource(Schmant::Directories.getFile(docbookRoot, Schmant::RelativeLocation.new("xhtml/chunk.xsl"))).
			setXslUriResolver(cr).create
		te.add(cs, catalogBuildDep)
		
		ss = Schmant::TemplateCompilerTF.new.
			setLogHeader("Compiling Docbook single HTML XSL stylesheet").
			setTransformerFactoryClassName(XSL_TRANSFORMER).
			setSource(Schmant::Directories.getFile(docbookRoot, Schmant::RelativeLocation.new("xhtml/docbook.xsl"))).
			setXslUriResolver(cr).create
		te.add(ss, catalogBuildDep)
			
		wosPgDir = Schmant::Directories.getDirectory(wosDocDir, "pg")
		parsePG = Schmant::DomParseXmlTF.new.
			setLogHeader("Parsing Programmer's Guide XML").
			setNamespaceAware(true).
			setEntityResolver(cr).
			# Workaround for error discussed at
			# http://forums.sun.com/thread.jspa?threadID=5390848
			putFeature("http://apache.org/xml/features/dom/defer-node-expansion", false).
			setSource(Schmant::Directories.getFile(wosPgDir, "pg.xml")).create
		te.add(parsePG, catalogBuildDep)
		
		# The XSL transformation does not seem to be thread safe. Add all
		# transformation tasks to this compound task to make them run
		# sequentially
		transformTasksFactory = Schmant::CompoundTF.new
		
		pgDir = Schmant::Directories.newDirectory(distDocDir, "pg")
		transformTasksFactory.addTask(
			createDocbookToXHtmlTask.
				setLogHeader("Creating Programmer's Guide multi-page HTML book").
				setSource(parsePG).
				setTemplates(cs).
				setTarget(Schmant::FutureFile.new(pgDir, "pg.xhtml")).
				setUriResolver(cr).
				setParameter("html.stylesheet", "../at4j_doc-1.1.css").
				setParameter("base.dir", Schmant::ECFileResolvableUtil.getFileObject(pgDir).getAbsolutePath + Java::JavaIo::File::separator).create)
				
		transformTasksFactory.addTask(
			createDocbookToXHtmlTask.
				setLogHeader("Creating Programmer's Guide single page HTML book").
				setSource(parsePG).
				setTemplates(ss).
				setTarget(Schmant::FutureFile.new(targetDir, "pg-single.xhtml")).
				setUriResolver(cr).
				setParameter("html.stylesheet", "../at4j_doc-1.1.css").
				setParameter("base.dir", Schmant::ECFileResolvableUtil.getFileObject(distDir).getAbsolutePath + Java::JavaIo::File::separator).create)
			
		pgSingle = Schmant::FutureFile.new(targetDir, "pg-single.xhtml")
		
		transformTasks = transformTasksFactory.create
		te.add(transformTasks, [parsePG, cs, ss])

		# This creates an empty pg.xhtml file in the documentation directory. Delete it.
		deletePG = Schmant::DeleteTF.new.
				setSource(Schmant::FutureFile.new(pgDir, "pg.xhtml")).create
		te.add(deletePG, transformTasks)
		
		# Postprocess the Programmer's Guide
		ugpp = Schmant::RecursiveActionTF.new.
			setLogHeader("Post-processing the multi-page Programmer's Guide").
			setSource(Schmant::DirectoryAndFilter.new(pgDir, Schmant::EFileNameExtensionFilter.new("xhtml"))).
			setTaskFactory(getPostProcessReplacementTF(ramTempDir, version)).create
		te.add(ugpp, deletePG)

		te.add(
			Schmant::RecursiveActionTF.new.
				setLogHeader("Adding character encoding information to the multi-page Programmer's Guide files").				
				setSource(Schmant::DirectoryAndFilter.new(pgDir, Schmant::EFileNameExtensionFilter.new("xhtml"))).
				setTaskFactory(
					Schmant::ReplaceSourceFileTF.new.
						setTaskFactory(createAddEncodingHeaderTF)),
			ugpp)

		# Copy all PNG images to the programmer's guide
#		te.add(
#			Schmant::TreeCopyTF.new.
#				setLogHeader("Copying images to the Programmer's Guide").
#				setSource(wosPgDir.newView(Schmant::EFileNameExtensionFilter.new("png"))).
#				setTarget(pgDir))		

		# Postprocess the single page user's guide
		ugspp = getPostProcessReplacementTF(ramTempDir, version).
			setLogHeader("Post-processing the single page Programmer's Guide").
			setSource(pgSingle).create
		te.add(ugspp, transformTasks)

		te.add(
			Schmant::ReplaceSourceFileTF.new.
				setLogHeader("Adding character encoding information to the single page Programmer's Guide file").
				setSource(pgSingle).
				setTaskFactory(
					createAddEncodingHeaderTF),
			ugspp)
	end
	
	# Copy and run variable substitution on static documentation files
	te.add(Schmant::RecursiveProcessTF.new.
		setLogHeader("Adding version information to documentation and copying to /doc").
		addSource(
			Schmant::DirectoryAndFilter.new(
				Schmant::Directories.getDirectory(wosDocDir, "doc"),
				Schmant::EFileFilter::FILTER)).
		setTarget(distDocDir).
		setTaskFactory(
			Schmant::TextReplaceTF.new.
				addReplace("###VERSION###", version)))

	# Copy and run variable substitution on miscellaneous distribution files
	te.add(
		Schmant::RecursiveProcessTF.new.
			setLogHeader("Adding version information to misc files and copying to /").
			addSource(
				Schmant::DirectoryAndFilter.new(
					Schmant::Directories.getDirectory(wosResourcesDir, "dist"),
					Schmant::EFileFilter::FILTER)).
			setTarget(distDir).
			setTaskFactory(
				Schmant::TextReplaceTF.new.
					addReplace("###VERSION###", version)))
	
	# Copy files from the lib directory to the lib directory in the
	# distribution
	te.add(
		Schmant::TreeCopyTF.new.
			setLogHeader("Copying Jar files to the /lib directory").
			setSource(Schmant::Directories.getDirectory(wosResourcesDir, "lib")).
			setTarget(distLibDir))
	
	te.waitFor
ensure
	te.shutdown
end

# The executor for building archive files
te = Schmant::TaskExecutor.new.
	setNumberOfThreads($props.getIntValue("buildThreads", Java::JavaLang::Runtime.getRuntime.availableProcessors * 2)).
	start

begin
	archiveRootDir = "/at4j-" + version
		
	enableTaskPackage "org.at4j"
		
	# Build a binary Zip distribution.
	zipBinTargetFile = Schmant::FutureFile.new(targetDir, "at4j-" + version + ".zip")
	te.add(
		Schmant::At4JZipTF.new.
	    	setLogHeader("Creating " + zipBinTargetFile.toString).
			setTarget(zipBinTargetFile).
			# Set maximum compression
			setCompressionLevel(Schmant::CompressionLevel::BEST).
#			setDefaultFileEntrySettings(
#				Schmant::ZipEntrySettings.new.
#					setCompressionMethod(
#						Schmant::DeflatedCompressionMethod::MAXIMUM_COMPRESSION)).
			addSource(Schmant::EntityAndAbsoluteLocation.new(distDir, archiveRootDir)))
		
	# Build binary distribution Tar archive
	te.add(
		Schmant::TarTF.new.
			setLogHeader("Creating binary distribution Tar archive").
			setTarget(
				Schmant::BZip2NewWritableFileProxy.new(
					Schmant::FutureFile.new(targetDir, "at4j-" + version + ".tar.bz2"),
					Schmant::DontOverwriteAndThrowException::INSTANCE)).
			addSource(Schmant::EntityAndAbsoluteLocation.new(distDir, archiveRootDir)))
			
	# A source object for the Eclipse workspace. It excludes the .metadata and 
	# At4J_releases directories and all "bin" directories with a parent
	# containing an underscore in the name (that should be a project directory)
	workspaceSource = 
		Schmant::EntityAndAbsoluteLocation.new(
			wosDir.newView(
				Schmant::EntityNameFilter.new("AT4J_releases").or(
				Schmant::EntityNameGlobFilter.new(".hg*")).or(
				Schmant::EntityNameFilter.new(".metadata")).or(
					Schmant::EntityNameFilter.new("bin").and(
						Schmant::DirectoryFilter::FILTER).and(
						Schmant::ParentFilter.new(Schmant::EntityNameGlobFilter.new("*_*")))).not), archiveRootDir + "/src")
		
	# Create the source distribution Zip archive
	zipSrcTargetFile = Schmant::FutureFile.new(targetDir, "at4j-src-" + version + ".zip")
	te.add(Schmant::At4JZipTF.new.
	    setLogHeader("Creating " + zipSrcTargetFile.toString).
		setTarget(zipSrcTargetFile).
		# Set maximum compression
		setCompressionLevel(Schmant::CompressionLevel::BEST).
#		setDefaultFileEntrySettings(
#			Schmant::ZipEntrySettings.new.
#				setCompressionMethod(
#					Schmant::DeflatedCompressionMethod::MAXIMUM_COMPRESSION)).
		addSource(Schmant::EntityAndAbsoluteLocation.new(distDir, archiveRootDir)).
		addSource(Schmant::EntityAndAbsoluteLocation.new(srcDistDir, archiveRootDir)).
		addSource(workspaceSource))
								
	# Build source distribution Tar archive
	te.add(
		Schmant::TarTF.new.
			setLogHeader("Creating source distribution Tar archive").
			setTarget(
				Schmant::BZip2NewWritableFileProxy.new(
					Schmant::FutureFile.new(targetDir, "at4j-src-" + version + ".tar.bz2"),
					Schmant::DontOverwriteAndThrowException::INSTANCE)).
			addSource(Schmant::EntityAndAbsoluteLocation.new(distDir, archiveRootDir)).
			addSource(Schmant::EntityAndAbsoluteLocation.new(srcDistDir, archiveRootDir)).
			addSource(workspaceSource))
			
	te.waitFor
ensure
	te.shutdown
end

if $props.containsKey("linkValidation")
	# Unzip the source and binary distributions in separate catalogs and run
	# link validation on their contents
	sdistdir = Schmant::TempFileUtil.createTempDirectory(tmpDir)
	Schmant::ZipFiles.unzip(zipSrcTargetFile.getFile, sdistdir)
	# Remove the src directory
	Schmant::IteratorDeleter.new(Schmant::Directories.getDirectory(sdistdir, Schmant::RelativeLocation.new("at4j-" + version + "/src"))).deleteEntities
	bdistdir = Schmant::TempFileUtil.createTempDirectory(tmpDir)
	Schmant::ZipFiles.unzip(zipBinTargetFile.getFile, bdistdir)
	
	# A collection containing valid links (so that the same link is not checked
	# several times).
	vlc = Java::JavaUtil::Collections.synchronizedSet Java::JavaUtil::HashSet.new
	
	# Create a new task executor
	te = Schmant::TaskExecutor.new.
		setNumberOfThreads($props.getIntValue("buildThreads", Java::JavaLang::Runtime.getRuntime.availableProcessors * 4)).
		start
	
	begin
		# Validate links in HTML pages
		te.add(
			Schmant::RecursiveActionTF.new.
				setDisableHeaderLogging(false).
				addSource(Schmant::DirectoryAndFilter.new(sdistdir, Schmant::EFileNameExtensionFilter.new("html").or(Schmant::EFileNameExtensionFilter.new("xhtml")))).
				addSource(Schmant::DirectoryAndFilter.new(bdistdir, Schmant::EFileNameExtensionFilter.new("html").or(Schmant::EFileNameExtensionFilter.new("xhtml")))).
				setTaskExecutor(te).
				setTaskFactory(
					Schmant::HtmlLinkValidationTF.new.
						setValidLinkCollection(vlc).
					    addJavadocPackage("org.at4j").
						addIgnorePattern("http://java.sun.com/javase/.*?/docs/api/.*")))
		
		te.waitFor
	ensure
		te.shutdown
	end
end
