#
#
#

all:
	echo "all"

build:
	( cd ..; mvn clean ant:ant install )

update-crontab:
	crontab data/crontab

# maintenance of save, log and crontab
clear-save-file:
	find /mnt/finance/save     -mtime +30 -print -delete
	find /mnt/finance/*/report -mtime +30 -print -delete
	
clear-log-file:
	tar cfz /mnt/fiancnce/save/log_$$(date +%Y%m%d).taz tmp/*.log
	echo -n >tmp/yokwe-finance.log
	echo -n >tmp/cron.log 


update-jpx-listing:
	ant provider-jpx-update-listring

update-jpx-stock-info:
	ant provider-jpx-update-etf
	ant provider-jpx-update-etn
	ant provider-jpx-update-foreign-stock
	ant provider-jpx-update-infra-fund
	ant provider-jpx-update-reit
	ant provider-jpx-update-stock-info
	ant provider-jpx-update-stock-price
	ant provider-jpx-update-stock-info
	ant provider-jpx-update-jpx-stock-info-jp

update-jpx-stock-price:
	ant provider-jpx-update-jpx-stock-price-jp 


update-jita:
	ant provider-jita-update-jita-fund-info-jp
	ant provider-jita-update-div-price
	ant provider-jita-update-jita-fund-price-jp


update-stock-info-us:
	ant provider-bats-update-stock-info
	ant provider-nasdaq-update-stock-info
	ant provider-nyse-update-stock-info

