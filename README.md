
cp_ads_video_cp_ads_video_TVBS_ads_video_user_most

mysql -hbireport.czcrimydwklw.us-east-1.rds.amazonaws.com -ubigdata_rw -Dbireport -pe20ycoy3yp09qij0kj8ngpcgxyywgmc9 -A

kafka-console-consumer.sh --bootstrap-server 10.66.100.37:9092 --topic cp_pull_vid_quality  

| grep "lm_view_end"



source /etc/profile
/data/Software/flink-1.11.3/bin/flink run \
--jobmanager yarn-cluster \
--yarnname cp_ads_video_user_most \
--yarnqueue stream_data \
--yarnjobManagerMemory 4096 \
--yarntaskManagerMemory 2048 \
--yarnslots 4 \
--parallelism 6 \
--class com.cp.data.exposure.Launcher \
/home/congpeng/app/flink/tob/cp_ads_video_user_most/cp_ads_video_user_most-1.0.jar



create table cp_ads_video_user_most 
(
    id int auto_increment  primary key,
    `vid` varchar(30) NOT NULL comment '视频id',
	`online_cnt` varchar(30) NOT NULL comment '在线人数-分钟',
    `minute_time` varchar(30) NOT NULL comment '在线时间-分钟',
    update_time    timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '记录更新时间'
)
comment '直播时每分钟在线人数';
