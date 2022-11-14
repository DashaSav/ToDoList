package com.example.todolist;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.todolist.models.Note;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NoteTakenActivity extends AppCompatActivity {
    EditText editText_title, editText_notes;
    ImageView imageView_save;
    Note note;
    boolean isOldNote = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_taken);
        imageView_save = findViewById(R.id.imageView_save);
        editText_title = findViewById(R.id.editText_title);
        editText_notes = findViewById(R.id.editText_notes);

        note = new Note();
        try {
            note = (Note) getIntent().getSerializableExtra("old_note");
            editText_title.setText(note.getTitle());
            editText_notes.setText(note.getNotes());
            isOldNote = true;

        }catch (Exception e){
            e.printStackTrace();

        }
        imageView_save.setOnClickListener(v -> {
            String title = editText_title.getText().toString();
            String description = editText_notes.getText().toString();

            if(description.isEmpty()){
                Toast.makeText(NoteTakenActivity.this, "Please, enter description", Toast.LENGTH_SHORT).show();
                return;
            }

            SimpleDateFormat formatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
            Date date = new Date();
            if (!isOldNote) {
                note = new Note();
            }

            note.setTitle(title);
            note.setNotes(description);
            note.setDate(formatter.format(date));

            Intent intent = new Intent();
            intent.putExtra("note", note);
            setResult(Activity.RESULT_OK, intent);

            finish();
        });
    }
}