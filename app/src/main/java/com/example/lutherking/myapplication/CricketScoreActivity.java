package com.example.lutherking.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CricketScoreActivity extends AppCompatActivity {


    private int scoreTeamA=0, extraByTeamA=0;
    private int scoreTeamB=0, extraByTeamB=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cricket_score);
    }

    private void displayForTeamA(int number){
        TextView scoreTextView = (TextView) findViewById(R.id.team_a_score);
        scoreTextView.setText("" + number);
    }
    private void displayExtraForTeamA(int number){
        TextView extraScoreTextView = (TextView) findViewById(R.id.extra_run_by_team_a);
        extraScoreTextView.setText("Extra runs : " + number);
    }

    private void displayForTeamB(int number){
        TextView scoreTextView = (TextView) findViewById(R.id.team_b_score);
        scoreTextView.setText("" + number);
    }
    private void displayExtraForTeamB(int number){
        TextView extraScoreTextView = (TextView) findViewById(R.id.extra_run_by_team_b);
        extraScoreTextView.setText("Extra runs : " + number);
    }

    public void addExtraForTeamA(View view){
        scoreTeamA = scoreTeamA + 1;
        extraByTeamA++;
        displayForTeamA(scoreTeamA);
        displayExtraForTeamA(extraByTeamA);
    }
    public void addOneForTeamA(View view){
        scoreTeamA = scoreTeamA + 1;
        displayForTeamA(scoreTeamA);
    }
    public void addTwoForTeamA(View view){
        scoreTeamA = scoreTeamA + 2;
        displayForTeamA(scoreTeamA);
    }
    public void addThreeForTeamA(View view){
        scoreTeamA = scoreTeamA + 3;
        displayForTeamA(scoreTeamA);
    }
    public void addFourForTeamA(View view){
        scoreTeamA = scoreTeamA + 4;
        displayForTeamA(scoreTeamA);
    }
    public void addSixForTeamA(View view){
        scoreTeamA = scoreTeamA + 6;
        displayForTeamA(scoreTeamA);
    }

    public void addExtraForTeamB(View view){
        scoreTeamB = scoreTeamB + 1;
        extraByTeamB++;
        displayForTeamB(scoreTeamB);
        displayExtraForTeamB(extraByTeamB);
    }
    public void addOneForTeamB(View view){
        scoreTeamB = scoreTeamB + 1;
        displayForTeamB(scoreTeamB);
    }
    public void addTwoForTeamB(View view){
        scoreTeamB = scoreTeamB + 2;
        displayForTeamB(scoreTeamB);
    }
    public void addThreeForTeamB(View view){
        scoreTeamB = scoreTeamB + 3;
        displayForTeamB(scoreTeamB);
    }
    public void addFourForTeamB(View view){
        scoreTeamB = scoreTeamB + 4;
        displayForTeamB(scoreTeamB);
    }
    public void addSixForTeamB(View view){
        scoreTeamB = scoreTeamB + 6;
        displayForTeamB(scoreTeamB);
    }

    public void resetScore(View view){
        scoreTeamA=0;
        scoreTeamB=0;
        extraByTeamA=0;
        extraByTeamB=0;
        displayForTeamA(scoreTeamA);
        displayForTeamB(scoreTeamB);
        displayExtraForTeamA(extraByTeamA);
        displayExtraForTeamB(extraByTeamB);
    }
}
