package com.example.dlsgh.myapplication3;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.os.Handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.Buffer;

/**
 * 음성 녹음을 하는 방법에 대해 알 수 있습니다.
 *
 * @author Mike
 *
 */


public class MainActivity extends AppCompatActivity {

    private short[] buffer; // 모노라면 여기
    private short[] mic_top; //스테레오라면 여기
    private short[] mic_bottom;

    int frequency = 48000;
    int channelConfiguration = AudioFormat.CHANNEL_IN_STEREO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    int recording_block = 50000;  //한번에 녹음하는 sample 수

    short[] mic_sound;

    private static String RECORDED_FILE;
    AudioTrack audioTrack;
    AudioRecord audiorecord;
    MediaPlayer player;
    MediaRecorder recorder;

    int bufferReadResult = -1;
    boolean isShake = false;
    boolean isSwipe = false;
    boolean isSqueeze = false;
    boolean record_stop = false;

    String option_s_p[] = {"-r", "range", "train.t", "train.t.scale"}; //svm scale option
    String option_p[] = {"-b", "1", "train.t.scale", "train.scale.model", "train.t.predict"}; /// svm predict option

    short[] Buffer=null;

    boolean keepGoing = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File sdcard = Environment.getExternalStorageDirectory();
        File file = new File(sdcard, "recorded.mp4");
        RECORDED_FILE = file.getAbsolutePath();

        Button recordBtn = (Button) findViewById(R.id.recordBtn);
        Button recordStopBtn = (Button) findViewById(R.id.recordStopBtn);
        Button playBtn = (Button) findViewById(R.id.playBtn);
        Button playStopBtn = (Button) findViewById(R.id.playStopBtn);
        Button playSoundBtn = (Button) findViewById(R.id.PlaysoundBtn);
        Button stopSoundBtn = (Button) findViewById(R.id.StopsoundBtn);



        recordBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (recorder != null) {
                    recorder.stop();
                    recorder.release();
                    recorder = null;
                }

                recorder = new MediaRecorder();

                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);

                recorder.setOutputFile(RECORDED_FILE);

                try {
                    Toast.makeText(getApplicationContext(), "녹음을 시작합니다.", Toast.LENGTH_LONG).show();

                   recorder.prepare();
                    recorder.start();
                } catch (Exception ex) {
                    Log.e("SampleAudioRecorder", "Exception : ", ex);
                }
            }
        });

        recordStopBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (recorder == null)
                    return;
                record_stop = true;
                recorder.stop();
                recorder.release();
                recorder = null;

                Toast.makeText(getApplicationContext(), "녹음이 중지되었습니다.", Toast.LENGTH_LONG).show();
            }
        });

        playBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (player != null) {
                    player.stop();
                    player.release();
                    player = null;
                }

                Toast.makeText(getApplicationContext(), "녹음된 파일을 재생합니다.", Toast.LENGTH_LONG).show();
                try {
                    player = new MediaPlayer();

                    player.setDataSource(RECORDED_FILE);
                    player.prepare();
                    player.start();
                } catch (Exception e) {
                    Log.e("SampleAudioRecorder", "Audio play failed.", e);
                }
            }
        });


        playStopBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (player == null)
                    return;
                Toast.makeText(getApplicationContext(), "재생이 중지되었습니다.", Toast.LENGTH_LONG).show();

                player.stop();
                player.release();
                player = null;
            }
        });
        playSoundBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
               playa();
            }
        });
        stopSoundBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                keepGoing=false;
                audioTrack.stop();
            }
        });
    }

    protected void onPause() {
        if (recorder != null) {
            recorder.release();
            recorder = null;
        }

        if (player != null) {
            player.release();
            player = null;
        }

        super.onPause();
    }

    void playSound() {
        Buffer = new short[48000*2];
        if(audioTrack != null) {
            audioTrack.release();
            audioTrack = null;
        }

        int minSize = AudioTrack.getMinBufferSize(48000,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                48000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, Buffer.length, AudioTrack.MODE_STATIC);
        generatePulse(25000,48000);
            audioTrack.write(Buffer, 0, Buffer.length);
            audioTrack.setStereoVolume(1.0f, 1.0f);
            audioTrack.play();



    }

    void generatePulse(int freq,int SamplingFreq){
        double omega,time;
        int i,Index=0;
        short Vout;

        omega=2*Math.PI*freq;

        for(i=0;i<=SamplingFreq-1;i++){
            time=(double)i/SamplingFreq;
            Vout =(short)(32767 * Math.sin(omega*time) );
            Buffer[Index]=Vout;   //LEFT 저장
            Index++;
            Buffer[Index]=Vout;  //RIGHT 저장
            Index++;
        }
    }

    void fullsensing(){
        ////////////////////////////////////////////////
        //////phase3 : reading PCM file and filtering
        int array_size;
        if( channelConfiguration == AudioFormat.CHANNEL_IN_STEREO)
            array_size = mic_top.length;
        else
            array_size = buffer.length;

        mic_sound = new short[array_size];


        if( channelConfiguration == AudioFormat.CHANNEL_IN_STEREO) {
            for (int j = 0; j < array_size; j++) {
                mic_sound[j] =  mic_top[j];
            }
        }
        else{
            for(int j=0; j< array_size; j++)
            {
                mic_sound[j] = buffer[j];
            }
        }
    }

    void ShowFull_Predict() {

        String r_str;
        String sd = Environment.getExternalStorageDirectory().getAbsolutePath();
        String filePath = sd+"/train.pcm";
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(filePath);
            Toast.makeText(getApplicationContext(), "파일이 추출되었습니다2.", Toast.LENGTH_LONG).show();


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        r_str = Integer.toString(-1);
        try {
            byte bData[] = short2byte(mic_sound);
            os.write(bData,0,2048 );
            Toast.makeText(getApplicationContext(), "파일이 추출되었습니다3.", Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    void playa(){
        int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
        AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.CAMCORDER, frequency,
                channelConfiguration, audioEncoding, bufferSize);
        buffer = new short[recording_block];
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
        }
        audioRecord.startRecording();
        Toast.makeText(getApplicationContext(), "녹음을 시작합니다.", Toast.LENGTH_LONG).show();

        //Log.d("SensingService","service_while");

            playSound();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
            }
            audioRecord.stop();
        Toast.makeText(getApplicationContext(), "녹음이 중지되었습니다.", Toast.LENGTH_LONG).show();

        bufferReadResult = audioRecord.read(buffer, 0, recording_block); //blockSize = 256
            if (channelConfiguration == AudioFormat.CHANNEL_IN_STEREO) {
                mic_top = new short[bufferReadResult / 2];
                mic_bottom = new short[bufferReadResult / 2];
                int i = 0;
                for (int j = 0; j < bufferReadResult; j = j + 2) {
                    mic_top[i] = buffer[j];
                    i++;
                }
                i = 0;
                for (int j = 1; j < bufferReadResult; j = j + 2) {
                    mic_bottom[i] = buffer[j];
                    i++;
                }
            } else {
                ///streo 녹음 아닐때 처리 일단은 갤럭시는 streo 녹음이기 때문에 처리 안함
            }
        fullsensing(); // matched filter 과정
        Toast.makeText(getApplicationContext(), "matched filter가 완료되었습니다.", Toast.LENGTH_LONG).show();

        ShowFull_Predict(); // FFT 과정 and Sound Signature 추출
        Toast.makeText(getApplicationContext(), "파일이 추출되었습니다.", Toast.LENGTH_LONG).show();
    }
    private byte[] short2byte(short[] sData) {

        int shortArrsize = sData.length;

        byte[] bytes = new byte[shortArrsize * 2];

        for (int i = 0; i < shortArrsize; i++) {

            bytes[i * 2] = (byte) (sData[i] & 0x00FF);

            bytes[(i * 2) + 1] = (byte) (sData[i] >> 8);

            sData[i] = 0;

        }

        return bytes;

    }
}

