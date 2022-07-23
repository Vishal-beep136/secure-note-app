package com.vishal.secure_note.activities;

import static com.vishal.secure_note.util.Constant.APPS_PASSWORD;
import static com.vishal.secure_note.util.Constant.APP_PASSWORD_KEY;
import static com.vishal.secure_note.util.Constant.APP_PASSWORD_SET_REQ_CODE;
import static com.vishal.secure_note.util.Constant.CUSTOM_PASSWORD;
import static com.vishal.secure_note.util.Constant.NORMAL_LABEL;
import static com.vishal.secure_note.util.Constant.NOTE_ADD_MODE;
import static com.vishal.secure_note.util.Constant.NOT_APP_PASSWORD_SET;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.vishal.secure_note.R;
import com.vishal.secure_note.databinding.ActivityAddNoteBinding;
import com.vishal.secure_note.room.daos.NoteDao;
import com.vishal.secure_note.room.database.NoteDatabase;
import com.vishal.secure_note.room.entities.NoteEntity;
import com.vishal.secure_note.util.Constant;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;
import java.util.concurrent.Executors;

import io.noties.markwon.Markwon;
import io.noties.markwon.editor.MarkwonEditor;
import io.noties.markwon.editor.MarkwonEditorTextWatcher;

public class AddNoteActivity extends AppCompatActivity {

    private ActivityAddNoteBinding binding;
    private int labelValue = NORMAL_LABEL;
    private int passwordType = -1;
    private String customPasswordStr = "";
    private NoteEntity oldNoteEntity;

    // TODO: 7/14/2022 Not Implemented Yet - Add styling options in text - bold, italic

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        int noteMode = getIntent().getIntExtra("noteMode", Constant.NOTE_ADD_MODE);
        int noteId = getIntent().getIntExtra("noteId", -99);

        // this is not typo Markwon
        final Markwon markwon = Markwon.create(this);
        final MarkwonEditor markwonEditor = MarkwonEditor.create(markwon);

        // on arrow btn pressed
        binding.backHomeArrowBtn.setOnClickListener(view -> onArrowBtnPressed());

        if (noteMode == NOTE_ADD_MODE) {
            // add note to db on click
            binding.noteDoneBtn.setOnClickListener(view -> {
                if (isNoteValid()) addNoteToDb();
                else
                    Toast.makeText(this, "Please add note title and description!", Toast.LENGTH_LONG).show();
            });
        } else {
            NoteDatabase.getInstance(this)
                    .noteDao().getNoteById(noteId)
                    .observe(this, note -> {
                        oldNoteEntity = note;
                        binding.noteTitleEt.setText(oldNoteEntity.getNoteTitle());
                        binding.noteDescEt.setText(oldNoteEntity.getNoteDescription());

                        passwordType = oldNoteEntity.getPasswordType();
                        labelValue = oldNoteEntity.getLabel();

                        changeToolbarColorOnLabelChange(labelValue);

                        binding.noteDoneBtn.setOnClickListener(view -> {
                            if (isSomethingChangedInNewNote()) {
                                updateNote();
                            } else {
                                Toast.makeText(this, "Nothing Changed!", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        });
                    });
        }

        // on lock btn pressed
        binding.privateNoteBtn.setOnClickListener(view -> showLockDialog());
        // change label of note on click
        binding.labelNoteBtn.setOnClickListener(view -> showLabelDialog());

        // setting markdown on text changed listener
        binding.noteDescEt.addTextChangedListener(MarkwonEditorTextWatcher.withPreRender(
                markwonEditor,
                Executors.newCachedThreadPool(),
                binding.noteDescEt
        ));
    }

    private void updateNote() {
        int noteID = oldNoteEntity.getUid();
        NoteEntity newNote = getNoteEntity();
        new Thread(() -> {
            NoteDao updateDao = NoteDatabase.getInstance(AddNoteActivity.this).noteDao();

            if (newNote.isPrivate() != oldNoteEntity.isPrivate())
                updateDao.updateVisibility(noteID, newNote.isPrivate());

            if (!newNote.getNoteTitle().equals(oldNoteEntity.getNoteTitle()))
                updateDao.updateNoteTitle(noteID, newNote.getNoteTitle());

            if (!newNote.getNoteDescription().equals(oldNoteEntity.getNoteDescription()))
                updateDao.updateNoteDescription(noteID, newNote.getNoteDescription());

            if (!newNote.getPassword().equals(oldNoteEntity.getPassword()))
                updateDao.updatePassword(noteID, newNote.getPassword());

            if (newNote.getPasswordType() != oldNoteEntity.getPasswordType())
                updateDao.updatePasswordType(noteID, newNote.getPasswordType());

            if (newNote.getLabel() != oldNoteEntity.getLabel())
                updateDao.updateLabel(noteID, newNote.getLabel());

            runOnUiThread(() -> {
                Toast.makeText(this, "Note Updated Successfully!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.putExtra("noteId", noteID);
                setResult(RESULT_OK, intent);
                finish();
            });
        }).start();

    }


    private void addNoteToDb() {
        NoteEntity noteEntity = getNoteEntity();

        // creating new thread for inserting note
        final Thread insertNoteThread = new Thread(() -> {
            // inserting note to db
            NoteDatabase.getInstance(AddNoteActivity.this).noteDao().insertNote(noteEntity);

            // starting activity and showing success msg from main thread
            runOnUiThread(() -> {
                Toast.makeText(this, "Note Added Successfully!", Toast.LENGTH_SHORT).show();
                finish();
            });

        });

        // starting thread
        insertNoteThread.start();
    }

    private void showLabelDialog() {
        final String[] labelsItem = new String[]{"Normal", "Needed", "Important"};
        AlertDialog.Builder labelDialog = new AlertDialog.Builder(AddNoteActivity.this);
        labelDialog.setIcon(R.drawable.ic_secure_note_logo);
        labelDialog.setTitle("choose label \n");
        labelDialog.setSingleChoiceItems(labelsItem, labelValue, (dialogInterface, i) -> {
            labelValue = i;
            changeToolbarColorOnLabelChange(labelValue);
            dialogInterface.dismiss();
        });
        labelDialog.setNegativeButton("cancel", null);
        labelDialog.show();
    }

    @SuppressLint("SetTextI18n")
    private void changeToolbarColorOnLabelChange(int label) {
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

    private void showLockDialog() {
        final String[] optionForLock = new String[]{
                "Use this App's password to lock",
                "Use custom password to lock"
        };
        AlertDialog.Builder lockDialog = new AlertDialog.Builder(AddNoteActivity.this);
        lockDialog.setIcon(R.drawable.ic_secure_note_logo);
        lockDialog.setTitle("Choose Label");
        lockDialog.setSingleChoiceItems(optionForLock, passwordType, (dialogInterface, i) -> {
            passwordType = i + 1;
            switch (i + 1) {
                case APPS_PASSWORD:
                    handleAppsPasswordSelected(dialogInterface);
                    break;
                case CUSTOM_PASSWORD:
                    handleCustomPasswordSelected(dialogInterface);
                    break;
            }
        });

        lockDialog.setNegativeButton("Clear", (dialogInterface, i) -> {
            passwordType = -1;
            updateLockIcon(false);
            Toast.makeText(this, "Note is Unlocked!", Toast.LENGTH_SHORT).show();
        });

        lockDialog.show();
    }

    private void handleCustomPasswordSelected(@NonNull DialogInterface dialogInterface) {
        dialogInterface.dismiss();
        Dialog dialog = new Dialog(AddNoteActivity.this);
        dialog.setContentView(R.layout.custom_lock_dialog_layout);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setCancelable(true);

        TextInputLayout customPasswordBox = dialog.findViewById(R.id.customLockPassword);
        TextInputLayout customConfirmPassword = dialog.findViewById(R.id.confirmCustomLockPassword);
        Button confirmPasswordBtn = dialog.findViewById(R.id.confirmCustomPasswordBtn);

        confirmPasswordBtn.setOnClickListener(view -> {
            String customPassword = Objects.requireNonNull(customPasswordBox.getEditText()).getText().toString();
            String confirmPassword = Objects.requireNonNull(customConfirmPassword.getEditText()).getText().toString();

            if (customPassword.equals(confirmPassword)) {
                updateLockIcon(true);
                customPasswordStr = customPassword;
                passwordType = CUSTOM_PASSWORD;
                dialog.dismiss();
                return;
            }
            passwordType = -1;
            updateLockIcon(false);
            Toast.makeText(this, "Password do not match!", Toast.LENGTH_SHORT).show();
        });
        dialog.show();
    }

    private void handleAppsPasswordSelected(DialogInterface dialogInterface) {
        SharedPreferences appPref = PreferenceManager.getDefaultSharedPreferences(AddNoteActivity.this);
        String appPassword = appPref.getString(APP_PASSWORD_KEY, "");
        if (appPassword.equalsIgnoreCase(NOT_APP_PASSWORD_SET) || appPassword.isEmpty()) {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra("requestSetPassword", APP_PASSWORD_SET_REQ_CODE);
            dialogInterface.dismiss();
            launchSetPasswordForApp.launch(intent);
            return;
        }
        dialogInterface.dismiss();
        passwordType = APPS_PASSWORD;
        updateLockIcon(true);
    }

    private boolean isNoteValid() {
        if (binding.noteTitleEt.getText().toString().isEmpty()) return false;
        return !binding.noteDescEt.getText().toString().isEmpty();
    }

    private boolean isSomethingChangedInNewNote() {
        NoteEntity updatedNote = getNoteEntity();
        return !updatedNote.equals(oldNoteEntity);
    }

    @NonNull
    private NoteEntity getNoteEntity() {
        final String noteTitle = binding.noteTitleEt.getText().toString();
        final String noteDesc = binding.noteDescEt.getText().toString();

        return new NoteEntity(
                noteTitle,
                noteDesc,
                System.currentTimeMillis(),
                passwordType != -1,
                customPasswordStr,
                labelValue,
                passwordType);
    }

    private void onArrowBtnPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Discard")
                .setMessage("Are you really want to close!")
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    startActivity(new Intent(AddNoteActivity.this, MainActivity.class));
                    finish();
                }).setNegativeButton("No", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void updateLockIcon(boolean isPrivate) {
        if (isPrivate) {
            binding.privateNoteBtn.setImageResource(R.drawable.ic_lock_closed);
            Toast.makeText(this, "Note Locked!", Toast.LENGTH_SHORT).show();
        } else binding.privateNoteBtn.setImageResource(R.drawable.ic_lock_open);
    }

    ActivityResultLauncher<Intent> launchSetPasswordForApp = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent intent = result.getData();
                    if (intent != null) {
                        boolean passwordAvailable = intent.getBooleanExtra("isPasswordSet", false);
                        if (!passwordAvailable) {
                            Toast.makeText(this, "Please set app password!", Toast.LENGTH_SHORT).show();
                            updateLockIcon(false);
                            return;
                        }
                        updateLockIcon(true);
                        passwordType = APPS_PASSWORD;
                    } else updateLockIcon(false);
                } else updateLockIcon(false);
            });

    @Override
    public void onBackPressed() {
        onArrowBtnPressed();
    }
}