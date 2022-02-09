package com.example.firebasemessenger;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView messageListView;
    private FirebaseMessageAdapter adapter;
    private ProgressBar progressBar;
    private ImageButton sendImageButton;
    private Button sendMessageButton;
    private EditText messageEditText;

    private String userName;

    FirebaseDatabase database; // класс базы данных
    DatabaseReference messagesDatabaseReference; // класс- ссылка на базу данных, который указывает уже на опр. узел в БД
    DatabaseReference usersDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = FirebaseDatabase.getInstance("https://fir-messenger-67f8c-default-rtdb.europe-west1.firebasedatabase.app/");
        // эта запись получает доступ ко всей бд, к корневой папке бд. Ссылка ведёт к бд на платформе firebase.
        messagesDatabaseReference = database.getReference().child("messages");
        // присваиваем к messagesDatabaseReference кусок от database по названию узла messages
        usersDatabaseReference = database.getReference().child("users");

        messagesDatabaseReference.child("message1").setValue("Hello Firebase!"); //уст. значение в вышеуказанный узел
        messagesDatabaseReference.child("message2").setValue("Hello World!");
        usersDatabaseReference.child("user1").setValue("Bob");

        progressBar = findViewById(R.id.progressBar);
        sendImageButton = findViewById(R.id.sendImageButton);
        sendMessageButton = findViewById(R.id.sendMessageButton);
        messageEditText = findViewById(R.id.messageEditText);

        userName = "Default User";

        messageListView = findViewById(R.id.messageListView);
        List<MessageModel> messageModels = new ArrayList<>();
        adapter = new FirebaseMessageAdapter(this, R.layout.message_item,
                messageModels);
        messageListView.setAdapter(adapter);

        progressBar.setVisibility(ProgressBar.INVISIBLE);

        messageEditText.addTextChangedListener(new TextWatcher() {
            //кнопка отправить изначально не работает из-за пустого поля. Этот метод позволяет отслеживать изменения.
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
                //s в данном случае- наше сообщение
                if (s.toString().trim().length() > 0) { //переводим в стринг-обрезаем пробелы-смотрим длину
                    sendMessageButton.setEnabled(true);
                } else {
                    sendMessageButton.setEnabled(false);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        messageEditText.setFilters(new InputFilter[]
                {new InputFilter.LengthFilter(500)}); //fильтр на макс. кол-во символов

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                messageEditText.setText("");

            }
        });

        sendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }
}