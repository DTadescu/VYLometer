package shdv.iotdev.vylometer.data


abstract class VydReport(speed:Double, distance:Double){
    val Speed = speed
    val Distance = distance
}

class BtVydReport(speed:Double, distance:Double, sensor:Int):VydReport(speed, distance){
    val Sensor = sensor
}

class GpsVydReport(speed:Double, distance:Double):VydReport(speed, distance){

}