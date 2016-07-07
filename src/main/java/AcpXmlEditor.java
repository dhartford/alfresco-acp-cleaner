/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/*
 * Assumptions:
 * If the max filesize of an ACP file continues to be 2GB, expect xml file to be no more than 50MB on average depending on the amount of custom attributes.
 * Also some moderate-managed ACP file sizes, as there are 'timeout' challenges such as with indexing if the ACP file gets too large.
 */ 
public class AcpXmlEditor {
	
static final	XPath xpath = XPathFactory.newInstance().newXPath();


   XPathExpression renditionXpath;
   XPathExpression renditionedXpath;
   
   XPathExpression creatorUserXpath;
   XPathExpression modifierUserXpath;
   
   XPathExpression folderXpath;
   File origfile;
   Document document;
   
   /**
    * empty constructor, setup xpath rules.
 * @throws XPathExpressionException 
    */
   public AcpXmlEditor() throws XPathExpressionException{
	   setupXpaths();
   }
   
   public AcpXmlEditor(String origxmlfile) throws XPathExpressionException, SAXException, IOException, ParserConfigurationException{
	   setupXpaths();
	 origfile = new File(origxmlfile); //keep File reference to rename later.
	 	document = buildDom(origfile);
   }   
   
   /**
    * single-source the xpath (rules) to find specifically needed nodes for various operations.
    * 
    * @throws XPathExpressionException
    */
   private void setupXpaths() throws XPathExpressionException{
	   renditionXpath = xpath.compile("//*[local-name()='rendition']"); //use local-name to ignore namespace prefixes
	   renditionedXpath = xpath.compile("//*[local-name()='renditioned']"); //use local-name to ignore namespace prefixes
	   
       creatorUserXpath = xpath.compile("//*[local-name()='creator']"); //use local-name to ignore namespace prefixes
       modifierUserXpath = xpath.compile("//*[local-name()='modifier']"); //use local-name to ignore namespace prefixes

       folderXpath = xpath.compile("//*[local-name()='folder']"); //use local-name to ignore namespace prefixes 
       //not used but keeping for reference
       XPathExpression thumbnailXpath = xpath.compile("//*[local-name()='thumbnail']"); //use local-name to ignore namespace prefixes

	   
   }
   
	 public static void main(String[] args) throws Exception {
		 	
		 	new Cli(args).parse();

	    }
	 
	 
	 private Document buildDom(File origfile) throws SAXException, IOException, ParserConfigurationException{
	        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        dbf.setNamespaceAware(true);
	        Document document = dbf.newDocumentBuilder().parse(origfile);
	        return document;
	 }
	 
	 /**
	  * This will remove <xyz:renditions> (regardless of namespace) entire node and children from the document. Note this does not change the file, only the in-memory dom.
	  */
	 public void purgeRenditionsFromDomDocument() throws XPathExpressionException{
	        NodeList renditionList = (NodeList)renditionXpath.evaluate(document, XPathConstants.NODESET);

            
	        NodeList renditionedList = (NodeList)renditionedXpath.evaluate(document, XPathConstants.NODESET);

	        

            //remove all rendition nodes..make sure to also remove renditioned (with ...ed), as they are tied together.
            for (int i = 0; i < renditionList.getLength(); i++) {
            	Node renditionNode = renditionList.item(i);
            	//get parent to remove 'itself' as a child node
            	renditionNode.getParentNode().removeChild(renditionNode);

			}  
            //remove all renditioned (with ed) nodes
            for (int i = 0; i < renditionedList.getLength(); i++) {
            	Node renditionedNode = renditionedList.item(i);
            	//get parent to remove 'itself' as a child node
            	renditionedNode.getParentNode().removeChild(renditionedNode);
			}  
	 }
	 

	 public int getRenditionCount() throws XPathExpressionException{
	        NodeList renditionList = (NodeList)renditionXpath.evaluate(document, XPathConstants.NODESET);
	        return renditionList.getLength();
	 }
	 public HashSet<String> getUniqueNamespaces() throws XPathExpressionException{
      	HashSet uniqueNamespaces = new HashSet<String>(); //unique list of users

      	Node firstFolderNode = (Node)folderXpath.evaluate(document, XPathConstants.NODE);
     	NamedNodeMap attributes = firstFolderNode.getAttributes();
    
      	for (int i = 0; i < attributes.getLength(); i++) {
      		Node item = attributes.item(i);
      		
      		String namespace = item.getTextContent();
      		if (namespace.startsWith("http://www.alfresco") || namespace.startsWith("http://www.jcp.org") || !namespace.startsWith("http://")){
      		}else{
      			uniqueNamespaces.add(namespace);
      			
      		}
      		
      		
			
		}
      	return uniqueNamespaces;
	 }
	 
	 /**
	  * return the unique list of users still listed in the DOM.  Note that if called after purging renditions, the list may be different than before purging.
	 * @throws XPathExpressionException 
	  */
	 public HashSet<String> getUniqueUsers() throws XPathExpressionException{
         	HashSet users = new HashSet<String>(); //unique list of users

         	NodeList creatorUser = (NodeList)creatorUserXpath.evaluate(document, XPathConstants.NODESET);
            
            for (int i = 0; i < creatorUser.getLength(); i++) {
            	String user = creatorUser.item(i).getTextContent();
            	users.add(user);
			//	System.out.println(user);
			}
            
         	NodeList modifierUsers = (NodeList)modifierUserXpath.evaluate(document, XPathConstants.NODESET);
            for (int i = 0; i < modifierUsers.getLength(); i++) {
            	String user = modifierUsers.item(i).getTextContent();
            	users.add(user);
			//	System.out.println(user);
			}
            
            
            //unique users
            for (Iterator iterator = users.iterator(); iterator.hasNext();) {
				Object object = (Object) iterator.next();
			}
            return users;
	 }

	 
	 public void modifyUser(String olduser, String newuser) throws XPathExpressionException{

      	NodeList creatorUser = (NodeList)creatorUserXpath.evaluate(document, XPathConstants.NODESET);
         
         for (int i = 0; i < creatorUser.getLength(); i++) {
         	String user = creatorUser.item(i).getTextContent();
         	
         	if(user.equalsIgnoreCase(olduser) || olduser.equalsIgnoreCase("*"))  creatorUser.item(i).setTextContent(newuser);
			}
         
      	NodeList modifierUsers = (NodeList)modifierUserXpath.evaluate(document, XPathConstants.NODESET);
         for (int i = 0; i < modifierUsers.getLength(); i++) {
         	String user = modifierUsers.item(i).getTextContent();
         	if(user.equalsIgnoreCase(olduser) || olduser.equalsIgnoreCase("*"))  modifierUsers.item(i).setTextContent(newuser);
			}
         

	 }
	 
	 /**
	  * when ready to write, rename the original file with a timestamp filename,
	  * then create the new file with the updated/changed content from the DOM.
	 * @throws TransformerException 
	  */
	 public void writeFile() throws TransformerException{
		 	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
		 	String dateformat = sdf.format(new Date());

		 	
		 	String origfullpath = origfile.getAbsolutePath();
			 	
		 	origfile.renameTo(new File(origfile.getAbsoluteFile()+ "." + dateformat));
		 	
		 	File updatedfile = new File(origfullpath);
		 	
	        TransformerFactory tf = TransformerFactory.newInstance();
	        Transformer t = tf.newTransformer();
	        t.transform(new DOMSource(document), new StreamResult(updatedfile));
	 }
	 
	 
	 
}
