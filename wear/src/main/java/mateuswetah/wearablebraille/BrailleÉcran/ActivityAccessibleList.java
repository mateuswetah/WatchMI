package mateuswetah.wearablebraille.BrailleÉcran;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.annotation.RequiresApi;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Locale;

import mateuswetah.wearablebraille.GestureDetectors.OneFingerDoubleTapDetector;
import mateuswetah.wearablebraille.GestureDetectors.Swipe4DirectionsDetector;
import mateuswetah.wearablebraille.GestureDetectors.TwoFingersSwipeDetector;
import mateuswetah.wearablebraille.R;

public class ActivityAccessibleList extends WearableActivity {

    // View Components
    RelativeLayout accessibleListContainer;
    MyScrollView scrollView;
    LinearLayout linearLayout;
    ArrayList<Button> itemButtons;

    String[] items;
    int selectedIndex = 0;
    GestureDetector gestureDetector;
    TwoFingersSwipeDetector twoFingersSwipeListener;
    OneFingerDoubleTapDetector doubleTapDetector;

    // Feedback Tools
    boolean isTTSInitialized = false;
    private Vibrator vibrator = null;
    private CharacterToSpeech tts;
    MediaPlayer mediaPlayer;
    PlaybackParams mediaParams;

    ActivityAccessibleList activity;
    private CharSequence introSpeakingSentence;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accessible_list);
        this.activity = this;

        // Updates settings variables
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            items = (String[]) extras.get("items");
            Log.d("RECEBENDO: ", "_" + items[0] + "..." + items[items.length - 1] + "_");
            introSpeakingSentence = (CharSequence) extras.get("introSpeakingSentence");
        };

        // Sets up media player feedback
        mediaParams = new PlaybackParams();
        mediaParams.setPitch(0.5f);

        // A transparent layer above view to handle some events not related to buttons
        ImageView img = new ImageView(this);
        img.setBackgroundColor(Color.TRANSPARENT);
        img.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                boolean shouldStopPropagation = false;

                if (twoFingersSwipeListener.onTouchEvent(motionEvent))
                    shouldStopPropagation = true;
                if (!gestureDetector.onTouchEvent(motionEvent)) {
                    if (doubleTapDetector.onTouchEvent(motionEvent)) {
                        shouldStopPropagation = true;
                    }
                } else {
                    shouldStopPropagation = true;
                }
                return true;
            }
        });
        accessibleListContainer = (RelativeLayout) findViewById(R.id.accessible_list_container);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        accessibleListContainer.addView(img, params);

        // The ScrollView
        scrollView = (MyScrollView) findViewById(R.id.options_scroll_view);
        scrollView.setSmoothScrollingEnabled(true);
        scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View view, int previousHorizontal, int previousVertical, int currentHorizontal, int currentVertical) {

                if (previousHorizontal != currentHorizontal || previousVertical != currentVertical) {
                    if (currentVertical < previousVertical || currentHorizontal < previousHorizontal) {
                        // Audio feedback for scroll list up navigation
                        mediaPlayer = MediaPlayer.create(activity, R.raw.scroll_tone);
                        mediaPlayer.setVolume(1.0f,1.0f);
                        try {
                            mediaParams.setPitch((mediaParams.getPitch() + 0.1f));
                            mediaPlayer.setPlaybackParams(mediaParams);
                        } catch (IllegalArgumentException exception) {
                            Log.d("PITCH", "Error: " + exception.getMessage());
                        }
                        mediaPlayer.start();
                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                mediaPlayer.release();
                            }
                        });
                    } else {
                        // Audio feedback for scroll list down navigation
                        mediaPlayer = MediaPlayer.create(activity, R.raw.scroll_tone);
                        try {
                            if (mediaParams.getPitch() > 0.1) {
                                mediaParams.setPitch(mediaParams.getPitch() - 0.1f);
                            }
                            mediaPlayer.setPlaybackParams(mediaParams);
                        } catch (IllegalArgumentException exception) {
                            Log.d("PITCH", "Error: " + exception.getMessage());
                        }
                        mediaPlayer.start();
                        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                mediaPlayer.release();
                            }
                        });
                    }
                }
            }
        });

        // Dynamically adds and set up buttons
        linearLayout = (LinearLayout) findViewById(R.id.items_list);
        itemButtons = new ArrayList<>();
        for (int i = 0; i < items.length; i++) {
            final Button itemButton = new Button(this);
            final int currentIndex = i;
            itemButton.setText(items[i]);
            itemButton.setTextColor(getColor(R.color.black));
            itemButton.setBackgroundResource(R.color.white);

            // Click Listener
            itemButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
                            selectedIndex = currentIndex;
                            if (isTTSInitialized) {
                                tts.speak(items[selectedIndex], TextToSpeech.QUEUE_ADD, null, "item_option");
                            }
                            itemButton.setBackgroundResource(R.drawable.braille_ecran_button);
                            for (int j = 0; j < items.length; j++) {
                                if (currentIndex != j) {
                                    itemButtons.get(j).setBackgroundResource(R.color.white);
                                }
                            }
                            // Audio feedback for list item selection
                            mediaPlayer = MediaPlayer.create(activity, R.raw.focus_actionable);
                            mediaPlayer.setVolume(1.0f,1.0f);
                            mediaPlayer.start();
                            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mediaPlayer) {
                                    mediaPlayer.release();
                                }
                            });

                            // Scrolls if necessary
                            scrollView.scrollTo((int) itemButton.getX(), (int) itemButton.getY());
                        }
//                    }, ViewConfiguration.getDoubleTapTimeout() + 200);
//                }
            });
            itemButtons.add(itemButton);
            LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            linearLayout.addView(itemButton, linearParams);

        }

        twoFingersSwipeListener = new TwoFingersSwipeDetector() {
            @Override
            protected void onTwoFingersSwipeLeft() { }

            @Override
            protected void onTwoFingersSwipeRight() {
                Intent data = new Intent();
                setResult(RESULT_CANCELED, data);
                finish();
            }
        };

        doubleTapDetector = new OneFingerDoubleTapDetector() {
            @Override
            public void onOneFingerDoubleTap() {
                Log.d("DOUBLE TAP", items[selectedIndex]);
                if (isTTSInitialized) {
                    tts.speak(items[selectedIndex], TextToSpeech.QUEUE_FLUSH, null, "item_option");
                }
                Intent data = new Intent();
                data.putExtra("selectedIndex", selectedIndex);
                data.putExtra("selectedItem", items[selectedIndex]);
                setResult(RESULT_OK, data);
                finish();
            }
        };
        gestureDetector = new GestureDetector(this, new Swipe4DirectionsDetector() {
            @Override
            public void onTopSwipe() {
                if (selectedIndex > 3) {
                    selectedIndex = selectedIndex - 4;
                    itemButtons.get(selectedIndex).callOnClick();
                } else if (selectedIndex > 0 && selectedIndex <= 3) {
                    selectedIndex = 0;
                    itemButtons.get(selectedIndex).callOnClick();
                } else {
                    mediaPlayer = MediaPlayer.create(activity, R.raw.complete);
                    mediaPlayer.start();
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            mediaPlayer.release();
                        }
                    });
                }
            }

            @Override
            public void onRightSwipe() {
                // Index switch and button click
                if (selectedIndex <= items.length - 2) {
                    selectedIndex++;
                    itemButtons.get(selectedIndex).callOnClick();
                } else {
                    mediaPlayer = MediaPlayer.create(activity, R.raw.complete);
                    mediaPlayer.start();
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            mediaPlayer.release();
                        }
                    });
                }
            }

            @Override
            public void onLeftSwipe() {
                if (selectedIndex > 0) {
                    selectedIndex--;
                    itemButtons.get(selectedIndex).callOnClick();
                } else {
                    mediaPlayer = MediaPlayer.create(activity, R.raw.complete);
                    mediaPlayer.start();
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            mediaPlayer.release();
                        }
                    });
                }
            }

            @Override
            public void onBottomSwipe() {
                if (selectedIndex <= items.length - 5) {
                    selectedIndex = selectedIndex + 4;
                    itemButtons.get(selectedIndex).callOnClick();
                } else if (selectedIndex > items.length - 5 && selectedIndex <= items.length - 2) {
                    selectedIndex = items.length - 1;
                    itemButtons.get(selectedIndex).callOnClick();
                } else {
                    mediaPlayer = MediaPlayer.create(activity, R.raw.complete);
                    mediaPlayer.start();
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            mediaPlayer.release();
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onPause() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            isTTSInitialized = false;
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Sets vibrator
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Sets TextToSpeech for feedback
        tts = new CharacterToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    Log.d("TTS", "TextToSpeech Service Initialized");
                    isTTSInitialized = true;

                    tts.setLanguage(Locale.getDefault());
                    // Sentence speaking Intro
                    tts.speak(introSpeakingSentence, TextToSpeech.QUEUE_FLUSH, null, "items_intro");
                    // Informs first button
                    itemButtons.get(selectedIndex).callOnClick();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            isTTSInitialized = false;
        }
        super.onDestroy();
    }

}
