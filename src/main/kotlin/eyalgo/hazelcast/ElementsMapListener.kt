package eyalgo.hazelcast

import com.hazelcast.core.EntryEvent
import com.hazelcast.core.EntryListener
import com.hazelcast.map.MapEvent
import eyalgo.model.Element
import eyalgo.model.Key

class ElementsMapListener : EntryListener<Key, Element> {
    override fun entryEvicted(event: EntryEvent<Key, Element>): Unit = println("${event.key} is evicted")
    override fun entryRemoved(event: EntryEvent<Key, Element>): Unit = println("${event.key} is removed")
    override fun entryExpired(event: EntryEvent<Key, Element>): Unit = println("${event.key} is expired")
    override fun entryAdded(event: EntryEvent<Key, Element>): Unit = println("${event.key} is added")
    override fun entryUpdated(event: EntryEvent<Key, Element>): Unit = println("${event.key} is updated")
    override fun mapCleared(event: MapEvent): Unit = println("${event.name} map cleared")
    override fun mapEvicted(event: MapEvent): Unit = println("${event.name} map evicted")
}
