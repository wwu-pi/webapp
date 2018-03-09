package m.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CLI {

	protected String[] args;
	protected Options options;
	
	protected String classPath;
	protected String cut;
	protected String mutName;
	protected String mutSignature;
	
	public CLI(String[] args) {
		this.args = args;
		this.options = new Options();
		this.options.addOption("h", "help", false, "show help.");
		this.options.addRequiredOption("cp", "classPath", true, "extra class path for the web application. multiple class pathes seperated by OS-seperator (e.g. Windows a semicolon, Unix a colon) ");
		this.options.addRequiredOption("c", "cut", true, "the full qualified name of the class under test");
		this.options.addRequiredOption("mn", "name", true, "the name of the method under test");
		this.options.addRequiredOption("ms", "signature", true, "the signature of the method under test");
	}
	
	public void parse() throws ParseException {
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch(MissingOptionException e) {
			e.printStackTrace();
			printHelp();
			System.exit(0);
		}
		
		if(cmd.hasOption("c")) {
			this.cut = cmd.getOptionValue("c");
		}
		if(cmd.hasOption("mn")) {
			this.mutName = cmd.getOptionValue("mn");
		}
		if(cmd.hasOption("ms")) {
			this.mutSignature = cmd.getOptionValue("ms");
		}
		if(cmd.hasOption("cp")) {
			this.classPath = cmd.getOptionValue("cp");
		}
	}
	
	private void printHelp() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("mugst", options);
	}

	public String getClassPath() {
		return this.classPath;
	}
	public String getCUT() {
		return this.cut;
	}
	public String getMUTSignature() {
		return this.mutSignature;
	}
	public String getMUTName() {
		return this.mutName;
	}
}
