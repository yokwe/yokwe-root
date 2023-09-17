#
#
#

all:
	echo "all"

build:
	( cd ..; mvn clean ant:ant install )

# maintenance of save, log and crontab
clear-save-file:
	find /mnt/finance/save     -mtime +30 -print -delete
	find /mnt/finance/*/report -mtime +30 -print -delete
	
clear-log-file:
	tar cfz /mnt/fiancnce/save/log_$$(date +%Y%m%d).taz tmp/*.log
	echo -n >tmp/yokwe-finance.log
	echo -n >tmp/cron.log

