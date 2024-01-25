package eyalgo.hazelcast

import com.github.blindpirate.extensions.CaptureSystemOutput
import com.hazelcast.config.Config
import com.hazelcast.config.MapConfig
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.map.IMap
import eyalgo.model.Element
import eyalgo.model.Key
import io.kotest.matchers.shouldBe
import org.hamcrest.CoreMatchers
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HazelcastTest {
    private val mapName = "elements"
    private lateinit var hazelcast: HazelcastInstance

    // TODO modify the value to list of elements (add tests)
    // TODO check the documentation regarding serialisation (avoiding it)
    private lateinit var map: IMap<Key, Element>

    @BeforeEach
    fun setUp() {
        val mapConfig = MapConfig(mapName)
        val config = Config()
            .addMapConfig(mapConfig)
        hazelcast = Hazelcast.newHazelcastInstance(config)

        map = hazelcast.getMap(mapName)
        map.addEntryListener(ElementsMapListener(), false)
    }

    @AfterEach
    fun tearDown() {
        hazelcast.shutdown()
    }

    @BeforeAll
    fun disableHazelcastLogging() {
        // See [logging configuration](https://docs.hazelcast.com/imdg/4.2/clusters/logging-configuration)
        System.setProperty("hazelcast.logging.type", "none")
    }

    @Test
    fun `only one entry in the time frame`() {
        val key = randomKey()
        val value = randomValue()
        map.put(key, value, 1, TimeUnit.MINUTES, 2, TimeUnit.MINUTES)
        map[key] shouldBe value
    }

    @Test
    @CaptureSystemOutput
    fun `entry is evacuated by TTL`(output: CaptureSystemOutput.OutputCapture) {
        val key = randomKey()
        val value = randomValue()

        map.put(key, value, 200, TimeUnit.MILLISECONDS, 600, TimeUnit.MILLISECONDS)
        Thread.sleep(250)
        map[key] shouldBe null

        output.expect(CoreMatchers.containsString("$key is expired"))
    }

    @Test
    @CaptureSystemOutput
    fun `entry is evacuated by Max Idle`(output: CaptureSystemOutput.OutputCapture) {
        val key = randomKey()
        val value = randomValue()

        map.put(key, value, 600, TimeUnit.MILLISECONDS, 200, TimeUnit.MILLISECONDS)
        Thread.sleep(250)
        map[key] shouldBe null

        output.expect(CoreMatchers.containsString("$key is expired"))
    }

    @Test
    @CaptureSystemOutput
    fun `an action happens when an element is evicted`(output: CaptureSystemOutput.OutputCapture) {
        val key = randomKey()
        val value = randomValue()

        map.put(key, value, 400, TimeUnit.MILLISECONDS, 600, TimeUnit.MILLISECONDS)
        Thread.sleep(250)
        map.evict(key)

        output.expect(CoreMatchers.containsString("$key is evicted"))
    }

    @Test
    @CaptureSystemOutput
    fun `an action happens when an element is deleted`(output: CaptureSystemOutput.OutputCapture) {
        val key = randomKey()
        val value = randomValue()

        map.put(key, value, 400, TimeUnit.MILLISECONDS, 600, TimeUnit.MILLISECONDS)
        Thread.sleep(250)
        map.delete(key)

        output.expect(CoreMatchers.containsString("$key is expired"))
    }

    @Test
    @CaptureSystemOutput
    @Disabled("WIP - need to investigate")
    fun `an action happens when an element is removed`(output: CaptureSystemOutput.OutputCapture) {
        val key = randomKey()
        val value = randomValue()

        map.put(key, value, 400, TimeUnit.MILLISECONDS, 600, TimeUnit.MILLISECONDS)
        Thread.sleep(250)
        map.remove(key)

        output.expect(CoreMatchers.containsString("$key is removed"))
    }

    @Test
    @Disabled("WIP - tests for the eviction ttl")
    fun `Max Idle is updated `() {
        val key = randomKey()
        val value = randomValue()

//        map.put(key, value, 500, MINUTES, 600, MILLISECONDS)
        map.put(key, value, 1, TimeUnit.MINUTES, 500, TimeUnit.SECONDS)
//        Thread.sleep(100)
        map[key] shouldBe value

        Thread.sleep(150)
        map[key] shouldBe value
        Thread.sleep(220)
        map[key] shouldBe null
    }

    private fun randomKey(): Key = Key(randomString(), Random.nextInt())
    private fun randomValue(): Element = Element(randomString())
    private fun randomString(): String = UUID.randomUUID().toString()
}
