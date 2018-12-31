package com.example.dlsgh.myapplication;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.InterruptedException;


public class MainActivity extends AppCompatActivity {
    AudioTrack audioTrack;
    byte generatedSnd[]=new byte[10000];
    int frequency = 48000;
    int channelConfiguration = AudioFormat.CHANNEL_IN_STEREO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    AudioRecord mAudioRecord = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = findViewById(R.id.button);
        Button button2 = findViewById(R.id.button2);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Toast.makeText(getApplicationContext(), "Hello world!", Toast.LENGTH_LONG).show();

                       playSound();
                }

            });
    }
    void playSound() {
        if(audioTrack != null) {
            audioTrack.release();
            audioTrack = null;
        }
        int minSize = AudioTrack.getMinBufferSize(11025,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                48000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, minSize, AudioTrack.MODE_STATIC);
        short[] buffer = { 8130, 15752, 32695, 12253,
                    4329, -3865, -19032, -32722, -16160,
                    -466, 8130, 15752, 22389, 27625, 31134,
                    32695, 32210, 29711, 25354, 19410, 12253,
                    4329, -3865, -11818, -19032, -25055, -29511,
                    -32121, -32722, -31276, -27874, -22728, -16160,
                    -8582, -466 };
        int mBufferSize = AudioTrack.getMinBufferSize(44100,
                AudioFormat.CHANNEL_IN_STEREO,AudioFormat.ENCODING_PCM_16BIT);
        byte[] readData = new byte[mBufferSize];
        String mFilepath = Environment.getExternalStorageDirectory().getAbsolutePath() +"/record.pcm";
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mFilepath);
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }
        }

    }


/*
import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
public class AudioMainActivity extends Activity implements OnClickListener {
    Button startSound;
    Button endSound;
    AudioSynthesisTask audioSynth;
    boolean keepGoing = false;
    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_audio_main);
            startSound = (Button) this.findViewById(R.id.StartSound);
            startSound.setOnClickListener(this);
            endSound = (Button) this.findViewById(R.id.EndSound);
            endSound.setOnClickListener(this);
            endSound.setEnabled(false);
    }
    @Override
        public void onPause() {
            super.onPause();
            keepGoing = false;
            endSound.setEnabled(false);
            startSound.setEnabled(true);
        }
        public void onClick(View v) {
            if (v == startSound) {
                keepGoing = true;
                audioSynth = new AudioSynthesisTask();
                audioSynth.execute();
                endSound.setEnabled(true);
                startSound.setEnabled(false);
            }
            else if (v == endSound) {
                keepGoing = false;
                endSound.setEnabled(false);
                startSound.setEnabled(true);
            }
    }

    private class AudioSynthesisTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            final int SAMPLE_RATE = 11025;
            int minSize = AudioTrack.getMinBufferSize(SAMPLE_RATE,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                    SAMPLE_RATE, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, minSize,
                    AudioTrack.MODE_STREAM);
            audioTrack.play();
            short[] buffer = { 8130, 15752, 32695, 12253,
                    4329, -3865, -19032, -32722, -16160,
                    -466, 8130, 15752, 22389, 27625, 31134,
                    32695, 32210, 29711, 25354, 19410, 12253,
                    4329, -3865, -11818, -19032, -25055, -29511,
                    -32121, -32722, -31276, -27874, -22728, -16160,
                    -8582, -466 };
            /*short[] buffer = { 8130, 15752, 32695, 12253, 4329,
             -3865, -19032, -32722, -16160, -466 };*/
            /* short[] buffer = { 8130, 15752, 22389, 27625, 31134,
             32695, 32210, 29711, 25354, 19410, 12253, 4329, -3865,
              -11818, -19032, -25055, -29511, -32121, -32722, -31276,
               -27874, -22728, -16160, -8582, -466 };
            while (keepGoing) {
                audioTrack.write(buffer, 0, buffer.length);
            }
            return null;
        }
    }
}

*/