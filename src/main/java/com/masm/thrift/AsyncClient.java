package com.masm.thrift;

import org.apache.thrift.TException;
import org.apache.thrift.async.AsyncMethodCallback;
import org.apache.thrift.async.TAsyncClientManager;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TNonblockingSocket;
import org.apache.thrift.transport.TNonblockingTransport;

/**
 * 
* @ClassName: AsyncClient 
* @Description: 调用非阻塞IO服务，异步 
* @author masm
* @date 2017-3-31 下午4:29:37 
* @version V1.0
 */
public class AsyncClient {

	public static void main(String[] args) throws Exception {
		//异步调用管理器
		TAsyncClientManager manager = new TAsyncClientManager();
		
		//设置传输通道，调用非阻塞IO
		final TNonblockingTransport transport = new TNonblockingSocket("localhost", 1234);
		
		//设置协议
		TProtocolFactory factory = new TCompactProtocol.Factory();
		
		//创建client
		final Hello.AsyncClient client = new Hello.AsyncClient(factory, manager, transport);
		
		//调用服务
		System.out.println("开始：" + System.currentTimeMillis());
		client.helloBoolean(false, new AsyncMethodCallback<Boolean>() {

			public void onComplete(Boolean arg0) {
				System.out.println("完成1：" + System.currentTimeMillis());
				try {
					client.helloBoolean(false, new AsyncMethodCallback<Boolean>() {

						public void onComplete(Boolean arg0) {
							System.out.println("完成2：" + System.currentTimeMillis());
						}

						public void onError(Exception arg0) {
							System.out.println("错误2：" + System.currentTimeMillis());
						}
					});
				} catch (TException e) {
					e.printStackTrace();
				}
			}

			public void onError(Exception arg0) {
				System.out.println("错误1：" + System.currentTimeMillis()); 
			}
		});
		System.out.println("结束：" + System.currentTimeMillis());
		Thread.sleep(5000);
	}
}
