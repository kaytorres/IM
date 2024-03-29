import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

public class QRtest {  
	
	
	 public static class MatrixToImageWriter {
		 
		   private static final int BLACK = 0xFF000000;
		   private static final int WHITE = 0xFFFFFFFF;
		 
		   private MatrixToImageWriter() {}
		 
		   
		   public static BufferedImage toBufferedImage(BitMatrix matrix) {
		     int width = matrix.getWidth();
		     int height = matrix.getHeight();
		     BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		     for (int x = 0; x < width; x++) {
		       for (int y = 0; y < height; y++) {
		         image.setRGB(x, y, matrix.get(x, y) ? BLACK : WHITE);
		       }
		     }
		     return image;
		   }
		 
		   
		   public static void writeToFile(BitMatrix matrix, String format, File file)
		       throws IOException {
		     BufferedImage image = toBufferedImage(matrix);
		     if (!ImageIO.write(image, format, file)) {
		       throw new IOException("Could not write an image of format " + format + " to " + file);
		     }
		   }
		 
		   
		   public void writeToStream(BitMatrix matrix, String format, OutputStream stream)
		       throws IOException {
		     BufferedImage image = toBufferedImage(matrix);
		     if (!ImageIO.write(image, format, stream)) {
		       throw new IOException("Could not write an image of format " + format);
		     }
		   }
	
	
	
	 }
	 public static void main(String[] args) throws Exception {  
		 
		 
		 
		 try {
             
		     String content = "kunlun:7c22898e-2339-4a9e-98b9-5562647478ac";
		     String path = "C:/Users/yar/Desktop/testImage";
		     
		     MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
		     
		     Map hints = new HashMap();
		     hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		     BitMatrix bitMatrix = multiFormatWriter.encode(content, BarcodeFormat.QR_CODE, 299, 299,hints);
		     File file1 = new File(path,"餐巾纸.jpg");
		     MatrixToImageWriter.writeToFile(bitMatrix, "jpg", file1);
		     
		     BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);
		     
		 } catch (Exception e) {
		     e.printStackTrace();
		 }
	 }
}