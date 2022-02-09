package com.example.firebasemessenger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
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
    ChildEventListener messagesChildEventListener; // все изм-я, которые происходят в определ-м узле отображаются тут

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = FirebaseDatabase.getInstance("https://fir-messenger-67f8c-default-rtdb.europe-west1.firebasedatabase.app/");
        // эта запись получает доступ ко всей бд, к корневой папке бд. Ссылка ведёт к бд на платформе firebase.
        messagesDatabaseReference = database.getReference().child("messages");
        // присваиваем к messagesDatabaseReference кусок от database по названию узла messages

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

                MessageModel message = new MessageModel();
                message.setText(messageEditText.getText().toString());
                message.setName(userName);
                message.setImageUrl(null);

                messagesDatabaseReference.push().setValue(message);

                messageEditText.setText("");

            }
        });

        sendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        messagesChildEventListener = new ChildEventListener() { // прикрепляем к messagesDatabaseReference  messagesChildEventListener
            @Override// 5 методов анонимного класса генерируются сами
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {

                MessageModel message = dataSnapshot.getValue(MessageModel.class);
                /* из dataSnapshot- общего "снимка" данных мы получаем значение, а внутри указываем, что это значение
                 можно распознать в классе MessageModel. После того, как мы получаем этот объект, у него такие же поля,
                  как и у нашего класса*/
                adapter.add(message); // Устанавливаем к адаптеру разметки страницы эту хуету
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        messagesDatabaseReference.addChildEventListener(messagesChildEventListener);
    }
}