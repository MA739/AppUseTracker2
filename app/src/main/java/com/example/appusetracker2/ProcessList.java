package com.example.appusetracker2;

import android.accessibilityservice.AccessibilityService;
import android.app.ActivityManager;

import java.sql.Time;
import java.time.LocalTime;
import java.util.Calendar;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.app.usage.UsageStats;
import android.app.usage.EventStats;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.os.SystemClock;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.example.appusetracker2.databinding.ActivityProcessListBinding;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.os.Process.getElapsedCpuTime;
import static android.os.Process.getStartElapsedRealtime;
import static android.os.Process.getStartUptimeMillis;
import static android.os.Process.myPid;

public class ProcessList extends AppCompatActivity {

    private ActivityProcessListBinding binding;
    TextView textView;
    Button button;
    private static final String CHANNEL_ID = "15";

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityProcessListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = getIntent();

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = binding.toolbarLayout;
        toolBarLayout.setTitle("App Use");

        //acquire list of currently running processes
        getCurrentProcesses();

        button = findViewById(R.id.goBack);
        button.setOnClickListener(v -> {
            //Should close the current activity and de-load it
            finishAndRemoveTask();
        });

        //also update Notification with last viewed use value
        //reference code from: https://stuff.mit.edu/afs/sipb/project/android/docs/training/notify-user/managing.html
        CharSequence textTitle = "Usage Stats Quick Access";
        CharSequence textContent = calculateUsage();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.notif_icon)
                .setContentTitle(textTitle)
                .setContentText(textContent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        Intent intent2 = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Intent resultIntent = new Intent(this, ProcessList.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(ProcessList.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent2, 0);
        builder.setContentIntent(pendingIntent);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        int notificationId = 7;
        notificationManager.notify(notificationId, builder.build());


    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    //used to calculate and display usage for the in-app display
    public void getCurrentProcesses() {
        //code snippet used from https://www.tutorialspoint.com/how-to-find-the-currently-running-applications-programmatically-in-android
        textView = findViewById(R.id.ScrollText);
        //get rid of the filler text
        textView.setText("");

        //this loop uses the usagestat pack to do the same as above
        UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        //converts a date to a millisecond representation
        //Reference code from: https://beginnersbook.com/2014/01/how-to-get-time-in-milliseconds-in-java/
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.set(Calendar.MILLISECOND, 59);
        calendar2.set(Calendar.SECOND, 59);
        calendar2.set(Calendar.MINUTE, 59);
        calendar2.set(Calendar.HOUR_OF_DAY, 11);
        //Setting the Calendar date and time to the given date and time
        //System.out.println("Given Time in milliseconds : "+calendar.getTimeInMillis());
        //beginning of the day, in milliseconds
        long startMillis = calendar.getTimeInMillis();
        long endMillis = calendar2.getInstance().getTimeInMillis();
        long totalUseTime = 0;
        Map<String, UsageStats> lUsageStatsMap = mUsageStatsManager.queryAndAggregateUsageStats(startMillis, endMillis);
        //UsageEvents useEvents = mUsageStatsManager.queryEvents(startMillis,endMillis);
        //System.out.println("OpenTracker " + AppOpenTrackMap.Event.ACTIVITY_RESUMED);
        //holds the values for the processes
        if (lUsageStatsMap.isEmpty()) {
            textView.setText("Unable to access Usage Stats. Need to enable this app in your settings under Usage Access");
            //testing calendar time format
            //textView.append("calendar1 " + calendar.getTime());
            //textView.append("calendar2 " + calendar2.getTime());
        } else {
            for (String key : lUsageStatsMap.keySet()) {
                UsageStats packageName = lUsageStatsMap.get(key);
                assert packageName != null;
                long totalTimeUsageInMillis = packageName.getTotalTimeInForeground();
                //UsageEvents AppOpenTrackMap = mUsageStatsManager.queryEvents(startMillis, endMillis );
                //figure out how to access the event of a specific app...
                //List<EventStats> useEvents;
                //System.out.println("OpenTracker " + packageName.Event.ACTIVITY_RESUMED);
                //int TimesOpened = packageName.
                //System.out.println(packageName + " and time " + totalTimeUsageInMillis);
                //only display the process if the time used is greater than 0
                if (totalTimeUsageInMillis > 0) {
                    //textView.append(new StringBuilder().append("Application running: ").append(packageName.getPackageName()).append("\n").toString());
                    //The app does not like using the string.split() method to get the names of the apps from the packageName
                    String appName = packageName.getPackageName();
                    String appNameArr[] = appName.split("\\.");
                    //System.out.println("Name array: " + appName);
                    //int nameIndex = appNameArr.length-1;
                    String appNameC = appNameArr[appNameArr.length - 1];
                    //System.out.println("Name: " + appNameC);
                    textView.append(new StringBuilder().append("App Name: ").append(appNameC).append("\n").toString());
                    textView.append("Elapsed Use Time: " + ((int) totalTimeUsageInMillis / 60000 + " minutes ") + ((totalTimeUsageInMillis % 60000) / 1000) + " seconds\n\n");
                    totalUseTime += totalTimeUsageInMillis;
                }
                //looks like the string contents worked :)
                //stringContents += "Application running: " + packageName.getPackageName() + "\n";
                //stringContents += "Elapsed Real Time: " + totalTimeUsageInMillis + " seconds\n\n";

                //textView.append("Elapsed Real Time: " + (totalTimeUsageInMillis + " seconds\n\n"));
            }
        }
        textView.append("Total: " + ((int) totalUseTime/3600000) + " hours " + (int) (totalUseTime % 3600000) / 60000 + " minutes " + ((totalUseTime % 60000) / 1000) + " seconds");
    }

    //used to calculate usage for the notification
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public String calculateUsage()
    {
        UsageStatsManager mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        //converts a date to a millisecond representation
        //Reference code from: https://beginnersbook.com/2014/01/how-to-get-time-in-milliseconds-in-java/
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.set(Calendar.MILLISECOND, 59);
        calendar2.set(Calendar.SECOND, 59);
        calendar2.set(Calendar.MINUTE, 59);
        calendar2.set(Calendar.HOUR_OF_DAY, 11);
        //Setting the Calendar date and time to the given date and time
        //System.out.println("Given Time in milliseconds : "+calendar.getTimeInMillis());
        //beginning of the day, in milliseconds
        long startMillis = calendar.getTimeInMillis();
        long endMillis = calendar2.getInstance().getTimeInMillis();
        long totalUseTime = 0;
        //long endMillis = Calendar.getInstance().getTimeInMillis();
        //List<UsageStats> lUsageStats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startMillis, endMillis);
        Map<String, UsageStats> lUsageStatsMap = mUsageStatsManager.queryAndAggregateUsageStats(startMillis, endMillis);
            for (String key : lUsageStatsMap.keySet()) {
                //List<UsageStats> list = Collections.singletonList(entry.getValue());
                //long totalTimeUsageInMillis = key.getTotalTimeInForeground()
                UsageStats packageName = lUsageStatsMap.get(key);
                assert packageName != null;
                long totalTimeUsageInMillis = packageName.getTotalTimeInForeground();
                //System.out.println(packageName + " and time " + totalTimeUsageInMillis);
                //only display the process if the time used is greater than 0
                if (totalTimeUsageInMillis > 0) {
                    totalUseTime += totalTimeUsageInMillis;
                }
                //looks like the string contents worked :)
                //stringContents += "Application running: " + packageName.getPackageName() + "\n";
                //stringContents += "Elapsed Real Time: " + totalTimeUsageInMillis + " seconds\n\n";

                //textView.append("Elapsed Real Time: " + (totalTimeUsageInMillis + " seconds\n\n"));
            }
        return ("Total: " + ((int) totalUseTime/3600000) + " hours " + (int) (totalUseTime % 3600000) / 60000 + " minutes " + ((totalUseTime % 60000) / 1000) + " seconds");
    }
}