package com.masm.ssl;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class GenerateCommand {
	private static String storepass = "logiscn";
	private static String baseDir = "D:/downloads/";
	private static String myIP = "localhost";
	
	public static void executeBat() throws Exception {
		List<String> list = new ArrayList<String>();
		Process process = Runtime.getRuntime().exec(baseDir + "command.bat");
		InputStream is = process.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line = null;
		while ((line=br.readLine()) != null) {
			list.add(line);
			if (line.contains("echo success")) {
				break;
			}
		}
		for (String str : list) {
			System.out.println(str);
		}
		br.close();
		is.close();
	}

	public static void main(String[] args) throws Exception {
		generateBat();
	}

	public static void generateBat() throws Exception {
		String filePath = baseDir + "command.bat";
		String cerDir = baseDir + "cert/";
		String p12Dir = baseDir + "p12/";
		String xmlFile = baseDir + "config.xml";
		String inputFile = "myint.inf";
		generateInputFile(baseDir + inputFile);
		generateXML(xmlFile);
		FileWriter fout = new FileWriter(filePath);
		PrintWriter writer = new PrintWriter(fout);
		StringBuffer sb = new StringBuffer();
		sb.append("cd d:/downloads");
		sb.append("\n\r");
		sb.append("mkdir cert");
		sb.append("\n\r");
		sb.append("mkdir p12");
		sb.append("\n\r");

		//生成服务端证书
		String serverCommand = "keytool -genkey -v -alias tomcat"
				+ " -keyalg RSA -keystore " + baseDir
				+ "tomcat.keystore -dname \"CN=" + myIP + ",OU=logiscn,"
				+ "O=logis,L=beijing,ST=beijing,C=CN\""
				+ " -validity 3650 -storepass " + storepass + " -keypass "
				+ storepass;
		sb.append(serverCommand);
		sb.append("\n\r");

		String namesStr = "tianli,wanghong,liuyu";
		String[] names = namesStr.split(",");

		for (String name : names) {
			//生成客户端证书
			String clientCert = "keytool -genkey -v -alias "
					+ name
					+ " -keyalg "
					+ "RSA -storetype PKCS12 -keystore "
					+ p12Dir
					+ name
					+ ".p12 -dname"
					+ " \"CN="
					+ name
					+ ",OU=logiscn,O=logis,L=beijing,ST=beijing,C=CN\" -validity 3650 -storepass "
					+ name + " -keypass " + name + "\"";
			sb.append(clientCert);
			sb.append("\n\r");

			//导出客户端证书
			String trustCert = "keytool -export -alias " + name
					+ " -keystore " + p12Dir + name
					+ ".p12 -storetype PKCS12 -storepass " + name
					+ " -rfc -file " + cerDir + name + ".cer";
			sb.append(trustCert);
			sb.append("\n\r");
			
			//将客户端证书导入服务端
			String impStr = "keytool -import -alias " + name + " -v -file "
					+ cerDir + name + ".cer -keystore " + baseDir
					+ "tomcat.keystore -storepass " + storepass + " <"
					+ inputFile;
			sb.append(impStr);
			sb.append("\n\r");
		}
		sb.append("echo success");
		writer.println(sb.toString());
		writer.println();
		writer.println();
		writer.flush();
		writer.close();
		fout.close();
		System.out.println("文件已经生成：" + filePath);
	}

	public static void outputStr(String str, PrintWriter writer)
			throws Exception {
		writer.println(str);
		writer.println();
	}

	public static void generateXML(String path) throws Exception {
		FileWriter fout = new FileWriter(path);
		PrintWriter writer = new PrintWriter(fout);
		StringBuffer sb = new StringBuffer();
		sb.append("把下面的代码覆盖到{tomcat.home}/config/server.xml中相应的位置，\r\n");
		sb.append("并使得可以使用.\r\n");
		sb.append("重新启动tomcat即可!\r\n");
		sb.append("把所有的8443字符替换为443,则可以访问默认的ssl\r\n");
		sb
				.append("<Connector port=\"443\" protocol=\"HTTP/1.1\" SSLEnabled=\"true\"\r\n");
		sb.append("\tmaxThreads=\"150\" scheme=\"https\" secure=\"true\"\r\n");
		sb.append("\tclientAuth=\"true\" sslProtocol=\"TLS\"\r\n");
		sb.append("\tkeystoreFile=\"" + baseDir
				+ "tomcat.keystore\" \r\n\t keystorePass=\"" + storepass
				+ "\"\r\n");
		sb.append("\ttruststoreFile=\"" + baseDir
				+ "tomcat.keystore\" \r\n\t truststorePass=\"" + storepass
				+ "\"/>");
		writer.println(sb.toString());
		System.out.println("生成配置文件：" + path);
		writer.flush();
		writer.close();
		fout.close();
	}

	public static void generateInputFile(String filePath) throws Exception {
		FileWriter fout = new FileWriter(filePath);
		PrintWriter writer = new PrintWriter(fout);
		StringBuffer sb = new StringBuffer();
		sb.append("y\r\n");
		writer.print(sb.toString());
		System.out.println("生成配置文件：" + filePath);
		writer.flush();
		writer.close();
		fout.close();
	}
}