package com.vedran.trivia;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.vedran.trivia.controller.AppController;
import com.vedran.trivia.data.AnswerListAsyncResponse;
import com.vedran.trivia.data.QuestionBank;
import com.vedran.trivia.model.Question;
import com.vedran.trivia.model.Score;
import com.vedran.trivia.util.Prefs;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private TextView tvCounter;
    private TextView tvQuestion;
    private TextView tvScore;
    private TextView tvHighestScore;

    private Button btnTrue;
    private Button btnFalse;
    private Button btnNewGame;

    private ImageButton ibPrev;
    private ImageButton ibNext;

    private int currentQuestionIndex = 0;

    private List<Question> questionList;

    private int scoreCounter = 0;
    private Score score;

    //HIGHEST SCORE
    private Prefs prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        score = new Score();
        prefs = new Prefs(MainActivity.this);

        initWidgets();

        setupListeners();

        tvScore.setText(MessageFormat.format("Current Score: {0}", String.valueOf(score.getScore())));
        tvHighestScore.setText(MessageFormat.format("Highest Score: {0}", String.valueOf(prefs.getHighScore())));

        questionList = new QuestionBank().getQuestion(new AnswerListAsyncResponse() {
            @Override
            public void processFinished(ArrayList<Question> questionArrayList) {

                tvCounter.setText((currentQuestionIndex + 1) + " / " + questionArrayList.size());

                tvQuestion.setText(questionArrayList.get(currentQuestionIndex).getAnswer());
                //Log.d("Main", "onCreate: " + questionArrayList);
            }
        });

    }


    @Override
    protected void onResume() {
        currentQuestionIndex = prefs.getState();
        super.onResume();
    }

    @Override
    protected void onPause() {

        prefs.saveHighScore(score.getScore());
        prefs.setState(currentQuestionIndex);

        super.onPause();
    }

    private void initWidgets() {
        tvCounter = findViewById(R.id.tvCounter);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvScore = findViewById(R.id.tvScore);
        tvHighestScore = findViewById(R.id.tvHighestScore);

        btnTrue = findViewById(R.id.btnTrue);
        btnFalse = findViewById(R.id.btnFalse);
        btnNewGame = findViewById(R.id.btnNewGame);

        ibPrev = findViewById(R.id.ib_prev);
        ibNext = findViewById(R.id.ib_next);
    }

    private void setupListeners() {
        ibPrev.setOnClickListener(this);
        btnTrue.setOnClickListener(this);
        btnFalse.setOnClickListener(this);
        ibNext.setOnClickListener(this);
        btnNewGame.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ib_prev:
                if (currentQuestionIndex > 0) {
                    currentQuestionIndex = (currentQuestionIndex - 1) % questionList.size();
                }
                updateQuestion();
                break;

            case R.id.ib_next:
                goNext();
                break;

            case R.id.btnTrue:
                checkAnswer(true);
                updateQuestion();
                break;

            case R.id.btnFalse:
                checkAnswer(false);
                updateQuestion();
                break;

            case R.id.btnNewGame:
                currentQuestionIndex = 0;
                score.setScore(0);
                tvScore.setText(MessageFormat.format("Current Score: {0}", String.valueOf(score.getScore())));
                Toast.makeText(this, getString(R.string.new_game_msg), Toast.LENGTH_SHORT).show();
                updateQuestion();
        }
    }

    public void updateQuestion() {
        String question = questionList.get(currentQuestionIndex).getAnswer();
        tvQuestion.setText(question);
        tvCounter.setText((currentQuestionIndex + 1) + " / " + questionList.size());

    }

    public void checkAnswer(boolean userChoice) {
        boolean answerIsTrue = questionList.get(currentQuestionIndex).isAnswerTrue();
        int toastMessageId = 0;

        if (userChoice == answerIsTrue) {
            fadeView();
            addPoints();
            toastMessageId = R.string.correct_answer;
        } else {
            shakeAnimation();
            deductPoints();
            toastMessageId = R.string.wrong_answer;
        }


        Toast.makeText(this, toastMessageId, Toast.LENGTH_SHORT).show();
    }

    private void addPoints() {
        scoreCounter += 100;
        score.setScore(scoreCounter);


        tvScore.setText(MessageFormat.format("Current Score: {0}", String.valueOf(score.getScore())));
    }

    private void deductPoints() {
        scoreCounter -= 50;
        if (scoreCounter > 0) {
            score.setScore(scoreCounter);
            tvScore.setText(MessageFormat.format("Current Score: {0}", String.valueOf(score.getScore())));
        } else {
            scoreCounter = 0;
            score.setScore(scoreCounter);
            tvScore.setText(MessageFormat.format("Current Score: {0}", String.valueOf(score.getScore())));
        }


    }


    private void fadeView() {
        final CardView cardView = findViewById(R.id.cardView);

        AlphaAnimation animation = new AlphaAnimation(1.0f, 0.3f);

        animation.setDuration(350);
        animation.setRepeatCount(1);
        animation.setRepeatMode(Animation.REVERSE);

        cardView.setAnimation(animation);

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                cardView.setCardBackgroundColor(Color.GREEN);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                cardView.setCardBackgroundColor(Color.WHITE);
                goNext();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void shakeAnimation() {
        Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake_animation);
        final CardView cardView = findViewById(R.id.cardView);
        cardView.setAnimation(shake);

        shake.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                cardView.setCardBackgroundColor(Color.RED);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                cardView.setCardBackgroundColor(Color.WHITE);
                goNext();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void goNext() {
        currentQuestionIndex = (currentQuestionIndex + 1) % questionList.size();
        updateQuestion();
    }
}
