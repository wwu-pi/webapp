package w.cli;

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
	
	protected String webappPath;
	protected String webClassPath;
	protected String libClassPath;
	protected String startPageName;
	protected String outputDirectory;
	protected int maxSteps = 1;
	
	public CLI(String[] args) {
		this.args = args;
		this.options = new Options();
		
		this.options.addOption("h", "help", false, "show help.");
		this.options.addRequiredOption("w", "webapp", true, "path to the webapp main folder.");
		this.options.addRequiredOption("wcp", "webClassPath", true, "extra class path for the web application, i.e. where to find the backing beans");
		this.options.addRequiredOption("lcp", "libClassPath", true, "library class path for the web application, i.e. where to find library classes (or jars)");
		this.options.addRequiredOption("s", "startpage", true, "the name of the start page.");
		this.options.addRequiredOption("o", "outputdir", true, "the name of the output directory for test cases.");
		this.options.addOption("steps", "steps", true, "maximum steps / maximum action-sequence");
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
		
		if(cmd.hasOption("w")) {
			this.webappPath = cmd.getOptionValue("w");
		}
		if(cmd.hasOption("s")) {
			this.startPageName = cmd.getOptionValue("s");
		}
		if(cmd.hasOption("wcp")) {
			this.webClassPath = cmd.getOptionValue("wcp");
		}
		if(cmd.hasOption("lcp")) {
			this.libClassPath = cmd.getOptionValue("lcp");
		}
		if(cmd.hasOption("o")) {
			this.outputDirectory = cmd.getOptionValue("o");
		}
		if(cmd.hasOption("steps")) {
			this.maxSteps = Integer.parseInt(cmd.getOptionValue("steps"));
		}
	}
	
	private void printHelp() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("webapp", options);
	}

	public String getWebappPath() {
		return webappPath;
	}

	public String getWebClassPath() {
		return webClassPath;
	}
	
	public String getLibClassPath() {
		return libClassPath;
	}

	public String getStartPageName() {
		return startPageName;
	}

	public String getOutputDirectory() {
		return outputDirectory;
	}

	public int getMaxSteps() {
		return maxSteps;
	}
}
