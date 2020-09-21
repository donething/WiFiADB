package net.donething.android.wifiadb

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences

    companion object {
        private val TAG = MainActivity::javaClass.name

        // sharedPreference 的文件名
        const val PREFS_NAME = "settings"

        // 记录在 sharedPreference 中的是否开启 WiFi adb 的键
        const val SW_ADB = "sw_adb"

        // WiFi ADB的端口
        private const val ADB_PORT = 5555

        /**
         * 开启或关闭 WiFi adb
         */
        fun switchWiFiAdb(switch: Boolean, context: Context) {
            val cmds = if (switch) {
                arrayOf("setprop service.adb.tcp.port $ADB_PORT", "stop adbd", "start adbd")
            } else {
                arrayOf("setprop service.adb.tcp.port -1", "stop adbd", "start adbd")
            }

            val result = ShellUtils.execCommand(cmds, true, true)
            if (result.result == 0) {
                Toast.makeText(context, "执行成功", Toast.LENGTH_LONG).show()
                Log.d(TAG, "执行成功：${result.successMsg}")
            } else {
                Toast.makeText(context, "执行失败", Toast.LENGTH_LONG).show()
                Log.d(TAG, "执行失败：${result.errorMsg}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = this.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        swEnable.setOnClickListener {
            prefs.edit().putBoolean(SW_ADB, swEnable.isChecked).apply()
            // 开关 WiFi adb
            switchWiFiAdb(swEnable.isChecked, this)
        }
    }

    override fun onStart() {
        super.onStart()
        initView()
    }

    /**
     * 初始化视图
     */
    private fun initView() {
        // WiFi adb开启状态
        var cmds = arrayOf("netstat -ntulp |grep $ADB_PORT")
        var result = ShellUtils.execCommand(cmds, true, true)
        Log.d(
            TAG,
            """WiFi ADB状态：result: ${result.result}, success: "${result.successMsg}", err: "${result.errorMsg}""""
        )
        swEnable.isChecked = result.result == 0 && result.successMsg.contains(ADB_PORT.toString())


        // 展示远程连接地址
        // 获取IP地址参考：https://blog.csdn.net/Tim_phper/article/details/53334029
        var ips = ""    // 获取eth0和wlan0的两种IP
        // eth0
        cmds = arrayOf("ifconfig eth0 |grep 'inet '| awk '{print \$2}'")
        result = ShellUtils.execCommand(cmds, true, true)
        // Log.d(TAG, "地址：${result.result}, ${result.successMsg}, ${result.errorMsg}")
        if (result.result == 0 && result.successMsg.isNotBlank()) {
            ips += "eth0 ${result.successMsg}:$ADB_PORT\n"
        }

        // wlan0
        cmds = arrayOf("ifconfig wlan0 |grep 'inet '| awk '{print \$2}'")
        result = ShellUtils.execCommand(cmds, true, true)
        if (result.result == 0 && result.successMsg.isNotBlank()) {
            ips += "wlan0 ${result.successMsg}:$ADB_PORT"
        }

        // 显示
        tvIP.text = ips
    }
}
