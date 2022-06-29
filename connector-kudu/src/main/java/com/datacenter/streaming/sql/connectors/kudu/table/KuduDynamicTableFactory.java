package com.datacenter.streaming.sql.connectors.kudu.table;

import com.datacenter.streaming.sql.connectors.kudu.KuduLookupOptions;
import com.datacenter.streaming.sql.connectors.kudu.KuduOptions;
import com.datacenter.streaming.sql.connectors.kudu.KuduSinkOptions;
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
 * KuduDynamicTableFactory
 *
 * @author wangpei
 */
public class KuduDynamicTableFactory implements DynamicTableSourceFactory, DynamicTableSinkFactory {

    // common options
    public static final String IDENTIFIER = "kudu";
    public static final ConfigOption<String> MASTER = ConfigOptions
            .key("master")
            .stringType()
            .noDefaultValue()
            .withDescription("the kudu master address.");
    public static final ConfigOption<String> TABLE = ConfigOptions
            .key("table")
            .stringType()
            .noDefaultValue()
            .withDescription("the jdbc table name.");
    public static final ConfigOption<String> USERNAME = ConfigOptions
            .key("username")
            .stringType()
            .noDefaultValue()
            .withDescription("the jdbc user name.");
    public static final ConfigOption<String> PASSWORD = ConfigOptions
            .key("password")
            .stringType()
            .noDefaultValue()
            .withDescription("the jdbc password.");


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

    // write options
    //private static final ConfigOption<Integer> SINK_BUFFER_FLUSH_MAX_ROWS = ConfigOptions
    //        .key("sink.buffer-flush.max-rows")
    //        .intType()
    //        .defaultValue(100)
    //        .withDescription("the flush max size (includes all append, upsert and delete records), over this number" +
    //                " of records, will flush data. The default value is 100.");
    private static final ConfigOption<Duration> SINK_BUFFER_FLUSH_INTERVAL = ConfigOptions
            .key("sink.buffer-flush.interval")
            .durationType()
            .defaultValue(Duration.ofSeconds(1))
            .withDescription("the flush interval mills, over this time, asynchronous threads will flush data. The " +
                    "default value is 1s.");
    private static final ConfigOption<Integer> SINK_MAX_RETRIES = ConfigOptions
            .key("sink.max-retries")
            .intType()
            .defaultValue(3)
            .withDescription("the max retry times if writing records to database failed.");

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
        return new KuduDynamicTableSource(
                getKuduOptions(helper.getOptions()),
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
        return new KuduDynamicTableSink(
                getKuduOptions(config),
                getKuduSinkOptions(config),
                physicalSchema);
    }

    private KuduOptions getKuduOptions(ReadableConfig readableConfig) {
        KuduOptions.KuduOptionsBuilder builder = KuduOptions.builder()
                .master(readableConfig.get(MASTER))
                .table(readableConfig.get(TABLE));
        readableConfig.getOptional(USERNAME).ifPresent(builder::username);
        readableConfig.getOptional(PASSWORD).ifPresent(builder::password);
        return builder.build();
    }

    private KuduLookupOptions getKuduLookupOptions(ReadableConfig readableConfig) {
        KuduLookupOptions.KuduLookupOptionsBuilder builder = KuduLookupOptions.builder();
        builder.cacheMaxSize(readableConfig.get(LOOKUP_CACHE_MAX_ROWS));
        builder.cacheExpireMs(readableConfig.get(LOOKUP_CACHE_TTL).toMillis());
        builder.maxRetryTimes(readableConfig.get(LOOKUP_MAX_RETRIES));

        return builder.build();
    }

    private KuduSinkOptions getKuduSinkOptions(ReadableConfig config) {
        KuduSinkOptions.KuduSinkOptionsBuilder builder = KuduSinkOptions.builder();
        //builder.batchSize(config.get(SINK_BUFFER_FLUSH_MAX_ROWS));
        builder.batchIntervalMs(config.get(SINK_BUFFER_FLUSH_INTERVAL).toMillis());
        builder.maxRetries(config.get(SINK_MAX_RETRIES));
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
        requiredOptions.add(MASTER);
        requiredOptions.add(TABLE);
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
        optionalOptions.add(USERNAME);
        optionalOptions.add(PASSWORD);

        optionalOptions.add(LOOKUP_CACHE_MAX_ROWS);
        optionalOptions.add(LOOKUP_CACHE_TTL);
        optionalOptions.add(LOOKUP_MAX_RETRIES);

        //optionalOptions.add(SINK_BUFFER_FLUSH_MAX_ROWS);
        optionalOptions.add(SINK_BUFFER_FLUSH_INTERVAL);
        optionalOptions.add(SINK_MAX_RETRIES);
        return optionalOptions;
    }


    /**
     * 验证配置
     *
     * @param config
     */
    private void validateConfigOptions(ReadableConfig config) {
        checkAllOrNone(config, new ConfigOption[]{
                USERNAME,
                PASSWORD
        });

        checkAllOrNone(config, new ConfigOption[]{
                LOOKUP_CACHE_MAX_ROWS,
                LOOKUP_CACHE_TTL
        });

        Preconditions.checkArgument(
                config.get(SINK_BUFFER_FLUSH_INTERVAL).compareTo(Duration.ofSeconds(1)) >= 0,
                SINK_BUFFER_FLUSH_INTERVAL.key() + " must >= 1000"
        );
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
