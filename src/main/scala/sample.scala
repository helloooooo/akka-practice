import akka.actor.{Actor , Props, ActorSystem}
import scala.io.StdIn

class PrintMyActorRefActor extends Actor{
  override def receive: Receive = {
    case "printit" =>
      val secondRef = context.actorOf(Props.empty,"second-actor") // actorの生成
      print(s"Second: $secondRef")
  }
}

object Akkatuto extends App{
  val system = ActorSystem("testSystem")
  val firstRef = system.actorOf(Props[PrintMyActorRefActor],"first-actor") // PrinntMyActorのAcotrを作成
  println(s"first : $firstRef")
  firstRef ! "printit"

  println(">>> Press ENTER to eixt <<<")
  try StdIn.readLine()
  finally system.terminate() //actorの停止
}
