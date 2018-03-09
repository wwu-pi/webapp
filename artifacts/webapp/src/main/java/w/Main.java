package w;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import org.apache.bcel.Repository;
import org.apache.bcel.classfile.ClassFormatException;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.commons.io.FilenameUtils;

import m.exe.ex.SymbolicExecutionException;
import m.javaee.db.SymbolicObjectDatabase;
import w.bean.BeanManager;
import w.cli.CLI;
import w.exe.SymbolicExecutor;
import w.xhtml.WebPageManager;
import w.xhtml.ui.WebPage;

public class Main {

	public static void main(String[] args) throws Exception {
		CLI cli = new CLI(args);
		cli.parse();
		
		sayHello(cli);
		
		SymbolicObjectDatabase database = new SymbolicObjectDatabase();
		BeanManager beanManager = new BeanManager(cli.getWebClassPath());
		WebPageManager webPageManager = new WebPageManager(cli.getWebappPath());
		WebPage startPage = webPageManager.getPageByName(cli.getStartPageName());
		
		buildRepository(cli.getWebClassPath(), cli.getLibClassPath());
		
		SymbolicExecutor executor = new SymbolicExecutor(webPageManager,database,beanManager,cli.getOutputDirectory(),cli.getMaxSteps());
		executor.start(startPage);		
	}

	private static void buildRepository(String webClassPath, String libClassPath) throws ClassFormatException, IOException {
		addClassPathEntries(webClassPath);
		addClassPathEntries(libClassPath);
	}

	private static void addClassPathEntries(String classPath) throws ClassFormatException, IOException {
		String pattern = Pattern.quote(System.getProperty("path.separator"));
		String[] classPaths = classPath.split(pattern);
		System.out.println("*** Analyze additional class path: " + Arrays.toString(classPaths));
		for(String cp : classPaths) {
			analyzeClassPath(new File(cp));
		}
	}
	
	private static void analyzeClassPath(File f) throws ClassFormatException, IOException {
		if(!f.exists()) throw new SymbolicExecutionException("Given class path does not exists!");

		if(f.isDirectory()) {
			for(File ff : f.listFiles()) {
				analyzeClassPath(ff);
			}
		} else {
			String extension = FilenameUtils.getExtension(f.getName());
			if(extension.equals("class")) {
				InputStream is = new FileInputStream(f);
				ClassParser cp = new ClassParser(is, f.getName());
				JavaClass jc = cp.parse();
				Repository.addClass(jc);
				System.out.println("   * class successfully added to internal repository:        " + jc.getClassName());
			} else if(extension.equals("jar")) {
				JarFile jarFile = new JarFile(f);
				Enumeration<JarEntry> entries = jarFile.entries();
				while(entries.hasMoreElements()) {
					JarEntry entry = entries.nextElement();
					if (!entry.getName().endsWith(".class")) {
		                continue;
		            }
					ClassParser parser = new ClassParser(f.getAbsolutePath(), entry.getName());
					JavaClass javaClass = parser.parse();
					Repository.addClass(javaClass);
				}
				System.out.println("   * successfully added classes from jar file:        " + f.getName());
				jarFile.close();
			}
		}
	}

	private static void sayHello(CLI cli) {
		System.out.println("***********************************");
		System.out.println("***     TEST CASE GENERATOR     ***");
		System.out.println("***********************************");
		System.out.println("*** Program started with arguments:");
		System.out.println("\twebClassPath="+cli.getWebClassPath());
		System.out.println("\tlibClassPath="+cli.getLibClassPath());
		System.out.println("\twebappPath="+cli.getWebappPath());		
		System.out.println("\tstartPage="+cli.getStartPageName());
	}
	
}
