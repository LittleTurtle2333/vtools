package com.omarea.vtools.activities

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import androidx.core.content.PermissionChecker
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.View
import android.widget.Switch
import com.omarea.common.shell.KeepShellPublic
import com.omarea.common.ui.DialogHelper
import com.omarea.permissions.Busybox
import com.omarea.shell_utils.AppErrorLogcatUtils
import com.omarea.store.SpfConfig
import com.omarea.utils.CommonCmds
import com.omarea.vtools.R
import kotlinx.android.synthetic.main.activity_other_settings.*

class ActivityOtherSettings : AppCompatActivity() {
    private lateinit var spf: SharedPreferences
    private var myHandler = Handler()

    override fun onPostResume() {
        super.onPostResume()
        delegate.onPostResume()

        home_hide_in_recents.isChecked = spf.getBoolean(SpfConfig.GLOBAL_SPF_AUTO_REMOVE_RECENT, false)

        settings_disable_selinux.isChecked = spf.getBoolean(SpfConfig.GLOBAL_SPF_DISABLE_ENFORCE, false)
        settings_theme_wallpaper.isChecked = spf.getInt(SpfConfig.GLOBAL_SPF_THEME, 1) == 10
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        spf = getSharedPreferences(SpfConfig.GLOBAL_SPF, Context.MODE_PRIVATE)
        ThemeSwitch.switchTheme(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_other_settings)

        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        // setTitle(R.string.app_name)

        // 显示返回按钮
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        home_hide_in_recents.setOnCheckedChangeListener { _, checked ->
            spf.edit().putBoolean(SpfConfig.GLOBAL_SPF_AUTO_REMOVE_RECENT, checked).apply()
        }

        settings_disable_selinux.setOnClickListener {
            if (settings_disable_selinux.isChecked) {
                KeepShellPublic.doCmdSync(CommonCmds.DisableSELinux)
                myHandler.postDelayed({
                    spf.edit().putBoolean(SpfConfig.GLOBAL_SPF_DISABLE_ENFORCE, settings_disable_selinux.isChecked).apply()
                }, 10000)
            } else {
                KeepShellPublic.doCmdSync(CommonCmds.ResumeSELinux)
                spf.edit().putBoolean(SpfConfig.GLOBAL_SPF_DISABLE_ENFORCE, settings_disable_selinux.isChecked).apply()
            }
        }
        settings_logcat.setOnClickListener {
            val log = AppErrorLogcatUtils().catLogInfo()
            settings_log_content.visibility = View.VISIBLE
            settings_log_content.setText(log)
            settings_log_content.setSelection(0, log.length)
        }

        val systemBusyboxInstalled = Busybox.systemBusyboxInstalled()
        settings_private_busybox.isChecked = (!systemBusyboxInstalled) && spf.getBoolean(SpfConfig.GLOBAL_USE_PRIVATE_BUSYBOX, false)
        settings_private_busybox.isEnabled = (!systemBusyboxInstalled)
        settings_private_busybox.setOnClickListener {
            val value = (it as Switch).isChecked
            if (value) {
                spf.edit().putBoolean(SpfConfig.GLOBAL_USE_PRIVATE_BUSYBOX, value).apply()
            } else {
                spf.edit().putBoolean(SpfConfig.GLOBAL_USE_PRIVATE_BUSYBOX, value).apply()
            }
            DialogHelper.animDialog(AlertDialog.Builder(this)
                    .setTitle(R.string.require_restart_app)
                    .setPositiveButton(R.string.btn_confirm) { _, _ ->
                        // KeepShellPublic.doCmdSync("am force-stop ${this.packageName}\nkillall -9 ${this.packageName}")
                    }.setCancelable(false))
        }
    }

    private fun checkPermission(context: Context, permission: String): Boolean = PermissionChecker.checkSelfPermission(context, permission) == PermissionChecker.PERMISSION_GRANTED

    private fun hasRWPermission(): Boolean {
        return checkPermission(this.applicationContext, Manifest.permission.READ_EXTERNAL_STORAGE)
                &&
                checkPermission(this.applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    fun onThemeClick(view: View) {
        val tag = view.tag.toString().toInt()
        if (tag == 10 && spf.getInt(SpfConfig.GLOBAL_SPF_THEME, 1) == 10) {
            spf.edit().putInt(SpfConfig.GLOBAL_SPF_THEME, 1).apply()
            this.recreate()
        } else {
            if (tag == 10 && !hasRWPermission()) {
                DialogHelper.helpInfo(view.context, "", getString(R.string.wallpaper_rw_permission))
                (view as Switch).isChecked = false
            } else {
                spf.edit().putInt(SpfConfig.GLOBAL_SPF_THEME, tag).apply()
                this.recreate()
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()

        spf.edit().putBoolean(SpfConfig.GLOBAL_SPF_DISABLE_ENFORCE, settings_disable_selinux.isChecked).apply()
    }

    public override fun onPause() {
        super.onPause()
    }
}
