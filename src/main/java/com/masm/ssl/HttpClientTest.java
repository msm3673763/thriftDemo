package com.masm.ssl;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

/**
 * 
* @ClassName: HttpClientTest 
* @Description: 使用httpClient发送带客户端证书的请求，对服务器端证书的校验 
* @author masm  
* @date 2017-4-13 下午5:22:06 
* @version V1.0
 */
public class HttpClientTest {

	public static void main(String[] args) throws Exception {
		httpClient();
	}

	/**
	 * 
	* @Title: client 
	* @Description: 使用httpclient发送带客户端证书的请求，对服务器端证书的校验  
	* @param @throws KeyStoreException
	* @param @throws IOException
	* @param @throws NoSuchAlgorithmException
	* @param @throws CertificateException
	* @param @throws FileNotFoundException
	* @param @throws KeyManagementException
	* @param @throws UnrecoverableKeyException
	* @param @throws ClientProtocolException    入参
	* @return void    返回类型
	* @author masm 
	* @throws
	* @date 2017-4-17 下午4:03:56 
	* @version V1.0
	 */
	public static void httpClient() throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException,
			FileNotFoundException, KeyManagementException, UnrecoverableKeyException, ClientProtocolException {
		KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream(new File("D:/ssl/client.p12")), "654321".toCharArray());
        SSLContext sslcontext = SSLContexts.custom()
                //忽略掉对服务器端证书的校验
//                .loadTrustMaterial(new TrustStrategy() {
//                    public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//                        return true;
//                    }
//                })
                
                //加载服务端提供的truststore(如果服务器提供truststore的话就不用忽略对服务器端证书的校验了)
//        		.loadTrustMaterial(new File("D:/Program Files (x86)/Java/jdk1.7.0_45/jre/lib/security/cacerts"), 
//                		"changeit".toCharArray(), new TrustSelfSignedStrategy())
                .loadTrustMaterial(new File("D:/ssl/server.keystore"), "123456".toCharArray(),
                        new TrustSelfSignedStrategy())
                .loadKeyMaterial(keyStore, "654321".toCharArray())
                .build();
        
        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
                sslcontext,
                new String[]{"TLSv1"},
                null,
                SSLConnectionSocketFactory.getDefaultHostnameVerifier());
        CloseableHttpClient httpclient = HttpClients.custom()
                .setSSLSocketFactory(sslConnectionSocketFactory)
                .build();
        try {

            HttpGet httpget = new HttpGet("https://localhost:8443/");
            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
                HttpEntity entity = response.getEntity();
                InputStreamReader insr = new InputStreamReader(entity.getContent());

        		//读取服务器的响应内容并显示
        		int respInt = insr.read();
        		while( respInt != -1){
        			System.out.print((char)respInt);
        			respInt = insr.read();
        		}
                EntityUtils.consume(entity);
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
	}
	
	/**
	 * 
	* @Title: getInfoByP12 
	* @Description: 获取p12证书的信息
	* @param @return
	* @param @throws Exception    入参
	* @return X509Certificate    返回类型
	* @author masm
	* @throws
	* @date 2017-4-17 下午3:31:59 
	* @version V1.0
	 */
	public static X509Certificate getInfoByP12() throws Exception {
		KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream(new File("D:/ssl/client.p12")), "654321".toCharArray());
        Enumeration<String> aliases = keyStore.aliases();
        String keyAlias = null;
        X509Certificate x509Certificate = null;
        while (aliases.hasMoreElements()) {
        	keyAlias = aliases.nextElement();
        	System.out.println("alias = [" + keyAlias + "]");
        	Certificate c = keyStore.getCertificate(keyAlias);
        	System.out.println(c.toString());
        	x509Certificate = (X509Certificate) c;
    		System.out.println("版本号 " + x509Certificate.getVersion());  
    	    System.out.println("序列号 " + x509Certificate.getSerialNumber().toString(16));  
    	    System.out.println("全名 " + x509Certificate.getSubjectDN());  
    	    System.out.println("签发者全名" + x509Certificate.getIssuerDN());  
    	    System.out.println("有效期起始日 " + x509Certificate.getNotBefore());  
    	    System.out.println("有效期截至日 " + x509Certificate.getNotAfter());  
    	    System.out.println("签名算法 " + x509Certificate.getSigAlgName());  
    	    byte[] sig = x509Certificate.getSignature();  
    	    System.out.println("签名" + new BigInteger(sig).toString(16));  
        }
        return x509Certificate;
	}
	
	/**
	 * 
	* @Title: getInfoByCert 
	* @Description: 获取cert证书信息 
	* @param @return
	* @param @throws Exception    入参
	* @return X509Certificate    返回类型
	* @author masm 
	* @throws
	* @date 2017-4-17 下午3:32:35 
	* @version V1.0
	 */
	public static X509Certificate getInfoByCert() throws Exception {
		CertificateFactory factory = CertificateFactory.getInstance("X.509");
		FileInputStream fis = new FileInputStream("d:/ssl/client.cer");
		Certificate certificate = factory.generateCertificate(fis);
		String str = certificate.toString();
		System.out.println(str);
		
		X509Certificate x509Certificate = (X509Certificate) certificate;
		System.out.println("版本号 " + x509Certificate.getVersion());  
	    System.out.println("序列号 " + x509Certificate.getSerialNumber().toString(16));  
	    System.out.println("全名 " + x509Certificate.getSubjectDN());  
	    System.out.println("签发者全名" + x509Certificate.getIssuerDN());  
	    System.out.println("有效期起始日 " + x509Certificate.getNotBefore());  
	    System.out.println("有效期截至日 " + x509Certificate.getNotAfter());  
	    System.out.println("签名算法 " + x509Certificate.getSigAlgName());  
	    byte[] sig = x509Certificate.getSignature();  
	    System.out.println("签名" + new BigInteger(sig).toString(16)); 
	    return x509Certificate;
	}
}
