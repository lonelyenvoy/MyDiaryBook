package ink.envoy.mydiarybook;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import ink.envoy.mydiarybook.model.Diary;
import ink.envoy.mydiarybook.model.DiarySaveStatus;
import ink.envoy.mydiarybook.util.DiaryDataAccessor;

public class DiaryDetailActivity extends AppCompatActivity {

    private TextView timeTextView;
    private EditText titleEditText;
    private EditText contentEditText;

    private ScheduledExecutorService executorService;
    private long currentDiaryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_detail);

        initialize();
    }

    @Override
    protected void onStop() {
        super.onStop();
        executorService.shutdownNow();
        saveDiary();
    }

    private void initialize() {
        bindViews();



        Intent intent = getIntent();
        currentDiaryId = intent.getIntExtra("diaryId", -1);
        if (currentDiaryId != -1) {
            Diary currentDiary = new DiaryDataAccessor(getApplicationContext()).get((int)currentDiaryId);
            showData(currentDiary);
        } else {
            showEmptyData();
        }
        setResult(1, new Intent());

        executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                String a = "1+1";
                String b = a + "1";
            }
        },0, 10, TimeUnit.MILLISECONDS);

        executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                new Thread(new DataPersistenceService()).start();
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    private void bindViews() {
        timeTextView = (TextView) findViewById(R.id.timeTextView);
        titleEditText = (EditText) findViewById(R.id.titleEditText);
        contentEditText = (EditText) findViewById(R.id.contentEditText);
    }

    @SuppressLint("SetTextI18n")
    private void showData(Diary diary) {
        timeTextView.setText(getFormattedUpdateTimeString(diary.updatedAt));
        titleEditText.setText(diary.title);
        contentEditText.setText(diary.content);
    }

    private void refreshUpdateTime() {
        if (currentDiaryId == -1) throw new IllegalStateException("currentDiaryId not specified");
        Diary currentDiary = new DiaryDataAccessor(getApplicationContext()).get((int)currentDiaryId);
        timeTextView.setText(getFormattedUpdateTimeString(currentDiary.updatedAt));
    }

    private String getFormattedUpdateTimeString(long time) {
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateStr = dateformat.format(time);
        return "最后编辑于 " + dateStr + " (数据实时保存)";
    }

    private void showEmptyData() {
        timeTextView.setVisibility(View.GONE);
    }

    private DiarySaveStatus saveDiary() {
        if (!titleEditText.getText().toString().trim().equals("")) {
            if (currentDiaryId == -1) {
                DiaryDataAccessor da = new DiaryDataAccessor(getApplicationContext());
                currentDiaryId = da.post(new Diary(
                        (int)currentDiaryId,
                        titleEditText.getText().toString(),
                        contentEditText.getText().toString(),
                        System.currentTimeMillis()));
                return DiarySaveStatus.CREATED;
            } else {
                return new DiaryDataAccessor(getApplicationContext())
                        .put(new Diary(
                                (int)currentDiaryId,
                                titleEditText.getText().toString(),
                                contentEditText.getText().toString(),
                                System.currentTimeMillis()));
            }
        } else {
            return DiarySaveStatus.NO_NEED_TO_SAVE;
        }
    }

    private class DataPersistenceService implements Runnable{
        public void run(){
            if (saveDiary() != DiarySaveStatus.NO_NEED_TO_SAVE) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refreshUpdateTime();
                    }
                });
            }
        }
    }


}
