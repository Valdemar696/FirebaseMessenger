package com.example.firebasemessenger;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private ListView messageListView;
    private FirebaseMessageAdapter adapter;
    private ProgressBar progressBar;
    private ImageButton sendImageButton;
    private Button sendMessageButton;
    private EditText messageEditText;

    private String userName;
    private String recipientUserId;

    private static final int RC_IMAGE_PICKER = 123;

    private FirebaseAuth auth;
    private FirebaseDatabase database; // класс базы данных
    private DatabaseReference messagesDatabaseReference; // класс- ссылка на базу данных, который указывает уже на опр. узел в БД
    private ChildEventListener messagesChildEventListener; // все изм-я, которые происходят в определ-м узле отображаются тут
    private DatabaseReference usersDatabaseReference;
    private ChildEventListener usersChildEventListener;

    private FirebaseStorage storage;
    private StorageReference chatImagesStorageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        auth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        if(intent != null) {
            userName = intent.getStringExtra("userName");
            recipientUserId = intent.getStringExtra("recipientUserId");
        } else {
            userName = "Default User";
        }

        database = FirebaseDatabase.getInstance("https://fir-messenger-67f8c-default-rtdb.europe-west1.firebasedatabase.app/");
        // эта запись получает доступ ко всей бд, к корневой папке бд. Ссылка ведёт к бд на платформе firebase.
        messagesDatabaseReference = database.getReference().child("messages");
        // присваиваем к messagesDatabaseReference кусок от database по названию узла messages
        usersDatabaseReference = database.getReference().child("users");

        storage = FirebaseStorage.getInstance("gs://fir-messenger-67f8c.appspot.com/");
        chatImagesStorageReference = storage.getReference().child("chat_images");

        progressBar = findViewById(R.id.progressBar);
        sendImageButton = findViewById(R.id.sendImageButton);
        sendMessageButton = findViewById(R.id.sendMessageButton);
        messageEditText = findViewById(R.id.messageEditText);

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
                message.setSender(auth.getCurrentUser().getUid());
                message.setRecipient(recipientUserId);
                message.setImageUrl(null);

                messagesDatabaseReference.push().setValue(message);

                messageEditText.setText("");

            }
        });

        sendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                // создаём интент. ГетКантент - указывает, что мы созд. интент для получения контента
                intent.setType("image/*"); // указываем тип интента ( этот для получения всех типов изображений)
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true); // изображения будут браться с локального хранилища телефона
                startActivityForResult(Intent.createChooser(intent, "Choose an image"), RC_IMAGE_PICKER);
                // создаём активити выбора, помещаем сюда интент и пишём заголовок. Второй параметр - код запроса
            }
        });

        usersChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                User user = dataSnapshot.getValue(User.class);
                if (user.getFirebaseId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    userName = user.getName();
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        usersDatabaseReference.addChildEventListener(usersChildEventListener);

        messagesChildEventListener = new ChildEventListener() { // прикрепляем к messagesDatabaseReference  messagesChildEventListener
            @Override// 5 методов анонимного класса генерируются сами
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {

                MessageModel message = dataSnapshot.getValue(MessageModel.class);
                /* из dataSnapshot- общего "снимка" данных мы получаем значение, а внутри указываем, что это значение
                 можно распознать в классе MessageModel. После того, как мы получаем этот объект, у него такие же поля,
                  как и у нашего класса*/
                if (message.getSender().equals(auth.getCurrentUser().getUid())
                && message.getRecipient().equals(recipientUserId) ||
                        message.getRecipient().equals(auth.getCurrentUser().getUid())
                                && message.getSender().equals(recipientUserId) ) {
                    adapter.add(message); // Устанавливаем к адаптеру разметки страницы это сообщение
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        messagesDatabaseReference.addChildEventListener(messagesChildEventListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) { // создание меню
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.sign_out:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(ChatActivity.this, SignInActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // переопределяем метод чтобы обработать результат startActivityForResult (sAFR выдаёт адрес изображения на телефоне)
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_IMAGE_PICKER && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            final StorageReference imageReference = chatImagesStorageReference.child(selectedImageUri.getLastPathSegment());
            // получаем последний сегмент Uri хранилища и присваиваем его imgRefrnce (название)

            UploadTask uploadTask = imageReference.putFile(selectedImageUri);

            uploadTask = imageReference.putFile(selectedImageUri); // отсюда пошёл код с документации файбэйза

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return imageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        MessageModel message = new MessageModel();
                        message.setImageUrl(downloadUri.toString());
                        message.setName(userName);
                        message.setSender(auth.getCurrentUser().getUid());
                        message.setRecipient(recipientUserId);
                        messagesDatabaseReference.push().setValue(message);
                    } else {
                        // Handle failures
                        // ...
                    }
                }
            }); // до сюда код с доков огнебазы

        }
    }
}