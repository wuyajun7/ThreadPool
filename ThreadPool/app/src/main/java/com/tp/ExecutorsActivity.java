package com.tp;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorsActivity extends Activity implements OnClickListener {

    ImageView imageView1;
    ImageView imageView2;
    ImageView imageView3;
    Button button;

    String url1 = "http://p4.gexing.com/kongjianpifu/20121124/1017/50b02e2d5cadc.jpg";
    String url2 = "http://g.hiphotos.baidu.com/zhidao/pic/item/ac345982b2b7d0a242e9110ac9ef76094b369a00.jpg";
    String url3 = "http://g.hiphotos.baidu.com/zhidao/pic/item/a71ea8d3fd1f41344972e455271f95cad1c85e50.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.executor);
        imageView1 = (ImageView) findViewById(R.id.exextor_img);
        imageView2 = (ImageView) findViewById(R.id.exextor_img1);
        imageView3 = (ImageView) findViewById(R.id.exextor_img2);
        button = (Button) findViewById(R.id.exector_btn);
        Log.i("tag", "main  " + Thread.currentThread().getName());
        button.setOnClickListener(this);

//		loadImg(url1, R.id.exextor_img);
//		loadImg(url2, R.id.exextor_img1);
//		loadImg(url3, R.id.exextor_img2);

        AsyncImageLoader loader = new AsyncImageLoader();
        loader.loadImage(url1, new AsyncImageLoader.ImageCallback() {

            @Override
            public void onImageLoaded(Drawable drawable) {
                imageView1.setImageDrawable(drawable);
            }
        });
        loader.loadImage(url2, new AsyncImageLoader.ImageCallback() {

            @Override
            public void onImageLoaded(Drawable drawable) {
                imageView2.setImageDrawable(drawable);
            }
        });
        loader.loadImage(url3, new AsyncImageLoader.ImageCallback() {

            @Override
            public void onImageLoaded(Drawable drawable) {
                imageView3.setImageDrawable(drawable);
            }
        });


    }

    @Override
    public void onClick(View v) {
    }

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            ((ImageView) findViewById(msg.arg1))
                    .setImageDrawable((Drawable) msg.obj);

        }

        ;
    };
    ExecutorService executorService = Executors.newFixedThreadPool(2);

    private void loadImage(final String url, final int id) {
        executorService.execute(new Runnable() {

            @Override
            public void run() {

                Log.i("tag", "executors  " + Thread.currentThread().getName());
                try {
                    Drawable drawable = BitmapDrawable.createFromStream(
                            new URL(url).openStream(), "logo.gif");
                    Message msg = handler.obtainMessage();
                    msg.obj = drawable;
                    msg.arg1 = id;
                    msg.sendToTarget();
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    private void loadImg(final String url, final int id) {
        executorService.submit(new Runnable() {

            @Override
            public void run() {
                Log.i("tag", "executors  " + Thread.currentThread().getName());
                try {
                    final Drawable drawable = BitmapDrawable.createFromStream(
                            new URL(url).openStream(), "logo.gif");
                    handler.post(new Runnable() {

                        @Override
                        public void run() {

                            ((ImageView) findViewById(id))
                                    .setImageDrawable(drawable);
                        }
                    });
                } catch (Exception e) {
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
        super.onDestroy();
    }
}

