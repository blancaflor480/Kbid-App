package com.example.myapplication.fragment.biblegames.quizgame;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizGame extends AppCompatActivity {
    private ImageView arrowback;
    private TextView questionText, questionNumber;
    private Button answer1, answer2, answer3;
    private List<Question> questionList;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private int totalQuestions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_quizgame);

        Log.d("BibleActivity", "RecyclerView and Adapter setup complete.");

        // Initialize views
        arrowback = findViewById(R.id.arrowback);
        questionText = findViewById(R.id.question);
        questionNumber = findViewById(R.id.questionupto);
        answer1 = findViewById(R.id.answer1);
        answer2 = findViewById(R.id.answer2);
        answer3 = findViewById(R.id.answer3);

        // Set the click listener for arrowback
        arrowback.setOnClickListener(v -> onBackPressed());

        // Initialize the quiz
        questionList = getQuizQuestions();
        totalQuestions = questionList.size();
        Collections.shuffle(questionList);
        displayQuestion();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d("BibleActivity", "Navigating back to FragmentHome");
    }

    private void displayQuestion() {
        if (currentQuestionIndex < totalQuestions) {
            Question currentQuestion = questionList.get(currentQuestionIndex);
            questionText.setText(currentQuestion.getQuestionText());
            questionNumber.setText("Question " + (currentQuestionIndex + 1) + " of " + totalQuestions);

            List<String> answers = new ArrayList<>(currentQuestion.getAllAnswers());
            Collections.shuffle(answers);

            answer1.setText(answers.get(0));
            answer2.setText(answers.get(1));
            answer3.setText(answers.get(2));

            // Reset button states before setting listeners
            resetButtonStates();
            setAnswerButtonListeners(currentQuestion);
        } else {
            // End of the quiz
            Log.d("BibleActivity", "Quiz completed. Your score: " + score);
            showFinalScore();
        }
    }

    private void setAnswerButtonListeners(final Question question) {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button selectedButton = (Button) v;
                String selectedAnswer = selectedButton.getText().toString();

                // Reference to CardView and TextView
                CardView questionCard = findViewById(R.id.questioncard);
                TextView questionTextView = findViewById(R.id.question);

                // Check the answer and update UI accordingly
                if (selectedAnswer.equals(question.getCorrectAnswer())) {
                    selectedButton.setBackgroundColor(Color.GREEN);
                    selectedButton.setTextColor(Color.WHITE); // Change text color to white for the correct answer
                    selectedButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.check_circle, 0);

                    questionTextView.setText("CORRECT"); // Update the question TextView to indicate correct answer
                    questionTextView.setTextSize(30);
                    questionCard.setCardBackgroundColor(Color.GREEN); // Change the CardView background to green
                    questionTextView.setTextColor(Color.WHITE); // Change the CardView background to green

                    score++;
                } else {
                    selectedButton.setBackgroundColor(Color.RED);
                    selectedButton.setTextColor(Color.WHITE); // Change text color to white for the wrong answer
                    selectedButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.baseline_exit_24, 0);
                    //questionTextView.setText("Incorrect! The correct answer is: " + question.getCorrectAnswer());
                    questionTextView.setText("WRONG");
                    questionTextView.setTextSize(30);
                    questionCard.setCardBackgroundColor(Color.RED); // Change the CardView background to red
                    questionTextView.setTextColor(Color.WHITE); // Change the CardView background to green


                    // Highlight the correct answer
                    highlightCorrectAnswer(question.getCorrectAnswer());
                }

                // Disable all buttons after an answer is selected
                disableButtons();

                // Move to the next question after a delay
                selectedButton.postDelayed(() -> {
                    currentQuestionIndex++;
                    displayQuestion();
                }, 2000); // 2-second delay before moving to the next question
            }
        };

        answer1.setOnClickListener(listener);
        answer2.setOnClickListener(listener);
        answer3.setOnClickListener(listener);
    }

    private void highlightCorrectAnswer(String correctAnswer) {
        // Highlight the correct answer button if the selected answer is incorrect
        if (answer1.getText().toString().equals(correctAnswer)) {
            answer1.setBackgroundColor(Color.GREEN);
            answer1.setTextColor(Color.WHITE);
            answer1.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.check_circle, 0);
        } else if (answer2.getText().toString().equals(correctAnswer)) {
            answer2.setBackgroundColor(Color.GREEN);
            answer2.setTextColor(Color.WHITE);
            answer2.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.check_circle, 0);
        } else if (answer3.getText().toString().equals(correctAnswer)) {
            answer3.setBackgroundColor(Color.GREEN);
            answer3.setTextColor(Color.WHITE);
            answer3.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.check_circle, 0);
        }
    }


    private void resetButtonStates() {
        // Reset all buttons' colors and text colors
        Button[] buttons = {answer1, answer2, answer3};
        for (Button button : buttons) {
            button.setBackgroundColor(Color.WHITE); // Reset background to white
            button.setTextColor(Color.BLACK); // Reset text color to black
            button.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0); // Remove icons
            button.setEnabled(true); // Enable buttons for the next question
        }

        CardView questionCard = findViewById(R.id.questioncard);
        questionCard.setCardBackgroundColor(Color.WHITE); // Reset CardView background to white

        TextView questionTextView = findViewById(R.id.question);
        questionTextView.setTextSize(20); // Clear the question text
        questionTextView.setTextColor(Color.RED); // Reset text color to black
    }

    private void disableButtons() {
        answer1.setEnabled(false);
        answer2.setEnabled(false);
        answer3.setEnabled(false);
    }

    private List<Question> getQuizQuestions() {
        List<Question> questions = new ArrayList<>();
        questions.add(new Question("What did God create on the first day?", "Light", "Sky", "Land"));
        questions.add(new Question("Who built the ark?", "Noah", "Moses", "Abraham"));
        questions.add(new Question("Who defeated Goliath?", "David", "Saul", "Solomon"));
        questions.add(new Question("Who was thrown into the lion's den?", "Daniel", "Jonah", "Joseph"));
        questions.add(new Question("Who was swallowed by a big fish?", "Jonah", "Daniel", "Elijah"));
        return questions;
    }

    private void showFinalScore() {
        // Calculate the percentage score
        double percentage = ((double) score / totalQuestions) * 100;

        // Pass the score and percentage to the fragment_score layout
        Intent intent = new Intent(QuizGame.this, ScoreActivity.class);
        intent.putExtra("score", score);
        intent.putExtra("totalQuestions", totalQuestions);
        startActivity(intent);
        finish();
    }

    private class Question {
        private String questionText;
        private String correctAnswer;
        private String[] incorrectAnswers;

        public Question(String questionText, String correctAnswer, String... incorrectAnswers) {
            this.questionText = questionText;
            this.correctAnswer = correctAnswer;
            this.incorrectAnswers = incorrectAnswers;
        }

        public String getQuestionText() {
            return questionText;
        }

        public String getCorrectAnswer() {
            return correctAnswer;
        }

        public List<String> getAllAnswers() {
            List<String> allAnswers = new ArrayList<>();
            allAnswers.add(correctAnswer);
            Collections.addAll(allAnswers, incorrectAnswers);
            return allAnswers;
        }
    }
}
