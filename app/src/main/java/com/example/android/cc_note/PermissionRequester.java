package com.example.android.cc_note;

import android.accessibilityservice.GestureDescription;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.UiAutomation;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.webkit.PermissionRequest;

public class PermissionRequester {

    public static final int NOT_SUPPORT_VERSION = 2;
    public static final int ALREADY_GRANTED = -1; // 이미 요청권한을 가진 경우
    public static final int REQUEST_PERMISSION = 0; // 권한 요청 결과

    private Activity context;
    private Builder builder;

    private void setBuilder(Builder builder){
        this.builder = builder;
    }
    public PermissionRequester(Activity context){
        this.context = context;
    }
    public int request(final String permission, final int requestCode, final OnClickDenyButtonListener denyAction){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            int permissionCheck = ContextCompat.checkSelfPermission(context, permission);
            if(permissionCheck == PackageManager.PERMISSION_DENIED){
                if(context.shouldShowRequestPermissionRationale(permission)){
                    AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                    dialog.setTitle(builder.getTitle())
                            .setMessage(builder.getMessage())
                            .setPositiveButton(builder.getPositiveButtonName(), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                                        context.requestPermissions(new String[]{permission}, requestCode);
                                    }
                                }
                            })
                            .setNegativeButton(builder.getNegativeButtonName(), new DialogInterface.OnClickListener(){
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    denyAction.onClick(context);
                                }
                            }).create().show();
                    return REQUEST_PERMISSION;
                }
                else{
                    context.requestPermissions(new String[] {permission}, requestCode);
                    return REQUEST_PERMISSION;
                }
            }
            else{
                return ALREADY_GRANTED;
            }
        }

        return NOT_SUPPORT_VERSION;
    }

    public interface OnClickDenyButtonListener{
        public void onClick(Activity activity);
    }

    public static class Builder {
        private PermissionRequester requester;

        public Builder(Activity context) {
            requester = new PermissionRequester(context);
        }

        private String title = "권한 요청";
        private String message = "기능의 사용을 위해 권한이 필요합니다.";
        private String positiveButtonName = "네";
        private String negativeButtonName = "아니요";

        public String getTitle() {
            return title;
        }

        public Builder setTitle(String title){
            this.title = title;
            return this;
        }

        public String getMessage(){
            return message;
        }

        public Builder setMessage(String message){
            this.message = message;
            return this;
        }

        public String getPositiveButtonName(){
            return positiveButtonName;
        }

        public Builder setPositiveButtonName(String positiveButtonName){
            this.positiveButtonName = positiveButtonName;
            return this;
        }

        public String getNegativeButtonName(){
            return negativeButtonName;
        }

        public Builder setNegativeButtonName(String negativeButtonName){
            this.negativeButtonName = negativeButtonName;
            return this;
        }
        public PermissionRequester create(){
            this.requester.setBuilder(this);
            return this.requester;
        }
    }
}