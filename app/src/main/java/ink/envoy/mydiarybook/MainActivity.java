package ink.envoy.mydiarybook;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ink.envoy.mydiarybook.model.Diary;
import ink.envoy.mydiarybook.util.DiaryDataAccessor;

public class MainActivity extends AppCompatActivity {

    private TextView noDiaryHintTextView;
    private RecyclerView recyclerView;

    private MyRecyclerViewAdapter myRecyclerViewAdapter;

    private List<Diary> diaries = new ArrayList<Diary>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        initialize();
    }

    private void initialize() {
        bindViews();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startDiaryDetailActivityForNewDiary();
            }
        });

        // 拿到RecyclerView
        recyclerView = (RecyclerView) findViewById(R.id.list);
        // 设置LinearLayoutManager
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // 设置ItemAnimator
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        // 设置固定大小
        recyclerView.setHasFixedSize(true);

        loadData();
        refreshDiaryViews();
    }

    private void bindViews() {
        noDiaryHintTextView = (TextView) findViewById(R.id.noDiaryHintTextView);
    }

    private void loadData() {
        diaries = new DiaryDataAccessor(getApplicationContext()).getAll();
    }

    private void startDiaryDetailActivityForNewDiary() {
        Intent intent = new Intent(getApplicationContext(), DiaryDetailActivity.class);
        startActivityForResult(intent, 1);
    }

    private void refreshDiaryViews() {
        // 初始化自定义的适配器
        myRecyclerViewAdapter = new MyRecyclerViewAdapter(this, diaries);

        myRecyclerViewAdapter.setOnItemClickListener(new MyItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(getApplicationContext(), DiaryDetailActivity.class);
                intent.putExtra("diaryId", diaries.get(position)._id);
                startActivityForResult(intent, 1);
            }

            @Override
            public void onItemLongClick(View view, int position) {
                deleteDiary(view, position);
            }
        });

        // 为mRecyclerView设置适配器
        recyclerView.setAdapter(myRecyclerViewAdapter);

        if (!diaries.isEmpty()) {
            noDiaryHintTextView.setVisibility(View.GONE);
        } else {
            noDiaryHintTextView.setVisibility(View.VISIBLE);
        }

    }

    private void deleteDiary(final View view, final int position) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage("要删除此日记吗？")
                .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        final int deletingDiaryId = diaries.get(position)._id;
                        final Handler handler = new Handler();
                        final Runnable runnable = new Runnable() {
                            @Override
                            public void run() {
                                new DiaryDataAccessor(getApplicationContext()).delete(deletingDiaryId);
//                                loadData();
//                                refreshDiaryViews();
                            }
                        };
                        handler.postDelayed(runnable, 3500);

                        Snackbar.make(view, "日记已删除", Snackbar.LENGTH_LONG)
                                .setAction("撤销", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        handler.removeCallbacks(runnable);
                                        loadData();
                                        refreshDiaryViews();
                                    }
                                }).show();

                        diaries.remove(position);
                        refreshDiaryViews();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {}
                })
                .create()
                .show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_new) {
            startDiaryDetailActivityForNewDiary();
            return true;
        } else if (id == R.id.action_delete_all) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("确认要清空日记本吗？")
                    .setMessage("此操作不可撤销。")
                    .setPositiveButton("清空", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            new DiaryDataAccessor(getApplicationContext()).clear();
                            loadData();
                            refreshDiaryViews();
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {}
                    })
                    .create()
                    .show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 1) {
            // 等待DiaryDetailActivity的onStop()方法执行完毕，将数据保存后再刷新
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadData();
                    refreshDiaryViews();
                }
            }, 500);
        }
    }
}

interface MyItemClickListener {
    public void onItemClick(View view, int position);
    public void onItemLongClick(View view, int position);
}

class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyViewHolder>
{

    private List<Diary> diaries;
    private Context mContext;

    private MyItemClickListener mItemClickListener;

    public MyRecyclerViewAdapter( Context context , List<Diary> diaries)
    {
        this.mContext = context;
        this.diaries = diaries;
    }

    @Override
    public MyViewHolder onCreateViewHolder( ViewGroup viewGroup, int i )
    {
        // 给ViewHolder设置布局文件
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_view, viewGroup, false);
        return new MyViewHolder(v, mItemClickListener);
    }

    @Override
    public void onBindViewHolder( MyViewHolder viewHolder, int i )
    {
        // 给ViewHolder设置元素
        Diary diary = diaries.get(i);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = dateFormat.format(diary.updatedAt);
        viewHolder.mMonthTextView.setText(date.substring(5, 7));
        viewHolder.mDayTextView.setText(date.substring(8, 10));
        viewHolder.mTitleTextView.setText(diary.title);
        viewHolder.mContentTextView.setText(diary.content);
    }

    @Override
    public int getItemCount()
    {
        // 返回数据总数
        return diaries == null ? 0 : diaries.size();
    }

    /**
     * 设置Item点击监听
     * @param listener
     */
    public void setOnItemClickListener(MyItemClickListener listener){
        this.mItemClickListener = listener;
    }

//    // 重写的自定义ViewHolder
//    public static class ViewHolder
//            extends RecyclerView.ViewHolder
//    {
//        public TextView mTitleTextView;
//        public TextView mContentTextView;
//
//        public ViewHolder( View v )
//        {
//            super(v);
//            mTitleTextView = (TextView) v.findViewById(R.id.title);
//            mContentTextView = (TextView) v.findViewById(R.id.content);
//        }
//    }
}

class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

    public TextView mMonthTextView;
    public TextView mDayTextView;
    public TextView mTitleTextView;
    public TextView mContentTextView;
    private MyItemClickListener mListener;

    public MyViewHolder(View rootView,MyItemClickListener listener) {
        super(rootView);
        mTitleTextView = (TextView) rootView.findViewById(R.id.title);
        mContentTextView = (TextView) rootView.findViewById(R.id.content);
        mMonthTextView = (TextView) rootView.findViewById(R.id.monthTextView);
        mDayTextView = (TextView) rootView.findViewById(R.id.dayTextView);
        this.mListener = listener;
        rootView.setOnClickListener(this);
        rootView.setOnLongClickListener(this);
    }

    /**
     * 点击监听
     */
    @Override
    public void onClick(View v) {
        if(mListener != null){
            mListener.onItemClick(v, getPosition());
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (mListener != null) {
            mListener.onItemLongClick(v, getPosition());
        }
        return true;
    }
}
