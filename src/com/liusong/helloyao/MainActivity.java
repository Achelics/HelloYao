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
	
	
	/*实现传递数据*/
	private static final int REQUEST_TIMEOUT = 5*1000;//设置请求超时10秒钟    
	private static final int SO_TIMEOUT = 10*1000;  //设置等待数据超时时间10秒钟    
	private String responseMsg = "";  //返回对应的数据
	private String yao;
	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //获取监听
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }
    
    @Override
	protected void onResume() {
		super.onResume();
		if (sensorManager != null) {// 注册监听器
			sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
			// 第一个参数是Listener，第二个参数是所得传感器类型，第三个参数值获取传感器信息的频率
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (sensorManager != null) {// 取消监听器
			sensorManager.unregisterListener(sensorEventListener);
		}
	}

	/**
	 * 重力感应监听
	 */
	private SensorEventListener sensorEventListener = new SensorEventListener() {

		@Override
		public void onSensorChanged(SensorEvent event) {
			// 传感器信息改变时执行该方法
			float[] values = event.values;
			float x = values[0]; // x轴方向的重力加速度，向右为正
			float y = values[1]; // y轴方向的重力加速度，向前为正
			float z = values[2]; // z轴方向的重力加速度，向上为正
			Log.i(TAG, "x轴方向的重力加速度" + x +  "；y轴方向的重力加速度" + y +  "；z轴方向的重力加速度" + z);
			// 一般在这三个方向的重力加速度达到40就达到了摇晃手机的状态。
			int medumValue = 20;// 三星 i9250怎么晃都不会超过20，没办法，只设置19了
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


	 //LoginThread线程类  
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
						Toast.makeText(getApplicationContext(), "蓝之青已好", 2000).show(); 
						break;
					default:
						break;
					}
				}
		};
		
		//初始化HttpClient，并设置超时  
		public HttpClient getHttpClient(){  
			BasicHttpParams httpParams = new BasicHttpParams();  
			HttpConnectionParams.setConnectionTimeout(httpParams, REQUEST_TIMEOUT);  
			HttpConnectionParams.setSoTimeout(httpParams, SO_TIMEOUT);  
			HttpClient client = new DefaultHttpClient(httpParams);  
			return client;  
		}  
		
		//提交psot请求
		@SuppressWarnings("unchecked")
		private boolean loginServer(String yao) {  
			boolean loginValidate = false;
			//使用HTTP客户端实现  
			String urlStr = "http://202.207.240.147:8089/get_lzq/get_lzq.php";  
			HttpPost request = new HttpPost(urlStr);  
			//如果传递参数多的话，可以对传递的参数进行封装  
			@SuppressWarnings("rawtypes")
			List params = new ArrayList();  
			//添加用户名和密码  
			params.add(new BasicNameValuePair("yao",yao));  
			try  {  
				//设置请求参数项  
				request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));  
				HttpClient client = getHttpClient();  
				//执行请求返回相应  
				HttpResponse response = client.execute(request);  
				//判断是否请求成功  
				if(response.getStatusLine().getStatusCode()==200) {  
					loginValidate = true;  
					/*获得响应信息 */ 
					responseMsg = EntityUtils.toString(response.getEntity());  
					/*登陆成功*/
					if (loginValidate) {
						/*释放连接*/
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
