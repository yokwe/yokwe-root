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
	find /mnt/stock/trade/save -mtime +30 -print -delete
	
clear-log-file:
	tar cfz /mnt/stock/trade/save/log_$$(date +%Y%m%d).taz tmp/*.log
	echo -n >tmp/yokwe-stock-trade.log
	echo -n >tmp/cron.log

show-ant-process:
	ps -xwww -o pid,lstart,etime,time,user,command | egrep 'ant|PID' | egrep -v 'egrep|show-ant-process'

kill-long-running-process:
	ps -wwxo pid,etimes,args | awk '/bin\/java/ && 3600<$$2 && !/org.eclipse.equinox.launcher/ {print}' | tee tmp/kill-long-running-process
	@if [ -s tmp/kill-long-running-process ]; then \
		awk '{COMMAND="kill -kill " $$1; print(COMMAND); system(COMMAND);}' <tmp/kill-long-running-process; \
	fi

backup:
	cp /mnt/stock/trade/activity/投資活動_monex.ods      /mnt/stock/trade/activity/backup/投資活動_monex_$$(date +%Y%m%d).ods
	cp /mnt/stock/trade/activity/投資活動_gmo.ods        /mnt/stock/trade/activity/backup/投資活動_gmo_$$(date +%Y%m%d).ods
	cp /mnt/stock/trade/activity/投資活動_firstrade.ods  /mnt/stock/trade/activity/backup/投資活動_firstrade_$$(date +%Y%m%d).ods
	cp /mnt/stock/trade/activity/投資活動_ソニー銀行.ods /mnt/stock/trade/activity/backup/投資活動_ソニー銀行_$$(date +%Y%m%d).ods

update-symbol: update-symbol-info
	ant update-symbol

update-symbol-info:
	ant monex-update-symbol-info
	ant sbi-update-symbol-info
	ant rakuten-update-symbol-info

monex-update-fx-tax:
	ant monex-update-fx-tax
	ant update-fx
