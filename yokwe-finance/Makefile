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

rync-to-sandisk2t:
	rsync -ah --delete --dry-run /Volumes/finance/ /Volumes/SanDisk2T/finance

rsync-to-truenas:
	rsync -ah --delete /Volumes/SanDisk2T/finance/ /Volumes/finance

# maintenance of save and log
clear-save-file: check-data-path
	find ${DATA_PATH}/save -mtime +7d -print -delete

clear-report-file: check-data-path
	find ${DATA_PATH}/report -name '*ods' -and -mtime +7d -print -delete

clear-log-file: check-data-path
	tar cfz $(DATA_PATH)/save/log_$$(date +%Y%m%d).taz tmp/*.log
	echo -n >tmp/yokwe-finance.log
	echo -n >tmp/cron.log

save-all: check-data-path save-stock save-provider save-report rsync-to-truenas

save-stock: check-data-path
	cd $(DATA_PATH); tar cfz save/stock_$$(date +%Y%m%d).taz    stock

save-provider: check-data-path
	cd $(DATA_PATH); tar cfz save/provider_$$(date +%Y%m%d).taz provider

save-report: check-data-path
	cd $(DATA_PATH); tar cfz save/report_$$(date +%Y%m%d).taz report


#
# JREIT
#
update-jreit:
	ant provider-jreit-update-reit-info
	ant provider-jreit-update-reit-div
	

#
# MANEBU
#
update-manebu:
	ant provider-manebu-update-etf-div-info
	

#
# FUND
#
update-fund:
	ant provider-jita-update-fund-info-jita
	ant provider-jita-update-fund-div-price-jita
	cp -p $(DATA_PATH)/provider/jita/fund-info-jita.csv $(DATA_PATH)/fund/fund-info.csv
	rm -f $(DATA_PATH)/fund/fund-price/*.csv
	cp -p $(DATA_PATH)/provider/jita/fund-price-jita/*.csv $(DATA_PATH)/fund/fund-price
	rm -f $(DATA_PATH)/fund/fund-div/*.csv
	cp -p $(DATA_PATH)/provider/jita/fund-div-jita/*.csv $(DATA_PATH)/fund/fund-div
	

#
# STOCK JP
#
update-jpx-listing:
	ant provider-jpx-update-listing	

update-stock-info-jp:
	ant provider-jpx-update-etf
	ant provider-jpx-update-etn
	ant provider-jpx-update-foreign-stock
	ant provider-jpx-update-infra-fund
	ant provider-jpx-update-reit
	ant provider-jpx-update-stock-info-jpx
	cp -p $(DATA_PATH)/provider/jpx/stock-info-jpx.csv $(DATA_PATH)/stock/stock-info-jp.csv
	
update-stock-price-jp:
	ant provider-jpx-update-stock-price-jpx
	mkdir -p $(DATA_PATH)/stock/stock-price-jp
	rm    -f $(DATA_PATH)/stock/stock-price-jp/*.csv
	cp    -p $(DATA_PATH)/provider/jpx/stock-price-jpx/*.csv $(DATA_PATH)/stock/stock-price-jp

update-stock-div-jp:
	ant provider-jpx-update-stock-div-jpx
	ant stock-update-stock-div-jp
	

#
# STOCK US
#
update-stock-info-us: update-trading-stock
	ant provider-bats-update-stock-info-bats
	ant provider-nasdaq-update-stock-info-nasdaq
	ant provider-nyse-update-stock-info-nyse
	ant stock-update-stock-info-us

update-trading-stock:
	ant provider-monex-update-trading-stock-monex
	ant provider-moomoo-update-trading-stock-moomoo
	ant provider-rakuten-update-trading-stock-rakuten
	ant provider-sbi-update-trading-stock-sbi

update-stock-price-us:
	ant stock-update-stock-price-us

update-stock-div-us:
	ant stock-update-stock-div-us
