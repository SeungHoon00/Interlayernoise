package com.example.dlsgh.sound_test_generate;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.icu.text.AlphabeticIndex;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;



import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by User on 2017-02-12.
 */
public class RecordAudio extends AsyncTask<Void, double[], Void> {


    static Context mContext;
    TextView showResult;
    short[] CorrShort;
    double sample[];
    double sample1[],sample2[],sample3[],sample4[];
    byte generatedSnd[];
    byte generatedSnd1[],generatedSnd2[],generatedSnd3[],generatedSnd4[];

    String option_s_p[] = {"-r", "range", "train.t", "train.t.scale"}; //svm scale option
    String option_p[] = {"-b", "1", "train.t.scale", "train.scale.model", "train.t.predict"}; /// svm predict option


    int under = 6;
    int upper = -6;

    int frequency = 48000;
    int channelConfiguration = AudioFormat.CHANNEL_IN_STEREO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    int recording_block = 50000;  //한번에 녹음하는 sample 수
    boolean isCompleted = false;  //현재 녹음 완료 ?
    double result_class = -1; //결과

    private short[] buffer; // 모노라면 여기
    private short[] mic_top; //스테레오라면 여기
    private short[] mic_bottom;


    double[] result1,result2,result3,result4;
    double[] mic_sound;
    short[] short_tmp;

    int peakIndex;
    int peakIndex1,peakIndex2,peakIndex3,peakIndex4;

    Handler Soundhandler = new Handler();
    AudioTrack audioTrack;
    int bufferReadResult = -1;
    boolean isShake = false;
    boolean isSwipe = false;
    boolean isSqueeze = false;
    //rivate OnCompleteListener mListener;


    private OnCompleteListener mListener;
    public void setOnCompleteListener(OnCompleteListener listener) {
        this.mListener = listener;
    }



    public interface OnCompleteListener {
        public void OnComplete(double result);
    }



    public RecordAudio(Context ac) {
        mContext = ac;
        //showResult =(TextView)((Activity)mContext).findViewById(R.id.result_class);
        genTone();
        getSample();
        Log.d("SensingService","service_record");
    }

    /*public void setOnCompleteListener(OnCompleteListener listener) {
        this.mListener = listener;
    }

    public interface OnCompleteListener {
        public void onComplete(double result_class);
    }
*/

    @Override
    protected Void doInBackground(Void... params) {
        String file_name = "predict_raw";
        Log.d("SensingService","service_background");
        try {
            int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
            AudioRecord audioRecord = new AudioRecord( MediaRecorder.AudioSource.CAMCORDER, frequency,
                    channelConfiguration, audioEncoding, bufferSize);

            buffer = new short[recording_block]; //blockSize = 256

            audioRecord.startRecording();

            while(true) {
                //Log.d("SensingService","service_while");
                if(isCancelled()){
                    audioRecord.stop();
                    break;
                }
                if(isShake){
                    Soundhandler.post(new Runnable() {
                        @Override
                        public void run() {
                            playSound();
                        }
                    });
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
                    ShowFull_Predict(); // FFT 과정 and Sound Signature 추출
                    try {
                        svmScale(option_s_p);
                        result_class = svmPredict(option_p);
                        mListener.OnComplete(result_class);

                        Log.d("result_class", Double.toString(result_class));
                        //mListener.onComplete(result_class);
                        // os.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    isShake=false;
                }
                else {
                    bufferReadResult = audioRecord.read(buffer, 0, recording_block); //blockSize = 256

                }
            }
            audioRecord.stop();
            //class_num.setText(Double.toString(result_class));

        } catch (Throwable t) {
            Log.e("AudioRecord", "Recording Failed");
        }
        return null;

    }

    void fullsensing(){
        ////////////////////////////////////////////////
        //////phase3 : reading PCM file and filtering
        int array_size;
        if( channelConfiguration == AudioFormat.CHANNEL_IN_STEREO)
            array_size = mic_top.length;
        else
            array_size = buffer.length;

        Log.d("array_size",Integer.toString(array_size));
        result1 = new double[array_size];
        result2 = new double[array_size];
        result3 = new double[array_size];
        result4 = new double[array_size];

        mic_sound = new double[array_size];


        if( channelConfiguration == AudioFormat.CHANNEL_IN_STEREO) {
            for (int j = 0; j < array_size; j++) {
                mic_sound[j] = (double) mic_top[j];
            }
        }
        else{
            for(int j=0; j< array_size; j++)
            {
                mic_sound[j] = (double)buffer[j];
            }
        }

        for(int i=0;i<4;i++)
        {
            if(i==0)
                peakIndex1 = convolve(mic_sound, sample1, result1);
            else if(i==1)
                peakIndex2 = convolve(mic_sound, sample2, result2);
            else if(i==2)
                peakIndex3 = convolve(mic_sound, sample3, result3);
            else
                peakIndex4 = convolve(mic_sound, sample4, result4);
        }

        Log.i("peakIndex1",Double.toString(peakIndex1));
        Log.i("peakIndex2",Double.toString(peakIndex2));
        Log.i("peakIndex3",Double.toString(peakIndex3));
        Log.i("peakIndex4",Double.toString(peakIndex4));

    }

    int convolve(double[] data, double[] operator, double[] output) {
        double tmpSignalAfterFilter = 0;
        peakIndex = 0;
        int r_peakIndex = 0;
        int dataLen = data.length;
        int operatorLen = operator.length;
        for(int i=0;  i< dataLen - operatorLen; i++) {
            output[i] = 0;
            for(int j= operatorLen-1; j >=0 ; j--) {
                output[i] += data[i+j]*operator[j];
            }

            if(Math.abs(output[i])>tmpSignalAfterFilter) {
                tmpSignalAfterFilter = Math.abs(output[i]);
                r_peakIndex = i;
                peakIndex = i;
            }
        }
        return r_peakIndex;
    }

    void ShowFull_Predict() {

        int feature_index = 1;
        String r_str;
        String filePath = "/sdcard/train.t";
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int windowSize = 1024;
        DoubleFFT_1D fft = new DoubleFFT_1D(windowSize);

        double[] fftData = new double[windowSize*2];
        double[] magnitude = new double[windowSize/2];


        short[] buffer_f = new short[windowSize];
        double[] toTransform = new double[windowSize];

        ////////////////////////////////////////////////////////////////////////////
        int peak_pointer =0;

        r_str = Integer.toString(-1);
        try {
            os.write(r_str.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(int m=0;m<4;m++) {

            if(m==0)
                peak_pointer = peakIndex1;
            else if(m==1)
                peak_pointer = peakIndex2;
            else if(m==2)
                peak_pointer = peakIndex3;
            else
                peak_pointer = peakIndex4;


            int j = 0;
            for(j=0; j<300; j++){
                buffer_f[j] = 0; //300개
            }

            for(int i=peak_pointer-100; i<peak_pointer+200; i++){
                buffer_f[j] = mic_top[i];
                j++;
                //400개
            }
            for(;j<1024;j++){
                buffer_f[j] = 0;
            }

            for(int i=0; i<windowSize; i++) {
                toTransform[i] = (double)buffer_f[i]/Short.MAX_VALUE;
            }



            for(int i=0; i< windowSize;i++) {
                fftData[2*i] = toTransform[i];
                fftData[2*i+1] = 0;
            }

            fft.complexForward(fftData);

            for(int i=0; i<windowSize/2;i++) {
                magnitude[i] = Math.sqrt(fftData[2*i]*fftData[2*i] + fftData[2+i+1]*fftData[2+i+1]);
            }

            try{
                for(int i=0;i<windowSize/2;i++)
                {
                    if(m==0)
                    {
                        //f(i>=192 + under && i<=213 + upper){
                        if(i>=341 + under && i<= 383 + upper){
                            r_str =" "+Integer.toString(feature_index)+":"+Double.toString(magnitude[i]);
                            feature_index++;
                            os.write(r_str.getBytes());
                        }
                    }
                    else if(m==1)
                    {
                        //if(i>=214 + under && i<=234 + upper){
                        if(i>=384 + under && i<=426 + upper){
                            r_str =" "+Integer.toString(feature_index)+":"+Double.toString(magnitude[i]);
                            feature_index++;
                            os.write(r_str.getBytes());
                        }
                    }
                    else if(m==2)
                    {
                        //if(i>=235 + under && i<=255 + upper){
                        if(i>=427 + under && i<=469 + upper){
                            r_str =" "+Integer.toString(feature_index)+":"+Double.toString(magnitude[i]);
                            feature_index++;
                            os.write(r_str.getBytes());
                        }
                    }
                    else
                    {
                        if(i>=470 + under && i<=511 + upper){
                            r_str =" "+Integer.toString(feature_index)+":"+Double.toString(magnitude[i]);
                            feature_index++;
                            os.write(r_str.getBytes());

                        }
                    }
                }


            } catch (IOException e) {

            }

        }
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void svmScale(String argv[]) throws IOException {
        svm_scale s = new svm_scale();
        Log.i("svmPredict","call svmScale");
        s.run(argv);
    }

    public double svmPredict(String argv[]) throws IOException {
        svm_predict s = new svm_predict();
        Log.i("svmPredict","call svmPredict");
        return s.main(argv);
    }

    void getSample() {

        byte[] CorreByte = null;
        //Save correlation sample

        for(int k=1;k<=4;k++) {
            File file = new File("/sdcard/Chirp" + Integer.toString(k) + ".pcm");
            Log.i("file.length", file.length() + "");
            CorreByte = new byte[(int) file.length()];


            FileInputStream in = null;
            try {
                in = new FileInputStream(file);
                in.read(CorreByte);
                in.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            CorrShort = new short[CorreByte.length / 2];
            CorrShort = byte2short(CorreByte);
            Log.i("CorrShort.length", CorrShort.length + "");

            if (k == 1) {
                sample1 = new double[CorrShort.length - 22];
                for(int i=0; i< CorrShort.length -22; i++)
                {
                    sample1[i] = 0.00005*CorrShort[i + 22];
                }
                generatedSnd1 = new byte[CorreByte.length-44];
                for(int i=0;i<CorreByte.length-44;i++)
                {
                    generatedSnd1[i]= CorreByte[i+44];
                }
            } else if (k == 2) {
                sample2 = new double[CorrShort.length - 22];
                for(int i=0; i< CorrShort.length -22; i++)
                {
                    sample2[i] = 0.00005*CorrShort[i + 22];
                }
                generatedSnd2 = new byte[CorreByte.length-44];
                for(int i=0;i<CorreByte.length-44;i++)
                {
                    generatedSnd2[i]= CorreByte[i+44];
                }
            } else if (k == 3) {
                sample3 = new double[CorrShort.length - 22];
                for(int i=0; i< CorrShort.length -22; i++)
                {
                    sample3[i] = 0.00005*CorrShort[i + 22];
                }
                generatedSnd3 = new byte[CorreByte.length-44];
                for(int i=0;i<CorreByte.length-44;i++)
                {
                    generatedSnd3[i]= CorreByte[i+44];
                }
            } else {
                sample4 = new double[CorrShort.length - 22];
                for(int i=0; i< CorrShort.length -22; i++)
                {
                    sample4[i] = 0.00005*CorrShort[i + 22];
                }
                generatedSnd4 = new byte[CorreByte.length-44];
                for(int i=0;i<CorreByte.length-44;i++)
                {
                    generatedSnd4[i]= CorreByte[i+44];
                }
            }
        }

    }
    private short[] byte2short(byte[] bData){
        int byteArrsize = bData.length;
        int shortArrsize = byteArrsize/2;
        short[] shorts = new short[shortArrsize];
        for(int i=0; i< shortArrsize; i++){
            shorts[i] = (short)( ((bData[(i*2) +1]&0xFF) <<8) | (bData[i*2]&0xFF) );
        }
        return shorts;
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
    void genTone() {

        byte[] CorreByte = null;
        //Save correlation sample
        File file = new File("/sdcard/fullChirp.pcm");

        CorreByte = new byte[(int) file.length()];


        FileInputStream in = null;
        try {
            in = new FileInputStream( file );
            in.read(CorreByte);
            in.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        CorrShort = new short[CorreByte.length/2];
        CorrShort = byte2short(CorreByte);
        Log.i("CorrShort.length",CorrShort.length+"");


        sample = new double[CorrShort.length -22];

        for(int i=0; i< CorrShort.length -22; i++)
        {
            sample[i] = CorrShort[i + 22];
        }

        generatedSnd = new byte[CorreByte.length-44];
        //generatedSnd = short2byte(CorrShort);

        for(int i=0;i<CorreByte.length-44;i++)
        {
            generatedSnd[i]= CorreByte[i+44];
        }

        for(int j=0; j < sample.length; ++j)
        {
            sample[j] = 0.00005*sample[j];
        }
    }
    void playSound() {
        if(audioTrack != null) {
            audioTrack.release();
            audioTrack = null;
        }
        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                frequency, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length, AudioTrack.MODE_STATIC);
        audioTrack.write(generatedSnd, 0, generatedSnd.length);
        audioTrack.play();
    }
}
