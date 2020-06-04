package com.example.aeschatappbycongson;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class CheckActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private Cipher cipher, decipher;
    private SecretKeySpec secretKeySpec;

    EditText cEdit;
    TextView cText;
    TextView cText2;
    TextView tNhap;
    TextView tNhap2;
    TextView g1;
    TextView g2;
    Button cButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);

        cEdit = (EditText) findViewById(R.id.cEdit);
        cText = (TextView) findViewById(R.id.cText);
        cText2 = (TextView) findViewById(R.id.cText2);
        g1 = (TextView) findViewById(R.id.g1);
        g2 = (TextView) findViewById(R.id.g2);
        tNhap = (TextView) findViewById(R.id.tNhap);
        tNhap2 = (TextView) findViewById(R.id.tNhap2);
        cButton = (Button) findViewById(R.id.cButton);

        try {
            databaseReference = FirebaseDatabase.getInstance().getReference("MainData").child("Check");

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
                String cKey = dataSnapshot.child("Key").getValue().toString();
                byte[] encryptionKey = {99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99};
                try{
                    for (int i = 0; i < 16; i++){
                        if(cKey.getBytes()[i] > 47 && cKey.getBytes()[i] <59){
                            encryptionKey[i] = cKey.getBytes()[i];
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                secretKeySpec = new SecretKeySpec(encryptionKey, "AES");
                try {
                    g1.setText(dataSnapshot.child("Value").getValue().toString());
                    String cValue = AESDecryptionMethod(dataSnapshot.child("Value").getValue().toString(), secretKeySpec);
                    cText2.setText(cValue);
                    try {
                        MessageDigest md = MessageDigest.getInstance("MD5");
                        byte[] temp = cValue.getBytes();
                        md.update(temp);
                        byte[] hashdata = md.digest();
                        String tempString = "";
                        for(int i = 0; i < hashdata.length; i++){
                            tempString = tempString + String.valueOf(hashdata[i]);
                        }
                        g2.setText(tempString);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        cButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Date date = new Date();
                String cKey = String.valueOf(date.getTime()) + String.valueOf(date.getTime());
                databaseReference.child("Key").setValue(cKey.substring(0,16));
                byte[] encryptionKey = {99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99};
                try{
                    for (int i = 0; i < 16; i++){
                        if(cKey.getBytes()[i] > 47 && cKey.getBytes()[i] <59){
                            encryptionKey[i] = cKey.getBytes()[i];
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                secretKeySpec = new SecretKeySpec(encryptionKey, "AES");
                String cValue = AESEncryptionMethod(cEdit.getText().toString(), secretKeySpec);
                try {
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    byte[] temp = cEdit.getText().toString().getBytes();
                    md.update(temp);
                    byte[] hashdata = md.digest();
                    String tempString = "";
                    for(int i = 0; i < hashdata.length; i++){
                        tempString = tempString + String.valueOf(hashdata[i]);
                    }
                    cText.setText(tempString);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                databaseReference.child("Value").setValue(cValue);
                tNhap.setText(cEdit.getText());
                tNhap2.setText(cValue);
                cEdit.setText("");
            }
        });
    }
    private String AESEncryptionMethod(String string, SecretKeySpec secretKeySpec){
        byte[] stringByte = string.getBytes();
        byte[] encryptedByte = new byte[stringByte.length];
        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            encryptedByte = cipher.doFinal(stringByte);
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        String returnString = null;
        try {
            returnString = new String(encryptedByte, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return returnString;
    };

    private String AESDecryptionMethod(String string, SecretKeySpec secretKeySpec)
            throws UnsupportedEncodingException {
        byte[] EncryptedByte = string.getBytes("ISO-8859-1");
        String decryptedString = string;
        byte[] decryption;
        try {
            decipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            decryption = decipher.doFinal(EncryptedByte);
            decryptedString = new String(decryption);
        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return decryptedString;
    };
}