import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyStore;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;


public class HttpsClientTest {

	public static void main(String[] args) throws Exception {
		//忽略hostname验证（172.17.2.144）
		HttpsURLConnection.setDefaultHostnameVerifier(new HttpsClientTest().new NullHostNameVerifier());
        
		SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new SecureRandom());
       
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        URL url = new URL("https://172.17.2.144:8443/");
       
        // 打开restful链接
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("POST");// POST GET PUT DELETE
        
        // 设置访问提交模式，表单提交
        conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
        conn.setConnectTimeout(130000);// 连接超时 单位毫秒
        conn.setReadTimeout(130000);// 读取超时 单位毫秒
        
        //取得该连接的输入流，以读取响应内容
		InputStreamReader insr = new InputStreamReader(conn.getInputStream());
        
		//读取服务器的响应内容并显示
		int respInt = 0;
		while((respInt=insr.read()) != -1){
			System.out.print((char)respInt);
		}
	}
	
	static TrustManager[] trustAllCerts = new TrustManager[] { 
		new X509TrustManager() {
	        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	        	System.out.println(1);
	        }
	
	        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	        	for (X509Certificate x509Certificate : chain) {
	        		System.out.println("输出证书信息:/n" + x509Certificate.toString());
	        	    System.out.println("版本号:" + x509Certificate.getVersion());
	        	    System.out.println("序列号:" + x509Certificate.getSerialNumber().toString(16));
	        	    System.out.println("主体名：" + x509Certificate.getSubjectDN());
	        	    System.out.println("签发者：" + x509Certificate.getIssuerDN());
	        	    System.out.println("有效期：" + x509Certificate.getNotBefore());
	        	    System.out.println("签名算法：" + x509Certificate.getSigAlgName());
	        	    PublicKey pk = x509Certificate.getPublicKey();
	        	    byte [] pkenc = pk.getEncoded();  
	        	    System.out.println("公钥");
	        	    for (int i=0;i<pkenc.length;i++) {
	        	    	System.out.print(pkenc[i] + ",");
	        	    }
	        		
	        	}
	        	System.out.println(0);
	        }
	
	        public X509Certificate[] getAcceptedIssuers() {
	            return null;
	        }
	    } 
	};

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

class MyX509TrustManager implements X509TrustManager {
	
	X509TrustManager sunJSSEX509TrustManager;
	
	MyX509TrustManager() throws Exception {
	    // create a "default" JSSE X509TrustManager.
		KeyStore ks = KeyStore.getInstance("JKS");
	    ks.load(new FileInputStream("d:/keys/tomcat1.keystore"), "123456".toCharArray());
	    TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509", "SunJSSE");
	    tmf.init(ks);
	    TrustManager tms [] = tmf.getTrustManagers();

	    for (int i=0; i<tms.length; i++) {
	        if (tms[i] instanceof X509TrustManager) {
	            sunJSSEX509TrustManager = (X509TrustManager) tms[i];
	            return;
	        }
	    }
	    throw new Exception("Couldn't initialize");
	}

	public void checkClientTrusted(X509Certificate[] chain, String authType)
	            throws CertificateException {
	    try {
	        sunJSSEX509TrustManager.checkClientTrusted(chain, authType);
	    } catch (CertificateException excep) {
	    }
	}

	public void checkServerTrusted(X509Certificate[] chain, String authType)
	            throws CertificateException {
	    try {
	        sunJSSEX509TrustManager.checkServerTrusted(chain, authType);
	    } catch (CertificateException excep) {
	    }
	}
    
	public X509Certificate[] getAcceptedIssuers() {
        return sunJSSEX509TrustManager.getAcceptedIssuers();
    }
}
