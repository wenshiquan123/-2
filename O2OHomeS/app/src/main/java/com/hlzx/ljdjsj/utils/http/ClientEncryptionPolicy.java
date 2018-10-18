package com.hlzx.ljdjsj.utils.http;

import android.util.Base64;


import com.hlzx.ljdjsj.MyApplication;
import com.hlzx.ljdjsj.utils.LogUtil;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Date;

import javax.crypto.Cipher;

/**
 * @author Carter
 * @version 创建时间：2015-5-25 下午03:16:44
 * @Modified By:Administrator
 * Version: 1.0
 * jdk : 1.6
 * 类说明：
 */

public class ClientEncryptionPolicy {
    private static ClientEncryptionPolicy instance;
    private PublicKey publicKey;
    private static final int MAX_ENCRYPT_BLOCK = 117;
    private static String aesKey;
    private static String aesIV;
    private static CryptLib _crypt;


    private ClientEncryptionPolicy() throws Exception {
        publicKey = readPublicKey();
        _crypt = new CryptLib();
    }

    public static ClientEncryptionPolicy getInstance() throws Exception {
        if (instance == null) {
            instance = new ClientEncryptionPolicy();
        }
        return instance;
    }

    /**
     * 生成随机 Key 默认128位
     *
     * @param length
     * @return
     */
    public static String generateRandom(Integer length) {
        if (length == null)
            length = 128;
        SecureRandom secureRandom = new SecureRandom();
        byte seedBytes[] = secureRandom.generateSeed(length);
        return byteToHex(seedBytes);
    }

    /**
     * 加密握手数据
     *
     * @param driverId
     * @return String[] index0=数据校验值 index1=加密后的数据
     * @throws Exception
     */
    public String[] encryptShakeHandsData(String driverId) throws Exception {
        ClientEncryptionPolicy.aesKey = CryptLib.SHA256(ClientEncryptionPolicy.generateRandom(null), 32);
        ClientEncryptionPolicy.aesIV = CryptLib.generateRandomIV(16);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("device_code", driverId);
        jsonObject.put("aes_key", aesKey);
        jsonObject.put("aes_iv", aesIV);
        LogUtil.e("ME","aesIV="+aesIV);
        jsonObject.put("timestamp", new Date().getTime());

        //使用公钥加密
        byte[] data = encryptByPublicKey(jsonObject.toString().getBytes("utf-8"), null);
        //压缩数据
//		data = GZipUtils.compress(data);TODO kjlkjlasdjf
        //使用base64编码以便在网络上进行传输

        String str = Base64.encodeToString(data, Base64.DEFAULT);

        return new String[]{getSha1Code(jsonObject.toString()), str};
    }

    /**
     * 加密(请自行在content中添加时间戳)
     *
     * @param content
     * @throws Exception
     */
    public String encrypt(String content) throws Exception {

        //生成新的iv
        ClientEncryptionPolicy.aesIV = generateNewIV();

        //使用base64编码以便在网络上进行传输
        /*BASE64Encoder encoder = new BASE64Encoder();
        byte[] data = _crypt.encrypt(content, aesKey, aesIV).getBytes("utf-8");
		//压缩密文
		data = GZipUtils.compress(data);
		
		return encoder.encode(data);*/
        return _crypt.encrypt(content, aesKey, aesIV);
    }

    /**
     * 加密(此方法会自动在数据中加上时间戳)
     *
     * @throws Exception
     */
    public String encrypt(JSONObject jsonObject) throws Exception {

        //生成新的iv
        ClientEncryptionPolicy.aesIV = generateNewIV();
        if(aesKey==null)
        {
             aesKey=MyApplication.getInstance().getAesKey();
        }

        jsonObject.put("timestamp", new Date().getTime());
		
		/*//使用base64编码以便在网络上进行传输
		BASE64Encoder encoder = new BASE64Encoder();
		
		byte[] data = _crypt.encrypt(jsonObject.toString(), aesKey, aesIV).getBytes("utf-8");
		//压缩密文
		data = GZipUtils.compress(data);
		
		return encoder.encode(data);*/

        return _crypt.encrypt(jsonObject.toString(), aesKey, aesIV);
    }

    /**
     * 解密
     *
     * @param encryptedContent
     * @return
     * @throws Exception
     */
    public String decrypt(String encryptedContent, String iv) throws Exception {
		//解密base64
		/*BASE64Decoder decoder = new BASE64Decoder();
		byte[] data = decoder.decodeBuffer(encryptedContent);
		//解压缩密文
		data = GZipUtils.decompress(data);
		
		data = _crypt.decrypt(new String(data, "utf-8"), aesKey, aesIV).getBytes("utf-8");
		
		return new String(data, "utf-8");*/

        /*wenshqiuan 解密步骤
        *
        * 1.Base64解密
        * 2.decrypt解密
        */
        //String urlDecode= URLDecoder.decode(encryptedContent);
        //byte[] baseDecode=Base64.decode(encryptedContent,0);
        //String cryptData=new String(baseDecode,"utf-8");

        return _crypt.decrypt(encryptedContent, aesKey, iv);
    }

    /**
     * 客户端调用此方法使用证书公钥加密
     *
     * @param data
     * @return
     * @throws Exception
     */
    private byte[] encryptByPublicKey(byte[] data, SecureRandom secureRandom) throws Exception {
        //APP端的默认RSA加密规则与服务端不一致,这里必须指定不能使用证书中的
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        if (secureRandom == null) {
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        } else {
            cipher.init(Cipher.ENCRYPT_MODE, publicKey, secureRandom);
        }

        //分段加密
        int inputLen = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段加密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(data, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_ENCRYPT_BLOCK;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();
        return encryptedData;
    }

    /**
     * 生成新的iv
     * @return
     * @throws Exception
     */
    private String generateNewIV() throws Exception
    {
        ClientEncryptionPolicy.aesIV = CryptLib.generateRandomIV(16);
        return ClientEncryptionPolicy.aesIV;
    }

    /**
     * 获取当前的加密iv
     * @return
     */
    public String getIV()
    {
        return ClientEncryptionPolicy.aesIV;
    }

    public void setIV(String aseIV)
    {
        ClientEncryptionPolicy.aesIV=aseIV;
    }

    public static String getAesKey() {
        return aesKey;
    }

    public static void setAesKey(String aesKey) {
        ClientEncryptionPolicy.aesKey = aesKey;
    }

    /**
     * 读取公钥
     *
     * @return
     * @throws Exception
     */
    private static PublicKey readPublicKey() throws Exception {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        InputStream inputStream = MyApplication.getInstance().getResources().getAssets().open("public_key.der");
        Certificate certificate = factory.generateCertificate(inputStream);
        return certificate.getPublicKey();
    }

    /**
     * 获取sha1摘要
     *
     * @param string
     * @return
     */
    private static String getSha1Code(String string) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(string.getBytes());
            byte messageDigest[] = digest.digest();
            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            // 字节数组转换为 十六进制 数
            for (int i = 0; i < messageDigest.length; i++) {
                String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
                if (shaHex.length() < 2) {
                    hexString.append(0);
                }
                hexString.append(shaHex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 字节与16进制字符串转换
     *
     * @param b
     * @return
     */
    private static String byteToHex(byte[] b) {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = (Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs.toUpperCase();
    }

    public static void main(String args[]) throws Exception {
        String[] data = ClientEncryptionPolicy.getInstance().encryptShakeHandsData("7897879");
        System.out.println(data[0]);
        System.out.println(URLEncoder.encode(data[1], "utf-8"));
		
		/*JSONObject jsonObject = new JSONObject();
		jsonObject.put("target_api","http://localhost:8080/O2O_API/e/v/shop/getShopInfoForEdit");
		jsonObject.put("user_id", "47527");
		jsonObject.put("store_id", "549");
		
		String edata = URLEncoder.encode(ClientEncryptionPolicy.getInstance().encrypt(jsonObject.toString()), "utf-8");
		
		System.out.println(edata);*/
        System.out.println("---------------------------------------");

        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("target_api", "http://localhost:8080/O2O_API/e/v/personal/smgr/base_info");
        jsonObject1.put("uid", "0");

        String edata1 = URLEncoder.encode(ClientEncryptionPolicy.getInstance().encrypt(jsonObject1), "utf-8");

        System.out.println(edata1);
        System.out.println("-------------------------------------------");

        //System.out.println(ClientEncryptionPolicy.getInstance().decrypt(URLDecoder.decode(edata1, "utf-8")));
		
		/*try 
		{
	    	String output= "";
	    	String plainText = "This is the text to be encrypted.";
	    	String key = CryptLib.SHA256("my secret key", 32); //32 bytes = 256 bit
	    	String iv = CryptLib.generateRandomIV(16); //16 bytes = 128 bit
	    	output = _crypt.encrypt(plainText, key, iv); //encrypt
	    	System.out.println("encrypted text=" + output);
	    	output = _crypt.decrypt(output, key, iv); //decrypt
	    	System.out.println("decrypted text=" + output);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/


    }

}


















