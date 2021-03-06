package com.example.dlsgh.myapplication3;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.Buffer;
import android.media.AudioManager;


public class MainActivity extends AppCompatActivity {


    private static String RECORDED_FILE;
    private static String PLAYERDED_FILE;
    AudioTrack audioTrack;
    MediaPlayer player;
    MediaRecorder recorder;

    byte[] music = null;
    short[] CorrShort;
    byte generatedSnd[];
    short[] Buffer = null;
    short[] Buffer_original = null;


    boolean keepGoing = false;

    private EditText etNum = null;
    private EditText etStart = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        etNum = (EditText) findViewById(R.id.Num_of_Records);
        etStart = (EditText) findViewById(R.id.StartIndex);
        //File sdcard = Environment.getExternalStorageDirectory();

        Button playSoundBtn = (Button) findViewById(R.id.PlaysoundBtn);

        playSoundBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {   // 소리재생, 녹음버튼///////////////////
                FileInputStream fis = null;
                //AssetManager am;
                String zeros;
                int index;
                int len;    // 자릿수
                int startIndex = Integer.parseInt(etStart.getText().toString());    // 시작 인덱스
                int iteration = Integer.parseInt(etNum.getText().toString());   // 원하는 녹음 파일 수

                File sdcard = Environment.getExternalStorageDirectory();
                File file2 = new File(sdcard, "sample.wav"); // change name part
                PLAYERDED_FILE = file2.getAbsolutePath();

                for(int i = 0; i < iteration; i++) {    // iteration 만큼 녹음
                    index = startIndex;
                    zeros = "";
                    len = 1;
                    while( true ) {   // 자릿수 확인
                        if( (index / 10) != 0 ) {
                            index /= 10;
                            len++;
                        }
                        else
                            break;
                    }

                    for(int dx = 0; dx < 6 - len; dx++) {   // 자릿수에 따라 앞에 0 붙이기, default 는 백만 단위
                        zeros += "0";
                    }

                    String file_num = zeros + String.valueOf(startIndex);
                    startIndex += 1;
                    File file = new File(sdcard, file_num + ".pcm");

                    RECORDED_FILE = file.getAbsolutePath();


                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                    }
                    if (recorder != null) {
                        recorder.stop();
                        recorder.release();
                        recorder = null;
                    }

                    if (player != null) {
                        player.stop();
                        player.release();
                        player = null;
                    }
                    recorder = new MediaRecorder();
                    player = new MediaPlayer();

                    recorder.setAudioSamplingRate(48000);
                    recorder.setAudioEncodingBitRate(128000);
                    recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

                    recorder.setOutputFile(RECORDED_FILE);

                    try {
                        Toast.makeText(getApplicationContext(), "녹음을 시작합니다.", Toast.LENGTH_LONG).show();
                        recorder.prepare();
                        recorder.start();
                    } catch (Exception ex) {
                        Log.e("SampleAudioRecorder", "Exception : ", ex);
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                    }
                    /*try {
                        fis = new FileInputStream(PLAYERDED_FILE);
                        FileDescriptor fd = fis.getFD();

                        player.setDataSource(fd);
                        player.prepare();
                        player.start();
                    } catch(IOException  e){
                        Log.e("sgaaweahha", "Exception : ", e);
                    }*/

                    playSound();
                    //audioTrack.setVolume(AudioTrack.getMaxVolume()*0.5f);
                    try {
                        Thread.sleep(11000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                    }

                    if (recorder == null)
                        return;

                    recorder.stop();
                    recorder.release();
                    recorder = null;

                    Toast.makeText(getApplicationContext(), "녹음이 중지되었습니다.", Toast.LENGTH_LONG).show();
                }
            }
        });
        /*
        stopSoundBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                keepGoing = false;
                int minSize = AudioTrack.getMinBufferSize(8000,
                        AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);

                audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                        8000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, minSize, AudioTrack.MODE_STATIC);
                audioTrack.play();
            }
        }); */


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
        Buffer = new short[48000 * 2+2];
        Buffer_original = new short[48000*2];
        int i = 0;
        /*if (audioTrack != null) {
            audioTrack.release();
            audioTrack = null;
        }*/
        FileInputStream is = null;
        DataInputStream dis = null;
        int bufferSize = 512;
        int count = 0;
        int minSize = AudioTrack.getMinBufferSize(48000,
                AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                48000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, 48000*40+2, AudioTrack.MODE_STATIC);

        try {
            music = new byte[48000*40+2];
            is = new FileInputStream(PLAYERDED_FILE);
            dis = new DataInputStream(is);
            while ((count = dis.read(music, 0, 48000*40+2)) > -1);
            {
                audioTrack.write(music, 0, 48000*40+2);
            }

            //audioTrack.stop();
            //audioTrack.release();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }


        //AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //am.setStreamVolume(AudioManager.STREAM_MUSIC, (int)(am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)*(7/10)),AudioManager.FLAG_PLAY_SOUND);
        //audioTrack.setPlaybackHeadPosition(100);
        audioTrack.play();
        //audioTrack.setPlaybackRate(88200);

               /*
        for(int q=100; q>0; q--) {
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            am.setStreamVolume(AudioManager.STREAM_RING, (int)(am.getStreamMaxVolume(AudioManager.STREAM_RING)*(q/10)),AudioManager.FLAG_PLAY_SOUND);

        }*/
    }
    void playSound2() {
        Buffer = new short[48000 * 2+2];
        Buffer_original = new short[48000*2];
        if (audioTrack != null) {
            audioTrack.release();
            audioTrack = null;
        }

        int minSize = AudioTrack.getMinBufferSize(48000,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                48000, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT, Buffer.length, AudioTrack.MODE_STATIC);
        generatePulse(500, 48000);

        audioTrack.write(Buffer, 0, Buffer.length);

        audioTrack.play();

    }
    void generatePulse(int freq, int SamplingFreq) {
        double omega, time;
        int i, Index = 0, Index2=0;
        short Vout;
        int freq2 = freq;


        for (i = 0; i <= SamplingFreq - 1; i++) {
           /*if ((1 / freq2) < (i / SamplingFreq)) {
                freq2 += 10;
            }*/

            omega = 2 * Math.PI * freq2;
            time = (double) i / SamplingFreq;
            Vout = (short) (32767 * Math.sin(omega * time));
            if(1/freq2>i/SamplingFreq) {
                Buffer_original[Index2] = Vout;   //LEFT 저장
                Index2++;
                Buffer_original[Index2] = Vout;  //RIGHT 저장
                Index2++;
            }
            Buffer[Index] = Vout;   //LEFT 저장
            Index++;
            Buffer[Index] = Vout;  //RIGHT 저장
            Index++;
        }
        Buffer[Index] = (short)freq;
        Index++;
        Buffer[Index] = (short)freq2;
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