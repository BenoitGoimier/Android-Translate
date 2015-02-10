package fr.benoitgoimier.translate;

import java.util.Locale;

import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ChildActivity extends Activity implements OnInitListener, GestureDetector.OnDoubleTapListener, GestureDetector.OnGestureListener {

    private Button btnOuvrir;
    private Button btnRetour;
    private TextView txtTitre;
    private TextToSpeech tts;
    private GestureDetector gesture;
    private static final String Client_ID = "net-michelmarie-net-appliTPtranslate";
    private static final String Client_SECRET = "lEZWZhfjuoiiwLMZbOHjsyOXC8Te9eVZOA7+/9Y4NLw=";
    private String motSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child);

        // Pour la capture du double tap
        gesture = new GestureDetector(this,this);
        gesture.setOnDoubleTapListener(this);

        // Pour la lecture du texte traduit
        tts = new TextToSpeech(this, this);
        motSource = getIntent().getStringExtra(fr.benoitgoimier.translate.MainActivity.PARAM_SOURCE);
        txtTitre = (TextView)findViewById(R.id.TextViewTitreMotSource);
        txtTitre.setText(txtTitre.getText().toString()+" "+motSource);

        Translate.setClientId(ChildActivity.Client_ID);
        Translate.setClientSecret(ChildActivity.Client_SECRET);
        translateFrtoEn(motSource);

        // Afficher un toast
        btnOuvrir =(Button)findViewById(R.id.buttonTraduireChild);
        btnOuvrir.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v) {
                try
                {
                    String motReponse = ((EditText)findViewById(R.id.mot_edittextChild)).getText().toString();
                    //speakFrenchAndEnglish(getResources().getString(R.string.phraseFormat,motSource,motReponse));
                    speakFrenchAndEnglish(getResources().getString(R.string.phraseFormatFR,motSource),Locale.FRANCE);
                    speakFrenchAndEnglish(motReponse,Locale.ENGLISH);
                }
                catch(Exception ex)
                {
                    Log.d("TTS","Exception..."+ex.getMessage());
                }

            }
        });

        btnRetour =(Button)findViewById(R.id.buttonRetourChild);
        btnRetour.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v) {
                String motReponse = ((EditText)findViewById(R.id.mot_edittextChild)).getText().toString();
                Intent retour = new Intent();
                retour.putExtra(fr.benoitgoimier.translate.MainActivity.PARAM_REPONSE, motReponse);
                setResult(fr.benoitgoimier.translate.MainActivity.RESULT_OK,retour);
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void translateFrtoEn(final String phraseAtraduire)
    {
        new AsyncTask<Void, Void, Void>(){
            String translateResult = "";
            @Override
            protected Void doInBackground(Void... params){
                try
                {
                    translateResult = Translate.execute(phraseAtraduire, Language.FRENCH, Language.ENGLISH);
                }
                catch(Exception ex)
                {
                    translateResult=ex.getMessage();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result){
                super.onPostExecute(result);
                // Mise � jour de la r�ponse
                ((EditText)findViewById(R.id.mot_edittextChild)).setText(translateResult);

            }
        }.execute(null,null,null);
    }

    private void speakFrenchAndEnglish(String textToSpeak, Locale locale)
    {
        tts.setLanguage(locale);
        tts.speak(textToSpeak, TextToSpeech.QUEUE_ADD, null);
    }

    @Override
    public void onInit(int status)
    {
        if(status==TextToSpeech.SUCCESS)
        {
            int resInit;
            String langueConfig = Locale.getDefault().getLanguage();
            if(langueConfig.equals( Locale.ENGLISH.getLanguage()))
            {
                resInit = tts.setLanguage(Locale.ENGLISH);
            }
            else
                resInit = tts.setLanguage(Locale.FRENCH);
            if(resInit == TextToSpeech.LANG_MISSING_DATA || resInit ==TextToSpeech.LANG_NOT_SUPPORTED )
            {
                Log.d("TTS","Langage non support�...");
            }
        }
        else
            Log.d("TTS","INIT ERREUR...");
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        tts.shutdown();
    }

    // Gesture - Premi�re �tape : d�tecter les �v�nements "touch" sur l'activit�
    // Signaler l'�v�nement � l'objet de prise en charge de la gesture
    @Override
    public boolean onTouchEvent(MotionEvent event){
        this.gesture.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        try
        {
            String motReponse = ((EditText)findViewById(R.id.mot_edittextChild)).getText().toString();
            //speakFrenchAndEnglish(getResources().getString(R.string.phraseFormat,motSource,motReponse));
            speakFrenchAndEnglish(getResources().getString(R.string.phraseFormatFR,motSource),Locale.FRANCE);
            speakFrenchAndEnglish(motReponse,Locale.ENGLISH);
        }
        catch(Exception ex)
        {
            Log.d("TTS","Exception..."+ex.getMessage());
        }
        return true;
    }
    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }
    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }
    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,float velocityY) {
        return false;
    }
    @Override
    public void onLongPress(MotionEvent e) {
    }
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {
        return false;
    }
    @Override
    public void onShowPress(MotionEvent e) {
    }
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }
}
