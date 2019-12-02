package file_update;
import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class stores all parameter values needed by following tasks.
 * 
 * @author hupmscy
 * 
 */

public class Setting {
	public static String databaseUrl;
	public static String databaseUserName;
	public static String databasePassword;
	public static String doi;
	public static String path;
	public static String prefix;
	public static boolean isload=false;
	private Setting() {
	}
	/**
	 * 
	 * @param settingLoc
	 *            Location of setting file
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws IOException
	 */

	public static void Loadsetting() throws ParserConfigurationException, SAXException, IOException {
		readXmlFiles();	
		isload=true;
	}
	// get document of xml file
	public static Document getDocument(String path) throws ParserConfigurationException,
			SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		File xmlFile = new File(path);
		return db.parse(xmlFile);
	}
	// read xml File
	static  void readXmlFiles() throws ParserConfigurationException, SAXException,
			IOException {
		// validation.xml
		String settingLoc ="configuration/setting.xml";
		Document document=getDocument(settingLoc);
		Element element = document.getDocumentElement();
		NodeList childList = element.getChildNodes();
		for (int i = 0; i < childList.getLength(); i++) {
			Node node = childList.item(i);
			if (!(node instanceof Element))
				continue;
			String name=node.getNodeName();
			if(name.equals("databaseUrl"))
				databaseUrl=node.getTextContent();
			else if(name.equals("databaseUserName"))
				databaseUserName=node.getTextContent();
			else if(name.equals("databasePassword"))
				databasePassword=node.getTextContent();
			else if(name.equals("doi"))
				doi=node.getTextContent();
			else if(name.equals("path"))
				path=node.getTextContent();
			else if(name.equals("prefix"))
				prefix=node.getTextContent();
			
		}
	}
}