/**
 * 
 */
package com.ewfresh.pay.util.bill99quick;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * <p>Title:mip</p>
 * <p>Description:</p>
 * <p>Copyright:</p>
 * <p>Company: </p>
 * @author panqr
 * @version 
 */
public class PropFile {
	private static Properties props = null;
	
	private PropFile(){
		
	}
	
	public static synchronized Properties getProps(String propFile){
		if (props == null){
			props = new Properties();
			InputStream in = null;
			try{
				in=PropFile.class.getResourceAsStream(propFile);
				props.load(in);
				return props;
			} catch (IOException e) {
				System.out.println("��ȡ�����ļ�ʧ��");
				e.printStackTrace();
			}finally{
				if (in != null){
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}else{
			return props;
		}
		return null;
	}

}
