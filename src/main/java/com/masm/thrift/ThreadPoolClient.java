package com.masm.thrift;

import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

/**
 * 
* @ClassName: ThreadPoolClient 
* @Description: 调用服务（阻塞式IO+多线程处理） 
* @author masm  
* @date 2017-3-31 下午4:04:12 
* @version V1.0
 */
public class ThreadPoolClient {

	public static void main(String[] args) throws Exception {
		//设置传输通道 -- 普通IO流通道
		TTransport transport = new TSocket("127.0.0.1", 1234);
		transport.open();
		
		//使用高密度二进制协议
		TProtocol protocol = new TCompactProtocol(transport);
		
		//创建Client
		Hello.Client client = new Hello.Client(protocol);
		
		long start = System.currentTimeMillis();
		for (int i=0;i<10000;i++) {
			client.helloBoolean(false);
			client.helloInt(0);
			client.helloNull();
			client.helloString("world");
			client.helloVoid();
		}
		System.out.println("耗时：" + (System.currentTimeMillis()-start));
		
		//关闭资源
		transport.close();
	}
}
