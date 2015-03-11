/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package myhttpserver;

/**
 *
 * @author bilal
 */
import java.io.*;
import java.net.*;
import java.util.*;

public class WebServerHTTP extends Thread {

    static final String HTML_START =
    "<html>" +
    "<title>Bilal's WebServer</title>" +
    "<body>";

    Socket connectedClient = null;
    BufferedReader serverin = null;
    DataOutputStream serverout = null;
    
    static final String HTML_END =
    "</body>" +
    "</html>";

    public WebServerHTTP(Socket client) {
        connectedClient = client;
    }

    public void run() {

        try {

            System.out.println( "The Client "+
            connectedClient.getInetAddress() + ":" + connectedClient.getPort() + " is connected");

            serverin = new BufferedReader(new InputStreamReader (connectedClient.getInputStream()));
            serverout = new DataOutputStream(connectedClient.getOutputStream());

            String requestString = serverin.readLine();
            String headerLine = requestString;

            StringTokenizer tokenizer = new StringTokenizer(headerLine);
            String httpMethod = tokenizer.nextToken();
            String httpQueryString = tokenizer.nextToken();

            StringBuffer responseBuffer = new StringBuffer();
            responseBuffer.append("<b> This is the HTTP Server Home Page.... </b><BR>");
            responseBuffer.append("The HTTP Client request is ....<BR>");

            System.out.println("The HTTP request string is ....");
            while (serverin.ready())
            {
                // Read the HTTP complete HTTP Query
                responseBuffer.append(requestString + "<BR>");
                System.out.println(requestString);
                requestString = serverin.readLine();
            }

            if (httpMethod.equals("GET") || httpMethod.equals("POST") || httpMethod.equals("HEAD")) {
                if (httpQueryString.equals("/")) {
                    // The default home page
                    sendResponse(200, responseBuffer.toString(), false);
                } 
                else {
                    //This is interpreted as a file name
                    String fileName = httpQueryString.replaceFirst("/", "");
                    fileName = URLDecoder.decode(fileName);
                    if (new File(fileName).isFile()){
                        sendResponse(200, fileName, true);
                    }
                    else{
                        sendResponse(404, "<b>404 file not found" +
                        "</b>", false);
                    }
                }
            }
            else sendResponse(404, "<b>The Requested resource not found ...." +
            "</b>", false);

        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendResponse (int statusCode, String responseString, boolean isFile) throws Exception {

        String statusLine = null;
        String serverdetails = "Server: Java HTTPServer";
        String contentLengthLine = null;
        String fileName = null;
        String contentTypeLine = "Content-Type: text/html" + "\r\n";
        FileInputStream fin = null;

        if (statusCode == 200)
            statusLine = "HTTP/1.1 200 OK" + "\r\n";
        else if(statusCode == 502)
            statusLine = "HTTP/1.1 502 error" + "\r\n";
        else if(statusCode == 302)
            statusLine = "HTTP/1.1 302 error" + "\r\n";
        else
            statusLine = "HTTP/1.1 404 Not Found" + "\r\n";
            
        if (isFile) {
        fileName = responseString;
        fin = new FileInputStream(fileName);
        contentLengthLine = "Content-Length: " + Integer.toString(fin.available()) + "\r\n";
        if (!fileName.endsWith(".htm") && !fileName.endsWith(".html"))
            contentTypeLine = "Content-Type: \r\n";
        }
        else {
        responseString = WebServerHTTP.HTML_START + responseString + WebServerHTTP.HTML_END;
        contentLengthLine = "Content-Length: " + responseString.length() + "\r\n";
        }

        serverout.writeBytes(statusLine);
        serverout.writeBytes(serverdetails);
        serverout.writeBytes(contentTypeLine);
        serverout.writeBytes(contentLengthLine);
        serverout.writeBytes("Connection: close\r\n");
        serverout.writeBytes("\r\n");

        if (isFile) sendFile(fin, serverout);
        else serverout.writeBytes(responseString);

        serverout.close();
    }

    public void sendFile (FileInputStream fin, DataOutputStream out) throws Exception {
        byte[] buffer = new byte[1024] ;
        int bytesRead;

        while ((bytesRead = fin.read(buffer)) != -1 ) {
            out.write(buffer, 0, bytesRead);
        }
        fin.close();
    }

    public static void main (String args[]) throws Exception {

        ServerSocket Server = new ServerSocket (8000, 10, InetAddress.getByName("127.0.0.1"));
        System.out.println ("TCPServer Waiting for client on port 8000");

        while(true) {
            Socket connected = Server.accept();
            (new WebServerHTTP(connected)).start();
        }
    }
}