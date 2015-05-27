package com.howoldareyou.app;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facepp.error.FaceppParseException;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {
	//����startActivityForResult()�������룬�������һ������0x110
	private static final int PIC_CODE = 0x110;
	
	//��ʾUI�ؼ��ı���
	private ImageView mPhoto = null;
	private Button mGetImage = null;
	private Button mDetect = null;
	private TextView mTip = null;
	private View mWaitting = null;
	
	//��ʾ��ǰ��ʹ�õ�ͼƬ��·��
	private String mCurrentPhotoStr = null;
	
	//���ڴ洢����ѹ�����Bitmap����
	private Bitmap mPhotoImg = null;
	
	//����һ�����ʣ����ڻ��ƾ��ο�
	private Paint mPaint = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//��ɸ���UI�ؼ��ĳ�ʼ��
		initViews();
		
		//��ɲ���UI�ؼ����ü������ĳ�ʼ��
		initEvents();
		
		//��ɻ��ʵĳ�ʼ��
		mPaint = new Paint();
		
	}

	private void initEvents() {
		mGetImage.setOnClickListener(this);
		mDetect.setOnClickListener(this);
	}
	
	//������������������ͼƬ����֮���ǳɹ����ǳ���
	private static final int MSG_SUCCESS = 0x111;
	private static final int MSG_ERROR = 0x112;
	
	//���ڱ������߳̿���UI���첽����
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case MSG_SUCCESS:
				mWaitting.setVisibility(View.GONE);
				JSONObject rs = (JSONObject) msg.obj;
				prepareRsBitmap(rs);
				mPhoto.setImageBitmap(mPhotoImg);
				break;
			case MSG_ERROR:
				mWaitting.setVisibility(View.GONE);
				String errorMsg = (String) msg.obj;
				if(TextUtils.isEmpty(errorMsg)) {
					mTip.setText("ȷ���Ƿ�����.");
				}
				else {
					mTip.setText(errorMsg);
				}
				break;
			}
			super.handleMessage(msg);
		};
	};
	
	
	private void prepareRsBitmap(JSONObject rs) {
		//��Ϊ����������ԭͼ���滭�ģ��������bitmapȡ��һ������Ʒ������Canvas������
		Bitmap bitmap = Bitmap.createBitmap(mPhotoImg.getWidth() , mPhotoImg.getHeight() , mPhotoImg.getConfig());
		Canvas canvas = new Canvas(bitmap);
		canvas.drawBitmap(mPhotoImg , 0 , 0 , null);
		
		try {
			JSONArray faces = rs.getJSONArray("face");
			
			//��¼��⵽�˶�������
			int faceCount = faces.length();
			mTip.setText("������" + faceCount + "����.");
			
			for(int i=0;i<faceCount;i++) {
				//�õ�������face����
				JSONObject face = faces.getJSONObject(i);
				//�������ǽ���face��������ԣ���Ϊ����ҪΧ������һ�����Σ�
				//�����Ҫ����������Ͻǵ���ʼ����������Ŀ�Ⱥ͸߶ȣ���������ȷ��һ������
				JSONObject positionObj = face.getJSONObject("position");
				float x = (float) positionObj.getJSONObject("center").getDouble("x");
				float y = (float) positionObj.getJSONObject("center").getDouble("y");
				float width = (float) positionObj.getDouble("width");
				float height = (float) positionObj.getDouble("height");
				//face++���ص���ЩJSON���ݶ��ǰٷֱȣ���Ҫת��Ϊʵ�����ص�λ�ú����ʹ��
				x = x / 100 * bitmap.getWidth();
				y = y / 100 * bitmap.getHeight();
				width = width / 100 * bitmap.getWidth();
				height = height / 100 * bitmap.getHeight();
				//���û��ʵ�����
				mPaint.setColor(Color.WHITE);
				mPaint.setStrokeWidth(3);
				//��ʼ������
				canvas.drawLine(x - width / 2 , y - height / 2 , x - width / 2 , y + height / 2 , mPaint);
				canvas.drawLine(x - width / 2 , y + height / 2 , x + width / 2 , y + height / 2 , mPaint);
				canvas.drawLine(x + width / 2 , y + height / 2 , x + width / 2 , y - height / 2 , mPaint);
				canvas.drawLine(x + width / 2 , y - height / 2 , x - width / 2 , y - height / 2 , mPaint);
				//���ͼ����������Ա������
				JSONObject attributeObj = face.getJSONObject("attribute");
				String sex = attributeObj.getJSONObject("gender").getString("value");
				int age = attributeObj.getJSONObject("age").getInt("value");
				//������������ʾ�Ա����������ݣ�ֱ����TextView����ʡ�ܶ��£�
				//���ݱ���background��һ��9patchͼƬ��drawableLeft���Ա�����text������
				//���ڵ������ǣ���һ��TextViewת����Bitmap�������ڵ�ǰ�Ļ�����
				Bitmap ageBitmap = buildAgeBitmap(age , "Male".equals(sex));
				//������Ϊ����������ͼ�εĴ�С����һ����г�ı���
				int ageWidth = ageBitmap.getWidth();
				int ageHeight = ageBitmap.getHeight();
				if(bitmap.getWidth() < mPhoto.getWidth() && bitmap.getHeight() < mPhoto.getHeight()) {
					float ratio = Math.max(bitmap.getWidth() * 1.0f / mPhoto.getWidth() , bitmap.getHeight() * 1.0f / mPhoto.getHeight());
					ageBitmap = Bitmap.createScaledBitmap(ageBitmap, (int)(ageWidth * ratio), (int)(ageHeight * ratio) , false);
				}
				canvas.drawBitmap(ageBitmap , x - ageBitmap.getWidth() / 2 , y - height / 2 - ageBitmap.getHeight() , null);
				//�ѻ����λͼ���¸����Ǹ�����
				mPhotoImg = bitmap;
			}
			
		} 
		catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	private Bitmap buildAgeBitmap(int age, boolean isMale) {
		TextView tv = (TextView) findViewById(R.id.id_ageAndSex);
		tv.setText(" " + age + " ");
		if(isMale == true) {
			tv.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.male) , null , null , null);
		}
		else {
			tv.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.female) , null , null , null);
		}
		tv.setDrawingCacheEnabled(true);
		Bitmap bitmap = Bitmap.createBitmap(tv.getDrawingCache());
		tv.destroyDrawingCache();
		return bitmap;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.id_getImage:
			//������ʵ�ֻ�ȡͼƬ
			Intent intent = new Intent(Intent.ACTION_PICK);
			intent.setType("image/*");
			startActivityForResult(intent, PIC_CODE);
			break;
		case R.id.id_detect:
			//�ý�������ʾ����
			mWaitting.setVisibility(View.VISIBLE);
			
			//Ϊ�˱����쳣���������ж�һ���û��Ƿ�ѡ������Ƭ
			if(mCurrentPhotoStr != null && mCurrentPhotoStr.trim().equals("") == false) {
				//����û��Ѿ�ѡ������Ƭ��
				resizePhoto();
			}
			else {
				//����û���û��ѡ��ͼƬ�������Ϳ�ʼ�����ˣ��Ͳ���Ĭ��ͼƬ
				mPhotoImg = BitmapFactory.decodeResource(getResources() , R.drawable.t4);
			}
			
			FaceDetect.detect(mPhotoImg, new FaceDetect.CallBack() {
				@Override
				public void success(JSONObject result) {
					Message msg = Message.obtain();
					msg.what = MSG_SUCCESS;
					msg.obj = result;
					mHandler.sendMessage(msg);
				}
				
				@Override
				public void error(FaceppParseException exception) {
					Message msg = Message.obtain();
					msg.what = MSG_ERROR;
					msg.obj = exception.getErrorMessage();
					mHandler.sendMessage(msg);
				}
			});
			break;
		}
	}
	


	/*
	 * �����õ���startActivityForResult()����ô�϶�����һ������Ҫ�Դ�������Ӧ
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		//����Ǵ����ȡͼƬ���Ǹ������룬�ͽ�һ��ִ����һ������
		if(requestCode == PIC_CODE) {
			//���ÿ���Intent�е������ǲ��ǿյ�
			if(intent != null) {
				Uri uri = intent.getData();
				Cursor cursor = getContentResolver().query(uri, null, null, null, null);
				cursor.moveToFirst();
				int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
				//�������õ���һ���ؼ������ݣ�ͼƬ��·��
				mCurrentPhotoStr = cursor.getString(idx);
				cursor.close();
				//�������·��֮�󣬾Ϳ���ȥ���·��ȥ��ȡͼƬ
				//��������ͨ��һ����Ƭ��һ��Ķ��������Ǻü�ʮMB����Face++��SDK�Դ������ƣ�Ҫ����Ƭת��Ϊ����������
				//����ܳ���3M�������Ҫ��Ҫ��ȡ��ͼƬ����һ��ѹ���������Զ�����һ��resizePhoto()����ȥѹ����Ƭ
				resizePhoto();
				//ѹ����֮���������Ļ����ʾ������
				mPhoto.setImageBitmap(mPhotoImg);
				mTip.setText("���Կ�ʼ������==>");
			}
		}
		super.onActivityResult(requestCode, resultCode, intent);
	}

	/*
	 * ������ѹ������Ҫע��������ǣ�����ѹ�����ͼƬ���ܳ���3M ����
	 */
	private void resizePhoto() {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(mCurrentPhotoStr , options);
		
		double ratio = Math.max(options.outWidth * 1.0d / 1024f , options.outHeight * 1.0d / 1024f);
		options.inSampleSize = (int) Math.ceil(ratio);
		options.inJustDecodeBounds = false;
		mPhotoImg = BitmapFactory.decodeFile(mCurrentPhotoStr , options);
	}

	private void initViews() {
		mPhoto = (ImageView) findViewById(R.id.id_photo);
		mGetImage = (Button) findViewById(R.id.id_getImage);
		mDetect = (Button) findViewById(R.id.id_detect);
		mTip = (TextView) findViewById(R.id.id_tip);
		mWaitting = findViewById(R.id.id_watting);
	}



}
