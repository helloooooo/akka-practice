package practice
import akka.actor.{Actor, ActorLogging, ActorRef,Props, Terminated}
import DeviceManager.RequestTrackDevice

object DeviceManager {
  def props():Props = Props(new DeviceManager)

  final case class RequestTrackDevice(groupId:String,deviceId:String)
  case object DeviceRegistered
}
class DeviceManager extends Actor with ActorLogging{
  var groupIdToActor = Map.empty[String, ActorRef]
  var actorToGroupId = Map.empty[ActorRef, String]

  override def preStart():Unit = log.info("DeviceManager start")

  override def postStop():Unit = log.info("DeviceManage stop")

  override def receive = {
    case trackMsg @ RequestTrackDevice(groupId,_) =>
      groupIdToActor.get(groupId) match {
        case Some(ref) =>
          ref forward trackMsg
        case None =>
          log.info("Creating device group actor for {}",groupId)
          val groupActor = context.actorOf(DeviceGroup.props(groupId),"group-"+groupId)
          context.watch(groupActor)
          groupActor forward trackMsg
          groupIdToActor += groupId -> groupActor
          actorToGroupId += groupActor -> groupId
      }
    case Terminated(groupActor) =>
      val groupId = actorToGroupId(groupActor)
      log.info("Device group actor for {} has been teeminated",groupId)
      actorToGroupId -= groupActor
      groupIdToActor -= groupId
  }
}
