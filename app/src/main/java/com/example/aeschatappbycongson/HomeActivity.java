package com.example.aeschatappbycongson;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity {

    TextView txtTest;
    EditText key;
    EditText key2;
    EditText key3;
    Button start;
    Button startRoom;
    Button startShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        txtTest = (TextView) findViewById(R.id.textView);
        key = (EditText) findViewById(R.id.key);
        key2 = (EditText) findViewById(R.id.key2);
        key3 = (EditText) findViewById(R.id.key3);
        start = (Button) findViewById(R.id.createRoom);
        startRoom = (Button) findViewById(R.id.roomButton);
        startShare = (Button) findViewById(R.id.roomShare);

        final String noidungid = getIntent().getStringExtra("ID");
        final String noidungname = getIntent().getStringExtra("Name");

        txtTest.setText("Welcome " + noidungname);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String roomname;
                final String nameroom = key.getText().toString();
                if (nameroom.getBytes()[0] > noidungid.getBytes()[0]){
                    roomname = noidungid + nameroom;
                }else {
                    roomname = nameroom + noidungid;
                }
                final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("MainData").child("CheckRoom");
                databaseReference.child(roomname).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String temp = dataSnapshot.child(roomname).getValue().toString();
                        if(temp != null) {
                            Intent intent = new Intent(HomeActivity.this, RoomActivity.class);

                            intent.putExtra("IDRoom", roomname);
                            intent.putExtra("ID", noidungid);
                            intent.putExtra("Name", noidungname);
                            intent.putExtra("Name2", nameroom);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });

        startRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String nameroomtemp = key2.getText().toString();
                final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("MainData").child("CheckRoom");
                databaseReference.child(nameroomtemp).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String temp2 = dataSnapshot.child(nameroomtemp).getValue().toString();
                        if(temp2 != null) {
                            Intent intent = new Intent(HomeActivity.this, RoomActivity.class);

                            intent.putExtra("IDRoom", nameroomtemp);
                            intent.putExtra("ID", noidungid);
                            intent.putExtra("Name", noidungname);
                            intent.putExtra("Name2", nameroomtemp);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });

        startShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String nameroomtemp = key3.getText().toString();
                final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("MainData").child("CheckRoomImage");
                databaseReference.child(nameroomtemp).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String temp2 = dataSnapshot.child(nameroomtemp).getValue().toString();
                        if(temp2 != null) {
                            Intent intent = new Intent(HomeActivity.this, ImageActivity.class);

                            intent.putExtra("IDRoom", nameroomtemp);
                            intent.putExtra("Name", noidungname);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });
    }
}