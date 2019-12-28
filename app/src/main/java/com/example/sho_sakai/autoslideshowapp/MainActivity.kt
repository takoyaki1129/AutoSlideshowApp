package com.example.sho_sakai.autoslideshowapp

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.provider.MediaStore
import android.content.ContentUris
import android.database.Cursor
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import android.os.Handler
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val PERMISSIONS_REQUEST_CODE = 10

    //タイマー
    private var mTimer: Timer? = null

    // タイマー用の時間のための変数
    private var mTimerSec = 0.0

    private var mHandler = Handler()

    var cursor: Cursor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prev.setOnClickListener(this)
        play.setOnClickListener(this)
        forward.setOnClickListener(this)


        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                    Log.d("ANDROID", "許可された")
                } else {
                    Log.d("ANDROID", "許可されなかった")
                    return
                }
        }

    }

    private fun getContentsInfo() {
        // 画像の情報を取得する
        val resolver = contentResolver
        cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        )
        if (cursor!!.moveToFirst()) {
            // indexからIDを取得し、そのIDから画像のURIを取得する
            val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
            val id = cursor!!.getLong(fieldIndex)
            val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

            imageView.setImageURI(imageUri)
        }

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.prev -> {
                if (cursor!!.isFirst()) {
                    cursor!!.moveToLast()
                } else {
                    cursor!!.moveToPrevious()
                }
                val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor!!.getLong(fieldIndex)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                imageView.setImageURI(imageUri)
            }

            R.id.play -> {
                if (mTimer == null) {
                    mTimer = Timer()
                    mTimer!!.schedule(object : TimerTask() {
                        override fun run() {
                            mHandler.post {
                                if (cursor!!.isLast()) {
                                    cursor!!.moveToFirst()
                                } else {
                                    cursor!!.moveToNext()
                                }
                                val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
                                val id = cursor!!.getLong(fieldIndex)
                                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                                imageView.setImageURI(imageUri)

                            }
                        }
                    }, 2000, 2000) // 最初に始動させるまで 2000ミリ秒、ループの間隔を 2000ミリ秒 に設定

                    prev.isEnabled = false
                    forward.isEnabled = false
                    play.text = "停止"


                } else {
                    if (mTimer != null) {
                        mTimer!!.cancel()
                        mTimer = null
                    }

                    prev.isEnabled = true
                    forward.isEnabled = true
                    play.text = "再生"
                }
            }


            R.id.forward -> {
                if (cursor!!.isLast()) {
                    cursor!!.moveToFirst()
                } else {
                    cursor!!.moveToNext()
                }
                val fieldIndex = cursor!!.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor!!.getLong(fieldIndex)
                val imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                imageView.setImageURI(imageUri)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (cursor!! != null)
            cursor!!.close()
    }
}


