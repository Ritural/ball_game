package com.example.kjcj1.ballgame;

import android.content.Intent;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends ActionBarActivity implements SensorEventListener {

    //Global variable to send back playerScore to welcomeScreen
    public final static String EXTRA_MESSAGE = "9000";
    private float globalDrawNum = 0;

    //PLAYER VARIABLES
    private float playerSpeed = 5; //DEFAULT is 7
    private final float MAX_SPEED = 20;
    private final float player_yAcceleration = 0.1f; //How fast the ball increases each bounce
    private int playerScore = 0;
    private final int MAX_HEALTH = 25; //DEFAULT is 25
    private final float PLAYER_SIZE =  50;
    private final int PLAYER_START_X = 50;
    Player player; //DECLARES THE NEW PLAYER

    //MINE STUFF / OTHER BALL STUFF
    private final float MINES_TO_SPAWN = 2;     //CHANGE THIS to increase number of mines that spawn
    private final float MINE_SIZE = 75;
    private int mineCounter = 2;                //CHANGE THIS to increase number of mines at start
    private ArrayList<Mine> mineList = new ArrayList<>();
    private final int MAX_NUMBER_OF_MINES = 150;
    private final float MINE_GROW_SIZE = 10;
    private final float MAX_MINE_SIZE = 155;
    private final float MINE_SCORE = 50;

    PowerUp powerUp;
    private float powerUpCount = 0;
    private float powerUpTimer = 0;
    private float directionTimer = 0;
    private final float CHANGE_INTERVAL = 250;
    private final float POWERUP_SPAWN_TIME = 500;

    //GAME VARIABLES
    private final float RECT_SIZE = 150;
    private int SPAWN_AREA = 200;
    private float difficultyRating = 0;
    private float chainScore = 0;
    private float screenHeight = 0;
    private float screenWidth = 0;
    private Random randy = new Random();
    private final int TEXT_SIZE = 75;
    private final int MAX_COLORS = 4;
    DrawableView drawableView;

    //GYRO STUFF
    private SensorManager mSensorManager;
    private Sensor accelSensor;
    private final float SMOOTHING = 4;
    private float accelX = 0.0f;

    //SCORE TEXT
    private ArrayList<ScoringText> scoreList = new ArrayList<>();
    private final float SCORE_DISPLAY_TIME = 25;

//**************************************************************************************************

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);

        // Create a new instance of our drawable view
        drawableView = new DrawableView(this);

        // Set our Drawable View as the content for this activity
        setContentView(drawableView);

        /* Only works if you are extending AppCompatActivity.
         * If you are extending Activity, use getActionBar().hide();
         */
        getSupportActionBar().hide();

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //If its null then set our Sensor manager to the default type of sensor on the phone
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            accelSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        } else {
            //Logs an error saying no gyro is available on the phone
            Log.e("AccelerometerDemo", "No Accelerometer");
        }

        //Setting the player speed based on the difficulty chosen on the welcome screen
        if(getIntent().getStringExtra(WelcomeScreen.DIFFICULTY_MESSAGE) != null)
        {
            //Use difficultyRating adds speed to the player and also the multiplier
            difficultyRating = ((Integer.parseInt(getIntent().getStringExtra(WelcomeScreen.DIFFICULTY_MESSAGE)) / 20));
        }

        //Setting screen window so it doesn't dim or fade
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        player = new Player(); //CREATE THE PLAYER

    }

//**************************************************************************************************

    @Override
    public void onResume() {
        super.onResume();
        // Register as a SensorEventListener if we have a gyro present
        mSensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_GAME);

    }

    @Override
    public void onPause() {
        super.onPause();
        // Tell the SensorManager to stop calling us with sensor information if we are paused
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == accelSensor) {
            accelX = Math.round(event.values[0] * SMOOTHING);
            // = (Math.abs(event.values[0] * SMOOTHING) > SENSITIVITY ? (event.values[0] * SMOOTHING) : 0.0f);
            //accelY = Math.round(event.values[1] * SMOOTHING);
            // = (Math.abs(event.values[1] * SMOOTHING) > SENSITIVITY ? (event.values[1] * SMOOTHING): 0.0f);
            //gyroZ = Math.round(event.values[2] * SMOOTHING);
            //gyroZ = (Math.abs(event.values[2]) > SENSITIVITY ? event.values[2] : 0.0f);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //We don't use this...
    }

//**************************************************************************************************

    //Create our drawableView class the overwrites the onDraw method to do the drawing for the app
    public class DrawableView extends View {
        Paint paint = new Paint();
        int drawNum = 0;

        public DrawableView(Context context) {
            super(context);
            // Set up the Paint object with some default settings
            paint.setStyle(Paint.Style.FILL);
            paint.setTypeface(Typeface.SANS_SERIF);
            paint.setTextSize(TEXT_SIZE);
        }

        // Called every time the screen is redrawn
        @Override
        protected void onDraw(Canvas canvas) {
            //SETTING UP THE BACKGROUND AND POPULATING SCREEN SIZE VARIABLES
            paint.setColor(Color.CYAN);
            canvas.drawPaint(paint);
            screenHeight = canvas.getHeight();
            screenWidth = canvas.getWidth();

            //Setting up the players position at the start of the game to be the middle of the screen
            if(drawNum == 0)
            {
                player.myX = screenWidth / 2;
                player.myY = PLAYER_SIZE;
            }



            //Log.e("POWER UP", "Timer: " + powerUpTimer);
            if(drawNum >= (powerUpTimer + POWERUP_SPAWN_TIME) && powerUpCount == 0)
            {
                powerUp = new PowerUp();
                powerUpCount = 1;
            }

            if(powerUpCount == 1 && powerUp != null) //There has to be a power up
            {
                powerUp.Move();

                if(drawNum >= (directionTimer + CHANGE_INTERVAL))
                {
                    powerUp.changeColour();
                    //powerUp.changeDirection();
                    Log.e("POWER UP", "Changed Colour");
                    directionTimer = drawNum;
                }

                if(powerUpActivity()) //If it has collided
                {
                    powerUpCount = 0;
                    powerUpTimer = drawNum;
                }
            }

            playerActivity();
            drawAll(canvas, paint);

            ++drawNum;
            globalDrawNum = drawNum;
            // Queue a redrawing operation
            invalidate();
        }

        // Called when the View has focus
        @Override
        public void onWindowFocusChanged(boolean focused) {
            super.onWindowFocusChanged(focused);
        }
    }

//**************************************************************************************************
    //A parent class holding what is shared between the player and mine
    private abstract class Parent
    {
        protected float myX = 0;
        protected float myY = 0;
        protected float mySize = 50;
        protected Paint myColor = new Paint(Color.DKGRAY); //Default color
        protected int number = 0;
        protected float deltaY = 0;
    }

//**************************************************************************************************

    //Scoring class that displays text when a mine is collided with
    private class ScoringText extends Parent
    {
        private float scoreNumber;
        private float count;

        //Just need to use the x and y
        public ScoringText(float x, float y, float s)
        {
            myX = x;
            myY = y;
            scoreNumber = s;
            count = 0;
        }
    }

//**************************************************************************************************

    private void scoreActivity(Canvas canvas, Paint paint)
    {
        ScoringText[] scoreArrayList = new ScoringText[scoreList.size()];
        scoreList.toArray(scoreArrayList);

        for(int i = 0; i < scoreArrayList.length; i++)
        {
            //Drawing the scoring text
            if(scoreArrayList[i].scoreNumber == 0)
            {
                //Set out paint brush to orange for hitting wrong coloured ball
                paint.setColor(Color.rgb(255,153,51));
            }
            else
            {
                //Set paint to purple for hitting the right coloured ball
                paint.setColor(Color.rgb(148,0,211));
            }

            canvas.drawText("+" + (int)scoreArrayList[i].scoreNumber, scoreArrayList[i].myX, scoreArrayList[i].myY, paint);
            scoreArrayList[i].count++;

            //If it has been so many seconds then remove the scoreText from the list
            if(scoreArrayList[i].count >= SCORE_DISPLAY_TIME)
            {
                scoreList.remove(i);
                break;
            }
        }
    }

//**************************************************************************************************

    private class Player extends Parent
    {
        private int topNumber = 0;
        private int bottomNumber = 0;
        private int playerHP = 0;

        public Player()
        {
            mySize = PLAYER_SIZE;
            deltaY = playerSpeed + difficultyRating;
            playerHP = MAX_HEALTH;
            topNumber = randy.nextInt(MAX_COLORS);
            number = topNumber;
            myColor.setColor(returnColor(topNumber));
            bottomNumber = randy.nextInt(MAX_COLORS);
        }

        public void newTopColor()
        {
            topNumber = randy.nextInt(MAX_COLORS);
            number = bottomNumber;
            myColor.setColor(returnColor(number));
        }

        public void newBottomColor()
        {
            bottomNumber = randy.nextInt(MAX_COLORS);
            number = topNumber;
            myColor.setColor(returnColor(number));
        }

    }

//**************************************************************************************************

    private void playerActivity()
    {
        //Boolean collideRight = false;
        //Boolean collideLeft = false;

        //Collision detection for the player outside the bounds
        if (player.myX + player.mySize >= screenWidth)
        {
            //collideRight = true;
            //Moves the player in towards the centre of the screen by 0.25
            player.myX = player.myX - 0.25f;
        }
        else if (player.myX - player.mySize <= 0)
        {
            //collideLeft = true;
            player.myX = player.myX + 0.25f;
        }
        else
        {
            player.myX -= accelX;
        }

        if(player.deltaY <= MAX_SPEED)
        {
            player.myY += player.deltaY;
        }
        else
        {
            player.deltaY = MAX_SPEED;
        }

        if (player.myY + player.mySize >= screenHeight)
        {
            player.deltaY *= -1;
            player.deltaY -= player_yAcceleration;
            player.newTopColor();
            mineCounter += MINES_TO_SPAWN;
            SpawnMines();
        }
        if (player.myY - player.mySize <= 0)
        {
            player.deltaY *= -1;
            player.deltaY += player_yAcceleration;
            player.newBottomColor();
            mineCounter += MINES_TO_SPAWN;
            SpawnMines();
        }
    }
//**************************************************************************************************

    private class PowerUp extends Parent
    {

        private float deltaX;
        private int[] mineColourCount;

        //deltaY

        public PowerUp()
        {
            int r = randy.nextInt(4);

            this.myX = screenWidth /2;
            this.myY = screenHeight /2;
            this.mySize = 50; //TODO Make this a constant up the top
            number = r;
            this.myColor.setColor(returnColor(number));

            this.deltaX = 7;
            this.deltaY = 2;

            //mineColourCount = new int[4];
        }

        public void Move()
        {
            if(this.myX + this.mySize >= screenWidth)
            {
                this.deltaX *= -1;
            }
            else if(this.myX - this.mySize <= 0)
            {
                this.deltaX *= -1;
            }

            if(this.myY - this.mySize <= 0)
            {
                this.deltaY *= -1;
            }
            else if (this.myY + this.mySize >= screenHeight)
            {
                this.deltaY *= -1;
            }

            this.myX -= deltaX;
            this.myY -= deltaY;
        }

        public void changeDirection()
        {
            int r = randy.nextInt(1);

            switch (r)
            {
                case 0:
                    Log.e("Switch", "ZERO");
                    deltaX *= -1;
                case 1:
                    deltaY *= -1;
                    Log.e("Switch", "ONE");
                default:
                    Log.e("Switch", "DEFAULT?");
            }
        }

        public void changeColour()
        {
            Mine[] mineArrayList = new Mine[mineList.size()];
            mineList.toArray(mineArrayList);

            mineColourCount = new int[4];

            //Loop through the mine list and the powerup will be the same colour as the most mines
            for(int i = 0; i < mineArrayList.length; i++)
            {
                for(int j = 0; j <= 3; j++)
                {
                    if(mineArrayList[i].number == j)
                    {
                        mineColourCount[j]++;
                    }
                }
            }

            int largestNumber = mineColourCount[0]; //default
            int arrayNumber = 0;

            for(int i = 1; i < mineColourCount.length; i++)
            {
                if(mineColourCount[i] > largestNumber)
                {
                    largestNumber = mineColourCount[i];
                }
            }

            for(int i = 0; i < mineColourCount.length; i++)
            {
                if(mineColourCount[i] == largestNumber)
                {
                    arrayNumber = i;
                }
            }

            //White  0
            //Green  1
            //Red    2
            //Yellow 3
/*
            for(int k = 0; k <= 3; k++)
            {
                Log.e("COLOUR ARRAY","Colours: " + k + " == " + mineColourCount[k]);
            }

            Log.e("COLOUR ARRAY", "Largest number is ball colour: " + arrayNumber + "\n");
*/
            //This now sets the colour of the power up to the most mines of that colour on the field
            this.myColor.setColor(returnColor(arrayNumber));
            this.number = arrayNumber;
        }
    }

    private boolean powerUpActivity()
    {
        //TODO
        if(player.myX - player.mySize >= powerUp.myX - powerUp.mySize *2 &&
                player.myX + player.mySize <= powerUp.myX + powerUp.mySize *2 &&
                player.myY - player.mySize >= powerUp.myY - powerUp.mySize *2 &&
                player.myY + player.mySize <= powerUp.myY + powerUp.mySize *2)
        {
            //Log.e("COLOUR", "PlayCol: " + player.number + " PowUpCol: " + powerUp.number);

            if(player.number == powerUp.number)
            {
                scoreList.add(new ScoringText(powerUp.myX, powerUp.myY, chainScore));
                chainScore += 250; //TODO Change this to a global variable, power up score value
                playerScore += chainScore;
                //Log.e("HERE1", "HERE!!!!");
                Mine[] mineArrayList = new Mine[mineList.size()];
                mineList.toArray(mineArrayList);
                ArrayList<Mine> tempList = new ArrayList<>();

                for(int i = 0; i < mineList.size(); i++)
                {
                    if(mineArrayList[i].number == powerUp.number)
                    {
                        chainScore += mineArrayList[i].mineScore + mineArrayList[i].mineScore * difficultyRating/5f;
                        playerScore += chainScore;
                        scoreList.add(new ScoringText(mineArrayList[i].myX, mineArrayList[i].myY, chainScore));
                    }
                    else
                    {
                        tempList.add(mineArrayList[i]);
                        //scoreList.add(new ScoringText(mineArrayList[i].myX, mineArrayList[i].myY, 0));
                    }
                }

                mineList = tempList;
            }
            else
            {

                chainScore = 0;
                scoreList.add(new ScoringText(powerUp.myX, powerUp.myY, chainScore));
                player.playerHP -= 2;
            }
            return true;
        }
        return false;
    }

// **************************************************************************************************

    //The mine is our on screen ball that the player can collide with
    private class Mine extends Parent
    {
        private float mineScore;
        private float myMaxSize;

        public Mine(float x, float y)
        {
            mineScore = MINE_SCORE;
            number = randy.nextInt(MAX_COLORS);
            myX = x;
            myY = y;
            mySize = 1;
            myMaxSize = MINE_SIZE;
            number = randy.nextInt(MAX_COLORS);
            myColor.setColor(returnColor(number));
        }

        //Increases the size of a mine, used in the spawning of mines to prevent overlapping of mines
        public void merge()
        {
            if(mySize < MAX_MINE_SIZE)
            {
                myMaxSize += MINE_GROW_SIZE;
                mineScore += MINE_SCORE; //Adds the mine score values together
            }
        }

        public void grow()
        {
            if(this.mySize < myMaxSize)
            {
                this.mySize += 0.5;
            }
        }
    }

//**************************************************************************************************

    private void SpawnMines()
    {
        if (mineList.size() < mineCounter && mineList.size() < MAX_NUMBER_OF_MINES)
        {
            for(int i = 0; i < MINES_TO_SPAWN; i++)
            {
                float x = SPAWN_AREA / 2 + (randy.nextInt((int)(screenWidth - SPAWN_AREA)));

                float y = 350 + (randy.nextInt((int)(screenHeight)));

                //Log.e("SPAWN MINES", "Y pos: " + y);

                //canvas.drawRect(0, 350f, screenWidth, screenHeight - 350f,

                //This should bring the mines up
                if(y >= screenHeight - 350)
                {
                    y = screenHeight - 350 - randy.nextInt(1000);
                    //Log.e("MINES TO HIGH", "Y pos CORRECTED: " + y);
                }
                //This should push the mines down
                if(y <= 350)
                {
                    y = 350 + randy.nextInt(1000);
                    //Log.e("MINES TO LOW", "Y pos CORRECTED: " + y);
                }

                if(!isLocationOccupied(x,y))
                {
                    mineList.add(new Mine(x, y));
                }
            }
        }
    }

//**************************************************************************************************

    private void mineActivity(Canvas canvas, Paint paint)
    {
        Mine[] mineArrayList = new Mine[mineList.size()];
        mineList.toArray(mineArrayList);

        //collision detection for the player and the mines
        for(int i = 0; i < mineArrayList.length; i++)
        {
            mineArrayList[i].grow();

            //Mine x
            float mineX = mineArrayList[i].myX;
            //Mine y
            float mineY = mineArrayList[i].myY;
            //Mine size
            float mineSize = mineArrayList[i].mySize;


            //If player is in the bounds of the mine then do
            if(player.myX - player.mySize >= mineX - mineSize *2 &&
                    player.myX + player.mySize <= mineX + mineSize *2 &&
                    player.myY - player.mySize >= mineY - mineSize *2 &&
                    player.myY + player.mySize <= mineY + mineSize *2)
            {
                //If the player and the mine are the same colour then add to the score and remove mine
                if(player.number == mineArrayList[i].number)
                {
                    chainScore += mineArrayList[i].mineScore + mineArrayList[i].mineScore * difficultyRating/5f;
                    playerScore += chainScore;
                    scoreList.add(new ScoringText(mineArrayList[i].myX, mineArrayList[i].myY, chainScore));
                }
                else
                {
                    chainScore = 0;
                    //Log.e("COLLISION","Player has hit the wrong color mine");
                    player.playerHP -= 1;
                    scoreList.add(new ScoringText(mineArrayList[i].myX, mineArrayList[i].myY, 0));
                }
                //Log.e("MINE_COLLISION", "Spawn celebration mine breaking here");

                mineList.remove(i);
                break;
            }
        }

        paint.setColor(Color.BLACK);
        //Drawing the mines
        for(int i = 0; i < mineArrayList.length; i++)
        {
            canvas.drawCircle(mineArrayList[i].myX, mineArrayList[i].myY, mineArrayList[i].mySize, mineArrayList[i].myColor);
        }

    }
//**************************************************************************************************
    private int returnColor(int number)
    {
        switch(number)
        {
            case 0:
                return (Color.WHITE);
            case 1:
                return(Color.GREEN);
            case 2:
                return(Color.RED);
            case 3:
                return(Color.YELLOW);
        }
        return Color.DKGRAY; //DEFAULT COLOR IF IT BREAKS
    }
//**************************************************************************************************

    private Boolean isLocationOccupied(float x, float y)
    {
        Mine[] mineArrayList = new Mine[mineList.size()];
        mineList.toArray(mineArrayList);

        for(int i = 0; i < mineArrayList.length; i++)
        {
            float mineX = mineArrayList[i].myX;
            float mineY = mineArrayList[i].myY;
            float mineSize = mineArrayList[i].myMaxSize; //Checks if they will grow ontop of each other

            if(x - MINE_SIZE >= mineX - mineSize * 2 &&
               x + MINE_SIZE <= mineX + mineSize * 2 &&
               y - MINE_SIZE >= mineY - mineSize * 2 &&
               y + MINE_SIZE <= mineY + mineSize * 2)
            {
                //Mines are on top of each other
                //So merge the mines
                mineArrayList[i].merge();
                return true;
            }
        }
        return false;
    }

//**************************************************************************************************

    private void displayWelcome()
    {
        //Sending the playerScore to the welcome screen activity
        Intent intent = new Intent(this, WelcomeScreen.class);
        String message = "" + playerScore;
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

//**************************************************************************************************

    private void drawAll(Canvas canvas, Paint paint)
    {
        endOfGameYet(canvas, paint); //Check if its at the end of the game
        createRectangles(canvas, paint);
        mineActivity(canvas, paint);
        DrawDebugMenu(canvas, paint);
        //Drawing of the circle and the player HP and number
        paint.setColor(Color.BLACK);
        canvas.drawCircle(player.myX, player.myY, player.mySize + 2, paint); //Draws a black outline around the player
        canvas.drawCircle(player.myX, player.myY, player.mySize, player.myColor);

        if(powerUpCount == 1)
        {
            canvas.drawCircle(powerUp.myX, powerUp.myY, powerUp.mySize + 2, paint); //Draws a black outline around the powerUp
            canvas.drawCircle(powerUp.myX, powerUp.myY, powerUp.mySize, powerUp.myColor);
        }

        //canvas.drawCircle(player.myX, player.myY, player.mySize - 10, paint);
        scoreActivity(canvas, paint);
        paint.setColor(Color.GREEN);
    }

    //Creates the rectangles at the top and the bottom of the screen and
    //Switches the colours based on the player next colour variable
    private void createRectangles(Canvas canvas, Paint paint)
    {
        paint.setColor( (returnColor(player.topNumber)));
        canvas.drawRect(0,0,screenWidth,RECT_SIZE, paint);
        paint.setColor( (returnColor(player.bottomNumber)));
        canvas.drawRect(0,screenHeight - RECT_SIZE, screenWidth, screenHeight, paint);
        paint.setColor(Color.BLACK);
        canvas.drawRect(0, RECT_SIZE - 10, screenWidth, RECT_SIZE, paint);
        canvas.drawRect(0, screenHeight - RECT_SIZE, screenWidth, screenHeight - RECT_SIZE + 10, paint);
    }

    //Menu and DEBUG drawing
    private void DrawDebugMenu(Canvas canvas, Paint paint)
    {
        paint.setColor(Color.BLACK);
        canvas.drawText("Score: " + playerScore, screenWidth / 3, 75, paint);
        canvas.drawText("Life: " + player.playerHP + " /" + MAX_HEALTH, screenWidth / 3, screenHeight - 75, paint);

        //paint.setColor(Color.LTGRAY);
        //canvas.drawRect(0, 350f, screenWidth, screenHeight - 350f, paint);

        //canvas.drawText("Draw Call : " + globalDrawNum, 25, 300, paint);
        //canvas.drawText("PowerUpNum: " + powerUpTimer, 25, 400, paint);
        //canvas.drawText("X position: " + accelX * 10, 25, 150, paint);
        //canvas.drawText("Y position: " + accelY, 25, 200, paint);
        //canvas.drawText("Z position: " + gyroZ, 25, 250, paint);
        //canvas.drawText("X: " + playerX + " Y: " + playerY, 25, 300, paint);
        //canvas.drawText(""+player.deltaY, player.myX /2, player.myY, paint);
        //canvas.drawText("MineCounter: " + mineList.size() + " / " + mineCounter, 25, 350, paint);
        //canvas.drawText("Width: " + screenWidth + " Height: " + canvas.getHeight(), 25, 400, paint);
    }

//**************************************************************************************************

    private void endOfGameYet(Canvas canvas, Paint paint)
    {
        //End game call.
        if(player.playerHP <= 0)
        {
            //Stop the game by setting player speed to zero
            accelX = 0;
            player.deltaY = 0;
            player.playerHP = 0;
            paint.setColor(Color.BLACK);
            canvas.drawText("Game Over!", screenWidth/4, screenHeight /2 - 150, paint);
            canvas.drawText("Your score was ", screenWidth/4, screenHeight /2 - 50, paint);
            canvas.drawText("" + playerScore, screenWidth/4, screenHeight /2 + 50, paint);
            displayWelcome();
        }
    }
}


