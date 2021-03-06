package tech.conexus.webautomator.shared.data.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class XMLUtils {
	
	public static final Element createOrGetElement(Document document, Element parent, String name, String value) {
		NodeList list = parent.getElementsByTagName(name);
		Element el = null;
		if (list.getLength() == 0) {
			el = document.createElement(name);
		} else {
			el = (Element)list.item(0);
		}
		
		el.setTextContent(value);
		return el;
	}
	
	public static final Element createElement(Document document, String name, String value) {
		Element el = document.createElement(name);
		el.setTextContent(value);
		
		return el;
	}
	
	public static final void appendElement(Document document, Element parent, String name, String value) {
		parent.appendChild(createElement(document, name, value));
	}
	
	public static final Element getElement(Element parent, String name) {
		NodeList list = parent.getElementsByTagName(name);
		Element el = null;
		if (list.getLength() > 0) {
			el = (Element)list.item(0);
		}
		return el;
	}
	
	public static final String getElementAttr(Element parent, String name) {
		Node n = parent.getElementsByTagName(name).item(0);
		if (n == null)
			return null;
		
		Node n2 = n.getFirstChild();
		if (n2 instanceof Text) {
			return n2.getTextContent();
		} else {
			return n.getTextContent();
		}
	}
	
	public static final Object getNodeValue(Node n) {
		Node n2 = n.getFirstChild();
		if (n2 instanceof Text) {
			return n2.getTextContent();
		} else {
			return "";
		}
	}
}
