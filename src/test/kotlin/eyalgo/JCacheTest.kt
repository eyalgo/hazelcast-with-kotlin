package eyalgo

import com.hazelcast.cache.HazelcastCachingProvider
import com.hazelcast.cache.HazelcastCachingProvider.propertiesByInstanceItself
import com.hazelcast.config.Config
import com.hazelcast.config.MapConfig
import com.hazelcast.core.Hazelcast.newHazelcastInstance
import com.hazelcast.core.HazelcastInstance
import eyalgo.model.Element
import eyalgo.model.Key
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.UUID
import javax.cache.Cache
import javax.cache.Caching
import javax.cache.configuration.MutableConfiguration
import kotlin.random.Random

class JCacheTest {
    private val cacheName = "elements"
    private lateinit var cache: Cache<Key, Element>
    fun setUp() {
        val cachingProvider = Caching.getCachingProvider(HazelcastCachingProvider::class.qualifiedName)
        val instance = setupHazelcast()
        val cacheManager = cachingProvider.getCacheManager(
            null,
            null,
            propertiesByInstanceItself(instance)
        )
        val configuration = MutableConfiguration<Key, Element>()
            .setTypes(Key::class.java, Element::class.java)

        cache = cacheManager.createCache(cacheName, configuration)
    }

    @BeforeEach
    fun setUp2() {
        val cachingProvider = Caching.getCachingProvider()
        val configuration = MutableConfiguration<Key, Element>()
            .setTypes(Key::class.java, Element::class.java)
        cache = cachingProvider.cacheManager.createCache(cacheName, configuration)
    }

    private fun setupHazelcast(): HazelcastInstance {
        val mapConfig = MapConfig(cacheName)
        val config = Config()
            .addMapConfig(mapConfig)
        return newHazelcastInstance(config)
    }

    @Test
    @Disabled("WIP jcache does not work")
    fun `add an entry and get it`() {
        val key = randomKey()
        val value = randomValue()
        cache.put(key, value)
        cache[key] shouldBe value
    }

    private fun randomKey(): Key = Key(randomString(), Random.nextInt())
    private fun randomValue(): Element = Element(randomString())
    private fun randomString(): String = UUID.randomUUID().toString()
}
