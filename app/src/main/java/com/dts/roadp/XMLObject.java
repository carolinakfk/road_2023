package com.dts.roadp;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.util.Log;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class XMLObject {
    private Cursor data;
    private int adimx,adimy;
    public String debg="";
    public String xmlresult;

    public XMLObject() {

    }

    public <T> T get(Class<? extends T> type, String source) throws Exception {

        try {

            String xnode=getXMLRegion(source);
            Serializer serializer = new Persister();
            return serializer.read( type, xnode);

        }catch (Exception e){
            throw new Exception(" XMLObject get : "+e.getMessage());
        }

    }

    public <T> T getresult(Class<? extends T> type, String source, String XMLresult) throws Exception {
        try {
            xmlresult=XMLresult;

            String xnode=getXMLRegion(source+"Result");
            Serializer serializer = new Persister();
            if (!xnode.isEmpty()){
                //T var=serializer.read( type, xnode);
                return serializer.read( type, xnode);
            }
        }catch (Exception e){
            throw new Exception(" XMLObject Error: "+ e.getMessage()+ " GetResult:");
        }
        return null;
    }

    public Cursor filldt() throws Exception {
        if (!parseXMLArray()) createVoidCursor();
        return data;
    }

    public Object getSingle( String name, Class<?> cl)  throws Exception {

        String body=getXMLRegionSingle(name);

        int start = body.indexOf("<" + name + ">");
        if (start>-1)  start += name.length() + 2;else start=0;//with <and > char
        int end = body.indexOf("</" + name + ">");
        if (end == -1) body = "";else body = body.substring(start, end);

        String gname = cl.getName();

        if (cl.getName().toLowerCase().contains("string")) {
            return body;
        }
        if (cl.getName().toLowerCase().contains("double")) {
            if (body.isEmpty()) return 0; else return
                    Double.parseDouble(body);
        }
        if (cl.getName().toLowerCase().contains("int")) {
            if (body.isEmpty()) return 0; else return
                    Integer.parseInt(body);
        }

        if (cl.getName().toLowerCase().contains("boolean")) {
            return Boolean.parseBoolean(body);
        }

        return null;
    }

    private boolean isParsing=false;

    public String getXMLRegion(String nodename) throws Exception {

        String ss ="";
        String sxml="";
        Node xmlnode;
        int cVals=0;
        double mequedeaqui=0;

        InputStream istream=null;
        DocumentBuilder docBuilder;
        DocumentBuilderFactory builderFactory;

        if (!isParsing)
        {
            isParsing =true;

            try {

                mequedeaqui=1;

                try{

                    istream = new ByteArrayInputStream( xmlresult.getBytes() );

                }catch (Exception ex){
                    Log.i("A","B");
                }

                builderFactory = DocumentBuilderFactory.newInstance();
                mequedeaqui=-1;
                docBuilder = builderFactory.newDocumentBuilder();
                mequedeaqui=-2;

                if(istream.available()==0)
                {
                    Log.i("notfound","nimershhere");
                    mequedeaqui=-2.5;
                    istream = new ByteArrayInputStream( xmlresult.getBytes());
                    mequedeaqui=-2.6;
                }

                if(istream.available()==0)
                {
                    Log.i("try2","de todas formas no se pudo asignar el result.. sospecho problema en la memoria *del dispositivo no la m[ia, la mia anda bien");
                    return  "";

                }

                mequedeaqui=-2.7;
                Document doc = docBuilder.parse(istream);

                mequedeaqui=2;
                Element root=doc.getDocumentElement();
                mequedeaqui=3;
                NodeList children=root.getChildNodes();
                Node bodyroot=children.item(0);
                NodeList body=bodyroot.getChildNodes();
                Node responseroot=body.item(0);
                NodeList response=responseroot.getChildNodes();
                mequedeaqui=4;
                ss="";

                for(int i =0;i<response.getLength();i++)
                {
                    mequedeaqui=5;
                    ss+=response.item(i).getNodeName()+",\n";

                    if (response.item(i).getNodeName().equalsIgnoreCase(nodename))
                    {
                        cVals=response.item(i).getChildNodes().getLength();
                        mequedeaqui=6;
                        if (cVals>0)
                        {
                            mequedeaqui+=7;
                            xmlnode=response.item(i);
                            sxml=nodeToString(xmlnode);
                            return sxml;
                        }
                    }
                }
            } catch (Exception e)
            {
                debg = e.getMessage() + "\n "+xmlresult;
                throw new Exception(" XMLObject getXMLRegion : "+ debg);
            } finally {
                isParsing =false;
            }
        }else{
            Log.e("imbussy","telodije");
        }

        return "";
    }

    public String getXMLRegionSingle(String nodename) throws Exception {
        String st,ss,sv,en,sxml;
        Node xmlnode;

        try {

            InputStream istream = new ByteArrayInputStream( xmlresult.getBytes() );
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = builderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(istream);

            Element root=doc.getDocumentElement();

            NodeList children=root.getChildNodes();
            Node bodyroot=children.item(0);
            NodeList body=bodyroot.getChildNodes();
            Node responseroot=body.item(0);
            NodeList response=responseroot.getChildNodes();

            ss="";
            for(int i =0;i<response.getLength();i++) {
                ss+=response.item(i).getNodeName()+",\n";

                if (response.item(i).getNodeName().equalsIgnoreCase(nodename)) {
                    xmlnode=response.item(i);
                    sxml=nodeToString(xmlnode);
                    return sxml;
                }
            }
        } catch (Exception e) {
            throw new Exception(" XMLObject getXMLRegion : "+ e.getMessage());
        }
        return "";
    }

    private String nodeToString(Node node)  throws Exception {
        StringWriter sw = new StringWriter();
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.transform(new DOMSource(node), new StreamResult(sw));
        } catch (Exception te) {
            throw new Exception("XMLObject nodeToString : "+te.getMessage());
        }
        return sw.toString();
    }

    private boolean parseXMLArray() throws Exception {
        String sv,en;

        try {
            InputStream istream = new ByteArrayInputStream(xmlresult.getBytes() );
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = builderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(istream);

            NodeList nList = null;
            try {
                nList = doc.getElementsByTagName("DocumentElement");
                NodeList ccList=nList.item(0).getChildNodes();
                adimy=ccList.getLength();
                NodeList vvList=ccList.item(0).getChildNodes();
                adimx=vvList.getLength();
            } catch (Exception e) {
                return false;
            }

            String[] crow = new String[adimx];
            MatrixCursor cursor = new MatrixCursor(crow);

            for (int i =0;i<nList.getLength();i++){

                NodeList cList=nList.item(i).getChildNodes();

                for (int ii =0;ii<cList.getLength();ii++){

                    Element elm = (Element) cList.item(ii);
                    NodeList vList=cList.item(ii).getChildNodes();

                    for (int vv =0;vv<vList.getLength();vv++){
                        en=vList.item(vv).getNodeName();
                        sv=getNodeValue(en,elm);
                        crow[vv]=sv;
                    }

                    cursor.addRow(crow);
                }
            }

            data=cursor;
            return true;
        } catch (Exception e) {
            throw new Exception("XMLObject parseXMLArray : "+e.getMessage());
        }
    }

    private void createVoidCursor() {
        String[] crow = new String[adimx];
        MatrixCursor cursor = new MatrixCursor(crow);
        data=cursor;
    }

    private String getNodeValue(String tag, Element element) {
        NodeList nodeList = element.getElementsByTagName(tag);
        Node node = nodeList.item(0);
        if (node!=null){
            if (node.hasChildNodes()){
                Node child = node.getFirstChild();
                while (child!=null){
                    if (child.getNodeType() == Node.TEXT_NODE){
                        return  child.getNodeValue();
                    }
                }
            }
        }
        return null;
    }

}
