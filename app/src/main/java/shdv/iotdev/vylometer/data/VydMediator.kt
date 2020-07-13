package shdv.iotdev.vylometer.data

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

typealias VydResultFun = (VydMediator) -> Unit

interface IMediator{
    fun start()
    fun pause()
    fun stop()
}

class VydMediator(context: Context):IMediator {

    private var reporter:VydReporter? = null
    private val preffs = PreferenceManager.getDefaultSharedPreferences(context)
    private var resulListeners: ArrayList<VydResultFun> = arrayListOf(fun(_:VydMediator){})
    private var errorListeners: ArrayList<(String)->Unit> = arrayListOf(fun(_:String){})



    fun setResultListener(del:VydResultFun){
        resulListeners.add(del)
    }

    fun removeResultListener(del: VydResultFun){
        try {
            resulListeners.remove(del)
        }
        catch(e: Exception) {e.printStackTrace()}
    }

    fun setErrorListener(del: (String) -> Unit){
        errorListeners.add(del)
    }

    fun removeErrorListener(del: (String) -> Unit){
        try {
            errorListeners.remove(del)
        }
        catch(e: Exception) {e.printStackTrace()}
    }

    private fun resultUpdated() {
        for (del in resulListeners) {
            del(this)
        }
    }

    override fun start() {

    }

    override fun pause() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun stop() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}