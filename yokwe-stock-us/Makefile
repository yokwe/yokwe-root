#
#
#

all:
	echo "all"

build:
	( cd ..; mvn clean ant:ant install )

# maintenance of save, log and crontab
clear-save-file:
	find /mnt/stock/us/save     -mtime +30 -print -delete
	find /mnt/stock/us/*/report -mtime +30 -print -delete
	
clear-log-file:
	tar cfz /mnt/stock/us/save/log_$$(date +%Y%m%d).taz tmp/*.log
	echo -n >tmp/yokwe-stock-us.log
	echo -n >tmp/cron.log

backup:
	cp /mnt/stock/us/nasdaq/TEMPLATE_NASDAQ_STOCK_STATS.ods /mnt/stock/us/backup/TEMPLATE_NASDAQ_STOCK_STATS_$$(date +%Y%m%d).ods

save:
	cd /mnt/stock/us ; tar cfz /mnt/stock/us/save/nasdaq_$$(date +%Y%m%d).taz nasdaq


# STOCK
update-stock:
	ant update-stock
	cd /mnt/stock/us ; tar cfz /mnt/stock/us/save/us_$$(date +%Y%m%d).taz bats nasdaq/*csv nyse *csv

# NASDAQ
nasdaq-update-stock-price:
	ant nasdaq-update-stock-price
	ant nasdaq-update-stock-stats
	cd /mnt/stock/us ; tar cfz /mnt/stock/us/save/nasdaq_$$(date +%Y%m%d).taz nasdaq/div nasdaq/price

nasdaq-update-stock-dividend:
	ant nasdaq-update-stock-dividend
	ant nasdaq-update-stock-stats
	cd /mnt/stock/us ; tar cfz /mnt/stock/us/save/nasdaq_$$(date +%Y%m%d).taz nasdaq/div nasdaq/price

nasdaq-update-stock-stats:
	ant nasdaq-update-stock-stats
	cd /mnt/stock/us ; tar cfz /mnt/stock/us/save/us_$$(date +%Y%m%d).taz bats nasdaq/*csv nyse *csv

# TRADING STOCK
update-trading-stock:
	ant monex-update-monex-stock
	ant sbi-update-sbi-stock
	ant rakuten-update-rakuten-stock
	ant update-trading-stock
	cd /mnt/stock/us ; tar cfz /mnt/stock/us/save/trading_$$(date +%Y%m%d).taz monex nikko rakuten sbi *csv
