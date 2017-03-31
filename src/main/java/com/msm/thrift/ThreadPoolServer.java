package com.msm.thrift;

import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.server.TThreadPoolServer.Args;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;

import com.msm.thrift.Hello.Iface;

/**
 * 
* @ClassName: ThreadPoolServer 
* @Description: 发布服务（阻塞式IO+多线程处理） 
* @author masm  
* @date 2017-3-31 下午3:56:28 
* @version V1.0
 */
public class ThreadPoolServer {

	public static void main(String[] args) throws Exception {
		//设置传输通道，普通IO流通道
		TServerTransport serverTransport = new TServerSocket(1234);
		
		//使用高密度二进制协议
		TProtocolFactory factory = new TCompactProtocol.Factory();
		
		//设置处理器helloImpl
		TProcessor processor = new Hello.Processor<Iface>(new HelloImpl());
		
		//创建服务器
		Args a = new Args(serverTransport);
		a.processor(processor);
		a.protocolFactory(factory);
		TServer server = new TThreadPoolServer(a);
		System.out.println("start server on port 1234");
		server.serve();
	}
}
