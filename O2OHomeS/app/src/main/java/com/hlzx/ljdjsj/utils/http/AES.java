package com.hlzx.ljdjsj.utils.http;

import java.security.Key;
import java.security.SecureRandom;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author Allen
 * @version 创建时间：2015-5-16 下午03:55:57
 * @Modified By:Administrator
 * Version: 1.0
 * jdk : 1.6
 * 类说明：
 */

public class AES {

	//加密算法
	private static final String KEY_ALGORITHM = "AES";
	//算法/模式/填充
	private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding"; 
	
	public static byte[] initSecretKey(SecureRandom random)  throws Exception
	{
		KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
		keyGenerator.init(random);
		SecretKey secretKey = keyGenerator.generateKey();
		return secretKey.getEncoded();
	}
	
	/**
	 * 将二进制数据包装为key
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static Key toKey(byte[] key) throws Exception
	{
		//SecureRandom.get
		
		// 密钥加密器生成器 
		KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_ALGORITHM);
		SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");  
        secureRandom.setSeed(key);
		keyGenerator.init(128, secureRandom);
		// 创建加密器 
		SecretKey secretKey = keyGenerator.generateKey();
		byte[] enCodeFormat = secretKey.getEncoded();
		SecretKeySpec key2 = new SecretKeySpec(enCodeFormat, KEY_ALGORITHM);
		return key2;
	}
	
	/**
	 * 加密
	 * @param data				待加密数据
	 * @param key				密钥
	 * @param cipherAlgorithm	算法/模式,具有以下形式：</br>
	 * 1."算法/模式/填充"</br>
	 * 2."算法"</br>
	 * [注]后一种情况下，使用模式和填充方案特定于提供者的默认值, 例以下是有效的转换:</br>
	 * Cipher c = Cipher.getInstance("DES/CBC/PKCS5Padding");
	 * @return
	 * @throws Exception
	 */
	public static byte[] encrypt(byte[] data, Key key, String cipherAlgorithm) throws Exception
	{
		Cipher cipher = Cipher.getInstance(cipherAlgorithm);
		cipher.init(Cipher.ENCRYPT_MODE, key);
		return cipher.doFinal(data);
	}
	
	/**
	 * 加密
	 * @param data				待加密数据
	 * @param key				二进制密钥
	 * @param cipherAlgorithm	算法/模式
	 * @return
	 * @throws Exception
	 */
	public static byte[] encrypt(byte[] data, byte[] key, String cipherAlgorithm) throws Exception
	{
		Key key2 = toKey(key);
		return encrypt(data, key2, cipherAlgorithm);
	}
	
	/**
	 * 加密
	 * @param data			待加密数据
	 * @param key			二进制密钥
	 * @return
	 * @throws Exception
	 */
	public static byte[] encrypt(byte[] data, byte[] key) throws Exception
	{
		return encrypt(data, key, DEFAULT_CIPHER_ALGORITHM);
	}
	
	/**
	 * 加密
	 * @param data		待加密数据
	 * @param key		密钥
	 * @return
	 * @throws Exception
	 */
	public static byte[] encrypt(byte[] data, Key key) throws Exception
	{
		return encrypt(data, key, DEFAULT_CIPHER_ALGORITHM);
	}
	
	
	/**
	 * 解密
	 * @param data				待解密数据
	 * @param key				密钥
	 * @param cipherAlgorithm	算法/模式
	 * @return
	 * @throws Exception
	 */
	public static byte[] decrypt(byte[] data, Key key, String cipherAlgorithm) throws Exception
	{
		Cipher cipher = Cipher.getInstance(cipherAlgorithm);
		cipher.init(Cipher.DECRYPT_MODE, key);
		return cipher.doFinal(data);
	}
	
	/** 
     * 解密 
     * @param data  待解密数据 
     * @param key   二进制密钥 
     * @param cipherAlgorithm   算法/模式
     * @return byte[]   解密数据 
     * @throws Exception 
     */  
    public static byte[] decrypt(byte[] data,byte[] key,String cipherAlgorithm) throws Exception
    {
        Key k = toKey(key);  
        return decrypt(data, k, cipherAlgorithm);  
    }  
	
    /** 
     * 解密 
     * @param data  待解密数据 
     * @param key   密钥 
     * @return byte[]   解密数据 
     * @throws Exception 
     */  
    public static byte[] decrypt(byte[] data,Key key) throws Exception
    {  
        return decrypt(data, key, DEFAULT_CIPHER_ALGORITHM);  
    } 
	
    /** 
     * 解密 
     * @param data  待解密数据 
     * @param key   二进制密钥 
     * @return byte[]   解密数据 
     * @throws Exception 
     */  
    public static byte[] decrypt(byte[] data,byte[] key) throws Exception
    {  
        return decrypt(data, key,DEFAULT_CIPHER_ALGORITHM);  
    }  
    
    private static String showByteArray(byte[] data){
        if(null == data){  
            return null;  
        }  
        StringBuilder sb = new StringBuilder("{");  
        for(byte b:data){  
            sb.append(b).append(",");  
        }  
        sb.deleteCharAt(sb.length()-1);  
        sb.append("}");  
        return sb.toString();  
    }  
    
    public static void main(String[] args)throws Exception {
    	
    	String source = "Hello World!的撒娇法律大姐夫我饿ur欧文uorewuoqruweiqourwoei发哦i解放东路教练组vo错位了陆战队job大幅讴歌偶然据了解 度搜佛额外解放了大街分类的撒娇了房屋诶哦出走vUCi哦程序走vUC哦组VCi偶像  ";
    	source += "堵塞发哦风星座女4d674z4vz3vcwe4fads4f在vczs4wf6awe4的持续成长性娃儿额问问地速度发了多少了骄傲法律为哦热计量程序将垃圾发电量睡觉了位24地方";
    	source += "堵塞发哦风星座女4d674z4vz3vcwe4fads4f在vczs4wf6awe4的持续成长性娃儿额问问地速度发了多少了骄傲法律为哦热计量程序将垃圾发电量睡觉了位24地方";
    	source += "堵塞发哦风星座女4d674z4vz3vcwe4fads4f在vczs4wf6awe4的持续成长性娃儿额问问地速度发了多少了骄傲法律为哦热计量程序将垃圾发电量睡觉了位24地方";
    	source += "堵塞发哦风星座女4d674z4vz3vcwe4fads4f在vczs4wf6awe4的持续成长性娃儿额问问地速度发了多少了骄傲法律为哦热计量程序将垃圾发电量睡觉了位24地方";
    	source += "堵塞发哦风星座女4d674z4vz3vcwe4fads4f在vczs4wf6awe4的持续成长性娃儿额问问地速度发了多少了骄傲法律为哦热计量程序将垃圾发电量睡觉了位24地方";
    	source += "堵塞发哦风星座女4d674z4vz3vcwe4fads4f在vczs4wf6awe4的持续成长性娃儿额问问地速度发了多少了骄傲法律为哦热计量程序将垃圾发电量睡觉了位24地方";
    	source += "堵塞发哦风星座女4d674z4vz3vcwe4fads4f在vczs4wf6awe4的持续成长性娃儿额问问地速度发了多少了骄傲法律为哦热计量程序将垃圾发电量睡觉了位24地方";
    	source += "堵塞发哦风星座女4d674z4vz3vcwe4fads4f在vczs4wf6awe4的持续成长性娃儿额问问地速度发了多少了骄傲法律为哦热计量程序将垃圾发电量睡觉了位24地方";
    	source += "堵塞发哦风星座女4d674z4vz3vcwe4fads4f在vczs4wf6awe4的持续成长性娃儿额问问地速度发了多少了骄傲法律为哦热计量程序将垃圾发电量睡觉了位24地方";
    	source += "堵塞发哦风星座女4d674z4vz3vcwe4fads4f在vczs4wf6awe4的持续成长性娃儿额问问地速度发了多少了骄傲法律为哦热计量程序将垃圾发电量睡觉了位24地方";
    	source += "堵塞发哦风星座女4d674z4vz3vcwe4fads4f在vczs4wf6awe4的持续成长性娃儿额问问地速度发了多少了骄傲法律为哦热计量程序将垃圾发电量睡觉了位24地方";
    	source += "堵塞发哦风星座女4d674z4vz3vcwe4fads4f在vczs4wf6awe4的持续成长性娃儿额问问地速度发了多少了骄傲法律为哦热计量程序将垃圾发电量睡觉了位24地方";
    	source += "堵塞发哦风星座女4d674z4vz3vcwe4fads4f在vczs4wf6awe4的持续成长性娃儿额问问地速度发了多少了骄傲法律为哦热计量程序将垃圾发电量睡觉了位24地方";
    	source += "堵塞发哦风星座女4d674z4vz3vcwe4fads4f在vczs4wf6awe4的持续成长性娃儿额问问地速度发了多少了骄傲法律为哦热计量程序将垃圾发电量睡觉了位24地方";
    	source += "堵塞发哦风星座女4d674z4vz3vcwe4fads4f在vczs4wf6awe4的持续成长性娃儿额问问地速度发了多少了骄傲法律为哦热计量程序将垃圾发电量睡觉了位24地方";
    	source += "堵塞发哦风星座女4d674z4vz3vcwe4fads4f在vczs4wf6awe4的持续成长性娃儿额问问地速度发了多少了骄傲法律为哦热计量程序将垃圾发电量睡觉了位24地方";
    	source += "堵塞发哦风星座女4d674z4vz3vcwe4fads4f在vczs4wf6awe4的持续成长性娃儿额问问地速度发了多少了骄傲法律为哦热计量程序将垃圾发电量睡觉了位24地方";
    	source += "堵塞发哦风星座女4d674z4vz3vcwe4fads4f在vczs4wf6awe4的持续成长性娃儿额问问地速度发了多少了骄傲法律为哦热计量程序将垃圾发电量睡觉了位24地方";
    	source += "堵塞发哦风星座女4d674z4vz3vcwe4fads4f在vczs4wf6awe4的持续成长性娃儿额问问地速度发了多少了骄傲法律为哦热计量程序将垃圾发电量睡觉了位24地方";
    	source += "堵塞发哦风星座女4d674z4vz3vcwe4fads4f在vczs4wf6awe4的持续成长性娃儿额问问地速度发了多少了骄傲法律为哦热计量程序将垃圾发电量睡觉了位24地方";
    	source += "堵塞发哦风星座女4d674z4vz3vcwe4fads4f在vczs4wf6awe4的持续成长性娃儿额问问地速度发了多少了骄傲法律为哦热计量程序将垃圾发电量睡觉了位24地方";
    	
    	String privateKey = "下UI人声鼎沸124是打发454x6vcxzwer8z5xc45xcv45sd4r5e";
    	Key key = toKey(privateKey.getBytes());
    	
    	Date s = new Date();
    	System.out.println();
    	for (int i= 0; i<10000 ; i++){
    		source += i;
    		byte[] b = AES.encrypt(source.getBytes(), key);
    		AES.decrypt(b, key);
//    		System.out.println(i + "->原始："+source);
//    		System.out.println(i + "->AES加密："+AES.encrypt(source.getBytes(), key));
		}
    	System.out.println("start-time:"+ s + "end-time:"+new Date());
    	
    	
	}
    
    /*
     * 
     * 
     * 
     * 
     * public static void main(String [] args) {
	 String Algorithm="DES"; //定义 加密算法,可用 DES,DESede,Blowfish
	 KeyGenerator keygen;
	 try {
	  keygen = KeyGenerator.getInstance(Algorithm);
	  SecretKey deskey = keygen.generateKey(); 
	  Cipher ci = Cipher.getInstance("DES");
	  //将其包装为byte[]发送
	  byte[] b = ci.wrap(deskey);
	  //发送
	  //...
	  //接收到后
	  //解封装
	  Key key = ci.unwrap(b, Algorithm, Cipher.PRIVATE_KEY);
	  //...
	  
	 } catch (NoSuchAlgorithmException e) {
	  e.printStackTrace();
	 } catch (KeyException e) {
	  e.printStackTrace();
	 } catch (IllegalBlockSizeException e) {
	  e.printStackTrace();
	 } catch (NoSuchPaddingException e) {
	  e.printStackTrace();
	 } 
	 
	 }
     * 
     * 
     * 
     */
}
