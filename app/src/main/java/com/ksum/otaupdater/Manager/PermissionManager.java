package com.ksum.otaupdater.Manager;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;
import android.view.View.OnClickListener;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.ksum.otaupdater.R;

import java.util.ArrayList;

public class PermissionManager {

    public static final int PERMISSION_DEFAULT_CODE = 1111;

    public final String[] locationPermissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public final String[] locationPermissions_upper_api29 = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
    };

    private Context context;
    private Activity activity;
    private View snackBarView;
    private ArrayList<String> requestPermissions;
    private ArrayList<String> deniedPermissions;
    private String rationale;
    private int permissionCode;
    private OnClickListener snackBarOnClick;

    public PermissionManager(Context context, Activity activity, ArrayList<String> requestPermissions, View snackBarView){
        this.context = context;
        this.activity = activity;
        this.snackBarView = snackBarView;
        this.requestPermissions = requestPermissions;
        this.permissionCode = PERMISSION_DEFAULT_CODE;
        this.rationale = activity.getString(R.string.permission_default_rationale);
    }

    public PermissionManager(Context context, Activity activity, int permissionCode, ArrayList<String> requestPermissions, String rationale, View snackBarView) {
        this.context = context;
        this.activity = activity;
        this.snackBarView = snackBarView;
        this.requestPermissions = requestPermissions;
        this.rationale = rationale;
        this.permissionCode = permissionCode;
    }

    public void setSnackBarOnClick(OnClickListener snackBarOnClick){
        this.snackBarOnClick = snackBarOnClick;
    }

    /*----------------------------- Permission Check Methods -----------------------------*/

    private boolean checkPermission(ArrayList<String> permissionList){
        if(permissionList == null || permissionList.isEmpty())
            return false;

        deniedPermissions = getDeniedPermissionList(permissionList);
        return deniedPermissions.isEmpty();
    }

    private ArrayList<String> getDeniedPermissionList(ArrayList<String> permissionList){
        int result;
        ArrayList<String> deniedList = new ArrayList<>();

        for(String permission : permissionList){
            result = ContextCompat.checkSelfPermission(context, permission);

            if(result != PackageManager.PERMISSION_GRANTED)
                deniedList.add(permission);
        }

        return deniedList;
    }

    private boolean isRequireRationale(ArrayList<String> permissionList){
        for(String permission : permissionList){
            if(ActivityCompat.shouldShowRequestPermissionRationale(activity, permission))
                return true;
        }
        return false;
    }

    private void showRationaleWithRequest(ArrayList<String> permissionList){
        if(snackBarOnClick == null)
            snackBarOnClick = v -> requestPermissionList(permissionList, permissionCode);

        Snackbar.make(snackBarView, rationale, Snackbar.LENGTH_INDEFINITE).setAction("확인", snackBarOnClick).show();
    }

    private void requestPermissionList(ArrayList<String> permissionList, int permissionCode){
        String[] permissions = permissionList.toArray(new String[0]);
        ActivityCompat.requestPermissions(activity, permissions, permissionCode);
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
}
