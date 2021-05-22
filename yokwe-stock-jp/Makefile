#
#
#

all:
	echo "all"

build:
	( cd ..; mvn ant:ant clean install )

# maintenance of save, log and crontab
clear-save-file:
	find /mnt/stock/jp/save -mtime +30 -delete
	
clear-log-file:
	tar cfz /mnt/stock/jp/save/log_$$(date +%Y%m%d).taz tmp/*.log
	echo -n >tmp/yokwe-stock-jp.log
	echo -n >tmp/cron.log

update-crontab:
	crontab data/crontab

# JASDEC
clear-jasdec:
	rm -rf /mnt/stock/jp/jasdec/download/page/*
	
update-jasdec:
	ant jasdec-data-file
	tar cfz /mnt/stock/jp/save/jasdec_$$(date +%Y%m%d).taz /mnt/stock/jp/jasdec

# TOUSHIN
clear-toushin:
	rm -rf /mnt/stock/jp/toushin/download/price/*
	
update-toushin:
	ant toushin-data-file
	tar cfz /mnt/stock/jp/save/toushin_$$(date +%Y%m%d).taz /mnt/stock/jp/toushin

# JPX
update-jpx-stock:
	ant jpx-update-stock
	tar cfz /mnt/stock/jp/save/jpx_$$(date +%Y%m%d).taz /mnt/stock/jp/jpx

# EDINET
update-edinet-info:
	ant edinet-update-info
	tar cfz /mnt/stock/jp/save/edinet_$$(date +%Y%m%d).taz /mnt/stock/jp/edinet/*.csv

update-edinet:
	ant edinet-data-file
	tar cfz /mnt/stock/jp/save/edinet_$$(date +%Y%m%d).taz /mnt/stock/jp/edinet/*.csv

# TDNET
update-tdnet:
	ant tdnet-update-release
	tar cfz /mnt/stock/jp/save/tdnet_$$(date +%Y%m%d).taz /mnt/stock/jp/tdnet/*.csv

