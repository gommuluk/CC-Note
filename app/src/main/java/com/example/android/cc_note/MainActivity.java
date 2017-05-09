package com.example.android.cc_note;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button btnTakePicture;
    private ImageView ivCapture;
    private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnTakePicture = (Button) findViewById(R.id.btntakePicture);
        ivCapture = (ImageView) findViewById(R.id.ivPicture);

        btnTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isExistsCameraApplication()){
                    Intent cameraApp = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File picture = savePictureFile();

                    if(picture != null){
                        cameraApp.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(picture));

//                        startActivityForResult(cameraApp, 10000);
                        setResult(10000, cameraApp);
                        finish();

                    }
                }

            }
        });
    }

    private boolean isExistsCameraApplication(){

        PackageManager packageManager = getPackageManager();
        Intent cameraApp = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> cameraApps = packageManager.queryIntentActivities(cameraApp, PackageManager.MATCH_DEFAULT_ONLY);

        return cameraApps.size() > 0;
    }

    private File savePictureFile(){
        PermissionRequester.Builder requester = new PermissionRequester.Builder(this);

        int result = requester.create()
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE, 20000, new PermissionRequester.OnClickDenyButtonListener(){
                    @Override
                    public void onClick(Activity activity){

                    }
                });

        if(result == PermissionRequester.ALREADY_GRANTED || result == PermissionRequester.REQUEST_PERMISSION){
            //파일 이름 설정
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = "IMG_" + timestamp;

            //파일 저장 위치 설정
            File pictureStorage = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MYAPP/");

            //저장될 폴더가 없으면 생성
            if( !pictureStorage.exists() ){
                pictureStorage.mkdirs();
            }

            try{
                File file = File.createTempFile(fileName, ".jpg", pictureStorage);

                // 사진 파일 절대 경로 가져옴
                imagePath = file.getAbsolutePath();

                //찍힌 사진을 갤러리에 저장
                Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                File f = new File (imagePath);
                Uri contentUri = Uri.fromFile(f);
                mediaScanIntent.setData(contentUri);
                this.sendBroadcast(mediaScanIntent);

                return file;
            } catch(Exception e){
                e.printStackTrace();
            }
        }
        return null;
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode == 10000 && resultCode == RESULT_OK){
//            String key = data.getStringExtra("key");


            BitmapFactory.Options factory = new BitmapFactory.Options();
            factory.inJustDecodeBounds = true;

            BitmapFactory.decodeFile(imagePath);

            factory.inJustDecodeBounds = false;
            factory.inPurgeable = true;

            Bitmap bitmap = BitmapFactory.decodeFile(imagePath, factory);
            ivCapture.setImageBitmap(bitmap);
        }
    }
}
