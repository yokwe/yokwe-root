#
#
#


DATA_PATH_FILE := data/DataPathLocation
DATA_PATH := $(shell cat $(DATA_PATH_FILE))

.PHONY: all build check_data_path


all: check_data_path
	@echo "all"


build:
	( cd ..; mvn clean ant:ant install )

check_data_path:
	@echo "DATA_PATH_FILE  !$(DATA_PATH_FILE)!"
	@echo "DATA_PATH       !$(DATA_PATH)!"
	@if [ ! -d $(DATA_PATH) ]; then \
	  	  echo "no directory  !${DATA_PATH}!" ; \
	  exit 1 ; \
	fi

update-crontab:
	crontab data/crontab


rync-to-sandisk2t:
	rsync -a -P /Volumes/finance/ /Volumes/SanDisk2T/finance


# maintenance of save, log and crontab
clear-save-file: check_data_path
	find ${DATA_PATH}/save     -mtime +30 -print -delete

save-file: check_data_path
	cd $(DATA_PATH); tar cfz save/stock_$$(date +%Y%m%d).taz    stock
	cd $(DATA_PATH); tar cfz save/provider_$$(date +%Y%m%d).taz provider

clear-log-file: check_data_path
	tar cfz $(DATA_PATH)/save/log_$$(date +%Y%m%d).taz tmp/*.log
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

update-jpx-stock-price:
	ant provider-jpx-update-stock-price

update-stock-jp:
	ant stock-jp-update-stock-info
	ant stock-jp-update-stock-price


update-jita:
	ant provider-jita-update-fund-info
	ant provider-jita-update-fund-div-price
	

update-stock-info-us:
	ant provider-bats-update-stock-info
	ant provider-nasdaq-update-stock-info
	ant provider-nyse-update-stock-info
	ant stock-us-update-stock-info


update-trading-sock:
	ant provider-monex-update-trading-stock
	ant provider-moomoo-update-trading-stock
	ant provider-rakuten-update-trading-stock
	ant provider-sbi-update-trading-stock
