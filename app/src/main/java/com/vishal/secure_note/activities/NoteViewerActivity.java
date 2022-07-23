package com.vishal.secure_note.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Spanned;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.vishal.secure_note.R;
import com.vishal.secure_note.databinding.ActivityNoteViewerBinding;
import com.vishal.secure_note.room.database.NoteDatabase;
import com.vishal.secure_note.room.entities.NoteEntity;
import com.vishal.secure_note.util.Constant;

import org.commonmark.node.Node;

import io.noties.markwon.Markwon;

public class NoteViewerActivity extends AppCompatActivity {

    private ActivityNoteViewerBinding binding;
    private NoteEntity noteEntity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNoteViewerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        noteEntity = (NoteEntity) getIntent().getSerializableExtra("note");
        int noteId = getIntent().getIntExtra("noteId", -99);
        initNoteViewer(noteId);

        binding.backHomeArrowBtn.setOnClickListener(view -> {
            startActivity(new Intent(NoteViewerActivity.this, MainActivity.class));
            finish();
        });

        binding.editNoteBtn.setOnClickListener(view -> {
            Intent intent = new Intent(NoteViewerActivity.this, AddNoteActivity.class);
            intent.putExtra("noteMode", Constant.NOTE_EDIT_MODE);
            intent.putExtra("noteId", noteId);
            launchNoteEditor.launch(intent);
        });

    }

    private void initNoteViewer(int noteId) {
        NoteDatabase.getInstance(this).noteDao()
                .getNoteById(noteId)
                .observe(this, note -> {
                    noteEntity = note;
                    manageToolbarColor(noteEntity.getLabel());
                    updateLockIcon(noteEntity.isPrivate());

                    binding.noteTitleText.setText(noteEntity.getNoteTitle());
                    setMarkdownText();
                });
    }

    @SuppressLint("SetTextI18n")
    private void manageToolbarColor(int label) {
        switch (label) {
            case Constant.NORMAL_LABEL:
                binding.toolBarContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.primary));
                binding.noteLabelText.setText("Normal Note");
                break;
            case Constant.NEEDED_LABEL:
                binding.toolBarContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.green));
                binding.noteLabelText.setText("Needed Note");
                break;
            case Constant.IMPORTANT_LABEL:
                binding.toolBarContainer.setBackgroundColor(ContextCompat.getColor(this, R.color.red));
                binding.noteLabelText.setText("Important Note");
                break;
        }
    }

    private void setMarkdownText() {
        final Markwon markwon = Markwon.create(this);
        final Node node = markwon.parse(noteEntity.getNoteDescription());
        final Spanned markdown = markwon.render(node);
        markwon.setParsedMarkdown(binding.noteText, markdown);
    }


    private void updateLockIcon(boolean isPrivate) {
        if (isPrivate) binding.lockIconImg.setImageResource(R.drawable.ic_lock_closed);
        else binding.lockIconImg.setImageResource(R.drawable.ic_lock_open);
    }

    ActivityResultLauncher<Intent> launchNoteEditor = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            Intent intent = result.getData();
            if (intent != null) {
                int noteId = intent.getIntExtra("noteId", -99);
                initNoteViewer(noteId);
            }
        }
    });

}