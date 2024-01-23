package eyalgo

import com.hazelcast.config.Config
import com.hazelcast.config.MapConfig
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance

class HazelcastTest {
    private lateinit var hazelcast: HazelcastInstance
    private val mapName = "elements"

    fun setUp() {
        val mapConfig = MapConfig(mapName)
        val config = Config().addMapConfig(mapConfig)
        hazelcast = Hazelcast.newHazelcastInstance(config)
    }
}
