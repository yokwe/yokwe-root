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

update-crontab:
	crontab data/crontab

