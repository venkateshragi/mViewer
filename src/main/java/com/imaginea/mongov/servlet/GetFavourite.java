/*
 * Copyright (c) 2011 Imaginea Technologies Private Ltd. 
 * Hyderabad, India
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following condition
 * is met:
 *
 *     + Neither the name of Imaginea, nor the
 *       names of its contributors may be used to endorse or promote
 *       products derived from this software.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.imaginea.mongov.servlet;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author aditya
 */
@XmlRootElement
class Favourite implements Serializable {

    private static final long serialVersionUID = 6414696841879452040L;
    List<String> name = new ArrayList<String>();
    List<String> host = new ArrayList<String>();
    List<Integer> port = new ArrayList<Integer>();
    List<String> username = new ArrayList<String>();
    List<String> password = new ArrayList<String>();
    int size = 0;
}

@WebServlet(name = "GetFavourite", urlPatterns = {"/GetFavourite"})
public class GetFavourite extends HttpServlet {

    private static final long serialVersionUID = 8428302912236127727L;

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void parseXML(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/x-java-serialized-object");
        OutputStream os = response.getOutputStream(); // returns an outputstream that writes to this connection
        ObjectOutputStream out = new ObjectOutputStream(os);
        try {
            File file = new File("C:\\Documents and Settings\\aditya\\My Documents\\NetBeansProjects\\Nevala\\favourite.xml");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            NodeList rootList = doc.getElementsByTagName("fav");
            Favourite data = new Favourite();
            int index = 0;
            for (index = 0; index < rootList.getLength(); index++) {
                //out.print("<br><br>Connection "+(index+1)+"<br>");
                Node fav = rootList.item(index);
                NodeList favNodes = fav.getChildNodes();
               // out.println("<br><br><img src=\"database.png\" alt=\"database\"> ");
                for (int anotherIndex = 0; anotherIndex < favNodes.getLength(); anotherIndex++) {
                    Node node = favNodes.item(anotherIndex);
                  //if (!node.getNodeName().equals("#text")) {
                 //       out.println(node.getNodeName() + " - " + node.getChildNodes().item(0).getNodeValue().trim() + "<br>");
                   //  }
                   String nodeName= node.getNodeName().toString();
                   if(nodeName.equals("Name")){
                       data.name.add(node.getChildNodes().item(0).getNodeValue().trim());
                   }
                }

            }
            if (index == 0) {
            //    out.print("<br><br>No favourites added");
            }
            System.out.println(data.name.get(0));
            out.writeObject(data);
        } catch (SAXParseException err) {
            System.out.println("** Parsing error" + ", line "
                    + err.getLineNumber() + ", uri " + err.getSystemId());
            System.out.println(" " + err.getMessage());

        } catch (SAXException e) {
            Exception x = e.getException();
            ((x == null) ? e : x).printStackTrace();

        } catch (Throwable t) {
           // out.print(t);
            t.printStackTrace();
        } finally {
            out.close();
        }
    }
//    public String print(){
//        return "hell";
//    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        parseXML(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        parseXML(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
