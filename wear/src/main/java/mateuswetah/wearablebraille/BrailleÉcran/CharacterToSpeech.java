package mateuswetah.wearablebraille.BrailleÉcran;

import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;

import mateuswetah.wearablebraille.R;

public class CharacterToSpeech extends TextToSpeech {
    
    Context context;
    
    public CharacterToSpeech(Context context, OnInitListener listener) {
        super(context, listener);
        this.context = context;
    }

    public CharacterToSpeech(Context context, OnInitListener listener, String engine) {
        super(context, listener, engine);
        this.context = context;
    }

    public CharSequence getAdaptedText(String text) {

        CharSequence adaptedText = text;

        if (!text.equals("") &&
                !text.equals("Nu") && !text.equals("Ma") && !text.equals("In") && !text.equals("Cf") && !text.equals("?!") &&
                !text.equals("NU") && !text.equals("MA") && !text.equals("IN") && !text.equals("CF")) {

            if (text.equals(" ")) {
                adaptedText = context.getResources().getString(R.string.WhiteSpaceSpeech);
            } else if (text.equals("!")) {
                adaptedText = context.getResources().getString(R.string.ExclamationSpeech);
            } else if (text.equals("?")) {
                adaptedText = context.getResources().getString(R.string.InterrogationSpeech);
            } else if (text.equals(".")) {
                adaptedText = context.getResources().getString(R.string.PeriodSpeech);
            } else if (text.equals("-")) {
                adaptedText = context.getResources().getString(R.string.HyphenSpeech);
            } else if (text.equals(",")) {
                adaptedText = context.getResources().getString(R.string.CommaSpeech);
            } else if (text.equals(":")) {
                adaptedText = context.getResources().getString(R.string.ColonSpeech);
            } else if (text.equals(";")) {
                adaptedText = context.getResources().getString(R.string.SemiColonSpeech);
            } else if (text.equals("-")) {
                adaptedText = context.getResources().getString(R.string.HyphenSpeech);
            } else if (text.equals("@")) {
                adaptedText = context.getResources().getString(R.string.AtSpeech);
            } else if (text.equals("\\")) {
                adaptedText = context.getResources().getString(R.string.BackSlashSpeech);
            } else if (text.equals("ç")) {
                adaptedText = context.getResources().getString(R.string.CedillaCSpeech);
            } else if (text.equals("á")) {
                adaptedText = context.getResources().getString(R.string.AcuteASpeech);
            } else if (text.equals("é")) {
                adaptedText = context.getResources().getString(R.string.AcuteESpeech);
            } else if (text.equals("í")) {
                adaptedText = context.getResources().getString(R.string.AcuteISpeech);
            } else if (text.equals("ó")) {
                adaptedText = context.getResources().getString(R.string.AcuteOSpeech);
            } else if (text.equals("ú")) {
                adaptedText = context.getResources().getString(R.string.AcuteUSpeech);
            } else if (text.equals("â")) {
                adaptedText = context.getResources().getString(R.string.CircumflexASpeech);
            } else if (text.equals("ê")) {
                adaptedText = context.getResources().getString(R.string.CircumflexESpeech);
            } else if (text.equals("ô")) {
                adaptedText = context.getResources().getString(R.string.CircumflexOSpeech);
            } else if (text.equals("ã")) {
                adaptedText = context.getResources().getString(R.string.TildeASpeech);
            } else if (text.equals("õ")) {
                adaptedText = context.getResources().getString(R.string.TildeOSpeech);
            } else if (text.equals("à")) {
                adaptedText = context.getResources().getString(R.string.CrasisASpeech);
            } else if (text.equals("In")) {
                adaptedText = context.getResources().getString(R.string.InvalidCharacterAlert);
            }
        }

        return adaptedText;
    }

    public String getAdaptedUtteranceId(CharSequence text, String utteranceId) {
        String adaptedUtteranceId = utteranceId;

        if (!text.equals("") &&
                !text.equals("Nu") && !text.equals("Ma") && !text.equals("In") && !text.equals("Cf") && !text.equals("?!") &&
                !text.equals("NU") && !text.equals("MA") && !text.equals("IN") && !text.equals("CF")) {

            if (text.equals(" ")) {
                adaptedUtteranceId = "White Space Character Output";
            } else if (text.equals("!")) {
                adaptedUtteranceId = "Exclamation Sign Output";
            } else if (text.equals("?")) {
                adaptedUtteranceId = "Interrogation Sign Output";
            } else if (text.equals(".")) {
                adaptedUtteranceId = "Period Sign Output";
            } else if (text.equals("-")) {
                adaptedUtteranceId = "Hyphen Sign Output";
            } else if (text.equals(",")) {
                adaptedUtteranceId = "Comma Sign Output";
            } else if (text.equals(":")) {
                adaptedUtteranceId = "Colon Sign Output";
            } else if (text.equals(";")) {
                adaptedUtteranceId = "Semi Colon Sign Output";
            } else if (text.equals("-")) {
                adaptedUtteranceId = "Hyphen Sign Output";
            } else if (text.equals("@")) {
                adaptedUtteranceId = "AT Sign Output";
            } else if (text.equals("\\")) {
                adaptedUtteranceId = "Back Slash Sign Output";
            } else if (text.equals("ç")) {
                adaptedUtteranceId = "Cedille C Sign Output";
            } else if (text.equals("á")) {
                adaptedUtteranceId = "Acute A Sign Output";
            } else if (text.equals("é")) {
                adaptedUtteranceId = "Acute E Sign Output";
            } else if (text.equals("í")) {
                adaptedUtteranceId = "Acute I Sign Output";
            } else if (text.equals("ó")) {
                adaptedUtteranceId = "Acute O Sign Output";
            } else if (text.equals("ú")) {
                adaptedUtteranceId = "Acute U Sign Output";
            } else if (text.equals("â")) {
                adaptedUtteranceId = "Circumflex A Sign Output";
            } else if (text.equals("ê")) {
                adaptedUtteranceId = "Circumflex E Sign Output";
            } else if (text.equals("ô")) {
                adaptedUtteranceId = "Circumflex O Sign Output";
            } else if (text.equals("ã")) {
                adaptedUtteranceId = "Tilde A Sign Output";
            } else if (text.equals("õ")) {
                adaptedUtteranceId = "Tilde O Sign Output";
            } else if (text.equals("à")) {
                adaptedUtteranceId = "Crasis A Sign Output";
            } else if (text.equals("In")) {
                adaptedUtteranceId = "Invalid Character Output";
            }
        }
        return adaptedUtteranceId;
    }

    @Override
    public int speak(CharSequence text, int queueMode, Bundle params, String utteranceId) {

//            if (tmpCapsOn == true) {
//                tmpCapsOn = false;
//                tts.speak(getString(R.string.DeactivatingCapitalLetter), TextToSpeech.QUEUE_FLUSH, null, "Caps Deactivated Message");
//            } else if (tmpNumOn == true) {
//                tmpNumOn = false;
//                tts.speak(getString(R.string.DeactivatingNumbers), TextToSpeech.QUEUE_FLUSH, null, "Numbers Deactivated Message");
//            }
        return super.speak(getAdaptedText((String) text), queueMode, params, getAdaptedUtteranceId(text, utteranceId));
    }
}
