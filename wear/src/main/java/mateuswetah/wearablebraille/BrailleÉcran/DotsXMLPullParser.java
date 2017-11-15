package mateuswetah.wearablebraille.BrailleÉcran;

import android.content.Context;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mateus on 9/25/15.
 */
public class DotsXMLPullParser {

    static final String KEY_DOT = "dots";
    static final String KEY_SYMBOL = "symbol";
    static final String KEY_INDEX = "index";

    public static List<Dots> getStaticDotsFromFile(Context ctx){

        // Dots objects that will hold the information read from the XML
        List<Dots> dots;
        dots = new ArrayList<Dots>();

        Dots currentDot = null;
        String currentText = "";

        try {
            // Factory to build the XMLPullParser
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();

            // Buffers, receive the raw XML file.
            InputStream fis = ctx.getAssets().open("brailleValues.xml");
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));

            // Associates the parser with the read XML string
            parser.setInput(reader);

            // Used to analyse current state fo the XML parsing
            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {

                // Current Tag, jumps between they with parser.next() in the end.
                String tagName = parser.getName();

                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        // A <dots> tag begin, we need to create a new Dot Object.
                        if (tagName.equalsIgnoreCase(KEY_DOT)) {
                            currentDot = new Dots();
                        }
                        break;

                    case XmlPullParser.TEXT:
                        // get content of a tag
                        currentText = parser.getText();
                        break;

                    case XmlPullParser.END_TAG:

                        if (tagName.equalsIgnoreCase(KEY_DOT)) {
                            // Done storing the <dots> tag, store the object in the list
                            if (currentDot != null) {
                                dots.add(currentDot);
                            }
                        } else if (tagName.equalsIgnoreCase(KEY_SYMBOL)) {
                            // put the Symbol content on the Dots Object
                            currentDot.setDotSymbol(currentText);
                        }else if (tagName.equalsIgnoreCase(KEY_INDEX)) {
                            // put the Index content on the Dots Object
                            currentDot.addIndexToDot(currentText);
                        }
                        break;

                    default:
                        break;

                }

                eventType = parser.next();
            }

        }catch (Exception e) {
            e.printStackTrace();
        }

        return dots;
    }
}
