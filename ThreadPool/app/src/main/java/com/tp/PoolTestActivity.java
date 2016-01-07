package com.tp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class PoolTestActivity extends Activity {

    ListView listView;
    private static int order = 0;
    boolean isCancle = false;

    /* 总共多少任务（根据CPU个数决定创建活动线程的个数,这样取的好处就是可以让手机承受得住 */
    private static final int count = Runtime.getRuntime().availableProcessors() * 3 + 2;
    private ArrayList<AsyTaskItem> list;
    // 。。每次只执行一条任务的线程池
    private static ExecutorService singleTaskService = null;
    // 。。每次执行限定个数任务的线程池
    private static ExecutorService limitedTaskService = null;
    // 。。 所有任务一次性开始的线程池
    private static ExecutorService allTaskService = null;
    // 。。指定时间里执行任务的线程池，可重复使用
    private static ExecutorService scheduledTaskService = null;
    /**
     * 创建一个可在指定时间里执行任务的线程池，亦可重复执行（不同之处：使用工程模式）
     */
    private static ExecutorService scheduledTaskFactoryExecutor = null;

    Button button;

    static {
        singleTaskService = Executors.newSingleThreadExecutor();
        limitedTaskService = Executors.newFixedThreadPool(3);
        allTaskService = Executors.newCachedThreadPool();
        scheduledTaskService = Executors.newScheduledThreadPool(3);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.demo);
        listView = (ListView) findViewById(R.id.task_list);
        listView.setAdapter(new MyAdapter(getApplication(), count));
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (position == 0) {
                    /**
                     * 会关闭线程池方式一：但不接收新的Task,关闭后，正在等待 执行的任务不受任何影响，会正常执行,无返回值!
                     */
                    // allTaskService.shutdown();

                    /**
                     * 会关闭线程池方式二：也不接收新的Task，并停止正等待执行的Task（也就是说，
                     * 执行到一半的任务将正常执行下去），最终还会给你返回一个正在等待执行但线程池关闭却没有被执行的Task集合！
                     */
                    List<Runnable> unExecRunn = allTaskService.shutdownNow();

                    for (Runnable r : unExecRunn) {
                        Log.i("KKK", "未执行的任务信息：=" + unExecRunn.toString());
                    }
                    Log.i("KKK", "Is shutdown ? = "
                            + String.valueOf(allTaskService.isShutdown()));
                    allTaskService = null;
                }
                if (position == 1) {

                } else {

                }
            }
        });
    }

    class MyAdapter extends BaseAdapter {
        Context context;
        LayoutInflater inflater;
        int taskCount;

        public MyAdapter(Context context, int taskCount) {
            this.context = context;
            this.taskCount = taskCount;
            inflater = LayoutInflater.from(context);
            list = new ArrayList<PoolTestActivity.AsyTaskItem>(taskCount);
        }

        @Override
        public int getCount() {
            return taskCount;
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_view_item, null);
                MyItem item = new MyItem(context, convertView, 1);

                AsyTaskItem asyitem = new AsyTaskItem(item);
                /**
                 * 下面两种任务执行效果都一样,形变质不变
                 * */
                // asyitem.execute();
                // asyitem.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
                /**
                 * 一个一个执行我们的任务,效果与按顺序执行是一样的(AsyncTask.SERIAL_EXECUTOR)
                 * */
                 asyitem.executeOnExecutor(singleTaskService);

                /**
                 * 按我们指定的个数来执行任务的线程池
                 * */
//                 asyitem.executeOnExecutor(limitedTaskService);
                /**、
                 * 不限定指定个数的线程池，也就是说：你往里面放了几个任务，他全部同一时间开始执行， 不管你手机受得了受不了 // *
                 */
//                asyitem.executeOnExecutor(allTaskService);
                /**
                 * 创建一个可在指定时间里执行任务的线程池，亦可重复执行
                 * */
//                 asyitem.executeOnExecutor(scheduledTaskService);
                list.add(asyitem);
            }

            return convertView;
        }

    }

    class AsyTaskItem extends AsyncTask<Void, Integer, Void> {

        MyItem item;

        String id;

        public AsyTaskItem(MyItem item) {
            this.item = item;
            if (order < count || order == count) {
                id = "执行:" + String.valueOf(++order);
            } else {
                order = 0;
                id = "执行:" + String.valueOf(++order);
            }
        }

        @Override
        protected void onPreExecute() {
            item.setTitle(id);
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (!isCancelled() && isCancle == false) {
                int pro = 0;
                while (pro < 101) {
                    if (pro < 70 && (pro > 0 || pro == 0)) {
                        SystemClock.sleep(100);
                    } else {
                        SystemClock.sleep(800);
                    }
                    publishProgress(pro);
                    pro++;
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            item.setProgress(values[0]);
        }

    }

    /**
     * 自定义一个显示的item
     *
     * @author Administrator
     */
    class MyItem extends LinearLayout {
        public MyItem(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        public MyItem(Context context, AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public MyItem(Context context) {
            this(context, null);
        }

        public MyItem(Context context, View def, int s) {
            this(context);
            this.rootView = def;

        }

        private View rootView = null;
        private TextView mTitle;

        private ProgressBar mProgress;

        public void setTitle(String title) {

            if (mTitle == null) {
                mTitle = (TextView) rootView.findViewById(R.id.task_name);
            }
            mTitle.setText(title);
        }

        public void setProgress(int pro) {
            if (mProgress == null) {
                mProgress = (ProgressBar) rootView.findViewById(R.id.task_progress);
            }
            mProgress.setProgress(pro);
        }
    }
}
