import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

import net.coobird.thumbnailator.Thumbnails;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;


public class pictest {  
	
	public static void main(String[] args) throws IOException{  

		  Scanner in = new Scanner(System.in);
		  System.out.print("BASE64：");
		  String BASE64CODE = in.next();
		  
		  String filePath = "D:\\temp\\1.png";  
	      String outFilePath = "D:\\temp";  
	      String outFileName = "temp.png";  
	      String thumbnailName = "thumbnail.png";
	      String thumbnailRealName = "thumbnail.thumbnail";
	      
	      //getFile(getBytes(filePath),outFilePath,outFileName);  
	      //System.out.println(GetImageStr(filePath));
	      
	      GenerateImage(BASE64CODE,outFilePath+"\\"+outFileName);
	      Thumbnails.of(outFilePath+"\\"+outFileName).size(240, 320).toFile(outFilePath+"\\"+thumbnailName);
	      
	     
	      File file = new File(outFilePath+"\\"+outFileName);
	      
	      
	      File thumbnailfile = new File(outFilePath+"\\"+thumbnailName);
	      
	   
	      String  targetURL = "http://172.30.3.107:3080/kunlunMedia/uploadFiles.htm"; 
	      PostMethod filePost = new PostMethod(targetURL);
	      try {
		  Part[] parts = { new FilePart(file.getName(), file), new FilePart(thumbnailfile.getName(), thumbnailfile)};
		  filePost.setRequestEntity(new MultipartRequestEntity(parts,filePost.getParams()));
			HttpClient client = new HttpClient();
			client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
			int status = client.executeMethod(filePost);
			if (status == HttpStatus.SC_OK) {
				System.out.println("上传成功");
				System.out.println(filePost.getResponseBodyAsString());
				
				// 上传成功
			} else {
				System.out.println("上传失败");
				// 上传失败
			}
			/*
			Part[] thumbnailparts = { new FilePart(thumbnailfile.getName(), file) };
			PostMethod filePost2 = new PostMethod(targetURL);
			  filePost2.setRequestEntity(new MultipartRequestEntity(thumbnailparts,filePost2.getParams()));
			  HttpClient client2 = new HttpClient();
			  client2.getHttpConnectionManager().getParams().setConnectionTimeout(5000);
			 int  status2 = client2.executeMethod(filePost2);
				if (status2 == HttpStatus.SC_OK) {
					System.out.println("上传缩略图成功");
					System.out.println(filePost2.getResponseBodyAsString());
					
					// 上传成功
				} else {
					System.out.println("上传失败");
					// 上传失败
				}*/
	      } catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				filePost.releaseConnection();
			}
	}
	
    /** 
     * 获得指定文件的byte数组 
     */  
    public static byte[] getBytes(String filePath){  
        byte[] buffer = null;  
        try {  
            File file = new File(filePath);  
            FileInputStream fis = new FileInputStream(file);  
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);  
            byte[] b = new byte[1000];  
            int n;  
            while ((n = fis.read(b)) != -1) {  
                bos.write(b, 0, n);  
            }  
            fis.close();  
            bos.close();  
            buffer = bos.toByteArray();  
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
        return buffer;  
    }  
	
    /** 
     * 根据byte数组，生成文件 
     */  
	public static void getFile(byte[] bfile, String filePath,String fileName) {  
        BufferedOutputStream bos = null;  
        FileOutputStream fos = null;  
        File file = null;  
        try {  
            File dir = new File(filePath);  
            if(!dir.exists()&&dir.isDirectory()){//判断文件目录是否存在  
                dir.mkdirs();  
            }  
            file = new File(filePath+"\\"+fileName);  
            fos = new FileOutputStream(file);  
            bos = new BufferedOutputStream(fos);  
            bos.write(bfile);  
        } catch (Exception e) {  
            e.printStackTrace();  
        } finally {  
            if (bos != null) {  
                try {  
                    bos.close();  
                } catch (IOException e1) {  
                    e1.printStackTrace();  
                }  
            }  
            if (fos != null) {  
                try {  
                    fos.close();  
                } catch (IOException e1) {  
                    e1.printStackTrace();  
                }  
            }  
        }  
    }  
	
	public static String GetImageStr(String imgFilePath) {// 将图片文件转化为字节数组字符串，并对其进行Base64编码处理  
		byte[] data = null;  
		  
		// 读取图片字节数组  
		try {  
		InputStream in = new FileInputStream(imgFilePath);  
		data = new byte[in.available()];  
		in.read(data);  
		in.close();  
		} catch (IOException e) {  
		e.printStackTrace();  
		}  
		// 对字节数组Base64编码  
		BASE64Encoder encoder = new BASE64Encoder();  
		return encoder.encode(data);// 返回Base64编码过的字节数组字符串  
	}
	
	public static boolean GenerateImage(String imgStr, String imgFilePath) {// 对字节数组字符串进行Base64解码并生成图片  
		if (imgStr == null) // 图像数据为空  
		return false;  
		BASE64Decoder decoder = new BASE64Decoder();  
		try {  
		// Base64解码  
		byte[] bytes = decoder.decodeBuffer(imgStr);  
		for (int i = 0; i < bytes.length; ++i) {  
		if (bytes[i] < 0) {// 调整异常数据  
		bytes[i] += 256;  
		}  
		}  
		// 生成jpeg图片  
		OutputStream out = new FileOutputStream(imgFilePath);  
		out.write(bytes);  
		out.flush();  
		out.close();  
		return true;  
		} catch (Exception e) {  
		return false;  
		}  
		}  
}