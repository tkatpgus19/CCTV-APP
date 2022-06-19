package com.example.cctv_app

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.core.view.updatePadding

class CctvLayout(var activity: Activity) : FrameLayout(activity) {

    // 카메라가 활성화 되었는지 체크하는 변수
    var isSetup = false
    var touchCnt = 0

    // DrawTablet(4x4) 에서는 해당 frame 이 확대될 위치를 저장,
    // DrawMobile(2x2) 에서는 해당 frame 위치를 저장하는 변수
    var startX = 0
    var startY = 0

    /* DrawTablet(4x4) 에서만 사용 */
    /* frame 그룹에서 확대되는 frame 제외한,
       3개의 frame 이 이동해야 할 목적지 frame 정보 저장 (4분할 되어야하는 frame 정보) */
    // frame 번호
    var goto = 0

    // frame 시작 위치
    var movedX = 0
    var movedY = 0

    private val labelView: TextView = TextView(activity)
    private val statusView: TextView = TextView(activity)

    // 음성 전송을 활성화시키는데 사용되는 마이크 모양 아이콘
    private var sendVoiceBtn: ImageView = ImageView(activity)

    init {
        labelView.setTextColor(Color.WHITE)
        labelView.updatePadding(
            left = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                5.0f,
                resources.displayMetrics
            ).toInt()
        )

        labelView.text = ""

        statusView.setBackgroundColor(Color.TRANSPARENT)
        statusView.setTextColor(Color.WHITE)
        statusView.textSize = 30f
        statusView.textAlignment = View.TEXT_ALIGNMENT_CENTER
        statusView.gravity = Gravity.CENTER
        statusView.text = "IDLE"

        sendVoiceBtn.setImageResource(R.drawable.ic_baseline_mic_off_24)
        sendVoiceBtn.visibility = View.GONE

        setPadding(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                2.0f,
                resources.displayMetrics
            ).toInt()
        )
        background = ContextCompat.getDrawable(activity, R.drawable.layout_border_normal)

        z = 5.0f

        addView(
            statusView, LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
        )
        addView(
            labelView, LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
            )
        )
        addView(sendVoiceBtn, LayoutParams(150, 150).apply { gravity = Gravity.BOTTOM or Gravity.CENTER })
    }

    fun setLabel(label: String) {
        val text = SpannableString("$label ")
        text.setSpan(ForegroundColorSpan(0xffffffff.toInt()), 0, text.length, 0)
        labelView.setText(text, TextView.BufferType.SPANNABLE)
        labelView.setShadowLayer(2.0f, 1.4f, 1.3f, 0xff000000.toInt())
    }

    fun setup(port: Int) {
        val status = SpannableString("Standby")
        status.setSpan(ForegroundColorSpan(0xccaaffaa.toInt()), 0, status.length, 0)
        status.setSpan(BackgroundColorSpan(0x66000000.toInt()), 0, status.length, 0)
        statusView.setText(status, TextView.BufferType.SPANNABLE)
        statusView.visibility = View.VISIBLE
        isSetup = true
    }

    /* 해당 카메라 frame 의 위치 저장 */
    fun setPos(startX: Int, startY: Int) {
        this.startX = startX
        this.startY = startY
    }

    /* 해당 카메라 frame 이 이동해야 할 frame 의 위치 저장 */
    fun setMovedPos(movedX: Int, movedY: Int) {
        this.movedX = movedX
        this.movedY = movedY
    }

    /* 해당 카메라 frame 비활성화 */
    fun unset() {
        statusView.text = "IDLE"
        labelView.text = ""
    }

    /* 총기감지 알림 수신 시 warning 테두리로 변경 */
    fun setWarningBorder(flag: Boolean){
        if(flag)
            background = ContextCompat.getDrawable(activity, R.drawable.layout_border_warning)
        else
            background = ContextCompat.getDrawable(activity, R.drawable.layout_border_normal)
    }

    /* 카메라 전체화면 시 음성 전송 마이크 모양 아이콘 활성화 */
    @SuppressLint("ClickableViewAccessibility")
    fun showVoiceBtn(flag: Boolean){
        /* 아이콘 활성화 */
        if(flag) {
            sendVoiceBtn.visibility = View.VISIBLE

            sendVoiceBtn.setOnTouchListener { view, event ->
                when(event.action){
                    /* 아이콘을 누르고 있을 때 아이콘 변경 */
                    MotionEvent.ACTION_DOWN ->{
                        sendVoiceBtn.setImageResource(R.drawable.ic_baseline_mic_24)
                        Toast.makeText(context,"말씀하세요", Toast.LENGTH_SHORT).show()
                    }
                    /* 손을 뗐을 때 기존 아이콘으로 변경 */
                    MotionEvent.ACTION_UP-> {
                        sendVoiceBtn.setImageResource(R.drawable.ic_baseline_mic_off_24)
                    }
                }
                true
            }
        }
        /* 아이콘 비활성화 */
        else {
            sendVoiceBtn.visibility = View.GONE
        }
    }

    /* 사용자에게 알림 전송 */
    fun pushAlarm(camNum: Int){
        val builder = createNotificationChannel("id", "name")
            .setSmallIcon(R.drawable.ic_baseline_warning_24)
            .setContentTitle("Weapon Detected!")
            .setContentText("${camNum}번 카메라에서 총기가 감지되었습니다! 앱에 와서 확인하세요.")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)){
            notify(camNum, builder.build())
        }
    }
    private fun createNotificationChannel(id :String, name :String) : NotificationCompat.Builder{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val manager = activity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH)

            manager.createNotificationChannel(channel)
            NotificationCompat.Builder(context, id)
        } else {
            NotificationCompat.Builder(context)
        }
    }
}