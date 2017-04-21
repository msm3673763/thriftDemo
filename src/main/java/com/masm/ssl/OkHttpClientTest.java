package com.masm.ssl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContexts;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

/**
 * 
* @ClassName: OkHttpClientTest 
* @Description: 使用OkHttpClient发送带客户端证书的请求，对服务器端证书的校验  
* @author masm  
* @date 2017-4-20 下午4:29:32 
* @version V1.0
 */
public class OkHttpClientTest {
	
	public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
	private static OkHttpClient client;
	
	/**
	 * 
	* @Title: init1 
	* @Description: 加载密钥库和信任库，初始化okhttpclient（第一种）
	* @param @throws Exception    入参
	* @return void    返回类型
	* @author masm
	* @throws
	* @date 2017-4-21 下午2:37:43 
	* @version V1.0
	 */
	public void init1() throws Exception {
		//初始化keystore
		KeyStore keyStore = KeyStore.getInstance("JKS");
//		keyStore.load(new FileInputStream("D:/ssl/server.keystore"), "123456".toCharArray());
		keyStore.load(new FileInputStream("D:/ssl/openssl/clientTrustStore"), "123456".toCharArray());
		TrustManagerFactory trustManagerFactory = TrustManagerFactory.
				getInstance(TrustManagerFactory.getDefaultAlgorithm());
		trustManagerFactory.init(keyStore);

		//初始化truststore
		KeyStore clientKeyStore = KeyStore.getInstance("PKCS12");
		clientKeyStore.load(new FileInputStream(new File("D:/ssl/openssl/client.p12")), "123456".toCharArray());
		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		keyManagerFactory.init(clientKeyStore, "123456".toCharArray());
		
		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());
		
		//初始化okhttpclient
		client = new OkHttpClient();
		client.setSslSocketFactory(sslContext.getSocketFactory());
//		client.setHostnameVerifier(new OkHttpClientTest().new NullHostNameVerifier());//忽略hostname的验证
	}

	/**
	 * 
	* @Title: init2 
	* @Description: 加载密钥库和信任库，初始化okhttpclient（第二种方式）
	* @param @throws Exception    入参
	* @return void    返回类型
	* @author masm 
	* @throws
	* @date 2017-4-20 下午3:56:27 
	* @version V1.0
	 */
	public void init2() throws Exception {
		KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream(new File("D:/ssl/openssl/client.p12")), "123456".toCharArray());
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
                .loadTrustMaterial(new File("D:/ssl/openssl/serverTrustStore"), "123456".toCharArray(),
                        new TrustSelfSignedStrategy())
                .loadKeyMaterial(keyStore, "123456".toCharArray())
                .build();
        
        SSLSocketFactory ssf = sslcontext.getSocketFactory();
        //初始化okhttpclient
        client = new OkHttpClient();
        client.setSslSocketFactory(ssf);
//        client.setHostnameVerifier(new OkHttpClientTest().new NullHostNameVerifier());//忽略hostname的验证
	}
	
	/**
	 * 
	* @Title: httpGet 
	* @Description: httpGet请求 
	* @param @throws Exception    入参
	* @return void    返回类型
	* @author masm
	* @throws
	* @date 2017-4-20 下午3:55:02 
	* @version V1.0
	 */
	public void httpGet() throws Exception {
		Request request = new Request.Builder().url("https://localhost:8443").build();
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
        	ResponseBody body = response.body();
        	System.out.println(body.string());
        	System.out.println(response.message());
        }
	}
	
	/**
	 * 
	* @Title: httpPost 
	* @Description: httpPost提交json数据 
	* @param @throws Exception    入参
	* @return void    返回类型
	* @author masm 
	* @throws
	* @date 2017-4-20 下午3:54:44 
	* @version V1.0
	 */
	public void httpPostForJson() throws Exception {
		RequestBody body = RequestBody.create(JSON, "json数据");
	    Request request = new Request.Builder()
	      .url("https://localhost:8443")
	      .post(body)
	      .build();
	    Response response = client.newCall(request).execute();
	    if (response.isSuccessful()) {
	        System.out.println(response.body().string());
	    } else {
	        throw new IOException("Unexpected code " + response);
	    }
	}

	/**
	 * 
	* @Title: httpPostForkeyVal 
	* @Description: post提交键值对
	* @param     入参
	* @return void    返回类型
	* @author masm
	* @throws
	* @date 2017-4-20 下午3:55:46 
	* @version V1.0
	 * @throws Exception 
	 */
	public void httpPostForkeyVal() throws Exception {
		RequestBody formBody = new FormEncodingBuilder()
	    .add("platform", "android")
	    .add("name", "bug")
	    .add("subject", "XXXXXXXXXXXXXXX")
	    .build();

	    Request request = new Request.Builder()
	      .url("https://localhost:8443")
	      .post(formBody)
	      .build();
	 
	    Response response = client.newCall(request).execute();
	    if (response.isSuccessful()) {
	        System.out.println(response.body().string());
	    } else {
	        throw new IOException("Unexpected code " + response);
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
	
	public static void main(String[] args) throws Exception {
		OkHttpClientTest test = new OkHttpClientTest();
		test.init1();
		test.httpPostForJson();
	}
}
