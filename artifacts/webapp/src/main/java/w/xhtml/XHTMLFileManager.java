package w.xhtml;

import java.io.File;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

import m.state.StateElement;
import w.exception.AnalyzeException;

public class XHTMLFileManager extends StateElement {

	private static final long serialVersionUID = 1L;

	/**
	 * The path of the web application folder.
	 */
	protected final String webappPath;
	
	/**
	 * A set of XHTML-files found in the given web application folder.
	 */
	protected final Set<File> xhtmlFiles;
	
	public XHTMLFileManager(String webappPath) {
		this.webappPath = webappPath;
		this.xhtmlFiles = new HashSet<>();
		analyzeWebappPath();
	}

	private void analyzeWebappPath() {
		System.out.println("*** Start searching for .xhtml files in folder: " + this.webappPath);
		
		File webappFolder = new File(webappPath);
		if(!webappFolder.exists()) throw new AnalyzeException("Could not find webapp main folder in: " + webappPath + " (looked in: " + webappFolder.getAbsolutePath() + ")");
		if(!webappFolder.isDirectory()) throw new AnalyzeException("Given path is not a folder: " + webappPath);
		if(!webappFolder.getAbsolutePath().endsWith("webapp")) throw new AnalyzeException("Given path is not a webapp folder (must end with direcotry folder named 'webapp'): " + webappPath);
		
		analyzeFolderFiles(webappFolder);
	}

	private void analyzeFolderFiles(File folder) {
		if(!folder.isDirectory()) throw new AnalyzeException("Given file is not a folder: " + folder);
		
		File[] files = folder.listFiles();
		for(File f : files) {
			if(f.isFile()) {
				String extension = FilenameUtils.getExtension(f.getName());
				if(extension.toLowerCase().equals("xhtml")) {
					System.out.println("\tfound XHMTL file: " + f);
					xhtmlFiles.add(f);
				}
			} else if(f.isDirectory()) {
				analyzeFolderFiles(f);
			}
		}
	}

	public File getFileByName(String pageName) {
		if(pageName.startsWith("\"")) pageName = pageName.substring(1);
		if(pageName.endsWith("\"")) pageName = pageName.substring(0, pageName.length()-1);
		for(File f : this.xhtmlFiles) {
			String name = FilenameUtils.getBaseName(f.getName());
			if(name.equals(pageName)) {
				return f;
			}
		}
		return null;
	}
	
}
