package com.example.aeschatappbycongson;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class ImageActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private Cipher cipher, decipher;
    private SecretKeySpec secretKeySpec;

    TextView t1;
    TextView tx;
    Button share;
    Button take;
    ImageView img;

    int REQUEST_CODE =1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        t1 = (TextView) findViewById(R.id.imageText);
        tx = (TextView) findViewById(R.id.tx);
        take = (Button) findViewById(R.id.buttonTake);
        share = (Button) findViewById(R.id.buttonShare);
        img = (ImageView) findViewById(R.id.imageView);

        final Intent intent = getIntent();
        final String noidungname = intent.getStringExtra("Name");
        final String noidungroom = intent.getStringExtra("IDRoom");

        try {
            databaseReference = FirebaseDatabase.getInstance().getReference("MainData").child("ImageRoom").child(noidungroom);

            try {
                cipher = Cipher.getInstance("AES");
                decipher = Cipher.getInstance("AES");
            } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        t1.setText("You are sharing in " + noidungroom);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                img.setImageDrawable(null);
                tx.setText(dataSnapshot.child("Name").getValue().toString() + " đã chia sẻ một hình ảnh");
                String CipherTemp = dataSnapshot.child("Key").getValue().toString();
                byte[] encryptionKey = {99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99};
                try{
                    for (int j = 0; j < 16; j++){
                        if(CipherTemp.getBytes()[j] > 47 && CipherTemp.getBytes()[j] <59){
                            encryptionKey[j] = CipherTemp.getBytes()[j];
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                secretKeySpec = new SecretKeySpec(encryptionKey, "AES");
                try {
                    String imageHinh = AESDecryptionMethod(dataSnapshot.child("Image").getValue().toString(), secretKeySpec);
                    byte[] mangGet = Base64.decode(imageHinh, Base64.DEFAULT);
                    Bitmap bm = BitmapFactory.decodeByteArray(mangGet, 0, mangGet.length);
                    img.setImageBitmap(bm);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        take.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,REQUEST_CODE);

            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte[] hinh = ImageViewToByte(img); //[1]
                String chuoiHinh = Base64.encodeToString(hinh, Base64.DEFAULT);  //[2]
                //[3]
                Date date = new Date();
                String ciphervaluex = String.valueOf(date.getTime()) + String.valueOf(date.getTime());
                byte[] encryptionKey = {99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99, 99};
                try{
                    for (int i = 0; i < 16; i++){
                        if(ciphervaluex.getBytes()[i] > 47 && ciphervaluex.getBytes()[i] <59){
                            encryptionKey[i] = ciphervaluex.getBytes()[i];
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                secretKeySpec = new SecretKeySpec(encryptionKey, "AES");
                //[3]
                //[4]
                databaseReference.child("Key").setValue(ciphervaluex.substring(0,16));
                databaseReference.child("Image").setValue(AESEncryptionMethod(chuoiHinh, secretKeySpec));
                //[4]
                databaseReference.child("Name").setValue(noidungname);
                img.setImageDrawable(null);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_CODE && data != null){
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            img.setImageBitmap(bitmap);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public byte[] ImageViewToByte(ImageView v){
        BitmapDrawable drawable = (BitmapDrawable) v.getDrawable();
        Bitmap bmp = drawable.getBitmap();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
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