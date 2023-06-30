#
#
#

all:
	echo "all"

build:
	( cd ..; mvn clean ant:ant install )

# maintenance of save, log and crontab
clear-save-file:
	find /mnt/stock/jp/save       -mtime +30 -print -delete
	find /mnt/stock/jp/jpx/report -mtime +30 -print -delete
	
clear-log-file:
	tar cfz /mnt/stock/jp/save/log_$$(date +%Y%m%d).taz tmp/*.log
	echo -n >tmp/yokwe-stock-jp.log
	echo -n >tmp/cron.log

# JASDEC
clear-jasdec:
	rm -rf /mnt/stock/jp/jasdec/download/page/*
	
update-jasdec:
	ant jasdec-data-file
	cd /mnt/stock/jp ; tar cfz /mnt/stock/jp/save/jasdec_$$(date +%Y%m%d).taz jasdec

# TOUSHIN
update-toushin:
	ant toushin-update-fund
	ant toushin-update-report
	ant toushin-update-stats
	cd /mnt/stock/jp ; tar cfz /mnt/stock/jp/save/toushin_$$(date +%Y%m%d).taz toushin

# JPX
update-jpx-stock:
	ant jpx-update-stock
	cd /mnt/stock/jp ; tar cfz /mnt/stock/jp/save/jpx_$$(date +%Y%m%d).taz jpx

update-jpx-stock-price:
	ant jpx-update-stock-price
	ant jpx-update-stats
	cd /mnt/stock/jp ; tar cfz /mnt/stock/jp/save/jpx_$$(date +%Y%m%d).taz jpx

# STOCK ETF REIT
update-stock-etf-reit:
	ant jpx-update-stock
	ant moneybujpx-update-etf
	ant japanreit-update-reit
	ant jpx-update-stats
	cd /mnt/stock/jp ; tar cfz /mnt/stock/jp/save/jpx_$$(date +%Y%m%d).taz jpx

# EDINET
update-edinet-info:
	ant edinet-update-info
	cd /mnt/stock/jp; tar cfz /mnt/stock/jp/save/edinet_$$(date +%Y%m%d).taz edinet/*.csv

update-edinet:
	ant edinet-data-file
	ant edinet-update-manifest
	cd /mnt/stock/jp; tar cfz /mnt/stock/jp/save/edinet_$$(date +%Y%m%d).taz edinet/*.csv

# TDNET
update-tdnet:
	ant tdnet-update-release
	cd /mnt/stock/jp ; tar cfz /mnt/stock/jp/save/tdnet_$$(date +%Y%m%d).taz tdnet/*.csv
	ant xbrl-tdent-report-update-reit-report
	ant xbrl-tdent-report-update-stock-report
	ant xbrl-tdent-update-dividend-reit
	ant xbrl-tdent-update-dividend-stock
	ant xbrl-tdent-update-dividend
	ant xbrl-tdent-update-dividend-annual
	cd /mnt/stock/jp; tar cfz /mnt/stock/jp/save/xbrl-tdnet_$$(date +%Y%m%d).taz xbrl/tdnet/*.csv

# SONY
update-sony:
	ant sony-update-fund
	ant sony-update-fund-info
