package nl.deholtmans.xmltojsontransformer;

import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.*;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

public class XmlToJsonTransformer {
    String TEST_XML_STRING = "<?xml version=\"1.0\" ?><test attrib=\"moretest\">Turn this to JSON</test>";
    String testXml1 = "<!-- Document comment --><aaa><bbb/><ccc/></aaa>";
    String testXml2 = "<xml><overall><car>car1</car><house><floor>floor1</floor></house></overall><overall><car>car2</car><house><floor>floor3</floor></house></overall></xml>";

    public static void main(String[] args) {
        XmlToJsonTransformer xml2Json = new XmlToJsonTransformer();
        xml2Json.xmlSplitter();
        xml2Json.xmlToJson();
//        xml2Json.splitXmlIntoHighlevelElementsFile();
//        xml2Json.splitXmlIntoHighlevelElements();
//        xml2Json.splitXmlIntoHighlevelElements2();
//        xml2Json.splitXmlIntoHighlevelElements4();
//        xml2Json.splitVtdXmlUsingXpath();
//        xml2Json.xmlStreamReader();
//        xml2Json.xmlStreamReaderTransform();
//        xml2Json.printNiceXML();
//        xml2Json.domStuff();
//        xml2Json.xlstStuff();
//        xml2Json.noTransformation();
    }

    private void xmlSplitter() {
        System.out.println( "*** xmlSplitter");
        String testCars = "<root><car><name>car1</name></car><other><something>Unknown</something></other><car><name>car2</name></car></root>";
        XMLInputFactory factory = XMLInputFactory.newInstance();
        try {
            XMLStreamReader streamReader = factory.createXMLStreamReader(new StringReader(testCars));
            streamReader.nextTag();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer();
            streamReader.nextTag();
            while ( streamReader.isStartElement() ||
                  ( ! streamReader.hasNext() && streamReader.nextTag() == XMLStreamConstants.START_ELEMENT)) {
                StringWriter writer = new StringWriter();
                StreamResult result = new StreamResult(writer);
                t.transform(new StAXSource(streamReader), result);
                System.out.println( "XmlElement: " + writer.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void xmlToJson() {
        System.out.println( "*** xmlStreamReaderTransform");
        String testCars = "<root><car><name>car1</name></car><other><something>Unknown</something></other><car><name>car2</name></car></root>";
        String startElement = "car";
        int volgnummer = 1;
        XMLInputFactory factory = XMLInputFactory.newInstance();
        try {
            XMLStreamReader streamReader = factory.createXMLStreamReader(new StringReader(testCars));
            streamReader.nextTag();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer();
            t.setOutputProperty("omit-xml-declaration", "yes");
            streamReader.nextTag();
            while ( streamReader.isStartElement() ||
                    ( ! streamReader.hasNext() && streamReader.nextTag() == XMLStreamConstants.START_ELEMENT)) {
                StringWriter writer = new StringWriter();
                StreamResult result = new StreamResult(writer);
                t.transform(new StAXSource(streamReader), result);
                JSONObject jsonObject = XML.toJSONObject(writer.toString());
                jsonObject.put("sequence", ++volgnummer);
                System.out.println("XmlChunkToJson: " + jsonObject.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void testXmlToJson() {
        int PRETTY_PRINT_INDENT_FACTOR = 4;
        try {
            JSONObject xmlJSONObj = XML.toJSONObject(TEST_XML_STRING);
            String jsonPrettyPrintString = xmlJSONObj.toString(PRETTY_PRINT_INDENT_FACTOR);
            System.out.println(jsonPrettyPrintString);
        } catch (JSONException je) {
            System.out.println(je.toString());
        }
    }

    private void printNiceXML() {
        System.out.println( "*** printNiceXML");
        try {
            Source xmlInput = new StreamSource(new StringReader(testXml1));
            StreamResult xmlOutput = new StreamResult(new StringWriter());
            Transformer transformer = TransformerFactory.newInstance().newTransformer(); // An identity transformer
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(xmlInput, xmlOutput);
            System.out.println(xmlOutput.getWriter().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void xmlEventReader() {
        System.out.println( "*** xmlEventReader");
        XMLInputFactory factory = XMLInputFactory.newInstance();
        Reader reader = new StringReader(testXml2);
        try {
            XMLEventReader eventReader = factory.createXMLEventReader(reader);
            while(eventReader.hasNext()){
                XMLEvent event = eventReader.nextEvent();
                if(event.getEventType() == XMLStreamConstants.START_ELEMENT){
                    StartElement startElement = event.asStartElement();
                    System.out.println( "XmlEvent: " + startElement.getName().getLocalPart());
                }
                //handle more event types here...
            }

        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    private void xmlStreamReader() {
        System.out.println( "*** xmlStreamReader");
        XMLInputFactory factory = XMLInputFactory.newInstance();
        Reader reader = new StringReader(testXml2);
        try {
            XMLStreamReader streamReader = factory.createXMLStreamReader(reader);
            while(streamReader.hasNext()){
                streamReader.next();
                if(streamReader.getEventType() == XMLStreamReader.START_ELEMENT){
                    System.out.println( "XmlStreamReader: " + streamReader.getLocalName());
                }
            }

        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    private void xmlStreamReaderTransform() {
        System.out.println( "*** xmlStreamReaderTransform");
        String testCars = "<root><car><name>car1</name></car><car><name>car2</name></car></root>";
        XMLInputFactory factory = XMLInputFactory.newInstance();
        Reader reader = new StringReader(testCars);
        try {
            XMLStreamReader streamReader = factory.createXMLStreamReader(reader);
            Source source = new StAXSource(streamReader);

            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);

            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();

            int volgnummer = 1;
            while(streamReader.hasNext()){
                streamReader.next();
                if(streamReader.getEventType() == XMLStreamReader.START_ELEMENT){
                    System.out.println( "** XmlStreamReaderXX: " + streamReader.getLocalName());
                    String localName = streamReader.getLocalName();
                    if (localName.equals( "car")) {
                        transformer.transform(source, result);
                        JSONObject jsonObject = XML.toJSONObject(writer.toString());
                        jsonObject.put("number", ++volgnummer);
                        System.out.println("JSON: " + jsonObject.toString());
                    }
                }
            }

        } catch ( Exception e) {
            e.printStackTrace();
        }
    }

    private void splitXmlIntoHighlevelElements() {
        System.out.println( "*** splitXmlIntoHighlevelElements");
        String testCars = "<root><car><name>car1</name></car><car><name>car2</name></car><car><name>car3</name></car><car><name>car4</name></car></root>";
        String element = "car";
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader streamReader = factory.createXMLStreamReader(new StringReader(testCars));
            streamReader.nextTag();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer();
            t.setOutputProperty("omit-xml-declaration", "yes");
            streamReader.nextTag();
            while (streamReader.isStartElement() || streamReader.nextTag() == XMLStreamConstants.START_ELEMENT) {
                StringWriter writer = new StringWriter();
                StreamResult result = new StreamResult(writer);
                t.transform(new StAXSource(streamReader), result);
                System.out.println("Element>>>: " + writer.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void splitXmlIntoHighlevelElements4() {
        System.out.println( "*** splitXmlIntoHighlevelElements4");
        String testCars = "<root><car><name>car1</name></car><car><name>car2</name></car><car><name>car3</name></car><car><name>car4</name></car></root>";
        String element = "car";
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader streamReader = factory.createXMLStreamReader(new StringReader(testCars));
            streamReader.nextTag();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer();
            t.setOutputProperty("omit-xml-declaration", "yes");
            while(streamReader.hasNext()) {
                streamReader.nextTag();
                if( streamReader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                    StringWriter writer = new StringWriter();
                    StreamResult result = new StreamResult(writer);
                    //                t.transform(new StAXSource(streamReader), result);
                    t.transform(new StAXSource(streamReader), result);
                    System.out.println("Element: " + writer.toString());
                } else {
                    System.out.println( "OTHER: " +  streamReader.getLocalName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void splitXmlIntoHighlevelElementsFile() {
        System.out.println( "*** splitXmlIntoHighlevelElementsFile");
        String element = "car";
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader streamReader = factory.createXMLStreamReader(new FileReader("input.xml"));
            streamReader.nextTag();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer();
            t.setOutputProperty("omit-xml-declaration", "yes");
            while(streamReader.nextTag() == XMLStreamConstants.START_ELEMENT) {
                File file = new File("out/" + streamReader.getAttributeValue(null, "id") + ".xml");
                t.transform(new StAXSource(streamReader), new StreamResult(file));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    private void splitVtdXmlUsingXpath() {
//        System.out.println( "*** splitVtdXmlUsingXpath");
//        try {
//            VTDGen vg = new VTDGen();
//            if (vg.parseFile("input.xml", true)) {
//                VTDNav vn = vg.getNav();
//                AutoPilot ap = new AutoPilot(vn);
//                ap.selectXPath("/root/car");
//                int i = -1, j = 0;
//                while ((i = ap.evalXPath()) != -1) {
//                    long l = vn.getElementFragment();
//                    System.out.println( "out: " + j + ": l = " + l + " string = " + new String( vn.getXML().getBytes()));
//                    (new FileOutputStream("out" + j + ".xml")).write(vn.getXML().getBytes(), (int) l, (int) (l >> 32));
//                    j++;
//                }
//            }
//        } catch( Exception e) {
//            e.printStackTrace();
//        }
//    }


    private void splitXmlIntoHighlevelElements2() {
        System.out.println( "*** splitXmlIntoHighlevelElements2");
        String testCars = "<root><car><name>car1</name></car><car><name>car2</name></car><car><name>car3</name></car><car><name>car4</name></car></root>";
        String element = "car";

        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader streamReader = factory.createXMLStreamReader(new StringReader(testCars));
            streamReader.nextTag(); // Advance to statements element

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer();
            streamReader.nextTag();
            while (streamReader.isStartElement() || streamReader.nextTag() == XMLStreamConstants.START_ELEMENT) {
                String elementName = streamReader.getLocalName();
                if (element.equals(elementName)) {
                    StringWriter writer = new StringWriter();
                    StreamResult result = new StreamResult(writer);
                    t.transform(new StAXSource(streamReader), result);
                    System.out.println("ElementX2: " + writer.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void xlstStuff() {
        String books = "<?xml version=\"1.0\"?>\n" +
                "<ARTICLE>\n" +
                "  <TITLE>A Sample Article</TITLE>\n" +
                "  <SECT>Section 1\n" +
                "     <PARA>Par 1</PARA>\n" +
                "     <SECT>Sect 1.1\n" +
                "       <PARA>Par 1.1.\n" +
                "       </PARA>\n" +
                "     </SECT>\n" +
                "  </SECT>\n" +
                "  <SECT>Section 2\n" +
                "     <PARA>Par 2</PARA>\n" +
                "     <SECT>Sect 2.1\n" +
                "       <PARA>Par 2.1.\n" +
                "       </PARA>\n" +
                "     </SECT>\n" +
                "  </SECT>\n" +
                "</ARTICLE>";
        String xsltRules = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" +
                "\n" +
                "<xsl:stylesheet \n" +
                "  xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" \n" +
                "  version=\"1.0\"\n" +
                "  >\n" +
                "  <xsl:output method=\"html\"/> \n" +
                " \n" +
                "  <xsl:template match=\"/\">\n" +
                "    <html><body>\n" +
                "       <xsl:apply-templates/>\n" +
                "    </body></html>\n" +
                "  </xsl:template>\n" +
                "\n" +
                "  <xsl:template match=\"/ARTICLE/TITLE\">\n" +
                "    <h1 align=\"center\"> <xsl:apply-templates/> </h1>\n" +
                "  </xsl:template>\n" +
                "\n" +
                "  <!-- Top Level Heading -->\n" +
                "  <xsl:template match=\"/ARTICLE/SECT\">\n" +
                "      <h2> <xsl:apply-templates select=\"text()|B|I|U|DEF|LINK\"/> </h2>\n" +
                "      <xsl:apply-templates select=\"SECT|PARA|LIST|NOTE\"/>\n" +
                "  </xsl:template>\n" +
                "    \n" +
                "  <!-- Second-Level Heading -->\n" +
                "  <xsl:template match=\"/ARTICLE/SECT/SECT\">\n" +
                "      <h3> <xsl:apply-templates select=\"text()|B|I|U|DEF|LINK\"/> </h3>\n" +
                "      <xsl:apply-templates select=\"SECT|PARA|LIST|NOTE\"/>\n" +
                "  </xsl:template>\n" +
                "\n" +
                "  <!-- Third-Level Heading -->\n" +
                "  <xsl:template match=\"/ARTICLE/SECT/SECT/SECT\">\n" +
                "     <xsl:message terminate=\"yes\">Error: Sections can only be nested 2 deep.</xsl:message>\n" +
                "  </xsl:template>\n" +
                "\n" +
                "  <!-- Paragraph -->\n" +
                "  <xsl:template match=\"PARA\">\n" +
                "      <p><xsl:apply-templates/></p>\n" +
                "  </xsl:template>\n" +
                "\n" +
                "</xsl:stylesheet>";
        try
        {
            DocumentBuilder parser =
                    DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = parser.parse( new InputSource(new StringReader( books)) );
            TransformerFactory tf = TransformerFactory.newInstance();
            System.out.printf("TransformerFactory: %s%n", tf);
            StringReader fr = new StringReader( xsltRules);
            StreamSource ssStyleSheet = new StreamSource(fr);
            Transformer t = tf.newTransformer(ssStyleSheet);
            Source source = new DOMSource(document);
            Result result = new StreamResult( System.out );
            t.transform(source, result);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void domStuff() {
        String xmlString = "<PHONEBOOK>" +
                "  <PERSON>" +
                "   <NAME>Joe Wang</NAME>" +
                "   <EMAIL>joe@yourserver.com</EMAIL>" +
                "   <TELEPHONE>202-999-9999</TELEPHONE>" +
                "   <WEB>www.java2s.com</WEB>" +
                "  </PERSON>" +
                "  </PHONEBOOK>";
        try {
            DocumentBuilder parser =
                    DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = parser.parse( new InputSource(new StringReader(xmlString)) );
            Transformer transformer =
                    TransformerFactory.newInstance().newTransformer();
            Source source = new DOMSource( document );
            Result output = new StreamResult( System.out );
            transformer.transform( source, output );
        } catch( Exception e) {
            e.printStackTrace();
        }
    }


    private void stax2() {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLEventReader eventReader = inputFactory.createXMLEventReader(new StringReader( testXml1));
            StAXSource source = new StAXSource(eventReader);
            StringWriter writer = new StringWriter();
            transformer.transform(source, new StreamResult(writer));
            System.out.println( "STAX: " + writer.toString());
        } catch( Exception e) {
            e.printStackTrace();
        }
    }

    private void noTransformation() {
        try {
            StreamSource source = new StreamSource(new StringReader("<xml>blabla</xml>"));
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            transformer.transform(source, result);
            String strResult = writer.toString();
            System.out.println("Result = " + strResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
