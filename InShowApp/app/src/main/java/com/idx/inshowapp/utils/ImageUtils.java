package com.idx.inshowapp.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by sunny on 18-7-24.
 */

public class ImageUtils {
    private static final String TAG=ImageUtils.class.getSimpleName();

    /**
     * 保存文件到指定路径
     * @param context
     * @param bitmap
     */
    public static void saveImage(Context context, Bitmap bitmap){
        boolean isSuccess;
        // 首先保存图片
        String storePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "InShow";
        File file=new File(storePath);
        if (! file.exists()){
            file.mkdir();
        }
        String fileName=System.currentTimeMillis()+".jpg";
        File file1=new File(file,fileName);
        try {
            FileOutputStream fileOutputStream=new FileOutputStream(file1);
            //通过io流的方式来压缩保存图片
            isSuccess= bitmap.compress(Bitmap.CompressFormat.JPEG, 60, fileOutputStream);
            if (isSuccess){
                Toast.makeText(context,"图片保存成功",Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(context,"图片保存失败",Toast.LENGTH_SHORT).show();
            }
            fileOutputStream.flush();
            fileOutputStream.close();
            //把文件插入到系统图库
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    file1.getAbsolutePath(),fileName,null);
            // 保存图片后发送广播通知更新数据库
            Uri uri = Uri.fromFile(file);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Bitmap转换成File：
     * @param bitmap
     * @return 路径
     */
    public static String saveBitmap(Bitmap bitmap){

        File file = new File(Environment.getExternalStorageDirectory().getPath());   //FILE_DIR自定义
        if (!file.exists()) {
            file.mkdir();
        }
        File tmpf = new File(file,  "temp"+ ".jpg");
        Log.i(TAG, "saveBitmap: tmpf="+tmpf);
        File f = tmpf;
        try {
            f.createNewFile();
            FileOutputStream fOut = null;
            fOut = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String image_file_url=f.getAbsolutePath();

        return image_file_url;
    }

    /**
     * 旋转180度
     * @param bitmap
     * @return
     */
    public static Bitmap rotationBitmap( Bitmap bitmap) {
            //旋转图片
            Matrix matrix = new Matrix();
            matrix.postRotate(180);
            matrix.setScale(1, -1);//垂直翻转
            // 创建新的图片
        if (bitmap !=null){
            Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            return resizedBitmap;
        }else {
            return null;
        }
    }

    /**
     * 旋转90度
     * @param bitmap
     * @return
     */
    public static Bitmap rotation(Bitmap bitmap){
        //旋转图片
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }

}
