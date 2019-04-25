package nl.deholtmans.xmltojsontransformer;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import javax.xml.stream.*;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.*;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

public class XmlToJsonTransformer {
    String TEST_XML_STRING = "<?xml version=\"1.0\" ?><test attrib=\"moretest\">Turn this to JSON</test>";
    String testXml1 = "<!-- Document comment --><aaa><bbb/><ccc/></aaa>";
    String testXml2 = "<xml><car>car1</car><house><floor>floor1</floor></house></xml>";

    public static void main(String[] args) {
        XmlToJsonTransformer xml2Json = new XmlToJsonTransformer();
        xml2Json.testXmlToJson();
        xml2Json.stax2();
        xml2Json.xmlEventReader();
        xml2Json.xmlStreamReader();
        xml2Json.xmlStreamReaderTransform();
        xml2Json.printNiceXML();
        xml2Json.noTransformation();
        xml2Json.xmlToJson();
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
        XMLInputFactory factory = XMLInputFactory.newInstance();
        Reader reader = new StringReader(testXml1);
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
        XMLInputFactory factory = XMLInputFactory.newInstance();
        Reader reader = new StringReader(testXml1);
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
        XMLInputFactory factory = XMLInputFactory.newInstance();
        Reader reader = new StringReader(testXml2);
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
                    System.out.println( "XmlStreamReader: " + streamReader.getLocalName());
                    transformer.transform(source,result);
                    JSONObject jsonObject = XML.toJSONObject( writer.toString());
                    jsonObject.put("volgnummer", ++volgnummer);
                    System.out.println( "JSON: " + jsonObject.toString());
                }
            }

        } catch ( Exception e) {
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


    private void xmlToJson() {
        int volgnummer = 1;
        String startElement = "car";
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader streamReader = factory.createXMLStreamReader(new StringReader(testXml2));
            Source source = new StAXSource(streamReader);

            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);

            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            transformer.transform(source, result);

            System.out.println( "Output = " + writer);

            while (streamReader.hasNext()) {
                int next = streamReader.next();
                if (next == XMLStreamConstants.START_ELEMENT) {
                    String localName = streamReader.getLocalName();
                    if (localName.equals(startElement)) {
                        transformer.transform(source, new StreamResult(out));
                        JSONObject jsonObject = XML.toJSONObject(out.toString());
                        jsonObject.put("volgnummer", ++volgnummer);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
