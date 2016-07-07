# alfresco-acp-cleaner

This is a really quickly-made commandline tool to help cleanup ACP files before importing them into target Alfresco instances.
  * purge renditions (thumbnails) from the old system/ACP file before they get imported into the new system.
  * review USERS and optionally rename them to make sure target system has the users before ACP import.
  * review CUSTOM CONTENT MODEL (defined as not http://www.alfresco... or http://www.jcp.org... namespaced) to make sure target system has the expected content models before ACP import.
  
This is a basic, blunt tool to quickly review and handle simple modifications to make the ACP import process easier.


# Common usage scenario
  - rename myacp.acp file to myacp.zip.
  - unzip myacp.zip, find myacp.xml file.
  - java -jar acp-cleaner.jar -a -f myacp.xml
  - review analysis report, often just need to purge renditions and, often, rename all users to a known target user like 'admin' before import
  - java -jar acp-cleaner.jar -p -r *:admin -f myacp.xml
  - review again if desired
  - java -jar acp-cleaner.jar -a -f myacp.xml
  - zip file and content backup with the newly generated myacp.xml file, rename .zip to .acp
  - import ACP

