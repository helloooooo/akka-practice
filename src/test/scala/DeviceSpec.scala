package practice
import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestActors, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}
import scala.concurrent.duration._

class DeviceSpec extends TestKit((ActorSystem("MySpec"))) with ImplicitSender with WordSpecLike {
  "Device actor" must {
    "reply to registration requests" in {
      val probe = TestProbe()
      val deviceActor = system.actorOf(Device.props("group","device"))

      deviceActor.tell(DeviceManager.RequestTrackDevice("group","device"),probe.ref)
      probe.expectMsg(DeviceManager.DeviceRegistered)
      probe.lastSender === (deviceActor)
    }
  }

}
