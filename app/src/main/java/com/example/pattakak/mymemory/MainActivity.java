package com.example.pattakak.mymemory;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Metadata;
import ai.api.model.Result;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, RecognitionListener {

    private static final String TAG = "aiDataService";
    private static final int REQUEST_RECORD_PERMISSION = 100;
    private TextView returnedText;
    private FloatingActionButton btnFab;
    private ProgressBar progressBar;
    private ImageView ivSend;
    private TextInputEditText etInput;

    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private String LOG_TAG = "VoiceRecognitionActivity";
    private Boolean statusSpeech = false;
    Context context;

    AIDataService aiDataService;
    TextSpeechAPI textSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        returnedText = (TextView) findViewById(R.id.textView1);
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        btnFab = (FloatingActionButton) findViewById(R.id.fab);
        etInput = (TextInputEditText) findViewById(R.id.et_input);
        ivSend = (ImageView) findViewById(R.id.iv_send);


        context = MainActivity.this;

        progressBar.setVisibility(View.INVISIBLE);
        textSpeech = TextSpeechAPI.getInstance(context);

        createSpeech();

        final AIConfiguration config = new AIConfiguration("1deb6b8f7d514f9baf545a36a38e82a6",
                AIConfiguration.SupportedLanguages.fromLanguageTag("TH"),
                AIConfiguration.RecognitionEngine.System);
        aiDataService = new AIDataService(config);

        btnFab.setOnClickListener(this);

        ivSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = etInput.getText().toString();
                if (!input.equals("")) {
                    callDialogFlow(input);
                    etInput.setText("");
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    speech.startListening(recognizerIntent);
                } else {
                    Toast.makeText(MainActivity.this, "Permission Denied!", Toast
                            .LENGTH_SHORT).show();
//                    speech.startListening(recognizerIntent);
                }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (speech != null) {
            speech.destroy();
            Log.i(LOG_TAG, "destroy");
        }
    }


    @Override
    public void onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech");
        progressBar.setIndeterminate(false);
        progressBar.setMax(10);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i(LOG_TAG, "onBufferReceived: " + buffer);
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(LOG_TAG, "onEndOfSpeech");
        progressBar.setIndeterminate(true);
        setChange(false);

    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        Log.d(LOG_TAG, "FAILED " + errorMessage + " - " + errorCode);
        returnedText.setText(errorMessage);
        setChange(false);
        setBtnOffSpeech();
        speech.destroy();

    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.i(LOG_TAG, "onEvent");
    }

    @Override
    public void onPartialResults(Bundle arg0) {
        Log.i(LOG_TAG, "onPartialResults");
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Log.i(LOG_TAG, "onReadyForSpeech");
    }

    @Override
    public void onResults(Bundle results) {
        Log.i(LOG_TAG, "onResults");
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String query = "";
        for (String result : matches) {
            query = result;
            break;
        }
        setBtnOffSpeech();
        callDialogFlow(query);
    }

    private void callDialogFlow(String query) {
        final AIRequest aiRequest = new AIRequest();
        aiRequest.setQuery(query);

        new AsyncTask<AIRequest, Void, AIResponse>() {
            @Override
            protected AIResponse doInBackground(AIRequest... requests) {
                final AIRequest request = requests[0];
                try {
                    Log.d("aiDataService", "++++++++++++++++++++++++++++++++ test");
                    final AIResponse response = aiDataService.request(aiRequest);

                    ai.api.model.Status status = response.getStatus();
                    Log.i(TAG, "Status code: " + status.getCode());
                    Log.i(TAG, "Status type: " + status.getErrorType());

                    return response;
                } catch (AIServiceException e) {
                }
                return null;
            }

            @Override
            protected void onPostExecute(AIResponse aiResponse) {
                if (aiResponse != null) {
                    // process aiResponse here
                    Log.i(TAG, "onPostExecute: ================================");
                    Result result = aiResponse.getResult();

                    Log.i(TAG, "onPostExecute Resolved query: " + result.getResolvedQuery());
                    Log.i(TAG, "onPostExecute Action: " + result.getAction());

                    final String speech = result.getFulfillment().getSpeech();
                    Log.i(TAG, "onPostExecute: " + speech);

                    Metadata metadata = result.getMetadata();
                    if (metadata != null) {
                        Log.i(TAG, "Intent id: " + metadata.getIntentId());
                        Log.i(TAG, "Intent name: " + metadata.getIntentName());
                    }

                    HashMap<String, JsonElement> params = result.getParameters();
                    if (params != null && !params.isEmpty()) {
                        Log.i(TAG, "Parameters: ");
                        for (final Map.Entry<String, JsonElement> entry : params.entrySet()) {
                            Log.i(TAG, String.format("%s: %s", entry.getKey(), entry.getValue().toString()));
                        }
                    }


                    returnedText.setText(speech);

                }
            }
        }.execute(aiRequest);
    }

    public void Mute() {
        Log.d("Mute", "++++++++++++++++++++++++++++++++++++++++++++++++");
        AudioManager amanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        amanager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
        amanager.setStreamMute(AudioManager.STREAM_ALARM, true);
        amanager.setStreamMute(AudioManager.STREAM_MUSIC, true);
        amanager.setStreamMute(AudioManager.STREAM_RING, true);
        amanager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
    }

    public void Unmute() {
        Log.d("Unmute", "++++++++++++++++++++++++++++++++++++++++++++++++");
        AudioManager amanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        amanager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
        amanager.setStreamMute(AudioManager.STREAM_ALARM, false);
        amanager.setStreamMute(AudioManager.STREAM_MUSIC, false);
        amanager.setStreamMute(AudioManager.STREAM_RING, false);
        amanager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
        progressBar.setProgress((int) rmsdB);
    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

    public void setChange(Boolean isChecked) {
        Log.d("CheckChange", "+++++++++++++" + isChecked);
        if (isChecked) {
            statusSpeech = true;
            createSpeech();
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setIndeterminate(true);
            ActivityCompat.requestPermissions
                    (MainActivity.this,
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            REQUEST_RECORD_PERMISSION);
//                    speech.startListening(recognizerIntent);
        } else {
            statusSpeech = false;
            progressBar.setIndeterminate(false);
            progressBar.setVisibility(View.INVISIBLE);
            speech.stopListening();

        }
    }

    public void createSpeech() {
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        Log.i(LOG_TAG, "isRecognitionAvailable: " + SpeechRecognizer.isRecognitionAvailable(this));
        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "th-TH");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                setBtnOnSpeech();
                break;
        }

    }

    public void setBtnOnSpeech(){
        setChange(true);
        btnFab.setImageResource(R.drawable.ic_stop_white);
        btnFab.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
        Log.i(LOG_TAG, "setBtnOnSpeech: ========================================");
    }

    public void setBtnOffSpeech(){
        setChange(false);
        btnFab.setImageResource(R.drawable.ic_mic_white);
        btnFab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorPrimary)));
        Log.i(LOG_TAG, "setBtnOffSpeech: ========================================");
    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

}
