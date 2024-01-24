package eyalgo.hazelcast

import com.hazelcast.core.EntryEvent
import com.hazelcast.map.listener.EntryEvictedListener
import java.util.UUID

class ElementsMapListener : EntryEvictedListener<UUID, UUID> {
    override fun entryEvicted(event: EntryEvent<UUID, UUID>?) {
        println("Printed to System.out!")
    }
}
