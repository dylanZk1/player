package com.android.iplayer.widget.util;

import android.content.Context;
import android.content.res.Resources;

import com.android.iplayer.widget.view.AbstractPaserView;

import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class DanmuPath {

    /**
     * 获取弹幕路径，目前只支持本地路径，且只支持xml格式
     * @param context 上下文
     * @param danmu 弹幕渲染容器对象
     * @return 弹幕文件对象
     */
    public static InputStream DanmuResource(Context context, AbstractPaserView view, Object danmu){
        if(danmu instanceof Integer){
            try{
                return view.getResources().openRawResource((Integer) danmu);
            }catch (Resources.NotFoundException e1){
                e1.printStackTrace();
                return null;
            }

        }else if(danmu instanceof String){
            try{
                if(((String) danmu).split("/").length == 1 && ((String) danmu).split("/")[0].split("\\.").length == 2){
                    return context.getAssets().open((String)danmu);
                }else{
                    if(isXmlDocument(String.valueOf(danmu))){
                        return new ByteArrayInputStream(String.valueOf(danmu).getBytes());
                    }else{
                        return null;
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }else if(danmu instanceof File){
            try{
                return new FileInputStream((File)danmu);
            }catch (FileNotFoundException e1){
                e1.printStackTrace();
                return null;
            }
        }else{
            return null;
        }
    }

    /**
     * 判断字符串是否为xml字符
     * @param rtnMsg
     * @return
     */
    private static boolean isXmlDocument(String rtnMsg){
        boolean flag = true;
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
            builder.parse( new InputSource( new StringReader( rtnMsg )));
        } catch (Exception e) {
            flag = false;
        }
        return flag;
    }
}
