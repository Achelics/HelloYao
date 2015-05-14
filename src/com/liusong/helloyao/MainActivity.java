package com.liusong.helloyao;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private SensorManager sensorManager;
	private Vibrator vibrator;
	private static final String TAG = "MainActivity";
	private static final int SENSOR_SHAKE = 20;
	
	
	/*ʵ�ִ�������*/
	private static final int REQUEST_TIMEOUT = 5*1000;//��������ʱ10����    
	private static final int SO_TIMEOUT = 10*1000;  //���õȴ����ݳ�ʱʱ��10����    
	private String responseMsg = "";  //���ض�Ӧ������
	private String yao;
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //��ȡ����
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }
    
    @Override
	protected void onResume() {
		super.onResume();
		if (sensorManager != null) {// ע�������
			sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
			// ��һ��������Listener���ڶ������������ô��������ͣ�����������ֵ��ȡ��������Ϣ��Ƶ��
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (sensorManager != null) {// ȡ��������
			sensorManager.unregisterListener(sensorEventListener);
		}
	}

	/**
	 * ������Ӧ����
	 */
	private SensorEventListener sensorEventListener = new SensorEventListener() {

		@Override
		public void onSensorChanged(SensorEvent event) {
			// ��������Ϣ�ı�ʱִ�и÷���
			float[] values = event.values;
			float x = values[0]; // x�᷽����������ٶȣ�����Ϊ��
			float y = values[1]; // y�᷽����������ٶȣ���ǰΪ��
			float z = values[2]; // z�᷽����������ٶȣ�����Ϊ��
			Log.i(TAG, "x�᷽����������ٶ�" + x +  "��y�᷽����������ٶ�" + y +  "��z�᷽����������ٶ�" + z);
			// һ����������������������ٶȴﵽ40�ʹﵽ��ҡ���ֻ���״̬��
			int medumValue = 20;// ���� i9250��ô�ζ����ᳬ��20��û�취��ֻ����19��
			if (Math.abs(x) > medumValue || Math.abs(y) > medumValue || Math.abs(z) > medumValue) {
				vibrator.vibrate(200);
				Thread loginThread = new Thread(new LoginThread());  
				loginThread.start(); 
				
			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}
	};


	 //LoginThread�߳���  
	  class LoginThread implements Runnable  {

		@Override
		public void run() {
			yao = "1";
			 boolean loginValidate = loginServer(yao);
			 Message msg = handler.obtainMessage();  
			 if(loginValidate){ 
				 if(responseMsg.equals("1")){  
					 msg.what = 0;  
					 handler.sendMessage(msg);
				 }
			 }
		}
		  
	  }
	  
	  	/*Handler*/  
		Handler handler = new Handler(){  
				public void handleMessage(Message msg){  
					switch(msg.what)  {  
					case 0:  
						Toast.makeText(getApplicationContext(), "��֮���Ѻ�", 2000).show(); 
						break;
					default:
						break;
					}
				}
		};
		
		//��ʼ��HttpClient�������ó�ʱ  
		public HttpClient getHttpClient(){  
			BasicHttpParams httpParams = new BasicHttpParams();  
			HttpConnectionParams.setConnectionTimeout(httpParams, REQUEST_TIMEOUT);  
			HttpConnectionParams.setSoTimeout(httpParams, SO_TIMEOUT);  
			HttpClient client = new DefaultHttpClient(httpParams);  
			return client;  
		}  
		
		//�ύpsot����
		@SuppressWarnings("unchecked")
		private boolean loginServer(String yao) {  
			boolean loginValidate = false;
			//ʹ��HTTP�ͻ���ʵ��  
			String urlStr = "http://202.207.240.147:8089/get_lzq/get_lzq.php";  
			HttpPost request = new HttpPost(urlStr);  
			//������ݲ�����Ļ������ԶԴ��ݵĲ������з�װ  
			@SuppressWarnings("rawtypes")
			List params = new ArrayList();  
			//����û���������  
			params.add(new BasicNameValuePair("yao",yao));  
			try  {  
				//�������������  
				request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));  
				HttpClient client = getHttpClient();  
				//ִ�����󷵻���Ӧ  
				HttpResponse response = client.execute(request);  
				//�ж��Ƿ�����ɹ�  
				if(response.getStatusLine().getStatusCode()==200) {  
					loginValidate = true;  
					/*�����Ӧ��Ϣ */ 
					responseMsg = EntityUtils.toString(response.getEntity());  
					/*��½�ɹ�*/
					if (loginValidate) {
						/*�ͷ�����*/
						client.getConnectionManager().closeExpiredConnections();
						client.getConnectionManager().shutdown();
					} 
				}
			}catch(Exception e) {  
				e.printStackTrace();  
			}  
			return loginValidate;  
		} 
	
	
	

}
