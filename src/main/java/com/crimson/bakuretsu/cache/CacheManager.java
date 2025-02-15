
package com.crimson.bakuretsu.cache;

import com.crimson.bakuretsu.annotations.Cached;
import com.crimson.bakuretsu.core.Model;
import com.crimson.bakuretsu.core.ModelMetadata;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class CacheManager {

	//region Cache Configuration
	private static final Map<Class<? extends Model<?>>, Cache<Object, Model<?>>> entityCache = new ConcurrentHashMap<>();
	//endregion

	private CacheManager() {
	}

	//region Public API
	public static <R extends Model<R>> Optional<R> getCachedEntity(Class<R> clazz, Object id) {
		ModelMetadata metadata = ModelMetadata.get(clazz);

		return Optional.ofNullable(
			metadata.isCached ? clazz.cast(getCache(clazz).getIfPresent(id)) : null
		);
	}

	public static <R extends Model<R>> void cacheEntity(Class<R> clazz, Object id, R entity) {
		ModelMetadata metadata = ModelMetadata.get(clazz);

		if (!metadata.isCached) {
			return;
		}

		getCache(clazz).put(id, entity);
	}

	public static void clearCache() {
		entityCache.values().forEach(Cache::invalidateAll);
		entityCache.clear();
	}

	public static <R extends Model<R>> void removeFromCache(Class<R> clazz, Object id) {
		ModelMetadata metadata = ModelMetadata.get(clazz);

		if (!metadata.isCached) {
			return;
		}

		Cache<Object, Model<?>> cache = entityCache.get(clazz);

		if (cache != null) {
			cache.invalidate(id);
		}
	}

	//endregion

	//region Cache Initialization
	public static <R extends Model<R>> Cache<Object, Model<?>> getCache(Class<R> clazz) {
		return entityCache.computeIfAbsent(clazz, CacheManager::createCache);
	}

	private static Cache<Object, Model<?>> createCache(Class<? extends Model<?>> clazz) {
		Cached cachedAnnotation = clazz.getAnnotation(Cached.class);

		if (cachedAnnotation == null) {
			throw new IllegalArgumentException("Class " + clazz.getName() + " must be annotated with @Cached");
		}

		return Caffeine.newBuilder()
			.maximumSize(cachedAnnotation.maxSize())
			.expireAfterWrite(cachedAnnotation.expireAfterMinutes(), TimeUnit.MINUTES)
			.build();
	}
	//endregion


}