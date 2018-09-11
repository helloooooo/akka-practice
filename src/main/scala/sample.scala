import akka.actor.{Actor , Props, ActorSystem}
import scala.io.StdIn

class PrintMyActorRefActor extends Actor{
  override def receive: Receive = {
    case "printit" =>
      val secondRef = context.actorOf(Props.empty,"second-actor") // actorの生成
      println(s"Second: $secondRef")
  }
}
class StartStopActor1 extends Actor{
  override def preStart(): Unit = {
    println("first started")
    context.actorOf(Props[StartStopActor2])
  }

  override def postStop(): Unit = println("first stopped")

  override def receive: Receive = {
    case "stop" => context.stop(self)
  }
}

class StartStopActor2 extends Actor{
  override def preStart(): Unit = println("second started")

  override def postStop(): Unit = println("second stopprd")

  override def receive: Receive = Actor.emptyBehavior
}

class SupervisingActor extends Actor{
  val child = context.actorOf(Props[SupervisedActor],"supervised-actor")

  override def receive: Receive = {
    case "failChild" => child ! "fail"
  }
}

class SupervisedActor extends Actor{
  override def preStart(): Unit = println("supervised actor started")

  override def postStop(): Unit = println("supervised actor stopped")

  override def receive: Receive = {
    case "fail" =>
      println("supervised actor fails now")
      throw new Exception("I failed")
  }
}

object Akkatuto extends App{
  val system = ActorSystem("testSystem")
  val firstRef = system.actorOf(Props[PrintMyActorRefActor],"first-actor") // PrinntMyActorのAcotrを作成
  println(s"first : $firstRef")
  val first = system.actorOf(Props[StartStopActor1],"first")
  first ! "stop"
  val supervisingActor = system.actorOf(Props[SupervisingActor],"supervising-actor")
  supervisingActor ! "failChild"
  firstRef ! "printit"
  println(">>> Press ENTER to eixt <<<")
  try StdIn.readLine()
  finally system.terminate() //actorの停止
}
