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
	cd /mnt/stock/jp ; tar cfz /mnt/stock/jp/save/jasdec_$$(date +%Y%m%d).taz jasdec

# TOUSHIN
clear-toushin:
	rm -rf /mnt/stock/jp/toushin/download/price/*
	
update-toushin:
	ant toushin-data-file
	cd /mnt/stock/jp ; tar cfz /mnt/stock/jp/save/toushin_$$(date +%Y%m%d).taz toushin

# JPX
update-jpx-stock:
	ant jpx-update-stock
	cd /mnt/stock/jp ; tar cfz /mnt/stock/jp/save/jpx_$$(date +%Y%m%d).taz jpx

update-jpx-stock-price:
	ant jpx-update-stock-price
	cd /mnt/stock/jp ; tar cfz /mnt/stock/jp/save/jpx_$$(date +%Y%m%d).taz jpx

# EDINET
update-edinet-info:
	ant edinet-update-info
	cd /mnt/stock/jp; tar cfz /mnt/stock/jp/save/edinet_$$(date +%Y%m%d).taz edinet/*.csv

update-edinet:
	ant edinet-data-file
	cd /mnt/stock/jp; tar cfz /mnt/stock/jp/save/edinet_$$(date +%Y%m%d).taz edinet/*.csv

# TDNET
update-tdnet:
	ant tdnet-update-release
	cd /mnt/stock/jp ; tar cfz /mnt/stock/jp/save/tdnet_$$(date +%Y%m%d).taz tdnet/*.csv