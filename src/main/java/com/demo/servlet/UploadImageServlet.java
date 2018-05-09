package com.demo.servlet;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.Log4JLogger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

public class UploadImageServlet extends javax.servlet.http.HttpServlet {

    private String filePathDir;     //这里存储的是文件夹相对web应用跟目录的路径，目的是为了固定相对路径
    private String tempDir; //临时路径

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        filePathDir = config.getInitParameter("filePathDir");   //在web.xml里已经设定好了路径
        tempDir = config.getInitParameter("tempDir");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/plain");
        //定义向客户端发送响应正文的outNet
        PrintWriter outNet = resp.getWriter();
        try {
            //创建一个基于硬盘的FileItem工厂
            DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
            //设定向硬盘写数据的缓冲区大小
            fileItemFactory.setSizeThreshold(4 * 1024);
            //设置临时目录
            fileItemFactory.setRepository(new File(tempDir));

            //创建一个文件上传处理器
            ServletFileUpload servletFileUpload = new ServletFileUpload(fileItemFactory);
            //设置允许上传的文件的最大尺寸
            servletFileUpload.setSizeMax(4 * 1024);

            try {
                List<FileItem> fileItems = servletFileUpload.parseRequest(req);
                Iterator iter = fileItems.iterator();
                while (iter.hasNext()) {
                    FileItem item = (FileItem) iter.next();
                    if(item.isFormField()){
                        processFormField(item,outNet);
                    } else {
                        processUploadedField(item,outNet);
                    }
                }
                outNet.close();
            } catch (FileUploadException e) {
                e.printStackTrace();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processFormField(FileItem item, PrintWriter outNet) {
        String name = item.getFieldName();  //获得表单域的名字
        String value = item.getString();    //获得表单域的值
        outNet.println(name + ": " + value + "\r\n");
    }

    private void processUploadedField(FileItem item, PrintWriter outNet){
        long fileSize = item.getSize();
        if(0 == fileSize) return;
        File uploadedFile = new File(filePathDir + "/" + "token1");
        try {
            item.write(uploadedFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        outNet.println("file is uploaded!");
    }
}