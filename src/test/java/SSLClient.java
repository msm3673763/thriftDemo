import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.security.KeyStore;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
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
* @ClassName: SSLClient 
* @Description: 使用httpclient发送带客户端证书的请求，对服务器端证书的校验 
* @author masm  
* @date 2017-4-13 下午5:22:06 
* @version V1.0
 */
public class SSLClient {

	public static void main(String[] args) throws Exception {
		KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream(new File("D:/keys/client.p12")), "654321".toCharArray());
        SSLContext sslcontext = SSLContexts.custom()
                //忽略掉对服务器端证书的校验
//                .loadTrustMaterial(new TrustStrategy() {
//                    public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//                        return true;
//                    }
//                })
                
                //加载服务端提供的truststore(如果服务器提供truststore的话就不用忽略对服务器端证书的校验了)
                .loadTrustMaterial(new File("D:/keys/tomcat.keystore"), "123456".toCharArray(),
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

            HttpGet httpget = new HttpGet("https://172.17.2.144:8443");
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
}
