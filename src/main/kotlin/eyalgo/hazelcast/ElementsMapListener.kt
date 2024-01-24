package eyalgo.hazelcast

import com.hazelcast.core.EntryEvent
import com.hazelcast.map.listener.EntryEvictedListener
import com.hazelcast.map.listener.EntryExpiredListener
import com.hazelcast.map.listener.EntryRemovedListener
import eyalgo.model.Key
import java.util.UUID

class ElementsMapListener :
    EntryEvictedListener<Key, UUID>,
    EntryRemovedListener<Key, UUID>,
    EntryExpiredListener<Key, UUID> {
    override fun entryEvicted(event: EntryEvent<Key, UUID>): Unit = println("${event.key} is evicted")
    override fun entryRemoved(event: EntryEvent<Key, UUID>): Unit = println("${event.key} is removed")
    override fun entryExpired(event: EntryEvent<Key, UUID>): Unit = println("${event.key} is expired")
}
