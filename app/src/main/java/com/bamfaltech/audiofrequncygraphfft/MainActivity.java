package com.bamfaltech.audiofrequncygraphfft;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.bamfaltech.audiofrequncygraphfft.fftpack.RealDoubleFFT;


public class MainActivity extends AppCompatActivity implements OnClickListener {

    int frequency = 8000;
    int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    private RealDoubleFFT transformer;
    int blockSize = 256;
    Button startStopButton;
    boolean started = false;
    ImageView imageView;
    Bitmap bitmap;
    Canvas canvas;
    Paint paint;
    RecordAudio recordTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startStopButton = (Button) this.findViewById(R.id.StartStopButton);
        startStopButton.setOnClickListener(this);
        transformer = new RealDoubleFFT(blockSize);
        imageView = (ImageView) this.findViewById(R.id.ImageView01);
        bitmap = Bitmap.createBitmap((int)256,(int)100,Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.GREEN);
        imageView.setImageBitmap(bitmap);
    }


    private class RecordAudio extends AsyncTask<Void, double[], Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            int bufferSize = AudioRecord.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
            AudioRecord audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC, frequency, channelConfiguration, audioEncoding, bufferSize);
            short[] buffer = new short[blockSize];
            double[] toTransform = new double[blockSize];
            if(isPermissionRecordRequired(MainActivity.this)) {
                audioRecord.startRecording();
            }
            int bufferReadResult = 0;
            while (started) {
                bufferReadResult = audioRecord.read(buffer, 0, blockSize);

                for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
                    toTransform[i] = (double) buffer[i] / 32768.0; // signed 16 bit
                }
                transformer.ft(toTransform);
                publishProgress(toTransform);
                Log.e("AudioRecord", "Recording Failed");
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(double[]... toTransform) {
            //super.onProgressUpdate(toTransform);
            canvas.drawColor(Color.BLACK);
            for (int i = 0; i < toTransform[0].length; i++) {
                int x = i;
                int downy = (int) (100 - (toTransform[0][i] * 10));
                int upy = 100;
                canvas.drawLine(x, downy, x, upy, paint);
                imageView.invalidate();
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

    @Override
    public void onClick(View v) {
        if (started) {
            started = false;
            startStopButton.setText("Start");
            recordTask.cancel(true);
        } else {
            started = true;
            startStopButton.setText("Stop");
            recordTask = new RecordAudio();
            recordTask.execute();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1212:
                break;
        }
    }

    public static boolean isPermissionRecordRequired(Activity activity) {

        return checkForPermission(activity, Manifest.permission.RECORD_AUDIO, 1212);
    }

    public static boolean checkForPermission(Activity activity, String permission, int permissionsReqCode) {

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            return true;
        }
        if (ContextCompat.checkSelfPermission(activity,
                permission)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation or not ?
            if (isPermissionAskedAgain(activity, permission)) {

                showExplanationDialogForPermission(activity, permission, permissionsReqCode);

            } else {
                requestPermission(activity, permission, permissionsReqCode);

            }
        } else {
            return true;
        }
        return false;
    }

    /*Use to request permission dialog*/
    private static void requestPermission(Activity activity, String permission, int permissionsReqCode) {
        ActivityCompat.requestPermissions(activity,
                new String[]{permission},
                permissionsReqCode);
    }

    /*Use to check is permission asked once before or it need explanation*/
    public static boolean isPermissionAskedAgain(Activity activity, String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity,
                permission);
    }

    /*Use to request group permission dialog*/
    private static void requestPermission(Activity activity, String[] permission, int permissionsReqCode) {
        ActivityCompat.requestPermissions(activity,
                permission,
                permissionsReqCode);
    }

    /*Use to show permission explanation when it is needed to user*/
    private static void showExplanationDialogForPermission(final Activity activity, final String permission, final int permissionReqCode) {

        //Dialog lExplanationDialog = explanationDialog(activity, permission, permissionReqCode);
        switch (permission) {
            case Manifest.permission.RECORD_AUDIO:
                //lExplanationDialog.findViewById(R.id.dialog_explanationContainer_Row6).setVisibility(View.VISIBLE);
                break;
        }

    }

}
