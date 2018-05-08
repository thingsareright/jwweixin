package com.demo.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.Log4JLogger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Hashtable;

public class UploadImageServlet extends javax.servlet.http.HttpServlet {


    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //读取请求Body
        byte[] body = readBody(request);
        //取得所有Body内容的字符串表示
        String textBody = new String(body, "UTF-8");
        //取得上传的文件名称
        String fileName = getFileName(textBody);
        //取得文件开始与结束位置
        Position p = getFilePosition(request, textBody);
        //输出至文件
        writeTo(fileName, body, p);
    }

    //构造类
    class Position {

        int begin;
        int end;

        public Position(int begin, int end) {
            this.begin = begin;
            this.end = end;
        }
    }

    private byte[] readBody(HttpServletRequest request) throws IOException {
        //获取请求文本字节长度
        int formDataLength = request.getContentLength();
        //取得ServletInputStream输入流对象
        DataInputStream dataStream = new DataInputStream(request.getInputStream());
        byte body[] = new byte[formDataLength];
        int totalBytes = 0;
        while (totalBytes < formDataLength) {
            int bytes = dataStream.read(body, totalBytes, formDataLength);
            totalBytes += bytes;
        }
        return body;
    }

    private Position getFilePosition(HttpServletRequest request, String textBody) throws IOException {
        //取得文件区段边界信息
        String contentType = request.getContentType();
        String boundaryText = contentType.substring(contentType.lastIndexOf("=") + 1, contentType.length());
        //取得实际上传文件的气势与结束位置
        int pos = textBody.indexOf("facility=\"");
        pos = textBody.indexOf("\n", pos) + 1;
        pos = textBody.indexOf("\n", pos) + 1;
        pos = textBody.indexOf("\n", pos) + 1;
        int boundaryLoc = textBody.indexOf(boundaryText, pos) - 4;
        int begin = ((textBody.substring(0, pos)).getBytes("UTF-8")).length;
        int end = ((textBody.substring(0, boundaryLoc)).getBytes("UTF-8")).length;

        return new Position(begin, end);
    }

    private String getFileName(String requestBody) {
        String fileName = requestBody.substring(requestBody.indexOf("facility=\"") + 10);
        fileName = fileName.substring(0, fileName.indexOf("\n"));
        fileName = fileName.substring(fileName.indexOf("\n") + 1, fileName.indexOf("\""));

        return fileName;
    }

    private void writeTo(String fileName, byte[] body, Position p) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream("F:\\GitHUb\\OPEN\\Chronicle-Map-master\\jwweixin\\src\\main\\resources\\" + fileName + ".png");
        fileOutputStream.write(body, p.begin, (p.end - p.begin));
        fileOutputStream.flush();
        fileOutputStream.close();
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">

    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }
}