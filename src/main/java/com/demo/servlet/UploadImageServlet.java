package com.demo.servlet;

import com.demo.util.BodyDetect;
import com.demo.util.MyDatabaseUtil;
import com.demo.util.MyDbUtil;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

public class UploadImageServlet extends javax.servlet.http.HttpServlet {

    public static final String UPLOADED_IMAGE_DIR = "C:/image/";
    private String filePathDir;     //这里存储的是文件夹相对web应用跟目录的路径，目的是为了固定相对路径
    private String tempDir; //临时路径
    private int fileMaxSize;   //允许上传
    private static MyDbUtil myDbUtil = new MyDatabaseUtil().getMyDaUtilImpl();
    private static final String TAGNAME = "UPLOADIMAGESERVLET";
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        filePathDir = config.getInitParameter("filePathDir");   //在web.xml里已经设定好了路径
        fileMaxSize = Integer.valueOf(config.getInitParameter("fileMaxSize"));
        //tempDir = config.getInitParameter("tempDir");
        filePathDir = UPLOADED_IMAGE_DIR + filePathDir;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //定义向客户端发送响应正文的outNet
        resp.setContentType("Appliation/Json");
        resp.setCharacterEncoding("UTF8");
        PrintWriter outNet = resp.getWriter();
        String myResult = "";    //存放返回的数据JSON格式
        char[] strings = new char[1024];
        /*BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(req.getInputStream()));
        StringBuilder stringBuilder = new StringBuilder();
        while (0 < bufferedReader.read(strings)){
            stringBuilder.append(strings);

        }
        logger.info("***" +stringBuilder.toString()  + "***\n");*/
        try {
            //创建一个基于硬盘的FileItem工厂
            DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
            //设定向硬盘写数据的缓冲区大小
            fileItemFactory.setSizeThreshold(fileMaxSize);
            //设置临时目录
            //fileItemFactory.setRepository(new File(tempDir));
            //创建一个文件上传处理器
            ServletFileUpload servletFileUpload = new ServletFileUpload(fileItemFactory);
            //设置允许上传的文件的最大尺寸
            servletFileUpload.setSizeMax(fileMaxSize);

            try {
                List<FileItem> fileItems = servletFileUpload.parseRequest(req);
                Iterator iter = fileItems.iterator();
                String fac_id = null;
                while (iter.hasNext()) {
                    FileItem item = (FileItem) iter.next();
                    if(item.isFormField()){
                        fac_id = processFormField(item,outNet);
                    } else {
                        if (null != fac_id){
                            myResult = processUploadedField(item,outNet,fac_id);
                            PrintWriter respWriter = resp.getWriter();
                            respWriter.write(myResult);//TODO 这个还没弄好
                            respWriter.flush();
                            respWriter.close();
                        }
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

    private String processFormField(FileItem item, PrintWriter outNet) {
        String name = item.getFieldName();  //获得表单域的名字
        String value = item.getString();    //获得表单域的值
        if ("facility".equals(name)){
            return value;
        }
        else {
            return null;
        }
    }

    /**
     * 返回人体检测的字符串
     * @param item
     * @param outNet
     * @param fac_id
     * @return 成功与否都会返回{"state":"",```},具体格式与王大佬商定了
     */
    private String processUploadedField(FileItem item, PrintWriter outNet, String fac_id){
        long fileSize = item.getSize();
        if(0 == fileSize) return "";
        //我们以用户的唯一标识加硬件设备名称作为唯一ID存储图片
        //从数据库获取用户唯一标识
        String fromUserName = (String) myDbUtil.doDataSelect("SELECT * FROM facility WHERE fac_id = " + fac_id, "user_id");
        String fac_name = (String) myDbUtil.doDataSelect("SELECT * FROM facility WHERE fac_id = " + fac_id, "fac_name");
        File uploadedFile = new File(filePathDir + "/" + fromUserName + fac_name + ".jpg");
        try {
            item.write(uploadedFile);
            //获取人体检测见过
            String myBodyDetectResult = null;
            String bodyDetectResult = BodyDetect.postRequestFrBodyDetect(filePathDir + "/" + fromUserName + fac_name + ".jpg");
            myBodyDetectResult = "{\"state\":" + (null == bodyDetectResult?"0":("1"+ "," + bodyDetectResult))   +"}";
            System.out.println(myBodyDetectResult);
            return myBodyDetectResult;
        } catch (Exception e) {
            e.printStackTrace();
        }
        outNet.println("file is uploaded!");
        return "";
    }
}