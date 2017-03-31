package com.msm.thrift;

import org.apache.thrift.TException;

import com.msm.thrift.Hello.Iface;

public class HelloImpl implements Iface {

	public String helloString(String param) throws TException {
		System.out.println("hello " + param);
		return "hello " + param;
	}

	public int helloInt(int param) throws TException {
		System.out.println("hello " + param);
		return param + 1;
	}

	public boolean helloBoolean(boolean param) throws TException {
		System.out.println("hello " + param);
		return !param;
	}

	public void helloVoid() throws TException {
		System.out.println("hello void");
	}

	public String helloNull() throws TException {
		System.out.println("hello null");
		return "hello null";
	}

}
