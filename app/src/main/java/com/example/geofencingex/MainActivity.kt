package com.example.geofencingex

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    private val FINE_LOCATION_PERMISSION = 100
    private val BACKGROUND_LOCATION_PERMISSION = 200

    // 테스트용 Geofence 데이터 생성
    val geofenceList : MutableList<Geofence> by lazy {
        mutableListOf(
            getGeofence("현대백화점",Pair(37.5085864,127.0601149)),
            getGeofence("삼성역역",Pair(7.5085864,127.0601149))
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 권한 체크
        checkPermission()

    }

    // Geofence Client 생성 - Location API 사용을 위한 지오펜싱 클라이언트 인스턴스 생성성
    private val geofencingClient:GeofencingClient by lazy {
        LocationServices.getGeofencingClient(this)
    }

    private val geofencingPendingIntent:PendingIntent by lazy {
        val intent = Intent(this,GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun addGeofences() {
        checkPermission()
        geofencingClient.addGeofences(getGeofencingRequest(geofenceList),geofencingPendingIntent).run {
            addOnSuccessListener {
                Toast.makeText(this@MainActivity,"add Success",Toast.LENGTH_LONG).show()
            }
            addOnFailureListener {
                Toast.makeText(this@MainActivity, "add Fail",Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getGeofence(reqId:String, geo:Pair<Double,Double>,radius:Float = 100f):Geofence {
        return Geofence.Builder()
            .setRequestId(reqId)    // 이벤트 발생시 BroadcastReceiver에서 구분할 id
            .setCircularRegion(geo.first , geo.second , radius) // 위치 및 반경
            .setExpirationDuration(Geofence.NEVER_EXPIRE)       // Geofence 만료 시간
            .setLoiteringDelay(10000)       // 머물기 체크 시간
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER  // 진입 감지시
                        or Geofence.GEOFENCE_TRANSITION_EXIT    // 이탈 감지시
                        or Geofence.GEOFENCE_TRANSITION_DWELL   // 머물기 감지시
            )
            .build()
    }

    // Geofencing Request 빌드
    // Geofence 지정 및 관련 이벤트 트리거 방식을 설정하기 위해 빌드
   private fun getGeofencingRequest(list: List<Geofence>): GeofencingRequest {
        return GeofencingRequest.Builder().apply {
            // Geofence 이벤트는 진입시부터 처리할 때
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            addGeofences(list)
        }.build()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode) {
            FINE_LOCATION_PERMISSION,BACKGROUND_LOCATION_PERMISSION -> {
                grantResults.apply {
                    if (this.isNotEmpty()) {
                        this.forEach {
                            if (it != PackageManager.PERMISSION_GRANTED) {
                                checkPermission()
                                return
                            }
                        }
                    } else {
                        checkPermission()
                    }
                }
            }
        }
    }

    private fun checkPermission() {
        val permissionAccessFineLocationApproved =
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (permissionAccessFineLocationApproved) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val backgroundLocationPermissionApproved =
                    ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED

                if (!backgroundLocationPermissionApproved) {
                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION),BACKGROUND_LOCATION_PERMISSION)
                }
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),FINE_LOCATION_PERMISSION)
            }
        }


    }



}