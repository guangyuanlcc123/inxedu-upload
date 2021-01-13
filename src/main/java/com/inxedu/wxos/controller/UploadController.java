package com.inxedu.wxos.controller;


import com.inxedu.wxos.pojo.Plupload;
import com.inxedu.wxos.util.JsonUtil;
import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import ws.schild.jave.MultimediaInfo;
import ws.schild.jave.MultimediaObject;


/**
 * @author www.inxedu.com
 */
@EnableAutoConfiguration
@Controller
@Slf4j
public class UploadController {
    @Value("${project.file.root}")
    private String path;

    @Value("${project.projectName}")
    private String projectName;

    @RequestMapping("index")
    public String index(){
        return "index";
    }

    @RequestMapping("test")
    @ResponseBody
    public String test(HttpServletRequest request){
        String rootPath = request.getServletContext().getRealPath("/svnProject/viodeUpload/");
        String url = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
        return rootPath + "___________" + url;
    }

    /**
     * form表单/ajax 文件上传
     * @param request
     * @param response
     * @param multipartFile
     * @param param
     * @return
     */
    @PostMapping("/uploadVideoTest")
    @ResponseBody
    public String uploadVideo(HttpServletRequest request,HttpServletResponse response,
                         @RequestParam("fileList") MultipartFile multipartFile, @RequestParam("param") String param){
        InputStream input = null;
        OutputStream out = null;
        try {
            String fileName = multipartFile.getOriginalFilename();
            String ext = fileName.substring(fileName.lastIndexOf(".")+1);
//            if (null == ext || "".equals(ext) || !"MP4".equals(ext.toUpperCase())) {
//                log.info("文件格式错误，上传失败");
//                return responseErrorData(response, 1, "文件格式错误，上传失败。");
//            }

            String targetFilePath =  "/images/upload/" + param + "/" +toString(new Date(), "yyyyMMdd") + "/" + System.currentTimeMillis() + randomString(6) + "." + ext;
            String tempFilePath = "/images/upload/temp/" + fileName;

            input = multipartFile.getInputStream();
            File targetFile = new File(this.path + targetFilePath);
            String time = "";

            if (!targetFile.getParentFile().exists()) {
                targetFile.getParentFile().mkdirs();
            }
            File tempFile = new File(this.path + tempFilePath);
            if (!tempFile.getParentFile().exists()) {
                tempFile.getParentFile().mkdirs();
            }
            out = new BufferedOutputStream(new FileOutputStream(tempFile), 2 * 1024);
            byte[] buffer = new byte[2 * 1024];
            int len = 0;
            //写入文件
            while ((len = input.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }

            input.close();
            out.close();

            FileUtils.copyFile(tempFile, targetFile);
            if (tempFile.exists()) {
                tempFile.delete();
                log.info("文件已经被成功删除");
            }else {
                log.info("失败");
            }
            if (targetFile.exists()) {
                time = ReadVideoTime(targetFile);
            }

            String url = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
            return responseData2(url + targetFilePath, fileName, 0, "上传成功", response,targetFile,time);
        } catch (Exception e) {
            log.error("uploadVideo:   ", e);
            return responseErrorData(response, 2, "系统繁忙，上传失败。");
        }finally {
            //关闭输入输出流
            if (null != input) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != out) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 删除文件
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/deletefile", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> deleteFile(HttpServletRequest request, @RequestParam(value = "filePath", required = true) String filePath) {
        Map<String, Object> json = new HashMap<String, Object>();
        try {
            if (filePath != null && filePath.trim().length() > 0 && filePath.startsWith("/images/upload/") && filePath.indexOf("./") == -1 && filePath.indexOf(".\\") == -1) {
                File file = new File(this.path + filePath);

                if (file.exists()) {
                    file.delete();
                    json = setJson(true, "文件删除成功", null);
                } else {
                    json = setJson(false, "文件不存在，删除失败", null);
                }
            } else {
                json = setJson(false, "删除失败", null);
            }
        } catch (Exception e) {
            json = setJson(false, "系统繁忙，文件删除失败", null);
            log.error("deleteFile()--error", e);
        }
        return json;
    }

    /**
     * 根据文件地址下载文件
     *
     * @param request
     */
    @RequestMapping(value = "/file/download")
    @ResponseBody
    public void download(HttpServletRequest request, HttpServletResponse response, String fileDownloadName, String filePath) {
        try {
            int status = 0;
            byte[] b = new byte[2 * 1024];
            FileInputStream in = null;
            ServletOutputStream out2 = null;
            try {
                String fileName;
                if (org.springframework.util.StringUtils.isEmpty(fileDownloadName)) {
                    File tempFile = new File(this.path + filePath);
                    fileName = tempFile.getName();
                } else {
                    fileName = fileDownloadName;
                }
                in = new FileInputStream(this.path + filePath);
                response.setContentType("application/zip");
                response.setHeader("content-disposition", "attachment; filename=" + URLEncoder.encode(fileName, "utf-8"));
                out2 = response.getOutputStream();
                while ((status = in.read(b)) != -1) {
                    out2.write(b, 0, status);
                }
                out2.flush();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (out2 != null) {
                    try {
                        out2.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Plupload插件文件上传
     * @param plupload
     * @param request
     * @param response
     * @param param
     * @return
     */
    @RequestMapping(value = "/uploadVideo", method = RequestMethod.POST)
    public String uploadVideo(Plupload plupload, HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "param", required = false) String param) {
        try {
            plupload.setRequest(request);
            String fileName = plupload.getName();
            String ext =  fileName.substring(fileName.lastIndexOf(".")+1);
//            if (!"mp4".equals(ext)) {
//                log.info("文件格式错误，上传失败");
//                return responseErrorData(response, 1, "文件格式错误，上传失败。");
//            }
            //上传文件
            return this.pluploadUtil(plupload, response, param, ext, request);
        } catch (Exception e) {
            log.error("uploadVideo error", e);
            return responseErrorData(response, 2, "系统繁忙，上传失败。");
        }
    }

    /**
     * 用于Plupload插件的文件上传
     *
     * @param plupload - 存放上传所需参数的bean
     * @throws IllegalStateException
     * @throws IOException
     */
    public String pluploadUtil(Plupload plupload, HttpServletResponse response, String param, String ext,HttpServletRequest request) throws Exception {
        int chunks = plupload.getChunks();  //获取总的碎片数
        int chunk = plupload.getChunk();    //获取当前碎片(从0开始计数)

        String filePath =  "/images/upload/" + param + "/" +toString(new Date(), "yyyyMMdd") + "/" + System.currentTimeMillis() + randomString(6) + "." + ext;

        //文件存储路径
        File targetFile = new File(this.path + filePath);
        //视频播放时间
        String time = "";

        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) plupload.getRequest();
        MultiValueMap<String, MultipartFile> map = multipartRequest.getMultiFileMap();

        if (map != null) {
            //事实上迭代器中只存在一个值,所以只需要返回一个值即可
            Iterator<String> iter = map.keySet().iterator();
            while (iter.hasNext()) {
                String str = (String) iter.next();
                List<MultipartFile> fileList = map.get(str);
                for (MultipartFile multipartFile : fileList) {
                    //因为只存在一个值,所以最后返回的既是第一个也是最后一个值
                    plupload.setMultipartFile(multipartFile);

                    //当chunks>1则说明当前传的文件为一块碎片，需要合并
                    if (chunks > 1) {
                        //需要创建临时文件名，最后再更改名称
                        File tempFile = new File(this.path + "/images/upload/temp/" + plupload.getName());
                        if (!tempFile.getParentFile().exists()) {
                            tempFile.getParentFile().mkdirs();
                        }
                        //如果chunk==0,则代表第一块碎片,不需要合并
                        saveUploadFile(multipartFile.getInputStream(), tempFile, chunk == 0 ? false : true);

                        //上传并合并完成，则将临时名称更改为指定名称
                        if (chunks - chunk == 1) {
                            if (!targetFile.getParentFile().exists()) {
                                targetFile.getParentFile().mkdirs();
                            }
                            tempFile.renameTo(targetFile);

                            /*获取参数cutImg 判断是否切图*/
                            zoomImageUtil(filePath, plupload.getRequest());
                        }

                    } else {
                        if (!targetFile.exists()) {
                            //如果目标文件夹不存在则创建新的文件夹
                            targetFile.getParentFile().mkdirs();
                        }
                        //否则直接将文件内容拷贝至新文件
                        multipartFile.transferTo(targetFile);

                        /*获取参数cutImg 判断是否切图*/
                        zoomImageUtil(filePath, plupload.getRequest());
                    }

                    if (targetFile.exists()) {
                        time = ReadVideoTime(targetFile);
                    }

                }
            }
        }

        //返回数据
        String url = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
        return responseData2(url + filePath, plupload.getName(), 0, "上传成功", response,targetFile,time);
    }

    public static String ReadVideoTime(File source) {
        String length = "";
        try {
            MultimediaObject instance = new MultimediaObject(source);
            MultimediaInfo result = instance.getInfo();
            long ls = Math.round(result.getDuration() / 1000.0);
            length = ls + "";
        } catch (Exception e) {
            e.printStackTrace();
        }
        return length;
    }

    /**
     * 保存上传文件，兼合并功能
     */
    private static void saveUploadFile(InputStream input, File targetFile, boolean append) throws IOException {
        OutputStream out = null;
        try {
            if (targetFile.exists() && append) {
                out = new BufferedOutputStream(new FileOutputStream(targetFile, true), 2048);
            } else {
                out = new BufferedOutputStream(new FileOutputStream(targetFile), 2048);
            }

            byte[] buffer = new byte[2048];
            int len = 0;
            //写入文件
            while ((len = input.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
        } catch (IOException e) {
            throw e;
        } finally {
            //关闭输入输出流
            if (null != input) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != out) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取参数cutImg 判断是否切图
     *
     * @param filePath
     * @param request
     * @throws Exception
     */
    private void zoomImageUtil(String filePath, HttpServletRequest request) throws Exception {
        /*获取参数cutImg 判断是否切图*/
        String cutImg = request.getParameter("cutImg");
        if ("true".equals(cutImg)) {
            /*获取传过来的图片宽高*/
            String width = request.getParameter("width");
            String height = request.getParameter("height");
            /*如果图片尺寸不为空调用图片拉伸方法*/
            if ((null != width && width.trim().length() != 0) && (null != height && height.trim().length() != 0) && !"undefined".equals(width) && !"undefined".equals(height)) {
                zoomImage(filePath, Integer.parseInt(width), Integer.parseInt(height));
            }
        }
    }

    /**
     * 图片缩放,w，h为缩放的目标宽度和高度
     * src为源文件目录
     */
    public void zoomImage(String src, int newWidth, int newHeight) throws Exception {
        if (newWidth == 0 || newHeight == 0 || src == null || src.trim().length() == 0) {
            return;
        }
        String dest = this.path;

        BufferedImage result = null;
        try {
            File f2 = new File(dest + src);
            BufferedImage bi2 = ImageIO.read(f2);
            int originalh = bi2.getHeight();
            int originalw = bi2.getWidth();

            if (originalh != newHeight || originalw != newWidth) {
                // 开始读取文件并进行压缩
                Image src1 = ImageIO.read(f2);
                // 构造一个类型为预定义图像类型之一的 BufferedImage
                BufferedImage tag = new BufferedImage((int) newWidth, (int) newHeight, BufferedImage.TYPE_INT_RGB);
                //绘制图像  getScaledInstance表示创建此图像的缩放版本，返回一个新的缩放版本Image,按指定的width,height呈现图像
                //Image.SCALE_SMOOTH,选择图像平滑度比缩放速度具有更高优先级的图像缩放算法。
                tag.getGraphics().drawImage(src1.getScaledInstance(newWidth, newHeight, Image.SCALE_AREA_AVERAGING), 0, 0, null);
                //创建文件输出流
                FileOutputStream out = new FileOutputStream(dest + src);
                //将图片按JPEG压缩，保存到out中
                JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
                encoder.encode(tag);
                //关闭文件输出流
                out.close();

            }
        } catch (Exception e) {
            log.info("创建缩略图发生异常" + e.getMessage());
        }
    }

    /**
     * 获得一个随机的字符串
     *
     * @param length 字符串的长度
     * @return 随机字符串
     */
    public static String randomString(int length) {
        String baseString = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        final StringBuilder sb = new StringBuilder();

        if (length < 1) {
            length = 1;
        }
        int baseLength = baseString.length();
        for (int i = 0; i < length; i++) {
            int number = ThreadLocalRandom.current().nextInt(baseLength);
            sb.append(baseString.charAt(number));
        }
        return sb.toString();
    }

    /**
     * 返回数据
     *
     * @param path     文件路径
     * @param fileName 文件名
     * @param error    状态 0成功 其状态均为失败
     * @param message  提示信息
     * @return 回调路径
     */
    public String responseData2(String path, String fileName, int error, String message, HttpServletResponse response,File file,String time) throws IOException {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("time", time);
        map.put("length",file.length());
        map.put("url", path);
        map.put("fileName", fileName);
        map.put("error", error);
        map.put("message", message);
        response.setContentType("application/json; charset=UTF-8");
        response.getWriter().write(JsonUtil.obj2JsonString(map));
        response.getWriter().flush();
        return null;
    }

    /**
     * 返回错误数据
     *
     * @param error   状态 0成功 其状态均为失败
     * @param message 提示信息
     */
    public String responseErrorData(HttpServletResponse response, int error, String message) {
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("error", error);
            map.put("message", message);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().print(JsonUtil.obj2JsonString(map));
            response.getWriter().flush();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return null;
    }

    /**
     * 设置ajax请返回结果
     *
     * @param success 请求状态
     * @param message 提示信息
     * @param entity  返回数据结果对象
     */
    public static Map<String, Object> setJson(boolean success, String message, Object entity) {
        Map<String, Object> json = new HashMap<String, Object>();
        json.put("success", Boolean.valueOf(success));
        json.put("message", message);
        json.put("entity", entity);
        return json;
    }

    public static String toString(Date date, String pattern) {
        if (date == null) {
            return "";
        } else {
            if (pattern == null) {
                pattern = "yyyy-MM-dd";
            }

            String dateString = "";
            SimpleDateFormat sdf = new SimpleDateFormat(pattern);

            try {
                dateString = sdf.format(date);
            } catch (Exception var5) {
                var5.printStackTrace();
            }

            return dateString;
        }
    }

}