package com.android.iplayer.widget.view;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

import master.flame.danmaku.danmaku.loader.ILoader;
import master.flame.danmaku.danmaku.loader.IllegalDataException;
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.IDataSource;
import master.flame.danmaku.defaultParser.DanmuXmlParser;

public class DanmuParserView extends AbstractPaserView<DanmuXmlParser>{

    public DanmuParserView(@NonNull @NotNull Context context) {
        super(context);
    }

    public DanmuParserView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs,0);
    }

    public DanmuParserView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected DanmuXmlParser createParser(InputStream stream) {
        if (stream == null) {
            return new DanmuXmlParser() {

                @Override
                protected Danmakus parse() {
                    return new Danmakus();
                }
            };
        }

        ILoader loader = DanmakuLoaderFactory.create(DanmakuLoaderFactory.TAG_BILI);

        try {
            assert loader != null;
            loader.load(stream);
        } catch (IllegalDataException e) {
            e.printStackTrace();
        }
        DanmuXmlParser parser = new DanmuXmlParser();
        IDataSource<?> dataSource = loader.getDataSource();
        parser.load(dataSource);
        return parser;
    }

    @Override
    protected void addInputDanmaku(String comment){
        super.addInputDanmaku(comment);
    }
}
