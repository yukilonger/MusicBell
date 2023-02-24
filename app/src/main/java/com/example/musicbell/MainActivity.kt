package com.example.musicbell

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.*
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.*
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.core.view.isVisible
import java.util.*


class MainActivity : ComponentActivity() {
    @SuppressLint("ClickableViewAccessibility", "ResourceAsColor", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        // 程序关闭后无法被广播唤醒，需要加入AutoStart？？？
        openAutoStart()

        // 时间选择
        val timePicker = findViewById<TimePicker>(R.id.time_picker)
        timePicker.setIs24HourView(true)
        hideKeyboardInputInTimePicker(this.resources.configuration.orientation, timePicker)
        timePicker.setOnTimeChangedListener { view, hourOfDay, minute ->
            val checkPower = findViewById<CheckBox>(R.id.checkBox_power)
            setAlarm(checkPower.isChecked)
        }

        // 开关
        val checkPower = findViewById<CheckBox>(R.id.checkBox_power)
        checkPower.setOnCheckedChangeListener { buttonView, isChecked ->
            setAlarm(isChecked)
        }

        // 音乐
        val buttonMusicControl = findViewById<ImageView>(R.id.button_music_control)
        buttonMusicControl.setOnClickListener{
            startActivity(Intent(applicationContext, MusicActivity::class.java))
        }
    }

    fun hideKeyboardInputInTimePicker(orientation: Int, timePicker: TimePicker)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            try
            {
                if (orientation == Configuration.ORIENTATION_PORTRAIT)
                {
                    ((timePicker.getChildAt(0) as LinearLayout).getChildAt(4) as LinearLayout).getChildAt(0).isVisible = false
                }
                else
                {
                    (((timePicker.getChildAt(0) as LinearLayout).getChildAt(2) as LinearLayout).getChildAt(2) as LinearLayout).getChildAt(0).isVisible = false
                }
            }
            catch (ex: Exception)
            {
            }
        }
    }

    fun setAlarm(isOn : Boolean) {
        val manager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if(isOn){
            val timePicker = findViewById<TimePicker>(R.id.time_picker)
            val checkEveryday = findViewById<CheckBox>(R.id.checkBox_everyday)

            val calendar = Calendar.getInstance();
            calendar.timeInMillis = System.currentTimeMillis();
            calendar.timeZone = TimeZone.getTimeZone("GMT+8");
            calendar.set(Calendar.MINUTE, timePicker.minute);
            calendar.set(Calendar.HOUR_OF_DAY, timePicker.hour);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            val currentTime = System.currentTimeMillis();
            var triggerTime = calendar.timeInMillis;

            if(checkEveryday.isChecked){
                // 每天响
                if (currentTime > triggerTime) {
                    // 当前时间大于触发时间，触发时间更改为次日
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    triggerTime = calendar.timeInMillis;
                }
            }
            else{
                // 工作日响
                if (currentTime > triggerTime) {
                    val date = Utility.GetNextWorkday(this)
                    if (date == null) {
                        // 工作日需要更新
                        checkEveryday.isChecked = true
                        TODO() // 提示更新软件
                    }
                    else {
                        triggerTime = Utility.GetTimestamp(date)
//                        val diffCount = Utility.DiffDay(date!!)
//                        // 节假日或当前时间大于触发时间，触发时间更改为次工作日
//                        calendar.add(Calendar.DAY_OF_MONTH, diffCount)
//                        triggerTime = calendar.timeInMillis;
                    }
                }
            }

            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
        else{
            manager.cancel(pendingIntent)
        }
    }

    companion object {
        private var mPendingIntent: PendingIntent? = null
    }
    @get:SuppressLint("UnspecifiedImmutableFlag")
    private val pendingIntent: PendingIntent?
        get() {
            if (mPendingIntent == null) {
                val requestCode = 0
                val intent = Intent(this.applicationContext, MusicReciever::class.java)
                intent.action = "com.example.musicbell"
                intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                mPendingIntent = PendingIntent.getBroadcast(
                    this.applicationContext,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
            return mPendingIntent
        }

    fun openAutoStart() {
        val POWERMANAGER_INTENTS = arrayOf(
            Intent().setComponent(
                ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.letv.android.letvsafe",
                    "com.letv.android.letvsafe.AutobootManageActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.optimize.process.ProtectActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.startupapp.StartupAppListActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.oppo.safe",
                    "com.oppo.safe.permission.startup.StartupAppListActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.iqoo.secure",
                    "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.iqoo.secure",
                    "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.vivo.permissionmanager",
                    "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.samsung.android.lool",
                    "com.samsung.android.sm.ui.battery.BatteryActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.htc.pitroad",
                    "com.htc.pitroad.landingpage.activity.LandingPageActivity"
                )
            ),
            Intent().setComponent(
                ComponentName(
                    "com.asus.mobilemanager",
                    "com.asus.mobilemanager.MainActivity"
                )
            )
        )

        val pref = getSharedPreferences("allow_notify", MODE_PRIVATE).edit();
        pref.apply();
        val sp = getSharedPreferences("allow_notify", MODE_PRIVATE);

        if (!sp.getBoolean("protected", false)) {
            for (intent in POWERMANAGER_INTENTS) {
                if (packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                    startActivity(intent)
                    sp.edit().putBoolean("protected", true).apply()
                    break
                }
            }
        }
    }
}