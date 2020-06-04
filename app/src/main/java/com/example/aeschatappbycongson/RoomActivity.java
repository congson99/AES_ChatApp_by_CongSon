package com.example.aeschatappbycongson;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class RoomActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private Cipher cipher, decipher;
    private SecretKeySpec secretKeySpec;
    private String stringMessage;

    TextView t1;
    ListView lv;
    EditText noidungtext;
    EditText noidungma;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        t1 = (TextView) findViewById(R.id.test1);
        lv = (ListView) findViewById(R.id.lv);
        noidungtext = (EditText) findViewById(R.id.noidungne);
        noidungma = (EditText) findViewById(R.id.mahoane);

        Intent intent = getIntent();
        final String noidungid = intent.getStringExtra("ID");
        final String noidungname = intent.getStringExtra("Name");
        final String noidungroom = intent.getStringExtra("IDRoom");
        final String noidungname2 = intent.getStringExtra("Name2");

        t1.setText("You are chatting with " + noidungname2);

        try {
            databaseReference = FirebaseDatabase.getInstance().getReference("MainData").child("Room").child(noidungroom);

            try {
                cipher = Cipher.getInstance("AES");
                decipher = Cipher.getInstance("AES");
            } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //[1]
                stringMessage = dataSnapshot.getValue().toString();
                stringMessage = stringMessage.substring(1, stringMessage.length() - 1);
                String[] stringMessageArray = stringMessage.split(", ");
                Arrays.sort(stringMessageArray);
                //[1]
                String[] stringFinal = new String[stringMessageArray.length];
                try {
                    for (int i = stringMessageArray.length - 1 ; i >= 0; i--) {
                        String[] stringKeyValue = stringMessageArray[i].split("=", 2);
                        String CipherTemp = stringKeyValue[0];  //[2]
                        byte[] encryptionKey = {99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99}; //[3]
                        try{
                            //[4]
                            for (int j = 0; j < 16; j++){
                                if(CipherTemp.getBytes()[j] > 47 && CipherTemp.getBytes()[j] <59){
                                    encryptionKey[j] = CipherTemp.getBytes()[j];
                                }
                            }
                            //[4]
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        secretKeySpec = new SecretKeySpec(encryptionKey, "AES"); //[5]
                        stringFinal[i] = AESDecryptionMethod(stringKeyValue[1], secretKeySpec).substring(0,13);
                        stringFinal[i] = (String) android.text.format.DateFormat.format("dd-MM-yyyy hh:mm:ss", Long.parseLong(stringFinal[i]));
                        stringFinal[i] = stringFinal[i] + " -- " + AESDecryptionMethod(stringKeyValue[1], secretKeySpec).substring(13);
                    }

                    lv.setAdapter(new ArrayAdapter<String>(RoomActivity.this, android.R.layout.simple_expandable_list_item_1, stringFinal));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void sendButton (View view){
        String TextValue = noidungtext.getText().toString(); // [1]
        String CipherTemp = noidungma.getText().toString();  // [2]
        Date date = new Date();
        String ciphervalue = String.valueOf(date.getTime()) + CipherTemp + "aaa"; //[3]
        Intent intent = getIntent();
        final String noidungname = intent.getStringExtra("Name");
        byte[] encryptionKey = {99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99};  //[4]
        try{
            for (int i = 0; i < 16; i++){                                                         //[5]
                if(ciphervalue.getBytes()[i] > 47 && ciphervalue.getBytes()[i] <59){
                    encryptionKey[i] = ciphervalue.getBytes()[i];
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        secretKeySpec = new SecretKeySpec(encryptionKey, "AES");    //[6]
        databaseReference.child(ciphervalue.substring(0,16)).setValue(AESEncryptionMethod(String.valueOf(date.getTime()) + noidungname + ": " + TextValue, secretKeySpec));
        noidungtext.setText("");
        noidungma.setText("");
    }

    private String AESEncryptionMethod(String string, SecretKeySpec secretKeySpec){
        //[1]
        byte[] stringByte = string.getBytes();
        byte[] encryptedByte = new byte[stringByte.length];
        //[1]
        try {
            //[2]
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            encryptedByte = cipher.doFinal(stringByte);
            //[2]
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        String returnString = null;
        try {
            returnString = new String(encryptedByte, "ISO-8859-1"); //[3]
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return returnString;
    };

    private String AESDecryptionMethod(String string, SecretKeySpec secretKeySpec)
            throws UnsupportedEncodingException {
        byte[] EncryptedByte = string.getBytes("ISO-8859-1"); //[1]
        String decryptedString = string;
        byte[] decryption;
        try {
            //[2]
            decipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            decryption = decipher.doFinal(EncryptedByte);
            //[2]
            decryptedString = new String(decryption); //[3]
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return decryptedString;
    };
}