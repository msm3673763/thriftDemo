import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

public class HttpsClient {

	public static void main(String[] args) throws Exception {
		//忽略hostname验证（172.17.2.144）
		HttpsURLConnection.setDefaultHostnameVerifier(new HttpsClient().new NullHostNameVerifier());
		System.setProperty("javax.net.ssl.keyStore", "D:\\keys\\tomcat1.keystore");
		System.setProperty("javax.net.ssl.keyStorePassword", "123456"); 
		System.setProperty("javax.net.ssl.trustStore", "D:\\keys\\tomcat1.keystore");
//		System.setProperty("javax.net.ssl.trustStorePassword", "123456"); 
		URL reqURL = new URL("https://172.17.2.144:8443/" ); //创建URL对象
		HttpsURLConnection httpsConn = (HttpsURLConnection)reqURL.openConnection();

		/*下面这段代码实现向Web页面发送数据，实现与网页的交互访问
		httpsConn.setDoOutput(true);
		OutputStreamWriter out = new OutputStreamWriter(huc.getOutputStream(), "8859_1");
		out.write( "……" );
		out.flush();
		out.close();
		*/

		//取得该连接的输入流，以读取响应内容
		InputStreamReader insr = new InputStreamReader(httpsConn.getInputStream());

		//读取服务器的响应内容并显示
		int respInt = insr.read();
		while( respInt != -1){
			System.out.print((char)respInt);
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
