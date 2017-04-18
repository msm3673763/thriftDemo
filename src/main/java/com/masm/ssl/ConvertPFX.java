package com.masm.ssl;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Enumeration;

/**
 * 
* @ClassName: ConvertPFX 
* @Description: JKS与PKCS12（PFX）互相转换 
* @author masm 
* @date 2017-4-18 下午4:56:16 
* @version V1.0
 */
public class ConvertPFX {      

	public static final String PKCS12 = "PKCS12";      
    public static final String JKS = "JKS";      
    public static final String PFX_KEYSTORE_FILE = "G:\\test.pfx";      
    public static final String KEYSTORE_PASSWORD = "123456";      
    public static final String JKS_KEYSTORE_FILE = "G:\\study\\ssl\\sm\\Server1Keystore.jks";      
    
    /**
     * 
    * @Title: coverTokeyStore 
    * @Description: PKCS12转换为JKS 
    * @param     入参
    * @return void    返回类型
    * @author masm 
    * @throws
    * @date 2017-4-18 下午4:56:51 
    * @version V1.0
     */
    public static void coverTokeyStore() {      
        try {      
            KeyStore inputKeyStore = KeyStore.getInstance("PKCS12");      
            FileInputStream fis = new FileInputStream(PFX_KEYSTORE_FILE);      
            char[] nPassword = null;      
            if ((KEYSTORE_PASSWORD == null)      
                    || KEYSTORE_PASSWORD.trim().equals("")) {      
                nPassword = null;      
            } else {      
                nPassword = KEYSTORE_PASSWORD.toCharArray();      
            }      
            inputKeyStore.load(fis, nPassword);      
            fis.close();      
            
            KeyStore outputKeyStore = KeyStore.getInstance("JKS");      
            outputKeyStore.load(null, KEYSTORE_PASSWORD.toCharArray());      
            Enumeration<String> enums = inputKeyStore.aliases();      
            while (enums.hasMoreElements()) { //we are read in just one certificate.        
                String keyAlias = (String) enums.nextElement();      
                System.out.println("alias=[" + keyAlias + "]");      
                if (inputKeyStore.isKeyEntry(keyAlias)) {      
                    Key key = inputKeyStore.getKey(keyAlias, nPassword);      
                    Certificate[] certChain = inputKeyStore      
                            .getCertificateChain(keyAlias);      
                    outputKeyStore.setKeyEntry(keyAlias, key, KEYSTORE_PASSWORD      
                            .toCharArray(), certChain);      
                }      
            }      
            FileOutputStream out = new FileOutputStream(JKS_KEYSTORE_FILE);      
            outputKeyStore.store(out, nPassword);      
            out.close();      
        } catch (Exception e) {      
            e.printStackTrace();      
        }      
    }    
    
    /**
     * 
    * @Title: coverToPfx 
    * @Description: JKS转换为PKCS12 
    * @param     入参
    * @return void    返回类型
    * @author masm 
    * @throws
    * @date 2017-4-18 下午4:57:26 
    * @version V1.0
     */
    public static void coverToPfx() {      
        try {      
            KeyStore inputKeyStore = KeyStore.getInstance("JKS");      
            FileInputStream fis = new FileInputStream(JKS_KEYSTORE_FILE);      
            char[] nPassword = null;      
            if ((KEYSTORE_PASSWORD == null)      
                    || KEYSTORE_PASSWORD.trim().equals("")) {      
                nPassword = null;      
            } else {      
                nPassword = KEYSTORE_PASSWORD.toCharArray();      
            }      
            inputKeyStore.load(fis, nPassword);      
            fis.close();      
           
            KeyStore outputKeyStore = KeyStore.getInstance("PKCS12");      
            outputKeyStore.load(null, KEYSTORE_PASSWORD.toCharArray());      
            Enumeration<String> enums = inputKeyStore.aliases();      
            while (enums.hasMoreElements()) { //we are read in just one certificate.        
                String keyAlias = (String) enums.nextElement();      
                System.out.println("alias=[" + keyAlias + "]");      
                if (inputKeyStore.isKeyEntry(keyAlias)) {      
                    Key key = inputKeyStore.getKey(keyAlias, nPassword);      
                    Certificate[] certChain = inputKeyStore      
                            .getCertificateChain(keyAlias);      
                    outputKeyStore.setKeyEntry(keyAlias, key, KEYSTORE_PASSWORD      
                            .toCharArray(), certChain);      
                }      
            }      
            FileOutputStream out = new FileOutputStream(PFX_KEYSTORE_FILE);      
            outputKeyStore.store(out, nPassword);      
            out.close();      
        } catch (Exception e) {      
            e.printStackTrace();      
        }      
    }      

    public static void main(String[] args) {      
        coverToPfx();      
    }      
}    
