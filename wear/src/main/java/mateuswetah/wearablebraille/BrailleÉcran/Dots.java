package mateuswetah.wearablebraille.BrailleÉcran;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mateus on 9/25/15.
 */
// This object holds six dots read and parsed from the XML
public class Dots {

    private String dotSymbol;
    private List<String> indexes;
    private int sumValue;

    public Dots() {
        indexes = new ArrayList<String>();
        sumValue = 0;
        dotSymbol = "";
    }

    public int getSumValue() {

        int numOfDots = indexes.size();
        for (int i = 0; i < numOfDots; i++) {

            int indexValue = Integer.parseInt(indexes.get(i).toString());
            sumValue += (Math.pow(2,(indexValue - 1)));
        }

        return sumValue;
    }

    public void setDotSymbol(String charSymbol) {
        dotSymbol = charSymbol;
    }

    public String getDotSymbol() {

        return dotSymbol;
    }

    public void addIndexToDot (String index) {
        indexes.add(index);
    }

}
