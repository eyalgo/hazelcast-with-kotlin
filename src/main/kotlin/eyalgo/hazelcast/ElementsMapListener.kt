package eyalgo.hazelcast

import com.hazelcast.core.EntryEvent
import com.hazelcast.map.listener.EntryEvictedListener
import eyalgo.model.Key
import java.util.UUID

class ElementsMapListener : EntryEvictedListener<Key, UUID> {
    override fun entryEvicted(event: EntryEvent<Key, UUID>) {
        println("${event.key} is evicted")
    }
}
