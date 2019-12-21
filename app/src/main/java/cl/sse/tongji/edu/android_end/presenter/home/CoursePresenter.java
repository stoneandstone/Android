package cl.sse.tongji.edu.android_end.presenter.home;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.List;

import cl.sse.tongji.edu.android_end.HomeActivity;
import cl.sse.tongji.edu.android_end.model.Course;
import cl.sse.tongji.edu.android_end.model.HttpTrust.TrustAllCerts;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;

public class CoursePresenter {

    HomeActivity activity;

    public CoursePresenter(HomeActivity iactivity) {
        activity = iactivity;
    }

    public void init() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //获得无安全检查的client
                OkHttpClient.Builder builder = new OkHttpClient.Builder();
                builder.sslSocketFactory(TrustAllCerts.createSSLSocketFactory(), new TrustAllCerts());
                builder.hostnameVerifier(new TrustAllCerts.TrustAllHostnameVerifier());
                OkHttpClient client = builder.build();

                //构建请求
                String url = "https://118.25.153.97/course/";
                SharedPreferences pref = activity.getSharedPreferences("token", MODE_PRIVATE);
                String token = pref.getString("token", null);
                Request request = new Request.Builder()
                        .url(url)
                        .addHeader("Authorization", token)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                    String response_string = response.body().string();
                    Gson gson = new Gson();
                    List<Course> courses = gson.fromJson(response_string, new TypeToken<List<Course>>() {
                    }.getType());

                    //更新ui
                    Message message = new Message();
                    message.obj = courses;
                    handler.sendMessage(message);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            List<Course> courses = (List<Course>) msg.obj;
            activity.showAllCourse(courses);
        }
    };
}
