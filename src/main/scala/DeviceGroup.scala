package practice
import akka.actor.{Actor, ActorLogging, ActorRef,Props,Terminated}
import DeviceGroup._
import DeviceManager.RequestTrackDevice

import scala.concurrent.duration._



object DeviceGroup {
  def props(groupId:String):Props = Props(new DeviceGroup(groupId))

  final case class RequestDeviceList(requestId:Long)
  final case class ReplyDeviceList(requestId:Long, ids:Set[String])
}
class DeviceGroup(groupId:String) extends Actor with ActorLogging{
  var deviceIdToActor = Map.empty[String,ActorRef]
  var actorToDeviceId = Map.empty[ActorRef,String]

  override def preStart():Unit = log.info("DeviceGroup {} Start",groupId)

  override def postStop():Unit = log.info("DeviceGroup {} stopped",groupId)

  override def receive: Receive = {
    case trackMsg@RequestTrackDevice(`groupId`, _) =>
      deviceIdToActor.get(trackMsg.deviceId) match {
        case Some(deviceActor) =>
          deviceActor forward trackMsg
        case None =>
          log.info("creating device actor for {} ", trackMsg.deviceId)
          val deviceActor = context.actorOf(Device.props(groupId, trackMsg.deviceId))
          context.watch(deviceActor)
          actorToDeviceId += deviceActor -> trackMsg.deviceId
          deviceIdToActor += trackMsg.deviceId -> deviceActor
          deviceActor forward trackMsg
      }
    case RequestTrackDevice(groupId, deviceId) =>
      log.warning(
        "Ignoring TrackDevice request for {} . This actor is resposible for {}",
        groupId, this.groupId
      )
    case RequestDeviceList(requestId) =>
      sender() ! ReplyDeviceList(requestId, deviceIdToActor.keySet)
    case Terminated(deviceActor) =>
      val deviceid = actorToDeviceId(deviceActor)
      log.info("Device actor for {} has been terminated ", deviceid)
      actorToDeviceId -= deviceActor
      deviceIdToActor -= deviceid
  }
}
