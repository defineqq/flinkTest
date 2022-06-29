package com.datacenter.streaming.sql.connectors.redis.table;

import com.datacenter.streaming.sql.connectors.redis.KeyType;
import com.datacenter.streaming.sql.connectors.redis.RedisLookupOptions;
import com.datacenter.streaming.sql.connectors.redis.RedisOptions;
import com.datacenter.streaming.sql.connectors.redis.RedisSinkOptions;
import org.apache.flink.configuration.ConfigOption;
import org.apache.flink.configuration.ConfigOptions;
import org.apache.flink.configuration.ReadableConfig;
import org.apache.flink.table.api.TableSchema;
import org.apache.flink.table.connector.sink.DynamicTableSink;
import org.apache.flink.table.connector.source.DynamicTableSource;
import org.apache.flink.table.factories.DynamicTableSinkFactory;
import org.apache.flink.table.factories.DynamicTableSourceFactory;
import org.apache.flink.table.factories.FactoryUtil;
import org.apache.flink.table.utils.TableSchemaUtils;
import org.apache.flink.util.Preconditions;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * RedisDynamicTableFactory
 *
 * @author wangpei
 */
public class RedisDynamicTableFactory implements DynamicTableSourceFactory, DynamicTableSinkFactory {

    // common options
    public static final String IDENTIFIER = "redis";
    public static final ConfigOption<String> HOST = ConfigOptions
            .key("host")
            .stringType()
            .noDefaultValue()
            .withDescription("the redis host.");
    public static final ConfigOption<Integer> PORT = ConfigOptions
            .key("port")
            .intType()
            .noDefaultValue()
            .withDescription("the redis port.");
    public static final ConfigOption<Integer> DB = ConfigOptions
            .key("db")
            .intType()
            .noDefaultValue()
            .withDescription("the redis db.");
    public static final ConfigOption<String> PASSWORD = ConfigOptions
            .key("password")
            .stringType()
            .noDefaultValue()
            .withDescription("the redis password.");
    public static final ConfigOption<KeyType> KEYTYPE = ConfigOptions
            .key("keyType")
            .enumType(KeyType.class)
            .noDefaultValue()
            .withDescription("the redis key type.");

    // lookup options
    private static final ConfigOption<Long> LOOKUP_CACHE_MAX_ROWS = ConfigOptions
            .key("lookup.cache.max-rows")
            .longType()
            .defaultValue(-1L)
            .withDescription("the max number of rows of lookup cache, over this value, the oldest rows will " +
                    "be eliminated. \"cache.max-rows\" and \"cache.ttl\" options must all be specified if any of them is " +
                    "specified. Cache is not enabled as default.");
    private static final ConfigOption<Duration> LOOKUP_CACHE_TTL = ConfigOptions
            .key("lookup.cache.ttl")
            .durationType()
            .defaultValue(Duration.ofSeconds(-1))
            .withDescription("the cache time to live.");
    private static final ConfigOption<Integer> LOOKUP_MAX_RETRIES = ConfigOptions
            .key("lookup.max-retries")
            .intType()
            .defaultValue(3)
            .withDescription("the max retry times if lookup database failed.");
    private static final ConfigOption<Integer> SINK_MAX_RETRIES = ConfigOptions
            .key("sink.max-retries")
            .intType()
            .defaultValue(3)
            .withDescription("the max retry times if writing records to database failed.");
    private static final ConfigOption<Integer> SINK_EXPIRE_SECONDS = ConfigOptions
            .key("sink.expire-seconds")
            .intType()
            .defaultValue(-1)
            .withDescription("the expiration time of the key.unit: second.");


    /**
     * DynamicTableSource 实例
     *
     * @param context
     * @return
     */
    @Override
    public DynamicTableSource createDynamicTableSource(Context context) {
        final FactoryUtil.TableFactoryHelper helper = FactoryUtil.createTableFactoryHelper(this, context);
        final ReadableConfig config = helper.getOptions();
        helper.validate();
        validateConfigOptions(config);
        TableSchema physicalSchema = TableSchemaUtils.getPhysicalSchema(context.getCatalogTable().getSchema());
        return new RedisDynamicTableSource(
                getRedisOptions(helper.getOptions()),
                getKuduLookupOptions(helper.getOptions()),
                physicalSchema);
    }

    /**
     * DynamicTableSink 实例
     *
     * @param context
     * @return
     */
    @Override
    public DynamicTableSink createDynamicTableSink(Context context) {
        final FactoryUtil.TableFactoryHelper helper = FactoryUtil.createTableFactoryHelper(this, context);
        final ReadableConfig config = helper.getOptions();
        helper.validate();
        validateConfigOptions(config);
        TableSchema physicalSchema = TableSchemaUtils.getPhysicalSchema(context.getCatalogTable().getSchema());
        return new RedisDynamicTableSink(
                getRedisOptions(config),
                getKuduSinkOptions(config),
                physicalSchema);
    }

    private RedisOptions getRedisOptions(ReadableConfig readableConfig) {
        RedisOptions.RedisOptionsBuilder builder = RedisOptions.builder()
                .host(readableConfig.get(HOST))
                .port(readableConfig.get(PORT))
                .db(readableConfig.get(DB))
                .keyType(readableConfig.get(KEYTYPE));
        readableConfig.getOptional(PASSWORD).ifPresent(builder::password);
        return builder.build();
    }

    private RedisLookupOptions getKuduLookupOptions(ReadableConfig readableConfig) {
        RedisLookupOptions.RedisLookupOptionsBuilder builder = RedisLookupOptions.builder();
        builder.cacheMaxSize(readableConfig.get(LOOKUP_CACHE_MAX_ROWS));
        builder.cacheExpireMs(readableConfig.get(LOOKUP_CACHE_TTL).toMillis());
        builder.maxRetryTimes(readableConfig.get(LOOKUP_MAX_RETRIES));
        return builder.build();
    }

    private RedisSinkOptions getKuduSinkOptions(ReadableConfig config) {
        RedisSinkOptions.RedisSinkOptionsBuilder builder = RedisSinkOptions.builder();
        builder.maxRetries(config.get(SINK_MAX_RETRIES));
        builder.expireSeconds(config.get(SINK_EXPIRE_SECONDS));
        return builder.build();
    }


    /**
     * 工厂唯一标识符
     *
     * @return
     */
    @Override
    public String factoryIdentifier() {
        return IDENTIFIER;
    }

    /**
     * 必选项
     *
     * @return
     */
    @Override
    public Set<ConfigOption<?>> requiredOptions() {
        Set<ConfigOption<?>> requiredOptions = new HashSet<>();
        requiredOptions.add(HOST);
        requiredOptions.add(PORT);
        requiredOptions.add(DB);
        requiredOptions.add(KEYTYPE);
        return requiredOptions;
    }

    /**
     * 可选项
     *
     * @return
     */
    @Override
    public Set<ConfigOption<?>> optionalOptions() {
        Set<ConfigOption<?>> optionalOptions = new HashSet<>();
        optionalOptions.add(PASSWORD);

        optionalOptions.add(LOOKUP_CACHE_MAX_ROWS);
        optionalOptions.add(LOOKUP_CACHE_TTL);
        optionalOptions.add(LOOKUP_MAX_RETRIES);

        optionalOptions.add(SINK_MAX_RETRIES);
        optionalOptions.add(SINK_EXPIRE_SECONDS);
        return optionalOptions;
    }


    /**
     * 验证配置
     *
     * @param config
     */
    private void validateConfigOptions(ReadableConfig config) {
        checkAllOrNone(config, new ConfigOption[]{
                LOOKUP_CACHE_MAX_ROWS,
                LOOKUP_CACHE_TTL
        });
    }

    /**
     * 要么一个都没有，要么都要有
     *
     * @param config
     * @param configOptions
     */
    private void checkAllOrNone(ReadableConfig config, ConfigOption<?>[] configOptions) {
        int presentCount = 0;
        for (ConfigOption configOption : configOptions) {
            if (config.getOptional(configOption).isPresent()) {
                presentCount++;
            }
        }
        String[] propertyNames = Arrays.stream(configOptions).map(ConfigOption::key).toArray(String[]::new);
        Preconditions.checkArgument(configOptions.length == presentCount || presentCount == 0,
                "Either all or none of the following options should be provided:\n" + String.join("\n", propertyNames));
    }
}
