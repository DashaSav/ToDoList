package com.example.todolist.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.todolist.models.Note;

@Database(entities = Note.class, version = 1)
public abstract class RoomDB extends RoomDatabase {
    private static RoomDB database;
    private final static String DATABASE_NAME = "NoteApp";

    public synchronized static RoomDB getInstance(Context context){
        if  (database == null) {
            database = Room.databaseBuilder(context.getApplicationContext(), RoomDB.class, DATABASE_NAME)
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return database;

    }
    public abstract mainDAO mainDao();
}
