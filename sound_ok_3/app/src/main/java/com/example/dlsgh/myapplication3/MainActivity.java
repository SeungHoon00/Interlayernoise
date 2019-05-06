package com.example.dlsgh.myapplication3;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
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
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {
    static int recordcount1 = 0;
    private static String RECORDED_FILE;
    private static String RECORDED_FILE_PCM;
    private static String RECORDED_FILE_WAV;
    private static String PLAYERDED_FILE;
    AudioTrack audioTrack;
    MediaPlayer player;
    MediaRecorder recorder;
    byte[] buffer = null;
    byte[] music = null;
    short[] CorrShort;
    byte generatedSnd[];
    short[] Buffer = null;
    short[] Buffer_original = null;

    boolean misrecording = false;
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

        File file = new File(sdcard, "recorded.pcm");
        File file2 = new File(sdcard, "test.wav");
        File file_pcm = new File(sdcard, "test.pcm");
        File file_wav = new File(sdcard, "test.wav");

        RECORDED_FILE = file.getAbsolutePath();
        PLAYERDED_FILE = file2.getAbsolutePath();
        RECORDED_FILE_PCM = file_pcm.getAbsolutePath();
        RECORDED_FILE_WAV = file_wav.getAbsolutePath();

        Button recordBtn = (Button) findViewById(R.id.recordBtn);
        Button recordStopBtn = (Button) findViewById(R.id.recordStopBtn);
        Button playBtn = (Button) findViewById(R.id.playBtn);
        Button playStopBtn = (Button) findViewById(R.id.playStopBtn);
        Button playSoundBtn = (Button) findViewById(R.id.PlaysoundBtn);
        Button stopSoundBtn = (Button) findViewById(R.id.StopsoundBtn);



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
        playSoundBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //misrecording = true;
                TimerTask mTask;
                Timer mTimer;

                mTask=new TimerTask(){
                    @Override
                    public void run(){
                        recordcount1++;
                    }
                };

                Timer timer = new Timer();
                FileInputStream fis = null;
                FileOutputStream outputStream = null;
                BufferedOutputStream bufferoutput = null;
                DataOutputStream dataoutput = null;
                AssetManager am;
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

                AudioRecord ar = null;
                int sizeInBytes = AudioRecord.getMinBufferSize(48000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 48000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, sizeInBytes);
                //recorder = new MediaRecorder();
                player = new MediaPlayer();

               /* recorder.setAudioSamplingRate(48000);
                recorder.setAudioEncodingBitRate(384000);
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

                recorder.setOutputFile(RECORDED_FILE);
*/
                int buffsize = AudioRecord.getMinBufferSize(48000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                ar = new AudioRecord(MediaRecorder.AudioSource.MIC, 48000, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, 2048);
                buffer = new byte[sizeInBytes];

                /*
                try {
                    Toast.makeText(getApplicationContext(), "녹음을 시작합니다.", Toast.LENGTH_LONG).show();
                    recorder.prepare();
                    recorder.start();
                } catch (Exception ex) {
                    Log.e("SampleAudioRecorder", "Exception : ", ex);
                }
                */
                ar.startRecording();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                }

                try {
                    outputStream = new FileOutputStream(RECORDED_FILE);
                    while(misrecording) {
                        ar.read(buffer, 0, sizeInBytes);
                        outputStream.write(buffer,0,sizeInBytes*2);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
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

                try {

                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                }
                /*
                if (recorder == null)
                    return;

                recorder.stop();
                recorder.release();
                recorder = null;
*/

                ar.stop();
                ar.release();
                try {
                    outputStream.close();
                }catch(IOException e)
                {
                    e.printStackTrace();
                }
                /*ar.read(buffer, 0, buffer.length);
                try {
                    outputStream.write(buffer, 0, buffer.length);
                }catch (IOException e) {
                    e.printStackTrace();
                }try {
                    outputStream.close();
                }catch (IOException e) {
                    e.printStackTrace();
                }*/
                /*
                String sd = Environment.getExternalStorageDirectory().getAbsolutePath();
                String filePath = sd + "/record_pcm.pcm";
                FileOutputStream os = null;

                int read = audioRecord.read(buffer, 0, buffer.length);

                try {
                    os = new FileOutputStream(filePath);
                    os.write(buffer, 0, read);
                }catch(IOException e){
                    e.printStackTrace();
                }*/
                /*try {
                    os = new FileOutputStream(filePath);
                    Toast.makeText(getApplicationContext(), "파일이 추출되었습니다2.", Toast.LENGTH_LONG).show();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    byte bData[] = short2byte(Buffer_original);
                    os.write(buffer, 0, 2048);
                    Toast.makeText(getApplicationContext(), "파일이 추출되었습니다3.", Toast.LENGTH_LONG).show();

                } catch (IOException e) {
                    e.printStackTrace();
                }*/
/*
                try {
                    writeWavHeader(outputStream, AudioFormat.CHANNEL_CONFIGURATION_MONO, 48000, AudioFormat.ENCODING_PCM_16BIT);

                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                Toast.makeText(getApplicationContext(), "녹음이 중지되었습니다.", Toast.LENGTH_LONG).show();

            }
        });
        stopSoundBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                misrecording=false;
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
                AudioFormat.ENCODING_PCM_16BIT, minSize, AudioTrack.MODE_STATIC);

        try {
            music = new byte[100000];
            is = new FileInputStream(PLAYERDED_FILE);
            dis = new DataInputStream(is);
            while ((count = dis.read(music, 0, 100000)) > -1);
            {
                audioTrack.write(music, 0, 100000);
            }
            //audioTrack.stop();
            //audioTrack.release();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }

        audioTrack.play();
    }
    void playSound2() {
        Buffer = new short[48000 * 2+2];
        Buffer_original = new short[48000*2];
        if (audioTrack != null) {
            audioTrack.release();
            audioTrack = null;
        }

        int minSize = AudioTrack.getMinBufferSize(48000,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                48000, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, Buffer.length, AudioTrack.MODE_STATIC);
        generatePulse(500, 48000);
        audioTrack.write(Buffer, 0, Buffer.length);
        audioTrack.setStereoVolume(1.0f, 1.0f);
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
    private static void writeWavHeader(FileOutputStream out, int channels, int sampleRate, int bitDepth) throws IOException {
        // Convert the multi-byte integers to raw bytes in little endian format as required by the spec
        byte[] littleBytes = ByteBuffer
                .allocate(14)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(channels)
                .putInt(sampleRate)
                .putInt(sampleRate * channels * (bitDepth / 8))
                .putInt((int) (channels * (bitDepth / 8)))
                .putInt(bitDepth)
                .array();

        // Not necessarily the best, but it's very easy to visualize this way
        out.write(new byte[]{
                // RIFF header
                'R', 'I', 'F', 'F', // ChunkID
                0, 0, 0, 0, // ChunkSize (must be updated later)
                'W', 'A', 'V', 'E', // Format
                // fmt subchunk
                'f', 'm', 't', ' ', // Subchunk1ID
                16, 0, 0, 0, // Subchunk1Size
                1, 0, // AudioFormat
                littleBytes[0], littleBytes[1], // NumChannels
                littleBytes[2], littleBytes[3], littleBytes[4], littleBytes[5], // SampleRate
                littleBytes[6], littleBytes[7], littleBytes[8], littleBytes[9], // ByteRate
                littleBytes[10], littleBytes[11], // BlockAlign
                littleBytes[12], littleBytes[13], // BitsPerSample
                // data subchunk
                'd', 'a', 't', 'a', // Subchunk2ID
                0, 0, 0, 0, // Subchunk2Size (must be updated later)
        });
    }



}
