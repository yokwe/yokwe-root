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
	@ps -wwxo pid,etimes,args | awk '/bin\/java/ && 3600<$$2 && !/org.eclipse.equinox.launcher/ && !/yokwe.stock.jp.nikkei/ {print}' | tee tmp/kill-long-running-process
	@if [ -s tmp/kill-long-running-process ]; then \
		date +'%y-%m-%d %H:%M:%S'; \
		awk '{COMMAND="kill -kill " $$1; print(COMMAND); system(COMMAND);}' <tmp/kill-long-running-process; \
	fi

backup:
	date=$$(date +%Y%m%d); cd /mnt/stock/trade/activity; \
	for i in *.ods; do name=$${i%.ods}; echo copy $${name}.ods; cp $${name}.ods  backup/$${date}_$${name}.ods; done


monex-update-fx-tax:
	ant monex-update-fx-tax
	ant update-fx
