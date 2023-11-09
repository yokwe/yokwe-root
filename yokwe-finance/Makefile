#
#
#


DATA_PATH_FILE := data/DataPathLocation
DATA_PATH := $(shell cat $(DATA_PATH_FILE))

.PHONY: all build check-data-path


all: check-data-path
	@echo "all"


update-crontab:
	crontab data/crontab


build:
	( cd ..; mvn ant:ant install )

full-build:
	( cd ..; mvn clean ant:ant install )


check-data-path:
	@echo "DATA_PATH_FILE  !$(DATA_PATH_FILE)!"
	@echo "DATA_PATH       !$(DATA_PATH)!"
	@if [ ! -d $(DATA_PATH) ]; then \
	  	  echo "no directory  !${DATA_PATH}!" ; \
	  exit 1 ; \
	fi


log-start:
	@date +'%F %T LOG START $(LOG_TITLE)'

log-stop:
	@date +'%F %T LOG STOP  $(LOG_TITLE)'

log-time:
	@date +'%F %T LOG TIME'


rync-to-sandisk2t:
	rsync -ah --delete --stats --dry-run /Volumes/finance/ /Volumes/SanDisk2T/finance

rsync-to-truenas:
	@date +'%F %T RSYNC START'
	rsync -ah --delete /Volumes/SanDisk2T/finance/ /Volumes/finance
	@date +'%F %T RSYNC STOP'

kill-soffice:
	@ps xg > tmp/kill_soffice
	@awk '/LibreOffice/ && /soffice/ {print; system("killall -v soffice");}' tmp/kill_soffice

# maintenance of save and log
clear-save-file: check-data-path
	find ${DATA_PATH}/save -mtime +7d -print -delete

clear-report-file: check-data-path
	find ${DATA_PATH}/report -name '*ods' -and -mtime +7d -print -delete

clear-log-file: check-data-path
	@date +'%F %T TAR START'
	tar cfz $(DATA_PATH)/save/log_$$(date +%Y%m%d).taz tmp/*.log
	@date +'%F %T TAR STOP'
	echo -n >tmp/yokwe-finance.log
	echo -n >tmp/cron.log

save-all: check-data-path save-stock save-provider save-report rsync-to-truenas

save-stock: check-data-path
	@date +'%F %T TAR START'
	cd $(DATA_PATH); tar cfz save/stock_$$(date +%Y%m%d).taz    stock
	@date +'%F %T TAR STOP'

save-provider: check-data-path
	@date +'%F %T TAR START'
	cd $(DATA_PATH); tar cfz save/provider_$$(date +%Y%m%d).taz provider
	@date +'%F %T TAR STOP'

save-report: check-data-path
	@date +'%F %T TAR START'
	cd $(DATA_PATH); tar cfz save/report_$$(date +%Y%m%d).taz report
	@date +'%F %T TAR STOP'


#
# JREIT
#
update-jreit:
	ant provider-jreit-update-jreit-info
	ant provider-jreit-update-jreit-div
	

#
# MANEBU
#
update-manebu:
	ant provider-manebu-update-etf-div-info
	

#
# FUND
#
update-trading-fund:
	ant provider-click-update-trading-fund-click
	ant provider-monex-update-trading-fund-monex
	ant provider-nikko-update-trading-fund-nikko
	ant provider-nomura-update-trading-fund-nomura
	ant provider-prestia-update-trading-fund-prestia
	ant provider-rakuten-update-trading-fund-rakuten
	ant provider-sbi-update-trading-fund-sbi
	ant provider-sony-update-trading-fund-sony

update-fund: check-data-path
	ant provider-jita-update-fund-info-jita
	ant provider-jita-update-fund-div-price-jita
	cp -p $(DATA_PATH)/provider/jita/fund-info-jita.csv $(DATA_PATH)/fund/fund-info.csv
	@date +'%F %T RSYNC START'
	rsync -ah --delete $(DATA_PATH)/provider/jita/fund-price-jita/ $(DATA_PATH)/fund/fund-price
	@date +'%F %T RSYNC START'
	rsync -ah --delete $(DATA_PATH)/provider/jita/fund-div-jita/   $(DATA_PATH)/fund/fund-div
	@date +'%F %T RSYNC STOP'


#
# STOCK JP
#
update-jpx-listing:
	ant provider-jpx-update-listing	

update-stock-info-jp: check-data-path
	ant provider-jpx-update-etf
	ant provider-jpx-update-etn
	ant provider-jpx-update-foreign-stock
	ant provider-jpx-update-infra-fund
	ant provider-jpx-update-reit
	ant provider-jpx-update-stock-info-jpx
	cp -p $(DATA_PATH)/provider/jpx/stock-info-jpx.csv $(DATA_PATH)/stock/stock-info-jp.csv
	
update-stock-price-jp: check-data-path
	ant provider-jpx-update-stock-price-jpx2
	@date +'%F %T RSYNC START'
	rsync -ah --delete $(DATA_PATH)/provider/jpx/stock-price-jpx/ $(DATA_PATH)/stock/stock-price-jp
	@date +'%F %T RSYNC STOP'

update-stock-div-jp:
	ant provider-jpx-update-stock-div-jpx
	ant stock-update-stock-div-jp
	

#
# STOCK US
#
update-stock-info-us:
	ant provider-bats-update-stock-info-bats
	ant provider-nasdaq-update-stock-info-nasdaq
	ant provider-nyse-update-stock-info-nyse
	ant stock-update-stock-info-us

update-trading-stock:
	ant provider-monex-update-trading-stock-monex
	ant provider-moomoo-update-trading-stock-moomoo
	ant provider-nikko-update-trading-stock-nikko2
	ant provider-rakuten-update-trading-stock-rakuten
	ant provider-sbi-update-trading-stock-sbi

update-stock-price-us:
	ant stock-update-stock-price-us

update-stock-div-us:
	ant stock-update-stock-div-us


#
# REPORT
#

report-stock-stats-us: kill-soffice
	ant report-update-stock-stats-us

report-stock-stats-us-monthly: kill-soffice
	ant report-update-stock-stats-us-monthly

report-stock-stats-jp: kill-soffice
	ant report-update-stock-stats-jp

report-fund-stats: kill-soffice
	ant report-update-fund-stats
