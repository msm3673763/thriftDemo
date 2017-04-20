package com.masm.ssl;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContexts;

import com.squareup.okhttp.OkHttpClient;

/**
 * 
* @ClassName: HttpsConnClient 
* @Description: 使用HttpsUrlConnection发送带客户端证书的请求，同时验证服务端证书 
* @author masm  
* @date 2017-4-13 下午6:13:06 
* @version V1.0
 */
public class HttpsConnClient {
	
	public static void main(String[] args) throws Exception {
		client1();
	}
	
	/**
	 * 
	* @Title: client1 
	* @Description: 第一种调用方式
	* @param @throws Exception    入参
	* @return void    返回类型
	* @author masm
	* @throws
	* @date 2017-4-17 下午3:35:37 
	* @version V1.0
	 */
	public static void client1() throws Exception {
		//忽略hostname验证（172.17.2.144）
		HttpsURLConnection.setDefaultHostnameVerifier(new HttpsConnClient().new NullHostNameVerifier());
		
		//初始化keystore
		KeyStore clientStore = KeyStore.getInstance("PKCS12");
        clientStore.load(new FileInputStream("D:/ssl/client.p12"),"654321".toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(clientStore, "654321".toCharArray());
        KeyManager[] kms = kmf.getKeyManagers();

        //初始化truststore
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(new FileInputStream("D:/ssl/server.keystore"), 
        		"123456".toCharArray());
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);
        TrustManager[] tms = tmf.getTrustManagers();

        SSLContext sslContext = null;
        sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kms, tms, new SecureRandom());
        
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        URL url = new URL("https://172.17.2.144:8443");
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.connect();
        // 取得该连接的输入流，以读取响应内容
 	    InputStreamReader insr = new InputStreamReader(con.getInputStream());
 	    // 读取服务器的响应内容并显示
 	    int respInt = insr.read();
 	    while (respInt != -1) {
 	    	System.out.print((char) respInt);
 	        respInt = insr.read();
 	    } 
        insr.close();
        con.disconnect();
	}
	
	/**
	 * 
	* @Title: client2 
	* @Description: 第二种 
	* @param @throws Exception    入参
	* @return void    返回类型
	* @author masm 
	* @throws
	* @date 2017-4-20 下午4:45:09 
	* @version V1.0
	 */
	public static void client2() throws Exception {
		//忽略hostname验证（172.17.2.144）
		HttpsURLConnection.setDefaultHostnameVerifier(new HttpsConnClient().new NullHostNameVerifier());
		
	    KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream(new File("D:/ssl/client.p12")), "654321".toCharArray());
	    SSLContext sslcontext = SSLContexts.custom()
	    		//忽略掉对服务器端证书的校验
//              .loadTrustMaterial(new TrustStrategy() {
//                  public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//                      return true;
//                  }
//              })
	    		
	    		//加载服务端提供的truststore(如果服务器提供truststore的话就不用忽略对服务器端证书的校验了)
//                .loadTrustMaterial(new File("D:/Program Files (x86)/Java/jdk1.7.0_45/jre/lib/security/cacerts"), 
//                		"changeit".toCharArray(), new TrustSelfSignedStrategy())
                .loadTrustMaterial(new File("D:/ssl/server.keystore"), 
                		"123456".toCharArray(), new TrustSelfSignedStrategy())
                .loadKeyMaterial(keyStore, "654321".toCharArray())
                .build();
	    
	    // 从上述SSLContext对象中得到SSLSocketFactory对象
	    SSLSocketFactory ssf = sslcontext.getSocketFactory();
	    // 创建URL对象
	    URL myURL = new URL("https://172.17.2.144:8443");
	    
	    OkHttpClient client = new OkHttpClient();
	    client.setSslSocketFactory(ssf);
	    
	    // 创建HttpsURLConnection对象，并设置其SSLSocketFactory对象
	    HttpsURLConnection httpsConn = (HttpsURLConnection) myURL.openConnection();
	    httpsConn.setSSLSocketFactory(ssf);
	    // 取得该连接的输入流，以读取响应内容
	    InputStreamReader insr = new InputStreamReader(httpsConn.getInputStream());
	    // 读取服务器的响应内容并显示
	    int respInt = insr.read();
	    while (respInt != -1) {
	        System.out.print((char) respInt);
	        respInt = insr.read();
	    } 
	}
	
	public class NullHostNameVerifier implements HostnameVerifier {
        /*
         * (non-Javadoc)
         * 
         * @see javax.net.ssl.HostnameVerifier#verify(java.lang.String,
         * javax.net.ssl.SSLSession)
         */
        public boolean verify(String arg0, SSLSession arg1) {
            return true;
        }
    }
}
