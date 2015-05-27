package com.howoldareyou.app;

import java.io.ByteArrayOutputStream;

import org.json.JSONObject;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;

import android.graphics.Bitmap;
import android.util.Log;

public class FaceDetect {
	
	//������Ǹ�detect()����������Ҫ���ض������ֵ��
	//������ɹ��ͷ���һ��JSON�ַ�����������ʧ�ܾͷ���һ����ʾ��Ϣ��
	//������������ض������ֵ�����Կ��ǡ��ӿڡ�
	public interface CallBack {
		void success(JSONObject result);
		void error(FaceppParseException exception);
	}
	
	//���ڷ���ͼƬ�ķ���
	public static void detect(final Bitmap bitmap , final CallBack callBack) {
		//�����Ǻܺ�ʱ�ģ�����Ҫ��һ�����߳̽���
		new Thread(new Runnable() {
			@Override
			public void run() {
				//����request����
				//�������ĵ��������������Ƿ����й��������������ԣ����ֵ����������͵��ĸ�������Ϊtrue���ܻ�ø��ɿ�����������
				HttpRequests requests = new HttpRequests(Constant.KEY , Constant.SECRET , true , true);
				//��������bitmap����Ҫ��һ������
				Bitmap bmSmall = Bitmap.createBitmap(bitmap , 0 , 0 , bitmap.getWidth() , bitmap.getHeight());
				//��bitmapѹ�������������ȥ��Ȼ���ٰ���������ת�����ֽ�����
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				//ͨ��compress()�������Ͱ�bmSmallѹ������stream����
				bmSmall.compress(Bitmap.CompressFormat.JPEG , 100 , stream);
				//�ٰ�streamת�����ֽ�����
				byte[] arrays = stream.toByteArray();
				//���˶����Ƶ��ֽ�֮�󣬾Ϳ���ƴ��һ��������
				PostParameters params = new PostParameters();
				params.setImg(arrays);
				//����Ϳ���ͨ��������з�����
				try {
					JSONObject jsonObject = requests.detectionDetect(params);
					Log.d("TAG" , jsonObject.toString());
					if(callBack != null) {
						callBack.success(jsonObject);
					}
				} 
				catch (FaceppParseException e) {
					e.printStackTrace();
					if(callBack != null) {
						callBack.error(e);
					}
				}
			}//run()��������
		}).start();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
