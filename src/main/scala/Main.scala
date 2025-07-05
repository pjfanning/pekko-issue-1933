import org.apache.pekko.actor.typed.ActorSystem

object Main extends App:

  // Create the ActorSystem with the TestActor behavior
  val system: ActorSystem[TestActor.Command] = ActorSystem(TestActor(), "TestActorSystem")

  try {
    system ! TestActor.Start
    // Wait for a while to allow the actor to process messages
    Thread.sleep(10000)
  } finally {
    // Terminate the ActorSystem
    system.terminate()
    println("ActorSystem terminated.")
  }
