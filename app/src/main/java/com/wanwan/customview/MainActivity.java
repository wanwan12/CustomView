package com.wanwan.customview;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.wanwan.customview.View.WaveView.WaveView;

import java.util.Random;
/**
 * @author wanwan
 * @email wanwan2017@foxmail.com
 * @date 2017/12/29
 * @desc
 */
public class MainActivity extends AppCompatActivity {

    private WaveView mProgressView;
    private WaveView mVoiceWaveView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressView = (WaveView) findViewById(R.id.id_progress_view);
        mVoiceWaveView = (WaveView) findViewById(R.id.id_voice_wave);

        mHadler.post(mRefreshRunnable);
    }

    private int progress = 0;
    private int range = 50;
    private Handler mHadler = new Handler();
    private RefreshProgressRunnable mRefreshRunnable = new RefreshProgressRunnable();
    private Random random = new Random(1);

    private class RefreshProgressRunnable implements Runnable {
        public void run() {
            synchronized (MainActivity.this) {
                progress = (progress + 1) % 100;
                mProgressView.setProgress(progress);

                int step = (random.nextInt()) % 50;
                mVoiceWaveView.setRange(range + step);


                mHadler.postDelayed(mRefreshRunnable, 200);
            }
        }
    }
}
