package com.sj.weixin.web.utils;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;


public class DESUtility {
	
	private final static String DES = "DES";

    /**
     * Description 根据键值进行加密
     * @param data
     * @param key  加密键byte数组
     * @return
     * @throws Exception
     */
    public static String encrypt(String data, String key) throws Exception {
        byte[] bt = encrypt(data.getBytes(), key.getBytes());
        String strs = new BASE64Encoder().encode(bt);
        return strs;
    }

    /**
     * Description 根据键值进行解密
     * @param data
     * @param key  加密键byte数组
     * @return
     * @throws IOException
     * @throws Exception
     */
    public static String decrypt(String data, String key) throws IOException,
            Exception {
        if (data == null)
            return null;
        BASE64Decoder decoder = new BASE64Decoder();
        byte[] buf = decoder.decodeBuffer(data);
        byte[] bt = decrypt(buf,key.getBytes());
        return new String(bt);
    }
    
    /**
     * Description 根据键值进行加密
     * @param data
     * @param key  加密键byte数组
     * @return
     * @throws Exception
     */
    private static byte[] encrypt(byte[] data, byte[] key) throws Exception {
        // 生成一个可信任的随机数源
        SecureRandom sr = new SecureRandom();
 
        // 从原始密钥数据创建DESKeySpec对象
        DESKeySpec dks = new DESKeySpec(key);
 
        // 创建一个密钥工厂，然后用它把DESKeySpec转换成SecretKey对象
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        SecretKey securekey = keyFactory.generateSecret(dks);
 
        // Cipher对象实际完成加密操作
        Cipher cipher = Cipher.getInstance(DES);
 
        // 用密钥初始化Cipher对象
        cipher.init(Cipher.ENCRYPT_MODE, securekey, sr);
 
        return cipher.doFinal(data);
    }
    
    /**
     * Description 根据键值进行解密
     * @param data
     * @param key  加密键byte数组
     * @return
     * @throws Exception
     */
    private static byte[] decrypt(byte[] data, byte[] key) throws Exception {
        // 生成一个可信任的随机数源
        SecureRandom sr = new SecureRandom();
 
        // 从原始密钥数据创建DESKeySpec对象
        DESKeySpec dks = new DESKeySpec(key);
 
        // 创建一个密钥工厂，然后用它把DESKeySpec转换成SecretKey对象
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
        SecretKey securekey = keyFactory.generateSecret(dks);

        // Cipher对象实际完成解密操作
        Cipher cipher = Cipher.getInstance(DES);
 
        // 用密钥初始化Cipher对象
        cipher.init(Cipher.DECRYPT_MODE, securekey, sr);
 
        return cipher.doFinal(data);
    }
    
    
    
    public static byte[]encryptDES(String encryptString, String encryptKey)    throws Exception 
	{
		byte[] iv = encryptKey.getBytes();
		
		 IvParameterSpec zeroIv =new IvParameterSpec(iv);
		
		 SecretKeySpec key = new SecretKeySpec(encryptKey.getBytes(), "DES");
		
		 Cipher cipher =Cipher.getInstance("DES/CBC/PKCS5Padding");
		
		 cipher.init(Cipher.ENCRYPT_MODE,key, zeroIv);
		
		 byte[] encryptedData =cipher.doFinal(encryptString.getBytes("UTF-8"));
		
		 return encryptedData;
	}
    
    public static String decryptDES(byte[] encryptedData, String decryptKey)   throws Exception 
	{
		byte[] iv = decryptKey.getBytes();
		
		 IvParameterSpec zeroIv =new IvParameterSpec(iv);
		
		 SecretKeySpec key = new SecretKeySpec(decryptKey.getBytes("UTF-8"), "DES");
		
		 Cipher cipher =Cipher.getInstance("DES/CBC/PKCS5Padding");
		
		 cipher.init(Cipher.DECRYPT_MODE,key, zeroIv);
		
		 byte decryptedData[] =cipher.doFinal(encryptedData);
		
		 String decryptedString =new String(decryptedData, "UTF-8");
		
		 return decryptedString;
	
	}
    
    

	public static String parseByte2HexStr(byte buf[]) {  
	    StringBuffer sb = new StringBuffer();  
	    for (int i = 0; i < buf.length; i++) {  
	            String hex = Integer.toHexString(buf[i] & 0xFF);  
	            if (hex.length() == 1) {  
	                    hex = '0' + hex;  
	            }  
	            sb.append(hex.toUpperCase());  
	    }  
	    return sb.toString();  
	}  
	
	/**将16进制转换为二进制 
	* @param hexStr 
	* @return 
	*/  
	public static byte[] parseHexStr2Byte(String hexStr) {  
	    if (hexStr.length() < 1)  
	            return null;  
	    byte[] result = new byte[hexStr.length()/2];  
	    for (int i = 0;i< hexStr.length()/2; i++) {  
	            int high = Integer.parseInt(hexStr.substring(i*2, i*2+1), 16);  
	            int low = Integer.parseInt(hexStr.substring(i*2+1, i*2+2), 16);  
	            result[i] = (byte) (high * 16 + low);  
	    }  
	    return result;  
	}  
	
	public static String getMD5(String str) {
		if (str == null) {
			return null;
		}
		byte[] source = str.getBytes();
		String s = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(source);
			byte[] tmp = md.digest(); // MD5 的计算结果是一个 128 位的长整数，    
			char[] c = new char[tmp.length * 2]; // 每个字节用 16 进制表示的话，使用两个字符，    
			int k = 0; // 表示转换结果中对应的字符位置   
			for (int i = 0; i < tmp.length; i++) { 
				byte byte0 = tmp[i]; // 取第 i 个字节   
				c[k++] = binaryToHex(byte0 >>> 4 & 0xf); // 取字节中高 4 位的数字转换,    
				// >>> 为逻辑右移，将符号位一起右移   
				c[k++] = binaryToHex(byte0 & 0xf); // 取字节中低 4 位的数字转换   
			}
			s = new String(c); // 换后的结果转换为字符串
		} catch (Exception e) {
			e.printStackTrace();
		}
		return s;
	}
	

	public static char binaryToHex(int binary) {
		return hexDigits[binary];
	}
	

	private static char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	
}