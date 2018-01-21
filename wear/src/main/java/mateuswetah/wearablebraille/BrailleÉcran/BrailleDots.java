package mateuswetah.wearablebraille.BrailleÉcran;

import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;

import mateuswetah.wearablebraille.R;

/**
 * Created by mateus on 9/1/15.
 */
public class BrailleDots {

    // Create an array of six initially invisible OutputDots and associate them with the XML
    public final ImageButton ButtonDots[] = new ImageButton[6];

    // An array that stores the 44 possible summations.
    private ArrayList<Integer> SummedValueDots = new ArrayList<Integer>();

    // List of 44 Dots Objects, that hold the information read from XML
    private List<Dots> DotsList;

    private WearableActivity activity;

    private int nSymbols = 50; // Number of symbols listed in the XML

    // Constructor
    public BrailleDots(WearableActivity activity) {

        this.activity = activity;

        // Uses the XMLPullParser to fill the Dots list with Dots Objects
        DotsList = DotsXMLPullParser.getStaticDotsFromFile(activity);

        // Fill the array with the 45 possible summations
        for (int i = 0; i < nSymbols; i++) {
            SummedValueDots.add(DotsList.get(i).getSumValue());
        }

        ButtonDots[0] = (ImageButton) activity.findViewById(R.id.dotButton1);
        ButtonDots[0].setTag(new Boolean(false));
        ButtonDots[0].setImageDrawable(activity.getDrawable(R.drawable.dot_unactive));

        ButtonDots[1] = (ImageButton) activity.findViewById(R.id.dotButton2);
        ButtonDots[1].setTag(new Boolean(false));
        ButtonDots[1].setImageDrawable(activity.getDrawable(R.drawable.dot_unactive));

        ButtonDots[2] = (ImageButton) activity.findViewById(R.id.dotButton3);
        ButtonDots[2].setTag(new Boolean(false));
        ButtonDots[2].setImageDrawable(activity.getDrawable(R.drawable.dot_unactive));

        ButtonDots[3] = (ImageButton) activity.findViewById(R.id.dotButton4);
        ButtonDots[3].setTag(new Boolean(false));
        ButtonDots[3].setImageDrawable(activity.getDrawable(R.drawable.dot_unactive));

        ButtonDots[4] = (ImageButton) activity.findViewById(R.id.dotButton5);
        ButtonDots[4].setTag(new Boolean(false));
        ButtonDots[4].setImageDrawable(activity.getDrawable(R.drawable.dot_unactive));

        ButtonDots[5] = (ImageButton) activity.findViewById(R.id.dotButton6);
        ButtonDots[5].setTag(new Boolean(false));
        ButtonDots[5].setImageDrawable(activity.getDrawable(R.drawable.dot_unactive));

    }

    // Appear or disappear with the Dots
    public boolean toggleDotVisibility(int i){

        if (((Boolean)ButtonDots[i].getTag()) == false) {
            ButtonDots[i].setTag(new Boolean(true));
            ButtonDots[i].setImageDrawable(activity.getDrawable(R.drawable.dot_active));
            return true;
        } else {
            ButtonDots[i].setTag(new Boolean(false));
            ButtonDots[i].setImageDrawable(activity.getDrawable(R.drawable.dot_unactive));
            return false;
        }
    }

    // Change dot visibility, regardless of it's current state.
    public void setDotVisibility(int i, boolean value){

        if (value == false)
            ButtonDots[i].setImageDrawable(activity.getDrawable(R.drawable.dot_unactive));
        else {
            ButtonDots[i].setImageDrawable(activity.getDrawable(R.drawable.dot_active));
        }
    }

    // Disappear all the Dots
    public void toggleAllDotsOff() {

        for (int i = 0; i < 6; i++) {
            ButtonDots[i].setImageDrawable(activity.getDrawable(R.drawable.dot_unactive));
            ButtonDots[i].setTag(new Boolean(false));
        }

    }

    // Called in MainActivity every time a Dot is added or removed.
    public String checkCurrentCharacter(boolean CapsOn, boolean tmpCapsOn, boolean NumOn, boolean tmpNumOn) {

        String latimOutput = "";
        int currentSum = 0, indexLatimOutput = 0;

        // Reads the current visibility of the dots and associates it with a summed value
        for (int i = 0; i < 6; i++) {
            if (((Boolean)ButtonDots[i].getTag()) == true)
                currentSum += Math.pow(2, i);
        }

        // Looks for which index of the SummedValuesDots vector equals the sum of the current Dots
        for (; indexLatimOutput < nSymbols; indexLatimOutput++) {
            if (SummedValueDots.get(indexLatimOutput) == currentSum) {
                break;
            }
        }
        //Log.d("LATIM_OUTPUT", "Current: " + indexLatimOutput);
        // Gets from the Dots object the relation between the found summation and a Symbol
        // the indexLatimOutput is checked to see if it's a valid character.
        if (indexLatimOutput < nSymbols) {
            latimOutput = DotsList.get(indexLatimOutput).getDotSymbol();

            // Removing Settings from the list, making it an Invalid Character
            if (latimOutput.equals("Cf") && CapsOn == false && tmpCapsOn == false)
                latimOutput = "";

            // Removing Help from the list, making it an Invalid Character
            if (latimOutput.equals("?!") && NumOn == false && tmpNumOn == false)
                latimOutput = "";

            // Apply the flags changes:
            if (CapsOn || tmpCapsOn)
                if (latimOutput != " ") // workaround, uoUpperCase() seems to remove white space
                    latimOutput = latimOutput.toUpperCase();

            if (NumOn || tmpNumOn) {

                switch (latimOutput)
                {
                    case "a":
                        latimOutput = "1";
                        break;
                    case "b":
                        latimOutput = "2";
                        break;
                    case "c":
                        latimOutput = "3";
                        break;
                    case "d":
                        latimOutput = "4";
                        break;
                    case "e":
                        latimOutput = "5";
                        break;
                    case "f":
                        latimOutput = "6";
                        break;
                    case "g":
                        latimOutput = "7";
                        break;
                    case "h":
                        latimOutput = "8";
                        break;
                    case "i":
                        latimOutput = "9";
                        break;
                    case " ":
                        latimOutput = " ";
                        break;
                    case "j":
                        latimOutput = "0";
                        break;
                    case "A":
                        latimOutput = "1";
                        break;
                    case "B":
                        latimOutput = "2";
                        break;
                    case "C":
                        latimOutput = "3";
                        break;
                    case "D":
                        latimOutput = "4";
                        break;
                    case "E":
                        latimOutput = "5";
                        break;
                    case "F":
                        latimOutput = "6";
                        break;
                    case "G":
                        latimOutput = "7";
                        break;
                    case "H":
                        latimOutput = "8";
                        break;
                    case "I":
                        latimOutput = "9";
                        break;
                    case "J":
                        latimOutput = "0";
                        break;
                    case ".":
                        latimOutput = ".";
                        break;
                    case "-":
                        latimOutput = "-";
                        break;
                    case "?":
                        latimOutput = "?";
                        break;
                    case "!":
                        latimOutput = "!";
                        break;
                    case "%":
                        latimOutput = "%";
                        break;
                    case ",":
                        latimOutput = ",";
                        break;
                    case ":":
                        latimOutput = ":";
                        break;
                    case "Cf":
                        latimOutput = "Cf";
                        break;
                    case "CF":
                        latimOutput = "Cf";
                        break;
                    case "?!":
                        latimOutput = "?!";
                        break;
                    default:
                        latimOutput = "In";
                        break;

                }
            }

        }

        Log.d("BRAILLE_DOTS", "Output: " + latimOutput);
        return latimOutput;
    }

}
