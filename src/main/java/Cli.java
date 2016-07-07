import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.xml.sax.SAXException;

public class Cli {
	 private static final Logger log = Logger.getLogger(Cli.class.getName());
	 private String[] args = null;
	 private Options options = new Options();

	 public Cli(String[] args) {

	  this.args = args;

	  options.addOption("?", "help", false, "show help.");
	  options.addOption("f", "file", true, "The ACP xml file to use. [REQUIRED]");
	  options.addOption("a", "analyze", false, "Analyze information from the ACP xml file (executed before any other operations): RENDITIONS count, USERS, and CUSTOMCONTENT_MODELS");
	  options.addOption("p", "purge", false, "Purge all <rendition> elements (often thumbnails) from the ACP xml file. Note that some USERS may only be found in renditions. Backup of old file created and new file generated.");
	  options.addOption("r", "rename", true, "Example: '-r olduser:newuser' or '-r *:newuser' will rename an olduser value (or * for all users) in the ACP xml file to newuser value. Multiple individual users require multiple runs. Backup of old file created and new file generated.");

	 }

	 public void parse() throws XPathExpressionException, SAXException, IOException, ParserConfigurationException, TransformerException {
	  CommandLineParser parser = new BasicParser();

	  CommandLine cmd = null;
	  try {
	   cmd = parser.parse(options, args);

	   if (cmd.hasOption("?")){
	    help();
	   }
	   
	   if (!cmd.hasOption("f") ||  cmd.getOptionValue("f").length()<2 ){
		    log.log(Level.SEVERE, "Missing --file <arg>, required.");
		    System.exit(0);
	   }else{
		   AcpXmlEditor acpXmlEditor = new AcpXmlEditor(cmd.getOptionValue("f"));
		  
		   if (cmd.hasOption("a")) { //analyze and list info about file
			   int renditionCount = acpXmlEditor.getRenditionCount();
			   HashSet<String> uniqueUsers = acpXmlEditor.getUniqueUsers();
			   HashSet<String> uniqueNamespaces = acpXmlEditor.getUniqueNamespaces();
			   printList(renditionCount,uniqueUsers,uniqueNamespaces);
		   }
		   
		   if (cmd.hasOption("p")) { //purge renditions.  If renaming users as well, make sure to purge first to reduce rename effort.
			  acpXmlEditor.purgeRenditionsFromDomDocument();
		   }
		   
		   if (cmd.hasOption("r")) { //rename user
			   if(cmd.getOptionValue("r").length() <2 || !cmd.getOptionValue("r").contains(":")){
				   log.log(Level.SEVERE, "The -r requires an argument like 'olduser:newuser' without apostrophes.");	
				   System.exit(0);
			   }else{
				   String[] split = cmd.getOptionValue("r").split(":");
				   acpXmlEditor.modifyUser(split[0], split[1]);
			   }
		   }
		   
		   
		   
		   
		   //write one file after multiple modification operations
		   if (cmd.hasOption("p") ||cmd.hasOption("r") ) { //any modification options
			  acpXmlEditor.writeFile();
		   }
		   
		   
	   }
	   
	   
	   


	  } catch (ParseException e) {
	   log.log(Level.SEVERE, "Failed to parse comand line properties", e);
	   help();
	  }
	 }

	 private void help() {
	  // This prints out some help
	  HelpFormatter formater = new HelpFormatter();

	  formater.printHelp("Main", options);
	  System.exit(0);
	 }
	 
	 
	 private void printList(int renditionCount,HashSet<String> uniqueUsers,HashSet<String> uniqueNamespaces ){
		 System.out.println("===RENDITIONS===");
		 System.out.println("RenditionCount: " + renditionCount);
		 System.out.println("");

		 System.out.println("===USERS===");
		 System.out.println(Arrays.toString(uniqueUsers.toArray()));
		 System.out.println("");
		 
		 System.out.println("===CUSTOMCONTENT_MODELS===");
		 System.out.println(Arrays.toString(uniqueNamespaces.toArray()));
		 System.out.println("");
	 }
	}
