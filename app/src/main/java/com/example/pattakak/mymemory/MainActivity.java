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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import java.util.List;
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

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "aiDataService";
    private static final int REQUEST_RECORD_PERMISSION = 100;
    private final static int REQUEST_VOICE_RECOGNITION = 10001;
    private ImageView ivSend;
    private ImageView ivSpeak;
    private TextInputEditText etInput;
    RecyclerView rv;

    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private String LOG_TAG = "VoiceRecognitionActivity";
    private Boolean statusSpeech = false;
    Context context;
    List<Object> dataObj;

    AIDataService aiDataService;
    TextSpeechAPI textSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etInput = (TextInputEditText) findViewById(R.id.et_input);
        ivSend = (ImageView) findViewById(R.id.iv_send);
        ivSpeak = (ImageView) findViewById(R.id.iv_speak);
        rv = (RecyclerView) findViewById(R.id.rv);


        context = MainActivity.this;

        textSpeech = TextSpeechAPI.getInstance(context);

        final AIConfiguration config = new AIConfiguration("1deb6b8f7d514f9baf545a36a38e82a6",
                AIConfiguration.SupportedLanguages.fromLanguageTag("TH"),
                AIConfiguration.RecognitionEngine.System);
        aiDataService = new AIDataService(config);


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
        ivSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "th-TH");
                startActivityForResult(intent, REQUEST_VOICE_RECOGNITION);
            }
        });

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(llm);
        dataObj = new ArrayList<Object>();


    }

    private void setChat(String text) {
        dataObj.add(text);
        RV_Adapter_Chat adapterList = new RV_Adapter_Chat(dataObj);
        rv.setAdapter(adapterList);

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

    private void callDialogFlow(String query) {
        final AIRequest aiRequest = new AIRequest();
        aiRequest.setQuery(query);
        setChat(query);

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

                    setChat(speech);

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
            ActivityCompat.requestPermissions
                    (MainActivity.this,
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            REQUEST_RECORD_PERMISSION);
//                    speech.startListening(recognizerIntent);
        } else {
            statusSpeech = false;
            speech.stopListening();

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        }

    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_VOICE_RECOGNITION &&
                resultCode == RESULT_OK &&
                data != null) {
            ArrayList<String> resultList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            callDialogFlow(resultList.get(0));

        }
    }


}
