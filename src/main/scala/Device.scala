package practice
import akka.actor.{Actor, ActorLogging,Props}


object Device {
  def props(groupId:String, deviceId:String):Props = Props(new Device(groupId,deviceId))
  final case class ReadTemperture(requestedId: Long)
  final case class RespondTemperture(requestedId:Long,value:Option[Double])
  final case class TempertureRecorded(requestId:Long)
  final case class RecordTemperture(requestId:Long,value:Double)
}



class Device(groupId:String,deviceId:String) extends Actor with ActorLogging {
  import Device._
  var lastTempertureReading:Option[Double] = None
  override def preStart():Unit = log.info("Device actor {} - {} started ",groupId,deviceId)
  override def postStop():Unit = log.info("Device actor {} - {} stopped ",groupId,deviceId)
  override def receive:Receive= {
    case DeviceManager.RequestTrackDevice(`groupId`, `deviceId`) =>
      sender() ! DeviceManager.DeviceRegistered
    case DeviceManager.RequestTrackDevice(groupId , deviceId) =>
      log.warning(
        "ignoring TrackDevice request for {}--{}. This actor is responsible for {}-{}",
        groupId, deviceId, this.groupId, this.deviceId
      )

    case RecordTemperture(id,value) =>
      log.info("Recorded temperture reading {} with {} ",value,id)
      lastTempertureReading =Some(value)
      sender() ! TempertureRecorded(id)
    case ReadTemperture(id) =>
      sender() ! RespondTemperture(id,lastTempertureReading)
  }
}
