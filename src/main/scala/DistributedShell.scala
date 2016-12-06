import org.apache.mesos.MesosSchedulerDriver
import org.apache.mesos.Protos.FrameworkInfo

object DistributedShell {

  def main(args: Array[String]) {

    val framework = FrameworkInfo.newBuilder.
      setName("DistributedShell").
      setUser("").
      setRole("*").
      setCheckpoint(false).
      setFailoverTimeout(0.0d).
      build()

    // Create instance of schedule and connect to mesos
    val scheduler = new ScalaScheduler

    // Submit shell commands, followed by stop which (hackily) stops the driver.
    scheduler.submitTasks(args:_*)
    scheduler.submitTasks("STOP")

    val mesosURL = "172.20.0.11:5050"
    val driver = new MesosSchedulerDriver(scheduler, framework, mesosURL)

    // Run the driver, and join (block) on it. Use driver.start() to avoid this.
    driver.run()
  }
}
