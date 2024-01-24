import com.hazelcast.config.Config
import com.hazelcast.config.MapConfig
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.map.IMap
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import java.util.UUID.randomUUID
import java.util.concurrent.TimeUnit.MINUTES

class HazelcastTest {
    private lateinit var hazelcast: HazelcastInstance
    private val mapName = "elements"

    @BeforeEach
    fun setUp() {
        val mapConfig = MapConfig(mapName)
        val config = Config().addMapConfig(mapConfig)
        hazelcast = Hazelcast.newHazelcastInstance(config)
    }

    @Test
    fun `only one entry in the time frame`() {
        val map: IMap<String, UUID> = hazelcast.getMap(mapName)
        val key = randomUUID().toString()
        val value = randomUUID()
        map.put(
            key,
            value,
            1,
            MINUTES,
            2,
            MINUTES
        )
        map[key] shouldBe value
    }
}
