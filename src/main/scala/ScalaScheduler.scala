import org.apache.mesos.{SchedulerDriver, Scheduler}
import org.apache.mesos.Protos._
import scala.collection.JavaConverters._
import scala.collection.mutable

class ScalaScheduler extends Scheduler {
  private val tasks: mutable.Queue[String] = mutable.Queue[String]()

  override def error(driver: SchedulerDriver, message: String) {}

  override def executorLost(driver: SchedulerDriver, executorId: ExecutorID, slaveId: SlaveID, status: Int) {}

  override def slaveLost(driver: SchedulerDriver, slaveId: SlaveID) {}

  override def disconnected(driver: SchedulerDriver) {}

  override def frameworkMessage(driver: SchedulerDriver, executorId: ExecutorID, slaveId: SlaveID, data: Array[Byte]) {}

  override def statusUpdate(driver: SchedulerDriver, status: TaskStatus) {
    println(s"Received status update: $status")
  }

  override def offerRescinded(driver: SchedulerDriver, offerId: OfferID) {}

  override def resourceOffers(driver: SchedulerDriver, offers: java.util.List[Offer]) {
    //for every available offer run tasks
    for (offer <- offers.asScala) {
      println(s"offer $offer")
      tasks.dequeueFirst(value => true) map (cmdString => {
        if (cmdString == "STOP") {
          driver.stop(false)
        } else {
          val cmd = CommandInfo.newBuilder
            .setValue(cmdString)

          // TODO this code doesn't check the offer contains these resources, but it probably does

          // Request 1 CPU
          val cpus = Resource.newBuilder.
            setType(org.apache.mesos.Protos.Value.Type.SCALAR)
            .setName("cpus")
            .setScalar(org.apache.mesos.Protos.Value.Scalar.newBuilder.setValue(1.0))
            .setRole("*")
            .build

          // Generate a task id
          val id = "task" + System.currentTimeMillis()

          // Create task with given command via protobuf builder
          val task = TaskInfo.newBuilder
            .setCommand(cmd)
            .setName(id)
            .setTaskId(TaskID.newBuilder.setValue(id))
            .addResources(cpus)
            .setSlaveId(offer.getSlaveId)
            .build

          // Launch
          driver.launchTasks(List(offer.getId).asJava, List(task).asJava)
        }
      })
    }
  }

  def submitTasks(tasks: String*) = {
    this.synchronized {
      this.tasks.enqueue(tasks: _*)
    }
  }

  override def reregistered(driver: SchedulerDriver, masterInfo: MasterInfo) {}

  override def registered(driver: SchedulerDriver, frameworkId: FrameworkID, masterInfo: MasterInfo) {}

}
