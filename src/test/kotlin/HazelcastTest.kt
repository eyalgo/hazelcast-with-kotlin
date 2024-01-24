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
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.MINUTES

class HazelcastTest {
    private val mapName = "elements"
    private lateinit var hazelcast: HazelcastInstance
    private lateinit var map: IMap<UUID, UUID>

    @BeforeEach
    fun setUp() {
        val mapConfig = MapConfig(mapName)
        val config = Config().addMapConfig(mapConfig)
        hazelcast = Hazelcast.newHazelcastInstance(config)

        map = hazelcast.getMap(mapName)
    }

    @Test
    fun `only one entry in the time frame`() {
        val key = randomUUID()
        val value = randomUUID()
        map.put(key, value, 1, MINUTES, 2, MINUTES)
        map[key] shouldBe value
    }

    @Test
    fun `entry is evacuated by TTL`() {
        val key = randomUUID()
        val value = randomUUID()

        map.put(key, value, 200, MILLISECONDS, 600, MILLISECONDS)
        Thread.sleep(250)
        map[key] shouldBe null
    }

    @Test
    fun `entry is evacuated by Max Idle`() {
        val key = randomUUID()
        val value = randomUUID()

        map.put(key, value, 600, MILLISECONDS, 200, MILLISECONDS)
        Thread.sleep(250)
        map[key] shouldBe null
    }
}
