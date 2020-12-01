//package com.ksum.otaupdater.Manager;
//
//import android.Manifest;
//import android.app.Activity;
//import android.app.AlertDialog;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.net.Uri;
//import android.os.Build;
//import android.provider.Settings;
//import android.util.Log;
//import android.view.View;
//import android.view.View.OnClickListener;
//
//import androidx.annotation.RequiresApi;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//
//import com.google.android.material.snackbar.Snackbar;
//import com.ksum.otaupdater.Log.Tag;
package com.ksum.otaupdater.Manager;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.ksum.otaupdater.Log.Tag;
import com.ksum.otaupdater.R;

import java.util.ArrayList;

public class PermissionManager {

    public static final int PERMISSION_DEFAULT_CODE = 1111;

    public static final String[] locationPermissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public static final String[] locationPermissions_upper_api29 = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
    };

    private final Context context;
    private final View snackBarView;

    private final String[] requestPermissions;
    private String[] deniedPermissions;
    private final String rationale;
    private final int permissionCode;
    private OnClickListener snackBarOnClick;
    private boolean isShowed;
    private boolean isRequestedOnSnack;
    private boolean isProcessDone;

    public static class Builder {
        // 필수 인자 => final 설정
        private Context context;
        private int permissionCode;
        private String[] requestPermissions;
        private View snackBarView;
        private String rationale;

        // 선택 인자 => 기본 값으로 초기화
        private boolean isRequestedOnSnack = false;
        private boolean isShowed = false;
        private boolean isProcessDone = false;

        public Builder setContext(Context value){
            context = value;
            return this;
        }

        public Builder setPermissionCode(int value){
            permissionCode = value;
            return this;
        }

        public Builder setRequestPermissions(String[] value){
            requestPermissions = value;
            return this;
        }

        public Builder setSnackBarView(View value){
            snackBarView = value;
            return this;
        }

        public Builder setRationale(String value){
            rationale = value;
            return this;
        }

        public PermissionManager build(){
            return new PermissionManager(this);
        }

    }

    private PermissionManager(Builder builder){
        this.context = builder.context;
        this.snackBarView = builder.snackBarView;
        this.requestPermissions = builder.requestPermissions;
        this.rationale = builder.rationale;
        this.permissionCode = builder.permissionCode;
        this.isRequestedOnSnack = false;
        this.isShowed = false;
        this.isProcessDone = false;
    }


    //    public PermissionManager(Context context, ArrayList<String> requestPermissions, View snackBarView){
    public PermissionManager(Context context, String[] requestPermissions, View snackBarView){
        this.context = context;
        this.snackBarView = snackBarView;
        this.requestPermissions = requestPermissions;
        this.permissionCode = PERMISSION_DEFAULT_CODE;
        this.rationale = context.getString(R.string.permission_default_rationale);
        this.isRequestedOnSnack = false;
        this.isShowed = false;
        this.isProcessDone = false;
    }

    // 이런걸 Builder 패턴으로 만드는건가?
//    public PermissionManager(Context context, int permissionCode, ArrayList<String> requestPermissions, View snackBarView, String rationale) {
    public PermissionManager(Context context, int permissionCode, String[] requestPermissions, View snackBarView, String rationale) {
        this.context = context;
        this.snackBarView = snackBarView;
        this.requestPermissions = requestPermissions;
        this.rationale = rationale;
        this.permissionCode = permissionCode;
        this.isRequestedOnSnack = false;
        this.isShowed = false;
        this.isProcessDone = false;
    }

    public void setSnackBarOnClick(OnClickListener snackBarOnClick){
        this.snackBarOnClick = snackBarOnClick;
    }

    /* Permission Check Methods --------------------------------------------------------------------------------------- */

    public boolean hasPermission(String permission){
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean hasPermissions(String[] permissions){
        int result;

        for(String permission : permissions){
            result = ContextCompat.checkSelfPermission(context, permission);
            if(result != PackageManager.PERMISSION_GRANTED)
                return false;
        }

        return true;
    }

    private boolean checkPermission(String[] permissionList){
        if(permissionList == null)
            return false;

        if(permissionList.length < 1)
            return false;

        deniedPermissions = getDeniedPermissionList(permissionList);
        return deniedPermissions.length <= 0;
    }

    private String[] getDeniedPermissionList(String[] permissionList){
        int result;
        ArrayList<String> deniedArrayList = new ArrayList<>();

        for(String permission : permissionList){
            result = ContextCompat.checkSelfPermission(context, permission);

            if(result != PackageManager.PERMISSION_GRANTED)
                deniedArrayList.add(permission);
        }

        return deniedArrayList.toArray(new String[0]);
    }

    private boolean isRequireRationale(String[] permissionList){
        for(String permission : permissionList){
            if(ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, permission))
                return true;
        }
        return false;
    }

    private void showRationaleWithRequest(String[] permissionList){
        if(isRequestedOnSnack)
            return;

        if(snackBarOnClick == null)
            snackBarOnClick = v -> requestPermissionList(permissionList, permissionCode);

        Snackbar.make(snackBarView, rationale, Snackbar.LENGTH_INDEFINITE).setAction("확인", snackBarOnClick).show();
        isRequestedOnSnack = true;
    }

    private void requestPermissionList(String[] permissionList, int permissionCode){
        ActivityCompat.requestPermissions((Activity) context, permissionList, permissionCode);
    }

    public void permissionCheckProcess(){

        Log.d(Tag.PERMISSION_MANAGER, "permissionCheckProcess!");

        // 1. 권한 승인 상태 체크
        if(checkPermission(requestPermissions)) {
            Log.d(Tag.PERMISSION_MANAGER, "checkPermission");
            processIsDone();
            return;
        }

        // 2. 권한 승인이 안되서 rationale 표시가 필요한게 있는지 체크
        if(isRequireRationale(deniedPermissions)){
            showRationaleWithRequest(deniedPermissions);
            Log.d(Tag.PERMISSION_MANAGER, "isRequireRationale");
            return;
        }

        if(deniedPermissions == null)
            return;

        // 3. 권한 승인이 안된 것들 요청
        requestPermissionList(deniedPermissions, permissionCode);
    }

    public void processIsDone(){
        isProcessDone = true;
        Log.d(Tag.PERMISSION_MANAGER, "processIsDone");
    }

    public boolean reRequestPermissionProcess(String[] permissionList, DialogInterface.OnClickListener negativeListener){

        Log.d(Tag.PERMISSION_MANAGER, "reRequestPermissionProcess!");

        if(isRequestedOnSnack == false){
            Log.d(Tag.PERMISSION_MANAGER, "showRationaleWithRequest!");
            showRationaleWithRequest(permissionList);
            return true;
        }

        if(isRequestedOnSnack && isShowed == false){
            Log.d(Tag.PERMISSION_MANAGER, "showPermissionDialog!");
            showPermissionDialog(negativeListener);
            return true;
        }

        return false;
    }

    public boolean isAllGranted(String[] permissions, int[] grantResults){
        for(int index = 0; index < grantResults.length; index++){
            if(grantResults[index] == PackageManager.PERMISSION_DENIED) {
                //Log.d(Tag.PERMISSION_MANAGER, "PERMISSION MANAGER : " + permissions[index] + " is denied!");
                return false;
            }
        }

        processIsDone();
        return true;
    }

    public void showPermissionDialog(DialogInterface.OnClickListener negativeListener){
        if(isShowed)
            return;


        Log.d(Tag.PERMISSION_MANAGER, "showPermissionDialog!!!!!!!!!");

        AlertDialog permissionDialog;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("승인되지 않은 권한 존재");
        builder.setMessage("권한이 승인되지 않으면 어플의 원활한 사용이 불가능합니다.\n권한 승인을 위해 앱 설정 화면으로 이동하시겠습니까?");
        builder.setPositiveButton("예", (dialog, which) -> {
            moveToPermissionSetting();
        });
        builder.setNegativeButton("아니오", negativeListener);
        permissionDialog = builder.create();
        permissionDialog.show();
        isShowed = true;
    }

    public void moveToPermissionSetting(){
        Intent appDetail = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:"+context.getPackageName()));
        appDetail.addCategory(Intent.CATEGORY_DEFAULT);
        appDetail.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(appDetail);
    }

    // OnResume 에서는 무조건 앱 내리고 권한 끄고 왔을때를 체크하기 위함
    public void permissionCheckOnResume(){
        Log.d(Tag.PERMISSION_MANAGER, "onResume permissionCheckOnResume");

        // check process 다 안끝남
        if(!isProcessDone){
            Log.d(Tag.PERMISSION_MANAGER, "onResume / is not Process Done!");
            return;
        }

        // permission 다 승인 됨
        if(checkPermission(requestPermissions)){
            Log.d(Tag.PERMISSION_MANAGER, "onResume checkPermission!");
            return;
        }

        // 승인 안된거 있음
        Log.d(Tag.PERMISSION_MANAGER, "onResume permission Check Start!");
        permissionCheckProcess();
    }

}