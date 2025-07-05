import org.apache.pekko.actor.typed.scaladsl.Behaviors
import org.apache.pekko.actor.typed.{ActorRef, Behavior}

object TestActor:
  sealed trait Command
  case object Start extends Command
  private final case class ItemStored(url: String, tag: Tag) extends Command

  private enum Tag:
    case Self
    case Peer

  case class ExternalMessage(message: String, replyTo: ActorRef[String])

  def apply(): Behavior[Command] =
    Behaviors.receive: (ctx, msg) =>
      msg match
        case _ =>
          val externalActor = ctx.spawnAnonymous(ExternalActor())

          // BUG: Both of these capture Tag.Peer instead of their respective values
          externalActor ! ExternalMessage(
            "Tag.Self",
            ctx.messageAdapter[String]: response =>
              ItemStored("adapterSelf: " + response, Tag.Self)  // Should capture Tag.Self but captures Tag.Peer
          )

          externalActor ! ExternalMessage(
            "Tag.Peer",
            ctx.messageAdapter[String]: response =>
              ItemStored("adapterPeer: " + response, Tag.Peer)  // This works correctly
          )

          Behaviors.receive: (ctx, msg) =>
            msg match
              case itemStored: ItemStored =>
                println(itemStored)  // Shows both as Peer
                Behaviors.same
              case Start =>
                println("TestActor started")
                Behaviors.same

object ExternalActor:
  def apply(): Behavior[TestActor.ExternalMessage] =
    Behaviors.receive: (ctx, msg) =>
      msg match
        case TestActor.ExternalMessage(message, replyTo) =>
          println("Received message in ExternalActor: " + message)
          replyTo ! message
          Behaviors.same
