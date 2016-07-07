# alfresco-acp-cleaner

This is a really quickly-made commandline tool to help cleanup ACP files before importing them into target Alfresco instances.
  * purge renditions (thumbnails) from the old system/ACP file before they get imported into the new system.
  * review USERS and optionally rename them to make sure target system has the users before ACP import.
  * review CUSTOM CONTENT MODEL (defined as not http://www.alfresco... or http://www.jcp.org... namespaced) to make sure target system has the expected content models before ACP import.
  
This is a basic, blunt tool to quickly review and handle simple modifications to make the ACP import process easier.

Example usage to purge renditions, rename all users to 'admin' for import:
java -jar acp-cleaner.jar -p -r *:admin -f myacp.xml

