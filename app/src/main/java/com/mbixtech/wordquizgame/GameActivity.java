package com.mbixtech.wordquizgame;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.mbixtech.wordquizgame.db.DatabaseHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

public class GameActivity extends AppCompatActivity {

    private static final String TAG = "GoTSaMaGameActivity";
    private int diff;

    // Class's properties for game logic
    private ArrayList<String> fileNameList;
    private ArrayList<String> quizWordList;
    private ArrayList<String> choiceWords;

    private String answerFileName;
    private int totalGuesses;
    private int score;

    private TextView questionNumberTextView;
    private ImageView questionImageView;
    private TextView answerTextView;
    private TableLayout buttonTableLayout;

    private Random random;
    private Handler handler;

    private Animation shakeAnimation;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase sqLiteDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Map defined variable to inflated instance
        fileNameList = new ArrayList<String>();
        quizWordList = new ArrayList<String>();
        choiceWords = new ArrayList<String>();

        questionNumberTextView = (TextView) findViewById(R.id.questionNumberTextView);
        questionImageView = (ImageView) findViewById(R.id.questionImageView);
        answerTextView = (TextView) findViewById(R.id.answerTextView);
        buttonTableLayout = (TableLayout) findViewById(R.id.buttonTableLayout);

        random = new Random();
        handler = new Handler();


        // Get intent
        Intent intent = getIntent();
        diff = intent.getIntExtra(MainActivity.KEY_DIFF, -1);
        Log.i(TAG, "คุณเลือก " + MainActivity.difficulty[diff]);
        Toast.makeText(GameActivity.this, "คุณเลือก " + MainActivity.difficulty[diff], Toast.LENGTH_LONG).show();


        // Dump intent
        Bundle bundle = intent.getExtras();
        for (String key : bundle.keySet()) {
            Object value = bundle.get(key);
            Log.d(TAG, String.format("%s %s (%s)", key,
                    value.toString(), value.getClass().getName()));
        }

        // animation
        shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake);
        shakeAnimation.setRepeatCount(3);

        // create DB helper object
        dbHelper = new DatabaseHelper(this);
        sqLiteDB = dbHelper.getWritableDatabase();

        getImageFIleNameList();
        startQuiz();



/*        // Get Back button
        ((Button) findViewById(R.id.btnGetBack)).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(GameActivity.this);
                dialog.setTitle("This is a title");
                dialog.setMessage("จะกลับไปหรือป่าววะ แสรด ?");


                // Yes button
                dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Toast.makeText(MainActivity.this, "You've click \"Yes\" button", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(GameActivity.this,MainActivity.class);
                        startActivity(intent);
                        Log.i(TAG, "ปุ่ม Yes โดนกดจ้าาาาา Game !!!");
                    }
                });

                // No button
                dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(GameActivity.this, "You've click \"No\" button", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "ปุ่ม No โดนกดจ้าาาาา Game !!!");
                    }
                });
                dialog.show();
            }
        });*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sqLiteDB.close();
        dbHelper.close();
    }

    private void getImageFIleNameList() {
        AssetManager as = getAssets();
        try {
            String[] categories = as.list("");

            // Read folders
            for (String category : categories) {
                if (!category.contentEquals("images") && !category.contentEquals("sounds") && !category.contentEquals("webkit")) {
                    String[] files = as.list(category);
                    Log.i(TAG, "Category is " + category);

                    // Read each file from a specific folder
                    for (String file : files) {
                        fileNameList.add(file.replace(".png", ""));
                        //Log.i(TAG, "File name is " + file);
                    }

                }
            }
            //Log.i(TAG, String.valueOf(fileNameList));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void startQuiz() {
        score = 0;
        totalGuesses = 0;
        quizWordList.clear();

        // Random until we got 5 of image name
        while (quizWordList.size() < 5) {
            int randomIndex = random.nextInt(fileNameList.size());
            String fileName = fileNameList.get(randomIndex);

            // Check if newly random file is not exist
            if (!quizWordList.contains(fileName)) {
                quizWordList.add(fileName);
            }
        }

        loadNextQuestion();
    }

    private void loadNextQuestion() {
        // Remove first file name from list
        answerFileName = quizWordList.remove(0);

        answerTextView.setText(null);

        // Show question number
        questionNumberTextView.setText(String.format("คำถามข้อที่ %d จากทั้งหมด 5 ข้อ", score + 1));

        loadQuestionImage();
        prepareChoiceWords();
        createChoiceButtons();

    }

    private void loadQuestionImage() {
        String category = answerFileName.substring(0, answerFileName.indexOf('-'));

        AssetManager as = getAssets();
        InputStream stream;
        try {
            stream = as.open(category + '/' + answerFileName + ".png");
            Drawable image = Drawable.createFromStream(stream, null);
            questionImageView.setImageDrawable(image);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error loading image file : " + answerFileName);
        }
    }

    private int numChoices = 0;

    private void prepareChoiceWords() {

        switch (diff) {
            case 0:
                numChoices = 2;
                break;
            case 1:
                numChoices = 4;
                break;
            case 2:
                numChoices = 6;
                break;
        }

        Log.i(TAG, "Num Choices is " + numChoices);

        choiceWords.clear();
        while (choiceWords.size() < numChoices) {
            int randomIndex = random.nextInt(fileNameList.size());
            String fileName = fileNameList.get(randomIndex);

            // add non-answer
            if (!choiceWords.contains(fileName) && !choiceWords.contains(answerFileName)) {
                choiceWords.add(getWord(fileName));
            }
        }

        // set answer
        int randomIndex = random.nextInt(numChoices);
        choiceWords.set(randomIndex, getWord(answerFileName));

        for (String word : choiceWords) {
            Log.i(TAG, "Word is " + word);
        }
    }

    // get word by using '-' as a separator
    private String getWord(String word) {
        return word.substring(word.indexOf('-') + 1);
    }

    private void createChoiceButtons() {
        for (int row = 0; row < buttonTableLayout.getChildCount(); row++) {
            TableRow tr = (TableRow) buttonTableLayout.getChildAt(row);
            tr.removeAllViews();
        }

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // each row
        for (int row = 0; row < numChoices / 2; row++) {
            TableRow tr = (TableRow) buttonTableLayout.getChildAt(row);

            // 2 column per row
            for (int column = 0; column < 2; column++) {
                // inflate (create) button from layout file (xml)
                Button guessButton = (Button) inflater.inflate(R.layout.guess_button, tr, false);

                // set button text from choice
                guessButton.setText(choiceWords.get((row * 2) + column));

                // set button listener
                guessButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        submitGuess((Button) v);
                    }
                });

                // add button to view
                tr.addView(guessButton);
            }
        }
    }

    private void submitGuess(Button button) {
        String guess = button.getText().toString();
        String answer = getWord(answerFileName);

        totalGuesses++;

        // if correct
        if (guess.equalsIgnoreCase(answer)) {

            // create media play and bound it to applause.wav in "raw" resource directory
            MediaPlayer mp = MediaPlayer.create(this, R.raw.applause);
            // adjust volume level
            mp.setVolume(0.05f, 0.05f);
            // play sound
            mp.start();

            score++;

            answerTextView.setText("คำตอบที่ \"" + answer + "\" เป็นคำตอบที่ถูกต้องนะคร๊าบบบบบ !!!");
            answerTextView.setTextColor(Color.GREEN);

            // disable all buttons for preventing user to rapid click
            disableAllButtons();


            // number of correct is 5 times (end game)
            if (score == 5) {
                // save to score to DB
                saveScore();

                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setTitle("สรุปผลแล้วว่า... ");

                String msg = String.format("จำนวนครั้งที่มึงมั่ว : %d\n", totalGuesses);
                msg += String.format("จำนวนครั้งที่เมิงฟลุ๊กตอบถูก : %d\n", score);
                msg += String.format("คิดเป็น Percent แล้ว อัตราความฟลุ๊กคือ : %.1f", (100 * score) / (double) totalGuesses);

                dialog.setMessage(msg);

                // Dialog button for new game
                dialog.setPositiveButton("มั่วใหม่", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startQuiz();
                    }
                });

                // Dialog button for goback to first page
                dialog.setNegativeButton("ยอมแพ้แล้ว", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });

                dialog.show();
            }
            // if not yet finish
            else {
                // delay 2 seconds
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(GameActivity.this, "ถ่วต้ม", Toast.LENGTH_SHORT);
                        loadNextQuestion();
                    }
                }, 2000);
            }
        }
        // if incorrect
        else {
            MediaPlayer mp = MediaPlayer.create(this, R.raw.fail3);
            mp.start();

            // set shake animation to ImageView
            questionImageView.setAnimation(shakeAnimation);

            // set TextView
            answerTextView.setText("มึงตอบผิด ไอสรัส !!!" + "\nตอบมาได้ไงวะว่า \"" + guess + "\"");
            answerTextView.setTextColor(Color.RED);
            button.setEnabled(false);
        }
    }

    private void saveScore() {
        // calculate score percentage
        double scorePercent = (100 * score) / (double) totalGuesses;

        // prepare data for insert
        ContentValues cv = new ContentValues();
        cv.put(dbHelper.COL_SCORE, String.format("%.1f", scorePercent));
        cv.put(dbHelper.COL_DIFFICULTY, diff);

        // insert data to DB
        long insertedRowID = sqLiteDB.insert(dbHelper.TABLE_NAME,null, cv);
        Log.i(TAG, "Inserted Row ID is " + insertedRowID);

        // detect if insert failed
        if (insertedRowID == -1) {
            Log.i(TAG, "Error occurred when insert data to DB");
        }
    }

    private void disableAllButtons() {
        for (int row = 0; row < buttonTableLayout.getChildCount(); row++) {
            TableRow tr = (TableRow) buttonTableLayout.getChildAt(row);
            for (int column = 0; column < tr.getChildCount(); column++) {
                tr.getChildAt(column).setEnabled(false);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");

        // Play music
        Music.play(this, R.raw.game);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");

        // Stop music
        Music.stop();
    }
}
