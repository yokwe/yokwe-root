#
#
#

all:
	echo "all"

build:
	( cd ..; mvn clean ant:ant install )

# maintenance of save, log and crontab
clear-save-file:
	find /mnt/stock/us/save -mtime +30 -print -delete
	
clear-log-file:
	tar cfz /mnt/stock/us/save/log_$$(date +%Y%m%d).taz tmp/*.log
	echo -n >tmp/yokwe-stock-us.log
	echo -n >tmp/cron.log

save:
	cd /mnt/stock/us ; tar cfz /mnt/stock/us/save/nasdaq_$$(date +%Y%m%d).taz nasdaq


# STOCK
update-stock:
	ant update-stock
	cd /mnt/stock/us ; tar cfz /mnt/stock/us/save/nasdaq_$$(date +%Y%m%d).taz nasdaq

# NASDAQ
nasdaq-update-stock-price:
	ant nasdaq-update-stock-price
	ant nasdaq-update-stock-stats
	cd /mnt/stock/us ; tar cfz /mnt/stock/us/save/nasdaq_$$(date +%Y%m%d).taz nasdaq

nasdaq-update-stock-dividend:
	ant nasdaq-update-stock-dividend
	ant nasdaq-update-stock-stats
	cd /mnt/stock/us ; tar cfz /mnt/stock/us/save/nasdaq_$$(date +%Y%m%d).taz nasdaq

nasdaq-update-stock-stats:
	ant nasdaq-update-stock-stats
	cd /mnt/stock/us ; tar cfz /mnt/stock/us/save/nasdaq_$$(date +%Y%m%d).taz nasdaq

