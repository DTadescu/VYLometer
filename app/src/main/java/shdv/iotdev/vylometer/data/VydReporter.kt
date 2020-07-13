package shdv.iotdev.vylometer.data

abstract class VydReporter(){
    abstract fun getData():VydReport
}

class BtVydReporter:VydReporter(){

    override fun getData(): VydReport {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

class GpsVydReporter:VydReporter(){
    override fun getData(): VydReport {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}