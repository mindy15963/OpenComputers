package li.cil.oc.common.tileentity.traits

import li.cil.oc.api.network
import li.cil.oc.api.network.SidedEnvironment
import li.cil.oc.server.TickHandler
import li.cil.oc.Settings
import li.cil.oc.util.ExtendedNBT._
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.ForgeDirection
import scala.math.ScalaNumber

trait Environment extends TileEntity with network.Environment {
  override protected def initialize() {
    super.initialize()
    if (isServer) {
      TickHandler.schedule(this)
    }
  }

  override protected def dispose() {
    super.dispose()
    if (isServer) {
      Option(node).foreach(_.remove)
      this match {
        case sidedEnvironment: SidedEnvironment => for (side <- ForgeDirection.VALID_DIRECTIONS) {
          Option(sidedEnvironment.sidedNode(side)).foreach(_.remove())
        }
        case _ =>
      }
    }
  }

  // ----------------------------------------------------------------------- //

  override def readFromNBT(nbt: NBTTagCompound) {
    super.readFromNBT(nbt)
    if (node != null && node.host == this) {
      node.load(nbt.getCompoundTag(Settings.namespace + "node"))
    }
  }

  override def writeToNBT(nbt: NBTTagCompound) {
    super.writeToNBT(nbt)
    if (node != null && node.host == this) {
      nbt.setNewCompoundTag(Settings.namespace + "node", node.save)
    }
  }

  // ----------------------------------------------------------------------- //

  override def onMessage(message: network.Message) {}

  override def onConnect(node: network.Node) {}

  override def onDisconnect(node: network.Node) {}

  // ----------------------------------------------------------------------- //

  final protected def result(args: Any*): Array[AnyRef] = {
    def unwrap(arg: Any): AnyRef = arg match {
      case x: ScalaNumber => x.underlying
      case x => x.asInstanceOf[AnyRef]
    }
    Array(args map unwrap: _*)
  }
}
