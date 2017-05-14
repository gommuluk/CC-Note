package com.example.android.cc_note;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int REQUEST_TAKE_PHOTO = 2;
    static final int REQUEST_IMAGE_CROP = 3;

    private Button btnTakePicture, btnGetAlbum;
    private ImageView ivCapture;
    Uri photoURI = null, albumURI = null;
    private String imagePath;
    Boolean album = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnTakePicture = (Button) findViewById(R.id.btntakePicture);
        ivCapture = (ImageView) findViewById(R.id.ivPicture);
        btnGetAlbum = (Button) findViewById(R.id.btnGetAlbum);

        btnTakePicture.setOnClickListener(this);
        btnGetAlbum.setOnClickListener(this);
    }

    public void dispatchTakePictureIntent(){
        Intent cameraApp = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(cameraApp.resolveActivity(getPackageManager()) != null){
            File photoFile = null;
            try{
                photoFile = createImageFile();
            } catch(IOException ex){
                Toast.makeText(getApplicationContext(), "createImageFile Fail", Toast.LENGTH_SHORT).show();
            }

            if(photoFile != null){
                Toast.makeText(getApplicationContext(), imagePath, Toast.LENGTH_SHORT).show();
                imagePath = photoFile.getAbsolutePath();
                cameraApp.putExtra(MediaStore.EXTRA_OUTPUT, imagePath);
                this.setResult(RESULT_OK, cameraApp);
                startActivityForResult(cameraApp, REQUEST_IMAGE_CAPTURE) ;

            }
        }
    }

    private File createImageFile() throws IOException{
        SimpleDateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd-HH-mm-ss", Locale.KOREA );
        Date currentTime = new Date ( );
        String imageFileName = "ccnote_" + formatter.format (currentTime) + ".jpg";

        File storageDir = new File(Environment.getExternalStorageDirectory(), imageFileName);
        imagePath = storageDir.getAbsolutePath();
        return storageDir;
    }

    private void doTakeAlbumAction(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        this.setResult(RESULT_OK, intent);
        startActivityForResult(intent, REQUEST_TAKE_PHOTO);
    }


    private void cropImage(){
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.setDataAndType(photoURI, "image/*");
        cropIntent.putExtra("scale", true);

        if(album == false){
            cropIntent.putExtra("output", photoURI);
        }
        else if (album == true){
            cropIntent.putExtra("output", albumURI);
        }
        this.setResult(RESULT_OK, cropIntent);
        startActivityForResult(cropIntent, REQUEST_IMAGE_CROP);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != RESULT_OK){
            Toast.makeText(getApplicationContext(), "onActivityResult : RESULT_NOT_OK - " + requestCode, Toast.LENGTH_SHORT).show();
        }
        else{

            switch(requestCode){
                case REQUEST_TAKE_PHOTO:
                    Toast.makeText(getApplicationContext(), "onActivityResult : 1", Toast.LENGTH_SHORT).show();
                    album = true;
                    File albumFile = null;
                    try{
                        albumFile = createImageFile();
                    } catch(IOException e){
                        e.printStackTrace();
                    }
                    if(albumFile != null){
                        albumURI = Uri.fromFile(albumFile);
                    }
                    photoURI = data.getData();

                case REQUEST_IMAGE_CAPTURE:
                    Toast.makeText(getApplicationContext(), "onActivityResult : 2", Toast.LENGTH_SHORT).show();
                    cropImage();
                    break;

                case REQUEST_IMAGE_CROP:
                    Toast.makeText(getApplicationContext(), "onActivityResult : 3", Toast.LENGTH_SHORT).show();
                    Bitmap photo = BitmapFactory.decodeFile(photoURI.getPath());
                    ivCapture.setImageBitmap(photo);

                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    if(album == false){
                        mediaScanIntent.setData(photoURI);
                    }
                    else if(album == true){
                        album = false;
                        mediaScanIntent.setData(albumURI);
                    }
                    this.sendBroadcast(mediaScanIntent);
                    break;
            }
        }
    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case (R.id.btntakePicture):
                dispatchTakePictureIntent();
                break;
            case (R.id.btnGetAlbum):
                doTakeAlbumAction();
                break;
        }
    }
}