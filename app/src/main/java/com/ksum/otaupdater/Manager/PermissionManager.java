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
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

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

//    public PermissionManager(Context context, ArrayList<String> requestPermissions, View snackBarView){
    public PermissionManager(Context context, String[] requestPermissions, View snackBarView){
        this.context = context;
        this.snackBarView = snackBarView;
        this.requestPermissions = requestPermissions;
        this.permissionCode = PERMISSION_DEFAULT_CODE;
        this.rationale = context.getString(R.string.permission_default_rationale);
    }

    // 이런걸 Builder 패턴으로 만드는건가?
//    public PermissionManager(Context context, int permissionCode, ArrayList<String> requestPermissions, View snackBarView, String rationale) {
    public PermissionManager(Context context, int permissionCode, String[] requestPermissions, View snackBarView, String rationale) {
        this.context = context;
        this.snackBarView = snackBarView;
        this.requestPermissions = requestPermissions;
        this.rationale = rationale;
        this.permissionCode = permissionCode;
    }

    public void setSnackBarOnClick(OnClickListener snackBarOnClick){
        this.snackBarOnClick = snackBarOnClick;
    }

    /* Permission Check Methods --------------------------------------------------------------------------------------- */

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
        if(snackBarOnClick == null)
            snackBarOnClick = v -> requestPermissionList(permissionList, permissionCode);

        Snackbar.make(snackBarView, rationale, Snackbar.LENGTH_INDEFINITE).setAction("확인", snackBarOnClick).show();
    }

    private void requestPermissionList(String[] permissionList, int permissionCode){
        ActivityCompat.requestPermissions((Activity) context, permissionList, permissionCode);
    }

    public boolean permissionCheckProcess(){
        if(checkPermission(requestPermissions))
            return true;

        if(isRequireRationale(deniedPermissions)){
            showRationaleWithRequest(deniedPermissions);
            return true;
        }

        if(deniedPermissions == null)
            return false;

        requestPermissionList(deniedPermissions, permissionCode);
        return true;
    }

    public boolean requestResult(String[] permissions, int[] grantResults){
        for(int index = 0; index < grantResults.length; index++){
            if(grantResults[index] == PackageManager.PERMISSION_DENIED) {
                Log.d(Tag.PERMISSION_MANAGER, "PERMISSION MANAGER : " + permissions[index] + " is denied!");
                return false;
            }
        }

        return true;
    }

    public void showPermissionDialog(DialogInterface.OnClickListener negativeListener){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("권한 설정");
        builder.setMessage("권한 승인을 위해 앱 설정 화면으로 이동하시겠습니까?");
        builder.setPositiveButton("예", (dialog, which) -> moveToPermissionSetting());
        builder.setNegativeButton("아니오", negativeListener);
        builder.create().show();
    }

    public void moveToPermissionSetting(){
        Intent appDetail = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:"+context.getPackageName()));
        appDetail.addCategory(Intent.CATEGORY_DEFAULT);
        appDetail.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(appDetail);
    }


//    public void showPermissionDialog(Runnable delayFinish){
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setTitle("권한 설정");
//        builder.setMessage("권한 승인을 위해 앱 설정 화면으로 이동하시겠습니까?");
//        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                moveToPermissionSetting();
//            }
//        });
//
//        builder.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                Toast.makeText(context, context.getString(R.string.toast_permission_denied), Toast.LENGTH_LONG).show();
//                new Handler().postDelayed(delayFinish, 1000);
//            }
//        });
//
//        builder.create().show();
//    }
}
