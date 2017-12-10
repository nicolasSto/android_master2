package com.example.nico.bluetooth_raspberry;

import android.app.Notification;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;


import android.graphics.Path;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Transaction;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import static com.example.nico.bluetooth_raspberry.MainActivity.EcranDessin.convertDistance;
import static com.example.nico.bluetooth_raspberry.MainActivity.EcranDessin.createRandomInt;
import static com.example.nico.bluetooth_raspberry.MainActivity.EcranDessin.myPath;
import static java.lang.System.out;


public class MainActivity extends AppCompatActivity {

    private Set<BluetoothDevice> devices;
    private BluetoothDevice myRaspBerry;
    private final static int REQUEST_CODE_ENABLE_BLUETOOTH = 0;
    public ClientBluetooth myClientBluetooth;

    // pour le radar
    EcranDessin v;
    float x, y ;

    // pour la BDD
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("distance");

    // pour recup les data de la BDD
    public static List<Integer> BDD_distances = new ArrayList<Integer>();
    public static int[] BDD_distances_10_last= { 0,0,0,0,0,0,0,0,0,0};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

      myRef.addChildEventListener(new ChildEventListener() {
          @Override
          public void onChildAdded(DataSnapshot dataSnapshot, String s) {

              int value = dataSnapshot.getValue(int.class);
              BDD_distances.add(value);
          }

          @Override
          public void onChildChanged(DataSnapshot dataSnapshot, String s) {

          }

          @Override
          public void onChildRemoved(DataSnapshot dataSnapshot) {

          }

          @Override
          public void onChildMoved(DataSnapshot dataSnapshot, String s) {

          }

          @Override
          public void onCancelled(DatabaseError databaseError) {

          }
      });

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    if(bluetoothAdapter==null)
            Toast.makeText(this,"Pas de Bluetooth", Toast.LENGTH_LONG).show();

    else {
        Toast.makeText(getApplicationContext(), "Avec Bluetooth", Toast.LENGTH_LONG).show();

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBlueTooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBlueTooth, REQUEST_CODE_ENABLE_BLUETOOTH);
        }


        devices = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice blueDevice : devices) {
            Toast.makeText(MainActivity.this, "Device = " + blueDevice.getName(), Toast.LENGTH_SHORT).show();
            if(blueDevice.getName().equals("raspberrypi")){
                myRaspBerry = blueDevice;
            }
        }

        if(myRaspBerry != null){
            myClientBluetooth = new ClientBluetooth(myRaspBerry);
            myClientBluetooth.run();
            if( out != null) {
                myClientBluetooth.sendString("test bg");
            }
        }
    }

        Button droitButton = (Button) findViewById(R.id.droit);
        Button droiteButton = (Button) findViewById(R.id.droite);
        Button gaucheButton = (Button) findViewById(R.id.gauche);
        Button basButton = (Button) findViewById(R.id.bas);
        Button stopButton = (Button) findViewById(R.id.stop);
        Button scannerButton = (Button) findViewById(R.id.scanner);

        //droit
        droitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                controlGoPiGo(1);
            }
        });
        //droite
        droiteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                controlGoPiGo(2);
            }
        });
        //gauche
        gaucheButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                controlGoPiGo(3);
            }
        });
        //bas
        basButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                controlGoPiGo(4);
            }
        });
        //stop
        stopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                controlGoPiGo(5);
            }
        });
        //scanner
        scannerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                controlGoPiGo(6);
                //EcranDessin.createPointsArray_random();
                EcranDessin.createPointsArray();
            }
        });



        /* graph library
        GraphView graph = (GraphView) findViewById(R.id.graph);
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)
        });
        graph.addSeries(series);
        */

        // pour afficher la surfaceview
        v = new EcranDessin(this);
        addContentView(v, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1200));
        x = y = 0;

}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_CODE_ENABLE_BLUETOOTH)
            return;
        if (resultCode == RESULT_OK) {
        } else {
        }
    }


    public void controlGoPiGo(int direction){

        if(myClientBluetooth != null) {
            switch (direction) {
                case 1:
                    myClientBluetooth.sendString("1");
                    break;
                case 2:
                    myClientBluetooth.sendString("2");
                    break;
                case 3:
                    myClientBluetooth.sendString("3");
                    break;
                case 4:
                    myClientBluetooth.sendString("4");
                    break;
                case 5:
                    myClientBluetooth.sendString("5");
                    break;
                case 6:
                    myClientBluetooth.sendString("6");
                    break;
            }
        }
    }



    @Override
    protected void onPause() {
        super.onPause();
        v.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        v.resume();
    }

    public static class EcranDessin extends SurfaceView implements Runnable{
        Thread t = null;
        SurfaceHolder holder;
        boolean isItOk = false;

        static class Pt{

            float x, y;
            Pt(float _x, float _y){
                x = _x;
                y = _y;
            }
        }

        // int tabelau de points
         static Pt[] myPath = { new Pt(100, 1180),
                new Pt(200, 1180),
                new Pt(300, 1180),
                new Pt(400, 1180),
                new Pt(500, 1180),
                new Pt(600, 1180),
                new Pt(700, 1180),
                new Pt(800, 1180),
                new Pt(900, 1180),
                new Pt(1000, 1180)
        };

        public EcranDessin(Context context) {
            super(context);
            holder = getHolder();
        }

        public static int createRandomInt(){
            int randomInt = 0;
            Random randomGenerator = new Random();
            for (int idx = 8; idx <= 200; ++idx){
                 randomInt = randomGenerator.nextInt(200);
            }
            return randomInt;
        }

        public static Pt[] createPointsArray_random(){
            myPath = new Pt[]{new Pt(50, convertDistance(createRandomInt())),
                    new Pt(160, convertDistance(createRandomInt())),
                    new Pt(270, convertDistance(createRandomInt())),
                    new Pt(380, convertDistance(createRandomInt())),
                    new Pt(490, convertDistance(createRandomInt())),
                    new Pt(600, convertDistance(createRandomInt())),
                    new Pt(710, convertDistance(createRandomInt())),
                    new Pt(820, convertDistance(createRandomInt())),
                    new Pt(930, convertDistance(createRandomInt())),
                    new Pt(1150, convertDistance(createRandomInt()))
            };
            return myPath;
        }


        public static Pt[] createPointsArray(){

            if(BDD_distances != null && BDD_distances.size() > 0) {
                int i = 0;
                int j = 0;
                int BDD_distances_size = BDD_distances.size() - 1;

                for (i = 9; i > 0; i--, BDD_distances_size--) {
                    BDD_distances_10_last[i] = BDD_distances.get(BDD_distances_size);
                }

                // tabelau de points
                myPath = new Pt[]{new Pt(50, convertDistance(BDD_distances_10_last[0])),
                        new Pt(160, convertDistance(BDD_distances_10_last[1])),
                        new Pt(270, convertDistance(BDD_distances_10_last[2])),
                        new Pt(380, convertDistance(BDD_distances_10_last[3])),
                        new Pt(490, convertDistance(BDD_distances_10_last[4])),
                        new Pt(600, convertDistance(BDD_distances_10_last[5])),
                        new Pt(710, convertDistance(BDD_distances_10_last[6])),
                        new Pt(820, convertDistance(BDD_distances_10_last[7])),
                        new Pt(930, convertDistance(BDD_distances_10_last[8])),
                        new Pt(1150, convertDistance(BDD_distances_10_last[9]))
                };
            }
            return myPath;
        }

        public static Pt[] getMyPath() {
            return myPath;
        }


        public static int convertDistance(int x){
            int newX = 1180 - ((x/2) * 10);
            return newX;
        }


        public void run() {

            while (isItOk==true) {

                if (!holder.getSurface().isValid())
                    continue;

                Canvas c = holder.lockCanvas();
                c.drawARGB(255, 255, 255, 255);

                Paint paint_background_radar = new Paint();
                paint_background_radar.setColor(Color.BLACK);

                Paint paint_line = new Paint();
                paint_line.setStrokeWidth(2);
                paint_line.setColor(Color.BLACK);
                paint_line.setStyle(Paint.Style.STROKE);

                Paint paint_border_circle = new Paint();
                paint_border_circle.setColor(Color.GREEN);
                paint_border_circle.setStrokeWidth(2);
                paint_border_circle.setStyle(Paint.Style.STROKE);

                Paint paint = new Paint();
                paint.setColor(Color.BLACK);
                paint.setStrokeWidth(8);
                paint.setStyle(Paint.Style.STROKE);

                Paint paint_path = new Paint();
                paint_path.setColor(Color.RED);
                paint_path.setStrokeWidth(5);
                paint_path.setStyle(Paint.Style.STROKE);

                c.drawRect(10,10,getWidth()-10,getHeight()-10,paint);
                Path path = new Path();
                myPath = getMyPath();

                // 0cm
                c.drawText("O cm", 20, 1170, paint_line);
                c.drawLine(10,1180,40,1180,paint);
                // 22cm
                c.drawText("22 cm", 20, 1052, paint_line);
                c.drawLine(10,1062,40,1062,paint);
                // 44cm
                c.drawText("44 cm", 20, 934, paint_line);
                c.drawLine(10,944,40,944,paint);
                // 66cm
                c.drawText("66 cm", 20, 816, paint_line);
                c.drawLine(10,826,40,826,paint);
                // 88cm
                c.drawText("88 cm", 20, 698, paint_line);
                c.drawLine(10,708,40,708,paint);
                // 110cm
                c.drawText("110 cm", 20, 580, paint_line);
                c.drawLine(10,590,40,590,paint);
                // 132cm
                c.drawText("132 cm", 20, 462, paint_line);
                c.drawLine(10,472,40,472,paint);
                // 154cm
                c.drawText("154 cm", 20, 344, paint_line);
                c.drawLine(10,354,40,354,paint);
                // 176cm
                c.drawText("176 cm", 20, 226, paint_line);
                c.drawLine(10,236,40,236,paint);
                // 198cm
                c.drawText("198 cm", 20, 108, paint_line);
                c.drawLine(10,118,40,118,paint);



                path.moveTo(myPath[0].x, myPath[0].y);

                for (int i = 1; i < myPath.length; i++){
                    path.lineTo(myPath[i].x, myPath[i].y);
                }
                c.drawPath(path, paint_path);
                holder.unlockCanvasAndPost(c);

                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {}
            }
        }

        public void pause() {

            isItOk=false;
            while (true) {
                try {
                    t.join();
                }
                catch (InterruptedException e)
                {e.printStackTrace();}
                break;
            }
            t=null;
        }

        public void resume() {

            isItOk=true;

            t = new Thread(this);
            t.start();
        }
    }
}

