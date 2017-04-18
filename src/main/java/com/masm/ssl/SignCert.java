package com.masm.ssl;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509CRLEntry;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import sun.security.x509.AlgorithmId;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateIssuerName;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateValidity;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;


public class SignCert {
	
    @SuppressWarnings({ "unchecked", "rawtypes", "null" })
	public static boolean verify(X509Certificate X509certificateRoot,  
	    Collection collectionX509CertificateChain, X509CRL X509crl, String stringTarget) {  
	  
    	//获取证书链长度  
	    int nSize = collectionX509CertificateChain.size();  
	    
	    //将证书链转化为数组  
	    X509Certificate[] arX509certificate = new X509Certificate[nSize];  
	    collectionX509CertificateChain.toArray(arX509certificate);  
	    
	    //声明list，存储证书链中证书主体信息  
	    ArrayList list = new ArrayList();  
	    
	    //沿证书链自上而下，验证证书的所有者是下一个证书的颁布者  
	    Principal principalLast = null;  
	    
	    for (int i=0;i<nSize;i++) {//遍历arX509certificate  
	    	X509Certificate x509Certificate = arX509certificate[i];  
	    	Principal principalIssuer = x509Certificate.getIssuerDN();//获取发布者标识 
	    	Principal principalSubject = x509Certificate.getSubjectDN();//获取证书的主体标识  
	    	list.add(x509Certificate.getSerialNumber());//保存证书的序列号  
	    	if (principalLast != null) {  
			    //验证证书的颁布者是上一个证书的所有者  
			    if (principalIssuer.equals(principalLast)) {  
			    	try{  
			    		//获取上个证书的公钥  
			    		PublicKey publickey = arX509certificate[i-1].getPublicKey();  
			    		//验证是否已使用与指定公钥相应的私钥签署了此证书   
			    		arX509certificate[i].verify(publickey);  
			    	}catch(Exception e){  
			    		return false;  
			    	}  
			    } else {  
			    	return false;  
			    }  
			}  
	    	principalLast = principalSubject;  
	    }  

	    //验证根证书是否在撤销列表中  
	    try {  
	    	if (!X509crl.getIssuerDN().equals(X509certificateRoot.getSubjectDN()))
	    		return false;  
	    	X509crl.verify(X509certificateRoot.getPublicKey());  
	    } catch(Exception e) {  
	    	return false;  
	    }  
	    
	    //在当前时间下，验证证书链中每个证书是否存在撤销列表中  
	    if (X509crl!=null) {  
	    	try {  
	    		//获取CRL中所有的项  
	    		Set setEntries = X509crl.getRevokedCertificates();  
	    		if (setEntries==null && !setEntries.isEmpty()) {  
	    			Iterator iterator = setEntries.iterator();  
	    			while (iterator.hasNext()) {  
	    				X509CRLEntry X509crlentry = (X509CRLEntry)iterator.next();  
	    				if (list.contains(X509crlentry.getSerialNumber()))
	    					return false;  
	    			}  
	    		}  
	    	} catch(Exception e) {  
	    		return false;  
	    	}  
	    }  
	    
	    //证明证书链中的第一个证书由用户所信任的CA颁布  
	    try {  
	    	PublicKey publickey = X509certificateRoot.getPublicKey();  
	    	arX509certificate[0].verify(publickey);  
	    } catch (Exception e) {  
	    	return false;  
	    }  
	    
	    //证明证书链中的最后一个证书的所有者正是现在通信对象  
	    Principal principalSubject = arX509certificate[nSize-1].getSubjectDN();  
	    if (!stringTarget.equals(principalSubject.getName()))
	    	return false;   
	    
	    //验证证书链里每个证书是否在有效期里  
	    Date date = new Date();  
	    for (int i=0;i<nSize;i++) {  
	    	try{  
	    		arX509certificate[i].checkValidity(date);  
	    	} catch(Exception e) {  
	    		return false;  
	    	}  
	    }  
	    return true;  
    }  
      
    public static boolean verifySign(X509Certificate X509certificateCA,String sign,String original){  
	    try{  
	    	//获得签名实例  
		    Signature signature=Signature.getInstance(X509certificateCA.getSigAlgName());  
		    //用证书公钥进行初始化  
		    signature.initVerify(X509certificateCA.getPublicKey());  
		    //更新源数据  
		    signature.update(original.getBytes());  
		    //验证数字签名  
		    return signature.verify(sign.getBytes());  
	    } catch(Exception e) {  
	    	return false;  
	    }  
    }  

	public static void main(String[] args) throws Exception {
		//从密钥库中读取CA的证书
		FileInputStream in = new FileInputStream("d:/ssl/server.keystore");
		KeyStore ks = KeyStore.getInstance("JKS");
		ks.load(in, "123456".toCharArray());
		Certificate c1 = ks.getCertificate("server");
		System.out.println(c1.toString());

		//从密钥库中读取CA的私钥
		PrivateKey caprk = (PrivateKey) ks.getKey("server", "123456".toCharArray());
		
		//从CA的证书中提取签发者的信息
		byte[] encod1 = c1.getEncoded();
		X509CertImpl cimpl1 = new X509CertImpl(encod1);
		X509CertInfo cinfo1 = (X509CertInfo) cimpl1.get(X509CertImpl.NAME + "." + X509CertImpl.INFO);
		X500Name issuer = (X500Name) cinfo1.get(X509CertInfo.SUBJECT + "." + CertificateIssuerName.DN_NAME);
		
		//获取待签发的证书
		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		FileInputStream fis = new FileInputStream("d:/ssl/client.cer");
		Certificate c2 = cf.generateCertificate(fis);
		
		//从待签发的证书中提取证书信息
		byte[] encod2 = c2.getEncoded();
		X509CertImpl cimpl2 = new X509CertImpl(encod2);
		X509CertInfo cinfo2 = (X509CertInfo)cimpl2.get(X509CertImpl.NAME + "." + X509CertImpl.INFO);
		
		//设置新证书有效期
		Date begindate=new Date(); 
		Date enddate=new Date(begindate.getTime()+3000*24*60*60*1000L);
		CertificateValidity cv=new CertificateValidity(begindate,enddate);
		cinfo2.set(X509CertInfo.VALIDITY,cv);
		
		//设置新证书序列号
		int sn=(int)(begindate.getTime()/1000);
		CertificateSerialNumber csn = new CertificateSerialNumber(sn);
		cinfo2.set(X509CertInfo.SERIAL_NUMBER,csn);
		
		//设置新证书签发者
		cinfo2.set(X509CertInfo.ISSUER+"."+CertificateIssuerName.DN_NAME,issuer);
		
		//设置新证书签名算法信息
		AlgorithmId algorithm=new AlgorithmId(AlgorithmId.md5WithRSAEncryption_oid);
		cinfo2.set(CertificateAlgorithmId.NAME+"."+CertificateAlgorithmId.ALGORITHM,algorithm);
		
		//创建证书并使用CA的私钥对其签名
		X509CertImpl newcert=new X509CertImpl(cinfo2);
		newcert.sign(caprk, "MD5WithRSA"); // 使用CA私钥对其签名
		
		//将新证书写入密钥库
		ks.setCertificateEntry("lf_signed",newcert);
		FileOutputStream out=new FileOutputStream("newstore");
		ks.store(out,"newpass".toCharArray());
		
	}
	
}
