package com.example.aeschatappbycongson;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    Button button;
    EditText id;
    EditText pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.login);
        id = (EditText) findViewById(R.id.idText);
        pass = (EditText) findViewById(R.id.passText);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("MainData").child("Info").child(id.getText().toString()).child(pass.getText().toString()).child("Name");
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String key = (String) dataSnapshot.getValue();
                        if(key != null){
                            Intent intent = new Intent(MainActivity.this, HomeActivity.class);

                            intent.putExtra("ID", id.getText().toString());
                            intent.putExtra("Name", key);
                            pass.setText("");
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