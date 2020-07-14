package shdv.iotdev.vylometer.fragments

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_monitor.*
import kotlinx.coroutines.*
import shdv.iotdev.vylometer.R
import shdv.iotdev.vylometer.utils.BtUtils

/**
 * A simple [Fragment] subclass.
 */
class monitorFragment : Fragment() {

    private val VYD_NAME = "vyd2020"

    private var busy = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_monitor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        start_button.setOnClickListener { startTest() }
        stop_button.setOnClickListener { stopTest() }
    }

    private  fun updateViewStates(){
        start_button.isEnabled = !busy
        stop_button.isEnabled = busy
    }

    private  fun onConnect(){
        busy = true
        updateViewStates()
    }

    private fun onError(code:Int){
        Toast.makeText(activity as Activity, "Error connection. Code = $code", Toast.LENGTH_LONG).show()
    }

    private suspend  fun testLoop(){
        while (BtUtils.connected()){
            while (BtUtils.readByteAsync().await() != 20){
                if(!BtUtils.connected()) break
            }
            if(!BtUtils.connected()) break
            BtUtils.readByteAsync()
            BtUtils.readByteAsync()
            val data = BtUtils.readByteArrayAsync(6, 500).await() as ByteArray
            if (data.size == 6){
                speed_monitor.text = ((((data[0].toInt() and 0xFF) shl 8) + (data[1].toInt() and 0xFF))/10.0).toString()
                distance_monitor.text = ((((data[2].toInt() and 0xFF) shl 8) + (data[3].toInt() and 0xFF))/10.0).toString()
                sensor_monitor.text = ((((data[4].toInt() and 0xFF) shl 8) + (data[5].toInt() and 0xFF))/1.0).toString()
            }
        }

        while (!BtUtils.connected()){
            BtUtils.connect(this@monitorFragment::onConnect,
                this@monitorFragment::onError,
                VYD_NAME)
            delay(5000)
        }

    }

    // Test func to start data receiving
    private fun startTest(){
       // status_view.text = "Connected to $VYD_NAME"
        if (!BtUtils.enabled()){
            GlobalScope.launch(Dispatchers.Main) {
                withContext(Dispatchers.Default) {
                    BtUtils.enable(
                        activity as Activity
                    )
                }
            }
        }
        else{
            GlobalScope.launch(Dispatchers.Main){
                BtUtils.connect(this@monitorFragment::onConnect,
                                this@monitorFragment::onError,
                                VYD_NAME)

            }
        }
    }

    // Test func to stop data receiving
    private fun stopTest(){
       BtUtils.disconnect()
        busy = false
        updateViewStates()
    }

}
