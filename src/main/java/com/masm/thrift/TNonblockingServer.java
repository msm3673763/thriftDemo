package com.masm.thrift;

import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.server.TThreadedSelectorServer.Args;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TTransportFactory;

import com.masm.thrift.Hello.Iface;

/**
 * 
* @ClassName: TNonblockingServer 
* @Description: 非阻塞IO（NIO）服务端 
* @author masm  
* @date 2017-3-31 下午4:11:17 
* @version V1.0
 */
public class TNonblockingServer {

	public static void main(String[] args) throws Exception {
		//传输通道 - 非阻塞方式
		TNonblockingServerTransport transport = new TNonblockingServerSocket(1234);
		
		//异步IO，需要使用TFramedTransport，它将分块缓存读取
		TTransportFactory factory = new TFramedTransport.Factory();
		
		//使用高密度二进制协议
		TProtocolFactory proFactory = new TCompactProtocol.Factory();
		
		//设置处理器 helloImpl
		TProcessor processor = new Hello.Processor<Iface>(new HelloImpl());
		
		//创建服务器
		Args a = new Args(transport);
		a.processor(processor);
		a.protocolFactory(proFactory);
		a.transportFactory(factory);
		
		TServer server = new TThreadedSelectorServer(a);
		
		System.out.println("start server on port 1234");
		server.serve();
	}
}
